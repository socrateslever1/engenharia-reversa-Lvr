package br.com.lever.unitvhybrid;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** Validação determinística antes de solicitar conteúdo remoto. */
public final class UrlPolicy {
    private UrlPolicy() {}

    public static String normalize(String input) {
        if (input == null) throw new IllegalArgumentException("Informe uma URL HTTPS de vídeo.");
        String url = input.trim();
        if (url.length() < 12 || url.length() > 2048) {
            throw new IllegalArgumentException("A URL precisa ter entre 12 e 2048 caracteres.");
        }
        if (url.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("URL contém caracteres inválidos.");
        }
        try {
            URI parsed = new URI(url);
            if (!"https".equalsIgnoreCase(parsed.getScheme()) || parsed.getHost() == null
                    || parsed.getHost().isEmpty() || parsed.getRawUserInfo() != null
                    || parsed.getRawFragment() != null) {
                throw new IllegalArgumentException("Use HTTPS sem credenciais embutidas ou fragmentos.");
            }
            if (parsed.getPort() < -1 || parsed.getPort() > 65535 || parsed.getPort() == 0) {
                throw new IllegalArgumentException("Porta inválida.");
            }
            return parsed.toASCIIString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL inválida.", e);
        }
    }

    public static boolean isHls(String url) {
        try {
            String path = new URI(url).getPath();
            return path != null && path.toLowerCase(Locale.ROOT).endsWith(".m3u8");
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
