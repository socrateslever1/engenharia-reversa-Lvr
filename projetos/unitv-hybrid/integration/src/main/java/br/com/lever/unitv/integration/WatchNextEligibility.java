package br.com.lever.unitv.integration;

/** Nunca publicar live TV, vídeo não iniciado ou já concluído. */
public final class WatchNextEligibility {
    private WatchNextEligibility() { }
    public static boolean canPublish(boolean vod, long positionMs, long durationMs) {
        return vod && durationMs > 60_000L && positionMs >= 60_000L
            && positionMs < durationMs
            && positionMs <= durationMs - Math.min(60_000L, durationMs / 20L);
    }
}
