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
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
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
    String surname = DataHandler.getSurname();
    if(surname.length() != 0){
      surname = " " + surname;
    }
    final Window window = new BasicWindow("Willkommen"+surname+"!");
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
    HashMap<String, Score> syllabusMap = DataHandler.getSyllabus();
    HashMap<Integer, ArrayList<Score>> sortedMap = sortSemester(syllabusMap);
    drawSemesters(sortedMap, notenPanel, 1);
    Button exit = new Button("Exit", new Runnable(){
      @Override
      public void run(){
        window.close();
      }
    });
    notenPanel.addComponent(exit);
    window.setComponent(notenPanel);
    textGUI.addWindowAndWait(window);
  }
  private void drawSemesters(HashMap<Integer, ArrayList<Score>> sortedMap, Panel notenPanel, int indent){
    for(int i = 0; i<sortedMap.size(); i++){
      HashMap<String,ArrayList<Score>> wpfMap = new HashMap<String, ArrayList<Score>>();
      Label heading = null;
      if(i == 0 && sortedMap.get(i) != null){
        heading = new Label("Unbekanntes Semester");
      }else if(sortedMap.get(i) != null){
        heading = new Label(i + ". Semester");
      }
      if(heading != null){
        heading.setLayoutData(GridLayout.createLayoutData(
                              GridLayout.Alignment.BEGINNING, // Horizontal alignment in the grid cell if the cell is larger than the component's preferred size
                              GridLayout.Alignment.BEGINNING, // Vertical alignment in the grid cell if the cell is larger than the component's preferred size
                              true,                           // Give the component extra horizontal space if available
                              false,                          // Give the component extra vertical space if available
                              3,                              // Horizontal span
                              1));                            // Vertical span
        heading.addStyle(SGR.BOLD);
        notenPanel.addComponent(heading);
        for(Score scoreSet: sortedMap.get(i)){
          if(scoreSet.isWpf()){
            if(wpfMap.get(scoreSet.getWpfTopic()) == null){
              ArrayList<Score> wpfScore = new ArrayList<Score>();
              wpfScore.add(scoreSet);
              wpfMap.put(scoreSet.getWpfTopic(), wpfScore);
            }else{
              wpfMap.get(scoreSet.getWpfTopic()).add(scoreSet);
            }
            continue;
          }
          String scoreString;
          if(scoreSet.getScore()==0.0){
            scoreString = "-";
          }else{
            scoreString = "" + scoreSet.getScore();
          }
          String attemptsString;
          if(scoreSet.getAttempts()==0){
            attemptsString = "-";
          }else{
            attemptsString = "" + scoreSet.getAttempts();
          }
          String tabs = "";
          for(int j = 0; j<indent; j++){
           tabs += "\t";
          }
          Label subject        = new Label(tabs + scoreSet.getSubject());
          Label score          = new Label(scoreString);
          Label attempts       = new Label(attemptsString);
          notenPanel.addComponent(subject);
          notenPanel.addComponent(score);
          notenPanel.addComponent(attempts);
          if(scoreSet.hasSubScore()){
            drawScores(scoreSet.getSubScore(), notenPanel, indent+1);
          }
        }
        drawWpf(wpfMap, notenPanel);
      }
    }
  }
  private void drawWpf(HashMap<String, ArrayList<Score>> sortedMap, Panel notenPanel){
    Iterator it = sortedMap.entrySet().iterator();
    while(it.hasNext()){
      Map.Entry<String, ArrayList<Score>> pair = (Map.Entry)it.next();
      String wpfTopic = pair.getKey();
      ArrayList<Score> wpfList = pair.getValue();
      Label heading = new Label("\t" + wpfTopic + "("+wpfList.get(0).getWpfWeight()[0]+" aus "+wpfList.get(0).getWpfWeight()[1]+")");
      heading.setLayoutData(GridLayout.createLayoutData(
                            GridLayout.Alignment.BEGINNING, // Horizontal alignment in the grid cell if the cell is larger than the component's preferred size
                            GridLayout.Alignment.BEGINNING, // Vertical alignment in the grid cell if the cell is larger than the component's preferred size
                            true,                           // Give the component extra horizontal space if available
                            false,                          // Give the component extra vertical space if available
                            3,                              // Horizontal span
                            1));                            // Vertical span
      heading.addStyle(SGR.BOLD);
      notenPanel.addComponent(heading);
      for(Score scoreSet: wpfList){
        String scoreString;
        if(scoreSet.getScore()==0.0){
          scoreString = "-";
        }else{
          scoreString = "" + scoreSet.getScore();
        }
        String attemptsString;
        if(scoreSet.getAttempts()==0){
          attemptsString = "-";
        }else{
          attemptsString = "" + scoreSet.getAttempts();
        }
        String tabs = "\t\t";
        Label subject        = new Label(tabs + scoreSet.getSubject());
        Label score          = new Label(scoreString);
        Label attempts       = new Label(attemptsString);
        notenPanel.addComponent(subject);
        notenPanel.addComponent(score);
        notenPanel.addComponent(attempts);
        if(scoreSet.hasSubScore()){
          drawScores(scoreSet.getSubScore(), notenPanel, 2);
        }
      }
      it.remove();
    }
  }
  private void drawScores(HashMap<String, Score> scoreSet, Panel notenPanel, int indent){
    for(Map.Entry<String,Score> subMap: scoreSet.entrySet()){
      Score subSet = subMap.getValue();
      String scoreSubString;
      if(subSet.getScore()==0.0){
        scoreSubString = "-";
      }else{
        scoreSubString = "" + subSet.getScore();
      }
      String attemptsSubString;
      if(subSet.getAttempts()==0){
        attemptsSubString = "-";
      }else{
        attemptsSubString = "" + subSet.getAttempts();
      }
      String tabs = "";
      for(int j = 0; j<indent; j++){
       tabs += "\t";
      }
      Label subSubject        = new Label(tabs + subSet.getSubject());
      Label subScore          = new Label(scoreSubString);
      Label subAttempts       = new Label(attemptsSubString);
      notenPanel.addComponent(subSubject);
      notenPanel.addComponent(subScore);
      notenPanel.addComponent(subAttempts);
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
  private HashMap<Integer, ArrayList<Score>> sortSemester(HashMap<String, Score> syllabusMap){
    HashMap <Integer, ArrayList<Score>> sorted = new HashMap<Integer, ArrayList<Score>>();
    for(Map.Entry score: syllabusMap.entrySet()){
      Score value = (Score) score.getValue();
      ArrayList<Score> scoreList = null;
      if(sorted.get(value.getSemester()) == null){
        scoreList = new ArrayList<Score>();
      }else{
        scoreList = sorted.get(value.getSemester());
      }
      scoreList.add(value);
      sorted.put(value.getSemester(), scoreList);
    }
    return sorted;
  }
}
