package br.com.lever.unitv.integration;
import org.junit.Test;
import static org.junit.Assert.*;
public class WatchNextEligibilityTest {
    @Test public void filmPartiallyWatched() { assertTrue(WatchNextEligibility.canPublish(true,300000,7200000)); }
    @Test public void liveNotEligible() { assertFalse(WatchNextEligibility.canPublish(false,300000,7200000)); }
    @Test public void notStarted() { assertFalse(WatchNextEligibility.canPublish(true,10000,7200000)); }
    @Test public void almostFinished() { assertFalse(WatchNextEligibility.canPublish(true,7190000,7200000)); }
    @Test public void unknownDuration() { assertFalse(WatchNextEligibility.canPublish(true,70000,-1)); }
}
