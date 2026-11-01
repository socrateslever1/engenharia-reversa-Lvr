package br.com.lever.unitv.integration;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/** Requer launcher TV compatível. No Google TV, publicar depende de certificação. */
public final class WatchNextPublisher {
    public enum Result { INSERTED, UPDATED, INELIGIBLE, UNSUPPORTED, FAILED }
    private final Context context;
    private final SharedPreferences prefs;
    private final ContentResolver resolver;
    private final DeviceModeResolver device;

    public WatchNextPublisher(Context context) {
        if (context == null) throw new IllegalArgumentException("Contexto obrigatório");
        this.context = context.getApplicationContext();
        prefs = this.context.getSharedPreferences("unitv_watch_next_uri", Context.MODE_PRIVATE);
        resolver = this.context.getContentResolver();
        device = new DeviceModeResolver(this.context);
    }

    /** Executar em thread de E/S. O intent deve ser explícito e interno ao aplicativo. */
    public synchronized Result upsert(String contentId, String title, boolean vod,
                                      long position, long duration, Intent launch) {
        offMainThread();
        if (Build.VERSION.SDK_INT < 26 || device.resolve() != DeviceModePolicy.Mode.TV) return Result.UNSUPPORTED;
        if (!WatchNextEligibility.canPublish(vod, position, duration)) return Result.INELIGIBLE;
        if (contentId == null || contentId.trim().isEmpty() || contentId.length() > 256
            || title == null || title.trim().isEmpty() || title.length() > 256
            || launch == null || launch.getComponent() == null
            || !Objects.equals(context.getPackageName(), launch.getComponent().getPackageName())) {
            return Result.INELIGIBLE;
        }
        String key = hash(contentId);
        ContentValues values = new ContentValues();
        values.put("type", 0); // Movie; episódios precisam de metadados de temporada/episódio.
        values.put("watch_next_type", 0); // Continue
        values.put("title", title);
        values.put("internal_provider_id", contentId);
        values.put("last_engagement_time_utc_millis", System.currentTimeMillis());
        values.put("intent_uri", launch.toUri(Intent.URI_INTENT_SCHEME));
        values.put("duration_millis", duration);
        values.put("last_playback_position_millis", position);
        try {
            String old = prefs.getString(key, null);
            if (old != null) {
                try { if (resolver.update(Uri.parse(old), values, null, null) == 1) return Result.UPDATED; }
                catch (RuntimeException ignored) { /* registro excluído ou provider indisponível */ }
            }
            Uri inserted = resolver.insert(TvContract.WatchNextPrograms.CONTENT_URI, values);
            if (inserted == null) return Result.FAILED;
            if (!prefs.edit().putString(key, inserted.toString()).commit()) {
                resolver.delete(inserted, null, null);
                return Result.FAILED;
            }
            return Result.INSERTED;
        } catch (RuntimeException ignored) { return Result.FAILED; }
    }

    /** Remover ao concluir o conteúdo ou desautenticar. Executar fora da thread principal. */
    public synchronized boolean remove(String contentId) {
        offMainThread();
        if (contentId == null || contentId.trim().isEmpty()) return false;
        String key = hash(contentId);
        String saved = prefs.getString(key, null);
        if (saved == null) return false;
        try {
            resolver.delete(Uri.parse(saved), null, null);
            return prefs.edit().remove(key).commit();
        } catch (RuntimeException ignored) { return false; }
    }

    private static void offMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) throw new IllegalStateException("I/O na thread principal");
    }
    private static String hash(String data) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) { s.append(Character.forDigit((b >>> 4) & 15, 16)); s.append(Character.forDigit(b & 15, 16)); }
            return s.toString();
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
