import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.CheckBoxList;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.WindowShadowRenderer;
import com.googlecode.lanterna.gui2.Separator;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.ProgressBar;
import com.googlecode.lanterna.gui2.CheckBox;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.RadioBoxList;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.SGR;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.EOFException;
import java.util.ArrayList;

public class Visual{
  private DefaultTerminalFactory terminalFactory = null;
  private Screen screen = null;
  private WindowBasedTextGUI textGUI = null;

  public Visual(){
    try{
      terminalFactory = new DefaultTerminalFactory();
      screen = terminalFactory.createScreen();
      textGUI = new MultiWindowTextGUI(screen);
    }catch(IOException e){
      e.printStackTrace();
    }
  }

  private void createWelcomeWindow(){
    Window window = new BasicWindow("Willkommen");
    Panel welcomePanel = new Panel(new LinearLayout(Direction.VERTICAL));
    welcomePanel.addComponent(new Label("Was möchtest du gerne tun?"));
    welcomePanel.addComponent(new Button("Noten anzeigen", new Runnable(){
      @Override
      public void run(){
        createScoreScreen();
      }
    }));
    welcomePanel.addComponent(new Button("Noten updaten", new Runnable(){
      @Override
      public void run(){
        window.close();
      }
    }));
    welcomePanel.addComponent(new Button("Note testen", new Runnable(){
      @Override
      public void run(){
        window.close();
      }
    }));
    welcomePanel.addComponent(new Button("Noten speichern", new Runnable(){
      @Override
      public void run(){
        window.close();
      }
    }));
    welcomePanel.addComponent(new Button("Exit", new Runnable(){
      @Override
      public void run(){
        close();
      }
    }));
    window.setComponent(welcomePanel);
    textGUI.addWindowAndWait(window);
  }

  private void createScoreScreen(){
    ObjectInputStream ois = null;
    try{
      ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(Hsmw.getDataPath())));
      final Window window = new BasicWindow("Notenliste");
      Panel notenPanel = new Panel(new GridLayout(3));
      Label head1 = new Label("Fach");
      Label head2 = new Label("Note");
      Label head3 = new Label("Versuch");
      head1.addStyle(SGR.BOLD);
      head2.addStyle(SGR.BOLD);
      head3.addStyle(SGR.BOLD);
      notenPanel.addComponent(head1);
      notenPanel.addComponent(head2);
      notenPanel.addComponent(head3);
      ArrayList<Score> scores = (ArrayList<Score>) ois.readObject();
      for(Score scoreSet: scores){
        Label subject        = new Label(scoreSet.getSubject());
        Label score          = new Label("" + scoreSet.getScore());
        Label attempts       = new Label("" + scoreSet.getAttempts());

        notenPanel.addComponent(subject);
        notenPanel.addComponent(score);
        notenPanel.addComponent(attempts);
      }
      Button exit = new Button("Exit", new Runnable(){
        @Override
        public void run(){
          window.close();
        }
      });
      notenPanel.addComponent(exit);
      window.setComponent(notenPanel);
      textGUI.addWindowAndWait(window);
    }catch(ClassNotFoundException e){
      e.printStackTrace();
    }/*catch(EOFException e){
      e.printStackTrace();
    }*/catch(IOException e){
      e.printStackTrace();
    }

  }
  public void run(){
    try{
      screen.startScreen();
      createWelcomeWindow();
    }catch(IOException e){
      e.printStackTrace();
    }
  }
  public void close(){
    if(screen != null){
      try{
        screen.stopScreen();
        System.out.println("");
        System.exit(0);
      }catch(IOException e){
        e.printStackTrace();
      }
    }
  }
}
