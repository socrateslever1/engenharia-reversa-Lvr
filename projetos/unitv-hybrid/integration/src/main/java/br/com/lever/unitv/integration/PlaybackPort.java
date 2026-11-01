package br.com.lever.unitv.integration;

/** Porta de adaptação ao player: não simula APIs privadas do UniTVnet. */
public interface PlaybackPort {
    boolean isPlaying();
    boolean isVideoActive();
    long positionMs();
    long durationMs();
    String title();
    void play();
    void pause();
}
