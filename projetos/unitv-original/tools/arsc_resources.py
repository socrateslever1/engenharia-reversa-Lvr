#!/usr/bin/env python3
"""Read-only Android resources.arsc indexer. Does not decode application code."""
from __future__ import annotations
import argparse
import json
import struct
import zipfile
from pathlib import Path
from typing import Iterator

def u16(data:bytes, off:int)->int:
    if off<0 or off+2>len(data): raise ValueError(f"Truncated u16 at {off}")
    return struct.unpack_from("<H",data,off)[0]

def u32(data:bytes, off:int)->int:
    if off<0 or off+4>len(data): raise ValueError(f"Truncated u32 at {off}")
    return struct.unpack_from("<I",data,off)[0]

def chunk(data:bytes,off:int)->tuple[int,int,int]:
    if off<0 or off+8>len(data): raise ValueError("Truncated chunk")
    typ,hdr,size=struct.unpack_from("<HHI",data,off)
    if hdr<8 or size<hdr or off+size>len(data):
        raise ValueError(f"Invalid chunk at {off}")
    return typ,hdr,size

def children(data:bytes,start:int,end:int)->Iterator[tuple[int,int,int,int]]:
    while start<end:
        typ,hdr,size=chunk(data,start)
        if start+size>end: raise ValueError("Child out of bounds")
        yield start,typ,hdr,size
        start+=size
    if start!=end: raise ValueError("Trailing bytes")

def _len8(data:bytes,p:int)->tuple[int,int]:
    if p>=len(data): raise ValueError("Truncated UTF8 length")
    v=data[p];p+=1
    if v&0x80:
        if p>=len(data): raise ValueError("Truncated UTF8 length")
        return ((v&127)<<8)|data[p],p+1
    return v,p

def _len16(data:bytes,p:int)->tuple[int,int]:
    v=u16(data,p);p+=2
    if v&0x8000: return ((v&32767)<<16)|u16(data,p),p+2
    return v,p

def parse_string_pool(data:bytes,off:int)->list[str]:
    typ,hdr,size=chunk(data,off)
    if typ!=1 or hdr<28: raise ValueError("Not a string pool")
    count=u32(data,off+8);flags=u32(data,off+16);strings_start=u32(data,off+20)
    if count>500000 or hdr+count*4>size or strings_start>size:
        raise ValueError("String pool header out of bounds")
    result=[]
    for i in range(count):
        p=off+strings_start+u32(data,off+hdr+i*4)
        if not off<=p<off+size: raise ValueError("Invalid string offset")
        if flags&0x100:
            _,p=_len8(data,p)
            length,p=_len8(data,p)
            if p+length>=off+size or data[p+length]!=0:
                raise ValueError("Malformed UTF8 string")
            s=data[p:p+length].decode("utf-8")
        else:
            length,p=_len16(data,p)
            if p+length*2+2>off+size or u16(data,p+length*2)!=0:
                raise ValueError("Malformed UTF16 string")
            s=data[p:p+length*2].decode("utf-16-le")
        result.append(s)
    return result

def resource_rows(data:bytes)->list[dict]:
    typ,hdr,size=chunk(data,0)
    if typ!=2 or hdr<12: raise ValueError("Not a resource table")
    values=None;unique={}
    for pos,ctype,chdr,csize in children(data,hdr,size):
        if ctype==1:
            values=parse_string_pool(data,pos)
            continue
        if ctype!=0x200: continue
        if chdr<284: raise ValueError("Invalid package header")
        package_id=u32(data,pos+8)
        types=parse_string_pool(data,pos+u32(data,pos+268))
        names=parse_string_pool(data,pos+u32(data,pos+276))
        type_offset=u32(data,pos+284) if chdr>=288 else 0
        for tp,t,th,ts in children(data,pos+chdr,pos+csize):
            if t!=0x201: continue
            type_id=data[tp+8]
            flags=data[tp+9]
            if type_id==0 or type_id>len(types):continue
            tname=types[type_id-1]
            count=u32(data,tp+12)
            entry_start=tp+u32(data,tp+16)
            offset_start=tp+th
            if count>300000 or entry_start>tp+ts:raise ValueError("Invalid resource offsets")
            if flags&1:
                offsets=[(u16(data,offset_start+i*4),u16(data,offset_start+i*4+2)*4)
                         for i in range(count)]
            elif flags&2:
                offsets=[(i,u16(data,offset_start+i*2)*4)
                         for i in range(count)
                         if u16(data,offset_start+i*2)!=0xffff]
            else:
                offsets=[(i,u32(data,offset_start+i*4))
                         for i in range(count)
                         if u32(data,offset_start+i*4)!=0xffffffff]
            for idx,delta in offsets:
                p=entry_start+delta
                if p+8>tp+ts: raise ValueError("Resource entry out of bounds")
                entry_size=u16(data,p)
                entry_flags=u16(data,p+2)
                key_index=u32(data,p+4)
                if entry_size<8 or key_index>=len(names): continue
                rid=(package_id<<24)|((type_id+type_offset)<<16)|idx
                key=names[key_index]
                path=None
                if not entry_flags&1 and p+entry_size+8<=tp+ts and values is not None:
                    dtype=data[p+entry_size+3]; val=u32(data,p+entry_size+4)
                    if dtype==3 and val<len(values) and values[val].startswith("res/"):
                        path=values[val]
                ident=(rid,key,path)
                unique.setdefault(ident,{
                    "resource_id":f"0x{rid:08x}",
                    "type":tname,
                    "name":key,
                    "file":path,
                })
    return sorted(unique.values(),key=lambda x:(x["type"],x["name"],x["resource_id"],x["file"] or ""))

def resources_from_apk(path:Path)->list[dict]:
    with zipfile.ZipFile(path) as archive:
        if archive.testzip() is not None:raise ValueError("APK CRC failure")
        return resource_rows(archive.read("resources.arsc"))

def main()->None:
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk",type=Path)
    parser.add_argument("--output",type=Path)
    args=parser.parse_args()
    rows=resources_from_apk(args.apk)
    data=json.dumps({"apk_name":args.apk.name,"resource_count":len(rows),"resources":rows},
                    ensure_ascii=False,indent=2)+"\n"
    if args.output:
        args.output.parent.mkdir(parents=True,exist_ok=True)
        args.output.write_text(data,encoding="utf-8")
    else: print(data,end="")

if __name__=="__main__":main()
