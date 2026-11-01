import importlib.util
import tempfile
import unittest
import zipfile
from pathlib import Path

source = Path(__file__).resolve().parents[1] / "apk_baseline.py"
spec = importlib.util.spec_from_file_location("apk_baseline", source)
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

class ApkBaselineTest(unittest.TestCase):
    def test_invalid_dex_magic(self):
        with tempfile.TemporaryDirectory() as d:
            p = Path(d) / "example.apk"
            with zipfile.ZipFile(p, "w") as archive:
                archive.writestr("classes.dex", "not-a-dex")
                archive.writestr("lib/arm64-v8a/libplay.so", b"example")
            result = module.inventory(p)
            self.assertTrue(result["zip_crc_valid"])
            self.assertEqual(result["abis"], ["arm64-v8a"])
            self.assertFalse(result["dex_headers"][0]["valid_magic"])

    def test_absent_file(self):
        with self.assertRaises(FileNotFoundError):
            module.inventory(Path("/path/that/does/not/exist/app.apk"))

if __name__ == "__main__":
    unittest.main()
