import com.googlecode.lanterna.input.KeyStroke;
import java.util.ArrayList;

public class Commands{
  final public static ArrayList<String> mainMenue = buildMainMenue();

  private static ArrayList<String> buildMainMenue(){
    ArrayList<String> menue = new ArrayList<String>();
    menue.add("F5 - Aktualisieren");
    menue.add("F6 - Note Testen");
    menue.add("F7 - Speichern");
    menue.add("ESC - Quit");
    return menue;
  }
}
