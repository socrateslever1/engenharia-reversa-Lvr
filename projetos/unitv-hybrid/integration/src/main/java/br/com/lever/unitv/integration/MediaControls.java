package br.com.lever.unitv.integration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

/** Usar apenas se o app não possuir outra sessão de mídia para o mesmo player. */
public final class MediaControls implements AutoCloseable {
    private final Context context;
    private final PlaybackPort player;
    private final MediaSession session;
    private final BroadcastReceiver receiver;
    private boolean registered;
    private boolean closed;

    public MediaControls(Context app, PlaybackPort player) {
        if (app == null || player == null) throw new IllegalArgumentException("Contexto/player obrigatórios");
        if (Looper.myLooper() != Looper.getMainLooper()) throw new IllegalStateException("Requer thread principal");
        this.context = app.getApplicationContext();
        this.player = player;
        session = new MediaSession(context, "UniTVHybridPlayback");
        session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setCallback(new MediaSession.Callback() {
            @Override public void onPlay() { if (!closed) { player.play(); refresh(); } }
            @Override public void onPause() { if (!closed) { player.pause(); refresh(); } }
            @Override public void onStop() { if (!closed) { player.pause(); refresh(); } }
        }, new Handler(Looper.getMainLooper()));
        receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent intent) {
                if (!closed && AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())
                        && player.isPlaying()) { player.pause(); refresh(); }
            }
        };
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        if (Build.VERSION.SDK_INT >= 33) context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        else context.registerReceiver(receiver, filter);
        registered = true;
        refresh();
        session.setActive(true);
    }

    /** Notificar quando o player mudar; obrigatório chamar na thread principal. */
    public void refresh() {
        if (closed) return;
        if (Looper.myLooper() != Looper.getMainLooper()) throw new IllegalStateException("Requer thread principal");
        boolean playing = player.isPlaying();
        session.setPlaybackState(new PlaybackState.Builder()
            .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE
                | PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_STOP)
            .setState(playing ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED,
                Math.max(0L, player.positionMs()), playing ? 1f : 0f, SystemClock.elapsedRealtime())
            .build());
        MediaMetadata.Builder meta = new MediaMetadata.Builder();
        String title = player.title();
        if (title != null && !title.trim().isEmpty())
            meta.putString(MediaMetadata.METADATA_KEY_TITLE, title);
        if (player.durationMs() > 0) meta.putLong(MediaMetadata.METADATA_KEY_DURATION, player.durationMs());
        session.setMetadata(meta.build());
    }

    @Override public void close() {
        if (closed) return;
        if (Looper.myLooper() != Looper.getMainLooper()) throw new IllegalStateException("Requer thread principal");
        closed = true;
        if (registered) { context.unregisterReceiver(receiver); registered = false; }
        session.setActive(false);
        session.release();
    }
}
