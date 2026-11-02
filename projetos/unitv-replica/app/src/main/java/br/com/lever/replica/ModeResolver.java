package br.com.lever.replica;

/** Decision independent of screen resolution; an explicit preference always wins. */
public final class ModeResolver {
    public enum Mode { MOBILE, TV }
    private ModeResolver() { }
    public static Mode resolve(String choice, boolean uiModeTelevision, boolean leanbackFeature) {
        if ("mobile".equals(choice)) return Mode.MOBILE;
        if ("tv".equals(choice)) return Mode.TV;
        return uiModeTelevision || leanbackFeature ? Mode.TV : Mode.MOBILE;
    }
}
