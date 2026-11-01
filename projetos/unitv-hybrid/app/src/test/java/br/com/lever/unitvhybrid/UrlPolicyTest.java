package br.com.lever.unitvhybrid;
import org.junit.Test;
import static org.junit.Assert.*;
public class UrlPolicyTest {
    @Test public void acceptsHttps() {
        assertEquals("https://example.org/video.mp4", UrlPolicy.normalize(" https://example.org/video.mp4 "));
    }
    @Test public void acceptsHlsWithQuery() {
        assertTrue(UrlPolicy.isHls(UrlPolicy.normalize("https://example.org/live.m3u8?token=abc")));
    }
    @Test(expected = IllegalArgumentException.class) public void refusesHttp() {
        UrlPolicy.normalize("http://example.org/video.mp4");
    }
    @Test(expected = IllegalArgumentException.class) public void refusesEmbeddedCredentials() {
        UrlPolicy.normalize("https://user:pass@example.org/video.mp4");
    }
    @Test(expected = IllegalArgumentException.class) public void refusesFile() {
        UrlPolicy.normalize("file:///storage/emulated/0/video.mp4");
    }
    @Test(expected = IllegalArgumentException.class) public void refusesFragment() {
        UrlPolicy.normalize("https://example.org/video.mp4#abc");
    }
}
