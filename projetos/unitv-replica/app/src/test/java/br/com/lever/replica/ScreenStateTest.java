package br.com.lever.replica;
import org.junit.Test;
import static org.junit.Assert.*;
public final class ScreenStateTest {
 @Test public void allTabsHaveKnownDestinations() {
  assertEquals(4,ScreenState.Tab.values().length);
  for (ScreenState.Tab tab:ScreenState.Tab.values()) assertNotNull(tab.title());
 }
 @Test public void noCatalogueMayBeInvented() { assertTrue(ScreenState.emptyFeed().isEmpty()); }
 @Test public void categoryChangesDoNotInventItems() {for (ScreenState.Category c:ScreenState.Category.values())assertEquals(0,ScreenState.forCategory(c).size());}
}
