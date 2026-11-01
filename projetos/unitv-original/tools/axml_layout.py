#!/usr/bin/env python3
"""Decode layout hierarchy from AXML without modifying APKs or running their code."""
from __future__ import annotations
import argparse
import json
import zipfile
from pathlib import Path
from arsc_resources import chunk,children,parse_string_pool,u16,u32,resources_from_apk

def parse_layout(data:bytes,resources:list[dict])->dict:
    typ,hdr,size=chunk(data,0)
    if typ!=3: raise ValueError("Not Android Binary XML")
    names=[];root=None;stack=[];namespace={}
    lookup={int(x["resource_id"],16):x for x in resources}
    for pos,typ,ch,sz in children(data,hdr,size):
        if typ==1:
            names=parse_string_pool(data,pos)
            continue
        if typ==0x0100:
            prefix=u32(data,pos+16);uri=u32(data,pos+20)
            if prefix<len(names) and uri<len(names):namespace[uri]=names[prefix]
        elif typ==0x0101:
            namespace.pop(u32(data,pos+20),None)
        elif typ==0x0102:
            if ch<16 or pos+36>pos+sz: raise ValueError("Bad element")
            ns=u32(data,pos+16);nam=u32(data,pos+20)
            if nam>=len(names):raise ValueError("Invalid element name")
            offset=u16(data,pos+24);width=u16(data,pos+26);count=u16(data,pos+28)
            if count>512 or width<20:raise ValueError("Bad attribute count")
            attrs={}
            for i in range(count):
                at=pos+16+offset+i*width
                if at+20>pos+sz: raise ValueError("Truncated attribute")
                attr_ns=u32(data,at);attr_name=u32(data,at+4)
                raw=u32(data,at+8);dtype=data[at+15];value=u32(data,at+16)
                if attr_name>=len(names):continue
                key=names[attr_name]
                if attr_ns!=0xffffffff and attr_ns in namespace:
                    key=namespace[attr_ns]+":"+key
                if raw!=0xffffffff and raw<len(names):v=names[raw]
                elif dtype==3 and value<len(names):v=names[value]
                elif dtype==1:
                    resource=lookup.get(value)
                    v=f"@{resource['type']}/{resource['name']}" if resource else f"@0x{value:08x}"
                elif dtype in (0x10,0x11):v=str(value)
                elif dtype==0x12:v="true" if value else "false"
                else:v=f"0x{value:08x} (dataType {dtype})"
                attrs[key]=v[:300]
            tag=names[nam]
            if ns!=0xffffffff and ns in namespace:tag=namespace[ns]+":"+tag
            node={"tag":tag,"attrs":attrs,"children":[]}
            if stack:stack[-1]["children"].append(node)
            elif root is None:root=node
            else:raise ValueError("Multiple XML roots")
            stack.append(node)
        elif typ==0x0103:
            if not stack:raise ValueError("Unmatched end tag")
            stack.pop()
    if root is None or stack:raise ValueError("Empty/unclosed layout")
    return root

def layout_outline(root:dict)->list[dict]:
    output=[]
    def walk(node:dict,depth:int)->None:
        attrs=node["attrs"]
        output.append({
            "depth":depth,"tag":node["tag"],"id":attrs.get("android:id"),
            "orientation":attrs.get("android:orientation")
        })
        for child in node["children"]:walk(child,depth+1)
    walk(root,0)
    return output

def main()->None:
    ap=argparse.ArgumentParser(description=__doc__)
    ap.add_argument("apk",type=Path)
    ap.add_argument("layout",type=str)
    args=ap.parse_args()
    resources=resources_from_apk(args.apk)
    items=[x for x in resources
           if x["type"]=="layout" and x["name"]==args.layout and x["file"]]
    if not items:raise SystemExit("Layout not found: "+args.layout)
    with zipfile.ZipFile(args.apk) as archive:
        result=[{
            "resource_name":args.layout,"file":row["file"],
            "tree":layout_outline(parse_layout(archive.read(row["file"]),resources))
        } for row in items]
    print(json.dumps(result,ensure_ascii=False,indent=2))

if __name__=="__main__":main()
