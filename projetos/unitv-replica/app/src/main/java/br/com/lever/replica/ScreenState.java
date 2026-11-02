package br.com.lever.replica;

import java.util.Collections;
import java.util.List;

/** No synthetic catalog is ever silently interpreted as real content. */
public final class ScreenState {
    public enum Tab {
        HOME("Início"), LIVE("TV"), NEWS("Novidades"), PROFILE("Perfil");
        private final String title;
        Tab(String title) { this.title = title; }
        public String title() { return title; }
    }
    public enum Category {
        RECOMMENDATIONS("Recomendações"), MOVIES("Filmes"), SERIES("Séries"), ANIME("Animes");
        private final String title;
        Category(String title) { this.title = title; }
        public String title() { return title; }
    }
    private ScreenState() { }
    public static List<String> emptyFeed() { return Collections.emptyList(); }
    public static List<String> forCategory(Category ignored) { return emptyFeed(); }
}
