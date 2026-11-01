package br.com.lever.unitv.integration;

import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.view.InputDevice;

/** Usa recursos do dispositivo, não resolução da tela. Preferência manual persistente. */
public final class DeviceModeResolver {
    private final Context context;
    private final SharedPreferences preferences;

    public DeviceModeResolver(Context applicationContext) {
        if (applicationContext == null) throw new IllegalArgumentException("Contexto obrigatório");
        context = applicationContext.getApplicationContext();
        preferences = context.getSharedPreferences("unitv_integration_device_mode", Context.MODE_PRIVATE);
    }

    public DeviceModePolicy.Mode resolve() {
        String value = preferences.getString("mode", DeviceModePolicy.Preference.AUTO.name());
        DeviceModePolicy.Preference preference;
        try { preference = DeviceModePolicy.Preference.valueOf(value); }
        catch (IllegalArgumentException | NullPointerException ignored) {
            preference = DeviceModePolicy.Preference.AUTO;
        }
        PackageManager pm = context.getPackageManager();
        UiModeManager ui = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        boolean tvMode = ui != null && ui.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
        return DeviceModePolicy.resolve(preference,
            pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK), tvMode,
            pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN), hasPhysicalDpad());
    }

    public boolean setPreference(DeviceModePolicy.Preference preference) {
        if (preference == null) throw new IllegalArgumentException("Preferência obrigatória");
        return preferences.edit().putString("mode", preference.name()).commit();
    }

    private boolean hasPhysicalDpad() {
        for (int id : InputDevice.getDeviceIds()) {
            InputDevice device = InputDevice.getDevice(id);
            if (device != null && !device.isVirtual()
                && (device.getSources() & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) return true;
        }
        return false;
    }
}
