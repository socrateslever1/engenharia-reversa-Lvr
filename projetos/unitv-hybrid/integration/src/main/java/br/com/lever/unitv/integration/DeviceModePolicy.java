package br.com.lever.unitv.integration;

public final class DeviceModePolicy {
    public enum Mode { MOBILE, TV }
    public enum Preference { AUTO, MOBILE, TV }

    private DeviceModePolicy() { }

    public static Mode resolve(Preference preference, boolean leanback, boolean tvMode,
                               boolean touchscreen, boolean physicalDpad) {
        if (preference == null) throw new IllegalArgumentException("Preferência obrigatória");
        if (preference == Preference.TV) return Mode.TV;
        if (preference == Preference.MOBILE) return Mode.MOBILE;
        return leanback || tvMode || (!touchscreen && physicalDpad) ? Mode.TV : Mode.MOBILE;
    }
}
