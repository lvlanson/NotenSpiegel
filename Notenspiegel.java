import java.io.Console;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import java.io.IOException;

public class Notenspiegel{
  public static void main(String args[]){

    /*File file;

    try{
      file = new File("notentest.html");
      ArrayList<Score> scores = Hsmw.extractScores((InputStream)new FileInputStream(file));
      for(Score score : scores){
      }
    }catch(Exception e){
      e.printStackTrace();
    }
    */




    DefaultTerminalFactory defaultTerminal = new DefaultTerminalFactory();
    Screen screen = null;
    try{
      Terminal terminal = defaultTerminal.createTerminal();
      screen = new TerminalScreen(terminal);
      screen.startScreen();
      screen.setCursorPosition(null);
      final TextGraphics textGraphics = screen.newTextGraphics();
      textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
      textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
      DrawScreen.splash(screen, textGraphics);
      Thread.sleep(2000);
      DrawScreen.menue(screen, textGraphics);
      while(true){
        KeyStroke keyStroke = screen.pollInput();
        if(keyStroke != null && (keyStroke.getKeyType() == KeyType.Escape)){
          break;
        }
      }

    }catch(IOException e){
      e.printStackTrace();
    }catch(InterruptedException e){
      e.printStackTrace();
    }finally{
      if(screen != null){
        try{
          screen.close();
        }catch(IOException e){
          e.printStackTrace();
        }
      }
    }
  }
}
