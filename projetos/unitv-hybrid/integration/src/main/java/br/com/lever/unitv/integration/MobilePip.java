package br.com.lever.unitv.integration;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.util.Rational;

/** PiP para Activity de vídeo; não substitui o player. */
public final class MobilePip {
    private final Activity activity;
    private final PlaybackPort player;
    private final DeviceModeResolver device;

    public MobilePip(Activity activity, PlaybackPort player) {
        if (activity == null || player == null) throw new IllegalArgumentException("Activity/player obrigatórios");
        this.activity = activity;
        this.player = player;
        this.device = new DeviceModeResolver(activity);
    }

    public boolean available() {
        return Build.VERSION.SDK_INT >= 26
            && device.resolve() == DeviceModePolicy.Mode.MOBILE
            && activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
    }

    public boolean eligible() { return available() && player.isVideoActive() && player.isPlaying(); }

    public void update(int width, int height, Rect videoRect) {
        if (!available()) return;
        double ratio = width > 0 && height > 0 ? (double) width / height : 16d / 9d;
        ratio = Math.max(0.45d, Math.min(2.2d, ratio));
        PictureInPictureParams.Builder config = new PictureInPictureParams.Builder()
            .setAspectRatio(new Rational((int) Math.round(ratio * 1000), 1000));
        if (videoRect != null && !videoRect.isEmpty()) config.setSourceRectHint(new Rect(videoRect));
        if (Build.VERSION.SDK_INT >= 31) config.setAutoEnterEnabled(eligible());
        activity.setPictureInPictureParams(config.build());
    }

    /** Chamar em Activity.onUserLeaveHint apenas em Android 8 a 11. */
    public boolean onUserLeaveHint() {
        if (Build.VERSION.SDK_INT < 26 || Build.VERSION.SDK_INT >= 31 || !eligible()) return false;
        try { return activity.enterPictureInPictureMode(); }
        catch (IllegalStateException | UnsupportedOperationException ignored) { return false; }
    }
}
