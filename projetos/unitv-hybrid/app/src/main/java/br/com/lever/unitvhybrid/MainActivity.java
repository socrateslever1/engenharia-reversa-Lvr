package br.com.lever.unitvhybrid;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import br.com.lever.unitv.integration.DeviceModePolicy;
import br.com.lever.unitv.integration.DeviceModeResolver;
import br.com.lever.unitv.integration.MediaControls;
import br.com.lever.unitv.integration.MobilePip;
import br.com.lever.unitv.integration.PlaybackPort;

/**
 * Aplicativo técnico independente: nenhum login/catálogo/backend UniTVnet.
 * Reprodução apenas de URLs HTTPS fornecidas e autorizadas pelo usuário.
 */
@OptIn(markerClass = UnstableApi.class)
public final class MainActivity extends Activity {
    private static final String SAMPLE_URL =
            "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

    private ExoPlayer player;
    private PlayerView playerView;
    private MediaControls mediaControls;
    private MobilePip pip;
    private DeviceModeResolver deviceResolver;
    private PlaybackAdapter adapter;
    private LinearLayout root;
    private TextView status;
    private TextView modeLabel;
    private EditText urlInput;
    private Button pipButton;
    private boolean disposed;
    private String currentTitle = "Vídeo de demonstração";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        deviceResolver = new DeviceModeResolver(getApplicationContext());
        player = new ExoPlayer.Builder(this).build();
        player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true);
        adapter = new PlaybackAdapter();
        pip = new MobilePip(this, adapter);
        mediaControls = new MediaControls(getApplicationContext(), adapter);
        buildUi();
        player.addListener(new Player.Listener() {
            @Override public void onIsPlayingChanged(boolean isPlaying) {
                refreshIntegration();
                if (!disposed) setStatus(isPlaying ? "Reproduzindo" : "Reprodução pausada");
            }
            @Override public void onPlaybackStateChanged(int state) {
                refreshIntegration();
                if (state == Player.STATE_BUFFERING) setStatus("Carregando vídeo...");
                if (state == Player.STATE_ENDED) setStatus("Vídeo concluído.");
                if (state == Player.STATE_READY && !player.isPlaying()) setStatus("Pronto para reproduzir.");
            }
            @Override public void onPlayerError(PlaybackException error) {
                setStatus("Falha de reprodução (" + error.errorCode + "). Verifique a fonte e a conexão.");
            }
        });
        refreshIntegration();
        setStatus("Nenhum vídeo selecionado. Use uma URL HTTPS autorizada ou o vídeo de demonstração.");
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private TextView text(String content, int sp, int color) {
        TextView t = new TextView(this);
        t.setText(content);
        t.setTextColor(color);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        return t;
    }

    private void buildUi() {
        boolean tv = deviceResolver.resolve() == DeviceModePolicy.Mode.TV;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(tv ? 28 : 12), dp(12), dp(tv ? 28 : 12), dp(12));
        root.setBackgroundColor(Color.rgb(12, 20, 32));
        setContentView(root);

        TextView title = text("UniTV Hybrid • Protótipo técnico", tv ? 24 : 18, Color.WHITE);
        title.setTypeface(null, 1);
        root.addView(title);

        modeLabel = text("", 13, Color.rgb(107, 218, 197));
        modeLabel.setPadding(0, dp(5), 0, dp(6));
        root.addView(modeLabel);

        TextView disclaimer = text("Não utiliza a conta nem o catálogo do UniTVnet. " +
                "Teste inicial de reprodução, controle remoto, fones e Picture-in-Picture.", tv ? 15 : 12,
                Color.rgb(183, 193, 207));
        root.addView(disclaimer);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, dp(8), 0, dp(8));
        root.addView(buttons);

        Button sample = button("Vídeo de teste", buttons);
        sample.setOnClickListener(v -> playUrl(SAMPLE_URL, "Big Buck Bunny (demonstração)"));

        Button switchMode = button("Modo", buttons);
        switchMode.setOnClickListener(v -> {
            DeviceModePolicy.Mode resolved = deviceResolver.resolve();
            DeviceModePolicy.Preference next = resolved == DeviceModePolicy.Mode.TV
                    ? DeviceModePolicy.Preference.MOBILE : DeviceModePolicy.Preference.TV;
            if (!deviceResolver.setPreference(next)) {
                setStatus("Não foi possível salvar a preferência."); return;
            }
            updateMode();
            setStatus("Modo alterado. Reinicie o aplicativo para ajustar a disposição da interface.");
        });

        pipButton = button("PiP", buttons);
        pipButton.setOnClickListener(v -> enterPip());

        urlInput = new EditText(this);
        urlInput.setHint("https://.../video.mp4 ou .m3u8");
        urlInput.setHintTextColor(Color.rgb(132, 145, 164));
        urlInput.setTextColor(Color.WHITE);
        urlInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, tv ? 16 : 14);
        urlInput.setSingleLine(true);
        urlInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        root.addView(urlInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(tv ? 55 : 48)));

        Button openUrl = button("Reproduzir URL HTTPS", root);
        openUrl.setOnClickListener(v -> {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
            playUrl(urlInput.getText().toString(), "Fonte fornecida pelo usuário");
        });

        playerView = new PlayerView(this);
        playerView.setPlayer(player);
        playerView.setUseController(true);
        playerView.setControllerAutoShow(true);
        playerView.setBackgroundColor(Color.BLACK);
        LinearLayout.LayoutParams videoLayout = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        videoLayout.topMargin = dp(10);
        root.addView(playerView, videoLayout);
        playerView.setFocusable(true);

        status = text("", 13, Color.rgb(211, 218, 230));
        status.setPadding(0, dp(8), 0, dp(3));
        root.addView(status);
        updateMode();
        sample.requestFocus();
    }

    private Button button(String label, LinearLayout parent) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        b.setFocusable(true);
        parent.addView(b, new LinearLayout.LayoutParams(
                parent == root ? ViewGroup.LayoutParams.MATCH_PARENT : 0,
                dp(50), parent == root ? 0 : 1f));
        return b;
    }

    private void updateMode() {
        boolean tv = deviceResolver.resolve() == DeviceModePolicy.Mode.TV;
        modeLabel.setText("Modo: " + (tv ? "TV • controle remoto" : "Mobile • toque")
                + "   |   Versão 0.1.0-alpha");
        pipButton.setVisibility(tv || !pip.available() ? View.GONE : View.VISIBLE);
        refreshIntegration();
    }

    private void playUrl(String input, String title) {
        try {
            String url = UrlPolicy.normalize(input);
            currentTitle = title;
            MediaItem.Builder item = new MediaItem.Builder()
                    .setUri(url)
                    .setMediaId("temporary-user-media");
            if (UrlPolicy.isHls(url)) item.setMimeType(MimeTypes.APPLICATION_M3U8);
            player.setMediaItem(item.build());
            player.prepare();
            player.play();
            refreshIntegration();
        } catch (IllegalArgumentException error) {
            setStatus(error.getMessage());
        } catch (RuntimeException error) {
            setStatus("Não foi possível iniciar a reprodução.");
        }
    }

    private void enterPip() {
        if (!pip.eligible()) {
            setStatus("PiP requer vídeo em reprodução em um dispositivo Mobile compatível.");
            return;
        }
        try {
            Rect source = new Rect();
            playerView.getGlobalVisibleRect(source);
            pip.update(player.getVideoSize().width, player.getVideoSize().height, source);
            if (Build.VERSION.SDK_INT >= 26) {
                if (!enterPictureInPictureMode()) setStatus("O Android não permitiu entrar no PiP.");
            }
        } catch (RuntimeException error) {
            setStatus("O modo PiP não está disponível neste dispositivo.");
        }
    }

    private void refreshIntegration() {
        if (disposed || player == null) return;
        if (mediaControls != null) mediaControls.refresh();
        if (pip != null && pip.available()) {
            Rect source = new Rect();
            if (playerView != null) playerView.getGlobalVisibleRect(source);
            pip.update(player.getVideoSize().width, player.getVideoSize().height, source);
        }
    }

    private void setStatus(String value) {
        if (!disposed && status != null) status.setText(value);
    }

    @Override public void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (pip != null) pip.onUserLeaveHint();
    }

    @Override public void onPictureInPictureModeChanged(boolean inPip, Configuration config) {
        super.onPictureInPictureModeChanged(inPip, config);
        if (playerView != null) playerView.setUseController(!inPip);
        if (root != null) {
            int children = root.getChildCount();
            for (int index = 0; index < children; index++) {
                View child = root.getChildAt(index);
                if (child != playerView) child.setVisibility(inPip ? View.GONE : View.VISIBLE);
            }
        }
    }

    @Override protected void onStop() {
        super.onStop();
        if (player != null && !isInPictureInPictureMode()) {
            player.pause();
        }
    }

    @Override protected void onDestroy() {
        disposed = true;
        if (mediaControls != null) mediaControls.close();
        if (playerView != null) playerView.setPlayer(null);
        if (player != null) player.release();
        super.onDestroy();
    }

    private final class PlaybackAdapter implements PlaybackPort {
        @Override public boolean isPlaying() { return player != null && player.isPlaying(); }
        @Override public boolean isVideoActive() {
            return player != null && player.getCurrentMediaItem() != null
                && player.getPlaybackState() != Player.STATE_ENDED;
        }
        @Override public long positionMs() { return player == null ? 0 : Math.max(0, player.getCurrentPosition()); }
        @Override public long durationMs() {
            if (player == null) return 0;
            long d = player.getDuration();
            return d == C.TIME_UNSET ? 0 : Math.max(0, d);
        }
        @Override public String title() { return currentTitle; }
        @Override public void play() { if (player != null) player.play(); }
        @Override public void pause() { if (player != null) player.pause(); }
    }
}
