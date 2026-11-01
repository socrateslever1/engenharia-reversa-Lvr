#!/usr/bin/env python3
"""Auditoria estática reprodutível de APK. Não extrai credenciais, URLs ou blobs."""
from __future__ import annotations
import argparse
import hashlib
import json
import struct
import sys
import zipfile
from pathlib import Path

def inventory(path: Path) -> dict:
    if not path.is_file():
        raise FileNotFoundError(path)
    with path.open("rb") as file:
        digest = hashlib.file_digest(file, "sha256").hexdigest()
    with zipfile.ZipFile(path) as package:
        bad = package.testzip()
        if bad:
            raise ValueError(f"Entrada com CRC inválido: {bad}")
        names = set(package.namelist())
        dex_headers = []
        for name in sorted(p for p in names if p.endswith(".dex") and "/" not in p):
            with package.open(name) as file:
                header = file.read(112)
            if len(header) < 112 or not header.startswith(b"dex\n"):
                dex_headers.append({"name": name, "valid_magic": False})
                continue
            dex_headers.append({
                "name": name,
                "valid_magic": True,
                "declared_file_size": struct.unpack_from("<I", header, 32)[0],
                "string_ids_size": struct.unpack_from("<I", header, 56)[0],
                "class_defs_size": struct.unpack_from("<I", header, 96)[0],
                "zip_uncompressed_size": package.getinfo(name).file_size,
            })
        native = sorted(p for p in names if p.startswith("lib/") and p.endswith(".so"))
        return {
            "file_label": path.name,
            "sha256": digest,
            "size_bytes": path.stat().st_size,
            "zip_crc_valid": True,
            "zip_entry_count": len(names),
            "dex_headers": dex_headers,
            "abis": sorted({p.split("/")[1] for p in native}),
            "native_library_names": sorted({p.rsplit("/",1)[-1] for p in native}),
            "has_secneo_components": any("libDexHelper" in p or "secneo" in p.lower() for p in names),
        }

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--tv", required=True, type=Path)
    parser.add_argument("--mobile", required=True, type=Path)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    out = json.dumps({"tv": inventory(args.tv), "mobile": inventory(args.mobile)},
                     ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(out, encoding="utf-8")
    else:
        sys.stdout.write(out)

if __name__ == "__main__":
    try:
        main()
    except (OSError, zipfile.BadZipFile, ValueError) as error:
        sys.stderr.write(f"Falha: {error}\n")
        raise SystemExit(2)
