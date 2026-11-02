package br.com.lever.replica;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public final class ModeResolverTest {
  @Test public void autoOnTouchPhoneIsMobile() { assertEquals(ModeResolver.Mode.MOBILE,ModeResolver.resolve("auto",false,false)); }
  @Test public void autoOnTvIsTv() { assertEquals(ModeResolver.Mode.TV,ModeResolver.resolve("auto",true,false)); }
  @Test public void autoOnLeanbackBoxIsTv() { assertEquals(ModeResolver.Mode.TV,ModeResolver.resolve("auto",false,true)); }
  @Test public void explicitOverrideWins() {assertEquals(ModeResolver.Mode.MOBILE,ModeResolver.resolve("mobile",true,true)); assertEquals(ModeResolver.Mode.TV,ModeResolver.resolve("tv",false,false));}
  @Test public void invalidSettingFailsSafelyToAuto() {assertEquals(ModeResolver.Mode.TV,ModeResolver.resolve("corrupted",false,true));}
}
