import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from manifest_probe import inspect_tree, inspect_apk

def node(tag, attrs=None, children=None):
    return {"tag": tag, "attrs": attrs or {}, "children": children or []}

class ManifestProbeTest(unittest.TestCase):
    def test_original_player_is_not_externally_callable_from_manifest(self):
        player = node("activity", {"android:name":".PlayAty"})
        app = node("application", {"android:name":"com.secneo.apkwrapper.AW"}, [player])
        result = inspect_tree(node("manifest", {"package":"example.app"}, [app]))
        self.assertTrue(result["secneo_wrapper"])
        self.assertFalse(result["exported_player_candidate"])
        self.assertFalse(result["pip_declared_on_player"])

    def test_explicitly_exported_player_is_a_candidate_not_proof_of_access(self):
        player = node("activity", {
            "android:name":".PlayAty",
            "android:exported":"true",
            "android:supportsPictureInPicture":"true"})
        result = inspect_tree(node("manifest", {"package":"example.app"},
                                   [node("application", children=[player])]))
        self.assertTrue(result["exported_player_candidate"])
        self.assertTrue(result["pip_declared_on_player"])

    def test_launcher_is_not_mistaken_for_player(self):
        launcher = node("activity", {"android:name":".Splash", "android:exported":"true"},
                        [node("intent-filter", children=[
                            node("action", {"android:name":"android.intent.action.MAIN"}),
                            node("category", {"android:name":"android.intent.category.LEANBACK_LAUNCHER"})])])
        result = inspect_tree(node("manifest", {"package":"example.app"},
                                   [node("application", children=[launcher])]))
        self.assertEqual(result["launchers"][0]["component"], "example.app.Splash")
        self.assertFalse(result["exported_player_candidate"])

    def test_no_application_must_fail(self):
        with self.assertRaises(ValueError):
            inspect_tree(node("manifest", {"package":"example.app"}))

    @unittest.skipUnless(Path("/mnt/data/UniTVnet_1.11.0.apk").exists(),
                         "APK proprietário não incluído na CI pública")
    def test_real_mobile_no_manifest_pip(self):
        result = inspect_apk(Path("/mnt/data/UniTVnet_1.11.0.apk"))
        self.assertEqual(result["package"], "com.unitvnet.mobs")
        self.assertFalse(result["pip_declared_on_player"])
        self.assertFalse(result["exported_player_candidate"])

    @unittest.skipUnless(Path("/mnt/data/UniTVnet_1.10.0.apk").exists(),
                         "APK proprietário não incluído na CI pública")
    def test_real_tv_no_exported_player(self):
        result = inspect_apk(Path("/mnt/data/UniTVnet_1.10.0.apk"))
        self.assertEqual(result["package"], "com.unitvnet.tvod")
        self.assertFalse(result["exported_player_candidate"])

if __name__ == "__main__":
    unittest.main()
