#!/usr/bin/env python3
"""Inspeção somente leitura de manifesto Android; não opera backend, DRM ou credenciais."""
from __future__ import annotations

import argparse
import hashlib
import json
import zipfile
from pathlib import Path

from arsc_resources import resources_from_apk
from axml_layout import parse_layout

LAUNCH = frozenset(("android.intent.category.LAUNCHER",
                    "android.intent.category.LEANBACK_LAUNCHER"))
PLAYERS = frozenset(("playaty", "playactivity", "playeractivity",
                     "liveplayeractivity", "vodplayeractivity",
                     "videoactivity", "videoplayeractivity"))

def is_player_name(name: str) -> bool:
    return name.rsplit(".", 1)[-1].lower() in PLAYERS

def children(node: dict, tag: str) -> list[dict]:
    return [c for c in node.get("children", ()) if c["tag"] == tag]

def full_name(package: str, name: str) -> str:
    if name.startswith("."):
        return package + name
    return package + "." + name if "." not in name else name

def inspect_tree(tree: dict) -> dict:
    if tree.get("tag") != "manifest" or not tree.get("attrs", {}).get("package"):
        raise ValueError("Manifesto Android inválido")
    package = tree["attrs"]["package"]
    apps = children(tree, "application")
    if len(apps) != 1:
        raise ValueError("Manifesto deve declarar exatamente uma Application")
    app = apps[0]
    manifest_attrs = tree["attrs"]
    launchers = []
    players = []
    other_external_view = []
    counts = {k: 0 for k in ("activity", "service", "receiver", "provider")}
    for component in app["children"]:
        tag = component["tag"]
        if tag not in counts:
            continue
        counts[tag] += 1
        attrs = component["attrs"]
        name = attrs.get("android:name")
        if not name:
            continue
        resolved = full_name(package, name)
        exported = attrs.get("android:exported") == "true"
        actions, categories = set(), set()
        for intent in children(component, "intent-filter"):
            actions.update(child["attrs"].get("android:name", "")
                           for child in children(intent, "action"))
            categories.update(child["attrs"].get("android:name", "")
                              for child in children(intent, "category"))
        if tag != "activity":
            continue
        if "android.intent.action.MAIN" in actions:
            for category in sorted(categories.intersection(LAUNCH)):
                launchers.append({"component": resolved, "category": category})
        if is_player_name(resolved):
            players.append({
                "component": resolved,
                "explicitly_exported": exported,
                "pip_declared": attrs.get("android:supportsPictureInPicture") == "true",
                "view_filter": "android.intent.action.VIEW" in actions,
            })
        elif exported and "android.intent.action.VIEW" in actions:
            other_external_view.append(resolved)
    return {
        "package": package,
        "version": manifest_attrs.get("android:versionName"),
        "application": app["attrs"].get("android:name"),
        "secneo_wrapper": app["attrs"].get("android:name") == "com.secneo.apkwrapper.AW",
        "counts": counts,
        "launchers": sorted(launchers, key=lambda x: (x["category"], x["component"])),
        "player_activity_candidates": sorted(players, key=lambda x: x["component"]),
        "exported_player_candidate": any(p["explicitly_exported"] for p in players),
        "pip_declared_on_player": any(p["pip_declared"] for p in players),
        "external_nonplayer_view_activities": sorted(set(other_external_view)),
    }

def inspect_apk(path: Path) -> dict:
    if not path.is_file():
        raise FileNotFoundError(path)
    with zipfile.ZipFile(path) as archive:
        bad = archive.testzip()
        if bad is not None:
            raise ValueError(f"Entrada ZIP corrompida: {bad}")
        manifest = archive.read("AndroidManifest.xml")
        dex = []
        for name in archive.namelist():
            if not (name.startswith("classes") and name.endswith(".dex") and "/" not in name):
                continue
            with archive.open(name) as stream:
                header = stream.read(112)
            if len(header) < 112 or not header.startswith(b"dex\n"):
                raise ValueError(f"DEX inválido: {name}")
            import struct
            dex.append({
                "name": name,
                "declared_classes": struct.unpack_from("<I", header, 96)[0],
                "declared_methods": struct.unpack_from("<I", header, 88)[0],
            })
    result = inspect_tree(parse_layout(manifest, resources_from_apk(path)))
    with path.open("rb") as stream:
        result["sha256"] = hashlib.file_digest(stream, "sha256").hexdigest()
    result["dex_header_counts"] = sorted(dex, key=lambda x: x["name"])
    return result

def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--tv", required=True, type=Path)
    ap.add_argument("--mobile", required=True, type=Path)
    ap.add_argument("--output", type=Path)
    args = ap.parse_args()
    data = {"tv": inspect_apk(args.tv), "mobile": inspect_apk(args.mobile)}
    data["manifest_declares_playback_bridge"] = (
        data["tv"]["exported_player_candidate"]
        or data["mobile"]["exported_player_candidate"]
    )
    data["important_limit"] = (
        "Manifesto nao comprova comportamento executado, acesso ao servidor, "
        "autenticacao nem recuperacao de codigo funcional."
    )
    rendered = json.dumps(data, ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(rendered, encoding="utf-8")
    else:
        print(rendered, end="")

if __name__ == "__main__":
    main()
