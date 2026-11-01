import struct
import sys
import unittest
from pathlib import Path

sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from arsc_resources import parse_string_pool,resources_from_apk
from axml_layout import parse_layout,layout_outline

class ParserTest(unittest.TestCase):
    def test_utf8_pool(self):
        body=b"\x03\x03foo\x00\x03\x03Bar\x00"
        header=struct.pack("<HHI",1,28,28+8+len(body))
        header+=struct.pack("<IIIII",2,0,0x100,36,0)
        blob=header+struct.pack("<II",0,6)+body
        self.assertEqual(parse_string_pool(blob,0),["foo","Bar"])
    def test_invalid_pool_rejected(self):
        with self.assertRaises(ValueError):parse_string_pool(b"bad",0)
    def test_non_axml_rejected(self):
        with self.assertRaises(ValueError):parse_layout(b"<xml/>",[])
    @unittest.skipUnless(Path("/mnt/data/UniTVnet_1.11.0.apk").exists(),
                         "Original Mobile APK must be local; never commit binary")
    def test_real_mobile_layout(self):
        import zipfile
        apk=Path("/mnt/data/UniTVnet_1.11.0.apk")
        resources=resources_from_apk(apk)
        row=next(x for x in resources if x["type"]=="layout" and x["name"]=="aty_main")
        with zipfile.ZipFile(apk) as z:
            result=layout_outline(parse_layout(z.read(row["file"]),resources))
        self.assertTrue(any(x["id"]=="@id/mainAtyTab" for x in result))
    @unittest.skipUnless(Path("/mnt/data/UniTVnet_1.10.0.apk").exists(),
                         "Original TV APK must be local; never commit binary")
    def test_real_tv_layout(self):
        import zipfile
        apk=Path("/mnt/data/UniTVnet_1.10.0.apk")
        resources=resources_from_apk(apk)
        row=next(x for x in resources if x["type"]=="layout" and x["name"]=="activity_home")
        with zipfile.ZipFile(apk) as z:
            result=layout_outline(parse_layout(z.read(row["file"]),resources))
        self.assertTrue(any(x["id"]=="@id/mGridTab" for x in result))
if __name__=="__main__":unittest.main()
