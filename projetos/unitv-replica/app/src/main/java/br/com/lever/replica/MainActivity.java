package br.com.lever.replica;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Objects;

/** Clean-room UI flow study. Does not impersonate or call original service APIs. */
public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(27, 27, 32);
    private static final int SURFACE = Color.rgb(44, 46, 57);
    private static final int WHITE = Color.rgb(247, 247, 248);
    private static final int MUTED = Color.rgb(172, 175, 185);
    private static final int BLUE = Color.rgb(0, 160, 245);

    private ScreenState.Tab tab = ScreenState.Tab.HOME;
    private ScreenState.Category category = ScreenState.Category.RECOMMENDATIONS;
    private ModeResolver.Mode mode;
    private LinearLayout root;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(SURFACE);
        readMode();
        render();
    }

    private void readMode() {
        UiModeManager manager=(UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
        boolean isTv=manager!=null && manager.getCurrentModeType()==Configuration.UI_MODE_TYPE_TELEVISION;
        boolean leanback=getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK);
        String chosen=getPreferences(MODE_PRIVATE).getString("mode", "auto");
        mode=ModeResolver.resolve(chosen,isTv,leanback);
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        readMode();
        render();
    }

    private int dp(float units) {
        return Math.round(units * getResources().getDisplayMetrics().density);
    }

    private GradientDrawable rounded(int color,float radius) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);d.setCornerRadius(dp(radius));return d;
    }

    private TextView label(String content, float sp, int color, boolean bold) {
        TextView text=new TextView(this);
        text.setText(content);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP,sp);
        text.setTextColor(color);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setIncludeFontPadding(true);
        if(bold)text.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return text;
    }

    private LinearLayout linear(boolean vertical) {
        LinearLayout v=new LinearLayout(this);
        v.setOrientation(vertical ? LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        return v;
    }
    private LinearLayout.LayoutParams lp(int w,int h) {return new LinearLayout.LayoutParams(w,h);}
    private void pad(View v,int l,int t,int r,int b) {v.setPadding(dp(l),dp(t),dp(r),dp(b));}
    private void add(LinearLayout parent,View child,int w,int h) {parent.addView(child,lp(w,h));}
    private void space(LinearLayout parent,int height) {add(parent,new View(this),1,dp(height));}

    private Button action(String text, boolean selected, Runnable callback) {
        Button b=new Button(this);
        b.setText(text);b.setAllCaps(false);
        b.setTextColor(selected?BLUE:WHITE);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP,mode==ModeResolver.Mode.TV?20:14);
        b.setGravity(Gravity.CENTER);
        b.setBackground(rounded(selected?SURFACE:BG,8));
        b.setFocusable(true);
        b.setOnClickListener(v->callback.run());
        return b;
    }

    private void changeTab(ScreenState.Tab next) {tab=next;render();}
    private void changeCategory(ScreenState.Category next) {category=next;render();}

    private void render() {
        root=linear(true);
        root.setBackgroundColor(BG);
        setContentView(root);
        int margin=mode==ModeResolver.Mode.TV?36:16;
        LinearLayout header=linear(false);
        header.setGravity(Gravity.CENTER_VERTICAL);
        pad(header,margin,12,margin,12);
        TextView brand=label("LVR", mode==ModeResolver.Mode.TV?28:22,BLUE,true);
        brand.setContentDescription("LVR, aplicativo em reconstrução independente");
        add(header,brand,dp(mode==ModeResolver.Mode.TV?90:60),dp(54));
        TextView title=label(mode==ModeResolver.Mode.TV?"Reprodução em TV":"Réplica de navegação",mode==ModeResolver.Mode.TV?27:15,WHITE,true);
        LinearLayout.LayoutParams tlp=lp(0,dp(54));tlp.weight=1;
        header.addView(title,tlp);
        if(mode==ModeResolver.Mode.TV) {
            Button modeButton=action("Modo",false,()->changeTab(ScreenState.Tab.PROFILE));
            add(header,modeButton,dp(110),dp(54));
        }
        root.addView(header,lp(-1,dp(mode==ModeResolver.Mode.TV?92:74)));

        if(mode==ModeResolver.Mode.TV) {
            LinearLayout n=linear(false);
            pad(n,margin,4,margin,4);
            for(ScreenState.Tab candidate:ScreenState.Tab.values()){
                LinearLayout.LayoutParams p=lp(0,dp(58));p.weight=1;
                n.addView(action(candidate.title(),candidate==tab,()->changeTab(candidate)),p);
            }
            root.addView(n,lp(-1,dp(66)));
        }

        ScrollView scroller=new ScrollView(this);
        scroller.setFillViewport(true);
        scroller.setVerticalScrollBarEnabled(false);
        LinearLayout body=linear(true);
        pad(body,margin,0,margin,22);
        scroller.addView(body);
        LinearLayout.LayoutParams sp=lp(-1,0);sp.weight=1;root.addView(scroller,sp);

        switch(tab){
            case HOME: home(body);break;
            case LIVE: unsupported(body,"TV ao vivo","Os canais reais dependem de integração autorizada com o serviço.");break;
            case NEWS: unsupported(body,"Novidades","Não há atualizações disponíveis sem uma fonte de conteúdo autorizada.");break;
            case PROFILE: profile(body);break;
            default:throw new IllegalStateException("Unknown tab "+tab);
        }
        if(mode==ModeResolver.Mode.MOBILE)bottomBar();
    }

    private void home(LinearLayout body) {
        if(mode==ModeResolver.Mode.MOBILE){
            EditText search=new EditText(this);
            search.setSingleLine(true);
            search.setTextColor(WHITE);
            search.setHintTextColor(MUTED);
            search.setHint("Pesquisar no catálogo");
            search.setTextSize(16);
            search.setEnabled(false); // Do not advertise a query against a service we do not possess.
            search.setBackground(rounded(SURFACE,12));
            pad(search,18,0,14,0);
            search.setContentDescription("Pesquisa desativada até existir catálogo autorizado");
            add(body,search,-1,dp(52));
            space(body,22);
        }
        HorizontalScrollView hs=new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout options=linear(false);
        for(ScreenState.Category c:ScreenState.Category.values()){
            Button b=action(c.title(),c==category,()->changeCategory(c));
            LinearLayout.LayoutParams p=lp(-2,dp(mode==ModeResolver.Mode.TV?58:50));p.rightMargin=dp(14);
            options.addView(b,p);
        }
        hs.addView(options);
        add(body,hs,-1,dp(mode==ModeResolver.Mode.TV?64:58));
        space(body,18);
        // Deliberately no copied film posters, premium titles, forged content or playable links.
        emptyCard(body,category.title(),"Sem dados autorizados para preencher esta seção.",mode==ModeResolver.Mode.TV?250:190);
        space(body,30);
        section(body,"Transmissão ao vivo");
        emptyCard(body,"Canais","Nenhuma transmissão foi conectada.",mode==ModeResolver.Mode.TV?160:116);
        space(body,28);
        section(body,"Jogos • Grátis");
        emptyCard(body,"Eventos esportivos","Agenda indisponível sem provedor autorizado.",mode==ModeResolver.Mode.TV?180:134);
    }

    private void section(LinearLayout body,String text) {
        TextView t=label(text,mode==ModeResolver.Mode.TV?28:23,WHITE,true);
        add(body,t,-1,dp(mode==ModeResolver.Mode.TV?52:48));
        space(body,12);
    }

    private void emptyCard(LinearLayout body,String title,String detail,int heightDp) {
        LinearLayout card=linear(true);
        card.setGravity(Gravity.CENTER);
        pad(card,18,12,18,12);
        card.setBackground(rounded(SURFACE,14));
        TextView heading=label(title,mode==ModeResolver.Mode.TV?28:19,WHITE,true);
        heading.setGravity(Gravity.CENTER);
        TextView info=label(detail,mode==ModeResolver.Mode.TV?19:13,MUTED,false);
        info.setGravity(Gravity.CENTER);
        info.setMaxLines(3);
        add(card,heading,-1,-2);space(card,10);add(card,info,-1,-2);
        add(body,card,-1,dp(heightDp));
    }

    private void unsupported(LinearLayout body,String title,String explanation) {
        section(body,title);
        emptyCard(body,title,explanation,mode==ModeResolver.Mode.TV?280:220);
    }

    private void profile(LinearLayout body) {
        section(body,"Configurações");
        TextView note=label("Escolha o modo da interface. A opção automática identifica TV ou Mobile pelo Android, não pelo tamanho da tela.",mode==ModeResolver.Mode.TV?22:16,MUTED,false);
        note.setMaxLines(6);add(body,note,-1,-2);space(body,16);
        String selected=getPreferences(MODE_PRIVATE).getString("mode","auto");
        for(String option:new String[]{"auto","mobile","tv"}) {
            String caption="auto".equals(option)?"Automático":"mobile".equals(option)?"Mobile":"Android TV";
            Button b=action(caption,Objects.equals(selected,option),()->{
                getPreferences(MODE_PRIVATE).edit().putString("mode",option).apply();
                readMode();tab=ScreenState.Tab.HOME;render();
            });
            add(body,b,-1,dp(58));space(body,10);
        }
        space(body,26);
        TextView disclaimer=label("Aplicativo independente de estudo. Não utiliza conta, catálogo, marcas, URLs privadas ou player do UniTVnet.",mode==ModeResolver.Mode.TV?19:13,MUTED,false);
        disclaimer.setMaxLines(6);add(body,disclaimer,-1,-2);
    }

    private void bottomBar() {
        LinearLayout nav=linear(false);nav.setBackgroundColor(SURFACE);
        pad(nav,5,4,5,4);
        for(ScreenState.Tab candidate:ScreenState.Tab.values()){
            Button b=action(candidate.title(),candidate==tab,()->changeTab(candidate));
            LinearLayout.LayoutParams p=lp(0,dp(64));p.weight=1;
            nav.addView(b,p);
        }
        root.addView(nav,lp(-1,dp(74)));
    }
}
