import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.Symbols;
import java.io.IOException;
import java.util.HashMap;

public class DrawScreen{
  public static void splash(Screen screen, TextGraphics textGraphics) throws IOException{
    // Setting size for SplashScreen
    TerminalSize terminalSize = screen.getTerminalSize();
    final int horizontalShift = "###################################".length()/2;
    final int verticalShift = 5;

    textGraphics.putString(terminalSize.getColumns()/2-horizontalShift,
                           terminalSize.getRows()/2-verticalShift,
                           "###################################",
                           SGR.BOLD);
    textGraphics.putString(terminalSize.getColumns()/2-horizontalShift,
                           terminalSize.getRows()/2-verticalShift+1,
                           "#                                 #",
                           SGR.BOLD);
    textGraphics.putString(terminalSize.getColumns()/2-horizontalShift,
                           terminalSize.getRows()/2-verticalShift+2,
                           "#         STREBER-HSMW-APP        #",
                           SGR.BOLD);
    textGraphics.putString(terminalSize.getColumns()/2-horizontalShift,
                           terminalSize.getRows()/2-verticalShift+3,
                           "#                                 #",
                           SGR.BOLD);
    textGraphics.putString(terminalSize.getColumns()/2-horizontalShift,
                           terminalSize.getRows()/2-verticalShift+4,
                           "###################################",
                           SGR.BOLD);
    screen.refresh();
  }
  public static void menue(Screen screen, TextGraphics textGraphics) throws IOException{
    TerminalSize terminalSize = screen.getTerminalSize();
    final int bottomStart = terminalSize.getRows()-3;
    screen.clear();
    for(int i = 0; i < terminalSize.getColumns(); i++){
      textGraphics.setCharacter(i,bottomStart, Symbols.DOUBLE_LINE_HORIZONTAL);
    }
    int offset = 0;
    for(int i = 0; i<Commands.mainMenue.size(); i+=2){
      textGraphics.putString(offset,
                             bottomStart+1,
                             Commands.mainMenue.get(i),
                             SGR.BOLD);
      if(i+1<=Commands.mainMenue.size()){
        textGraphics.putString(offset,
                               bottomStart+2,
                               Commands.mainMenue.get(i+1),
                               SGR.BOLD);
        if(Commands.mainMenue.get(i).length() > Commands.mainMenue.get(i+1).length()){
          offset+=Commands.mainMenue.get(i).length()+3;
        }else{
          offset+=Commands.mainMenue.get(i+1).length()+3;
        }
      }
    }
    screen.refresh();
  }
}
