package br.com.lever.unitv.integration;
import org.junit.Test;
import static org.junit.Assert.*;
import static br.com.lever.unitv.integration.DeviceModePolicy.*;
public class DeviceModePolicyTest {
    @Test public void leanbackTv() { assertEquals(Mode.TV,resolve(Preference.AUTO,true,false,true,false)); }
    @Test public void tvUiMode() { assertEquals(Mode.TV,resolve(Preference.AUTO,false,true,true,false)); }
    @Test public void genericTvBox() { assertEquals(Mode.TV,resolve(Preference.AUTO,false,false,false,true)); }
    @Test public void remoteConnectedPhone() { assertEquals(Mode.MOBILE,resolve(Preference.AUTO,false,false,true,true)); }
    @Test public void forceMobile() { assertEquals(Mode.MOBILE,resolve(Preference.MOBILE,true,true,false,true)); }
    @Test public void forceTv() { assertEquals(Mode.TV,resolve(Preference.TV,false,false,true,false)); }
    @Test(expected=IllegalArgumentException.class) public void invalidPreference() { resolve(null,false,false,false,false); }
}
