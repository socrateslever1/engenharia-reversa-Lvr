import br.com.lever.replica.ModeResolver;
import br.com.lever.replica.ScreenState;
public final class SmokeContract {
 public static void main(String[] args) {
  check(ModeResolver.resolve("auto",false,false)==ModeResolver.Mode.MOBILE,"phone default");
  check(ModeResolver.resolve("auto",false,true)==ModeResolver.Mode.TV,"box default");
  check(ModeResolver.resolve("mobile",true,true)==ModeResolver.Mode.MOBILE,"manual override");
  check(ModeResolver.resolve("bad",true,false)==ModeResolver.Mode.TV,"corrupt setting");
  check(ScreenState.emptyFeed().isEmpty(),"no fictional content");
  check(ScreenState.Tab.values().length==4,"four navigation destinations");
  System.out.println("6 smoke assertions passed");
 }
 private static void check(boolean valid, String msg) {if(!valid)throw new AssertionError(msg);}
}
