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
    for(int i = 0; i<Commands.menue.size(); i+=2){
      textGraphics.putString(offset,
                             bottomStart+1,
                             Commands.menue.get(i),
                             SGR.BOLD);
      if(i+1<=Commands.menue.size()){
        textGraphics.putString(offset,
                               bottomStart+2,
                               Commands.menue.get(i+1),
                               SGR.BOLD);
        if(Commands.menue.get(i).length() > Commands.menue.get(i+1).length()){
          offset+=Commands.menue.get(i).length()+3;
        }else{
          offset+=Commands.menue.get(i+1).length()+3;
        }
      }
    }
    screen.refresh();
  }
}
