import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
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
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.RadioBoxList;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalPosition;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.EOFException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;
import java.net.MalformedURLException;

/**
 * Die Klasse Visual verarbeitet alle Vorgänge zur Darstellung der Informationen
 * auf der Konsole.
 * @author Thomas Davies
 * @version 1.0
*/
public class Visual{
  private DefaultTerminalFactory terminalFactory = null;
  private Screen screen = null;
  private WindowBasedTextGUI textGUI = null;
  private ComboBox.Listener changeScoreEventListener;

  /**
   * Der Konstruktor initialisiert alle notwendigen Variablen um die grafische
   * Oberfläche des Terminals zu erzeugen.
   * <br> Um das Programm zu starten muss die Methode run() aufgerufen werden.
   */
  public Visual(){
    try{
      terminalFactory = new DefaultTerminalFactory();
      screen = terminalFactory.createScreen();
      textGUI = new MultiWindowTextGUI(screen);
    }catch(IOException e){
      e.printStackTrace();
    }
  }
  /**
   * Erzeugt Menü- und Durchschnittsfenster.
   */
  private void createWelcomeWindows(){
    String surname = DataHandler.getSurname();
    if(surname.length() != 0){
      surname = " " + surname;
    }

    final Window menueWindow = new BasicWindow("Willkommen"+surname+"!");
    final Window averageWindow = new BasicWindow("Durchschnitte");
    Panel averagePanel = new Panel(new LinearLayout(Direction.VERTICAL));
    Panel welcomePanel = new Panel(new LinearLayout(Direction.VERTICAL));
    welcomePanel.addComponent(new Label("Was möchtest du gerne tun?"));
    welcomePanel.addComponent(new Button("Noten anzeigen", new Runnable(){
      @Override
      public void run(){
        if(!DataHandler.testfileExists()){

        }
        createScoreScreen();
      }
    }));
    welcomePanel.addComponent(new Button("Note testen", new Runnable(){
      @Override
      public void run(){
        createTestScreen(0);
      }
    }));
    welcomePanel.addComponent(new Button("Notentester Zurücksetzen", new Runnable(){
      @Override
      public void run(){
        createAreYouSureScreen(textGUI);
      }
    }));
    welcomePanel.addComponent(new Button("Noten updaten", new Runnable(){
      @Override
      public void run(){
        updateData(menueWindow, averageWindow);
      }
    }));
    welcomePanel.addComponent(new Button("Exit", new Runnable(){
      @Override
      public void run(){
        close();
      }
    }));
    Label average = new Label("Durchschnitt: " + DataHandler.getUser().getAverage());
    Label bestAverage = new Label("Beste Endnote: " + DataHandler.getUser().getBestAverage());
    Label worstAverage = new Label("Schlechteste Endnote: " + DataHandler.getUser().getWorstAverage());
    averagePanel.addComponent(average);
    averagePanel.addComponent(bestAverage);
    averagePanel.addComponent(worstAverage);
    averageWindow.setComponent(averagePanel);
    averageWindow.setHints(Arrays.asList(Window.Hint.FIXED_POSITION, Window.Hint.NO_FOCUS, Window.Hint.FIXED_SIZE));
    menueWindow.setComponent(welcomePanel);
    textGUI.addWindow(menueWindow);
    TerminalSize menueSize = menueWindow.getDecoratedSize();
    TerminalPosition averagePos = new TerminalPosition(1, menueSize.getRows()+2);
    TerminalSize averageSize = new TerminalSize(menueSize.getColumns()-2, 3);
    averageWindow.setPosition(averagePos);
    averageWindow.setSize(averageSize);
    textGUI.addWindowAndWait(averageWindow);

  }

  /**
   * Diese Methode wird über das Menü aufgerufen. Sie erzeugt ein Fenster, in dem alle Noten dargestellt werden.
   */
  private void createScoreScreen(){
    final Window window = new BasicWindow("Notenliste");
    window.setHints(Arrays.asList(Window.Hint.FIXED_POSITION));
    window.setPosition(new TerminalPosition(0,0));
    Panel notenPanel = new Panel(new GridLayout(3));
    Label head1 = new Label("Fach");
    Label head2 = new Label("Versuch");
    Label head3 = new Label("Note");
    head1.addStyle(SGR.BOLD);
    head2.addStyle(SGR.BOLD);
    head3.addStyle(SGR.BOLD);
    notenPanel.addComponent(head1);
    notenPanel.addComponent(head2);
    notenPanel.addComponent(head3);
    HashMap<String, Score> syllabusMap = DataHandler.getSyllabus();
    User user = DataHandler.getUser();
    HashMap<Integer, ArrayList<Score>> sortedMap = sortSemester(syllabusMap);
    drawSemesters(sortedMap, notenPanel, 1);
    drawAverage(user, notenPanel);
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
  /**
   * Diese Methode wird von der createScoreScreen() Methode aufgerufen. Hiermit werden alle Module dem Notenpanel hinzugefügt.
   * <br> Mit jeder Iteration wird eine Map mit den Wahlpflichtfächern erstellt.
   * <br> Falls ein Modul Untermodule hat, werden diese mit höherer Identation ausgegeben.
   *
   * @param sortedMap Ist die Sortierte Map der Module. Das Fachsemester ist als Key gesetzt und der Score als Value.
   * @param notenPanel Ist das Panel was für das neue Fenster erzeugt wurde. Alle Einträge müssen hierin gesetzt werden.
   * @param indent Gibt an wie weit ein Eintrag eingerück werden muss.
   */
  private void drawSemesters(HashMap<Integer, ArrayList<Score>> sortedMap, Panel notenPanel, int indent){
    for(int i = 0; i<=sortedMap.size(); i++){
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
          if(scoreSet.hasSubScore()){
            attemptsString = "";
          }else if(scoreSet.getAttempts()==0){
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
          notenPanel.addComponent(attempts);
          notenPanel.addComponent(score);
          if(scoreSet.hasSubScore()){
            drawScores(scoreSet.getSubScore(), notenPanel, indent+1);
          }
        }
        drawWpf(wpfMap, notenPanel);
      }
    }
  }
  /**
   * Diese Methode wird von drawSemesters() aufgerufen. Sie fügt alle WahlPflichtfächer dem Notenpanel zu.
   * @param sortedMap Ist die Map, die alle WahlPflichtfächer des entsprechenden Semesters enthält.
   * @param notenPanel Ist das Panel was für das neue Fenster erzeugt wurde. Alle Einträge müssen hierin gesetzt werden.
   */
  private void drawWpf(HashMap<String, ArrayList<Score>> sortedMap, Panel notenPanel){
    Iterator<Map.Entry<String,ArrayList<Score>>> it = sortedMap.entrySet().iterator();
    while(it.hasNext()){
      Map.Entry<String, ArrayList<Score>> pair = it.next();
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
        notenPanel.addComponent(attempts);
        notenPanel.addComponent(score);
        if(scoreSet.hasSubScore()){
          drawScores(scoreSet.getSubScore(), notenPanel, 3);
        }
      }
      it.remove();
    }
  }
  /**
   * Diese Methode fügt alle nötigen Element dem Notenpanel hinzu. Von hier aus werden keine weiteren Funktionen aufgerufen.
   * @param scoreSet Enthält alle not abzubildenden Untermodule.
   * @param notenPanel Ist das Panel was für das neue Fenster erzeugt wurde. Alle Einträge müssen hierin gesetzt werden.
   * @param indent Gibt an um wieviel ein Eintrag eingerückt werden muss.
   */
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
      notenPanel.addComponent(subAttempts);
      notenPanel.addComponent(subScore);
    }
  }
  /**
   * Diese Methode ist dafür zuständig die Informationen des aktuellen Durchschnitts in das Notenpanel einzufügen
   * @param user enthält die Daten des Users inklusive seiner Durchschnitte
   * @param notenPanel Ist das Panel was für das neue Fenster erzeugt wurde. Alle Einträge müssen hierin gesetzt werden.
   */
  private void drawAverage(User user, Panel notenPanel){
    Label durchschnitt = new Label("Durchschnitt: ");
    durchschnitt.addStyle(SGR.BOLD);
    durchschnitt.setLayoutData(GridLayout.createLayoutData(
                               GridLayout.Alignment.BEGINNING, // Horizontal alignment in the grid cell if the cell is larger than the component's preferred size
                               GridLayout.Alignment.BEGINNING, // Vertical alignment in the grid cell if the cell is larger than the component's preferred size
                               true,                           // Give the component extra horizontal space if available
                               false,                          // Give the component extra vertical space if available
                               2,                              // Horizontal span
                               1));
    Label note = new Label("" + user.getAverage());
    note.addStyle(SGR.BOLD);
    notenPanel.addComponent(durchschnitt);
    notenPanel.addComponent(note);
  }
  /**
   * Diese Methode zeichnet das Notenfenster mit Testfunktion.
   * @param focus Gibt an welche Box im Fokus steht, wenn der Testscreen angelegt wird.
   */
  private void createTestScreen(int focus){
    if(!DataHandler.testfileExists()){
      DataHandler.createTestfile();
      DataHandler.createTestWpfCounter();
    }
    final BasicWindow window = new BasicWindow("Notenliste");
    window.setHints(Arrays.asList(Window.Hint.FIXED_POSITION));
    window.setPosition(new TerminalPosition(0,0));
    Panel notenPanel = new Panel(new GridLayout(3));
    Label head1 = new Label("Fach");
    Label head2 = new Label("Versuch");
    Label head3 = new Label("Note");
    head1.addStyle(SGR.BOLD);
    head2.addStyle(SGR.BOLD);
    head3.addStyle(SGR.BOLD);
    notenPanel.addComponent(head1);
    notenPanel.addComponent(head2);
    notenPanel.addComponent(head3);
    HashMap<String, Score> testMap = DataHandler.getTestMap();
    User user = DataHandler.getUser();
    HashMap<Integer, ArrayList<Score>> sortedMap = sortSemester(testMap);
    drawTestSemesters(window, sortedMap, notenPanel, 1, testMap, 0);
    drawTestAverage(notenPanel);
    Button exit = new Button("Exit", new Runnable(){
      @Override
      public void run(){
        window.close();
      }
    });
    notenPanel.addComponent(exit);
    window.setComponent(notenPanel);
    textGUI.addWindow(window);
    window.setFocusedInteractable(setFocus(notenPanel, focus));
  }
  /**
   * Diese Methode legt alle Informationen der einzelnen Semester an.
   * @param window Ist das Fenster indem alle Module dargestellt werden.
   * @param sortedMap Hier sind alle Semester nach ihrem Semester sortiert. Die Semester sind der Key und die Scores der entsprechende Value.
   * @param notenPanel Ist das Panel was für das neue Fenster erzeugt wurde. Alle Einträge müssen hierin gesetzt werden.
   * @param indent Gibt an wie weit ein Modul eingerückt werden muss.
   * @param testMap Hier sind alle Testnoten gespeichert.
   * @param boxCount Fügt jeder Box einen Index zu, damit sie beim Neuzeichnen des Fensters wiedergefunden werden kann.
   * @return Gibt den aktuellen Index der Boxen zurück
   */
  private int drawTestSemesters(Window window,
                                 HashMap<Integer, ArrayList<Score>> sortedMap,
                                 Panel notenPanel,
                                 int indent,
                                 HashMap<String,Score> testMap,
                                 int boxCount){
    for(int i = 0; i<=sortedMap.size(); i++){
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
                              false,                          // Give the component extra vertical space if available+
                              3,                              // Horizontal span
                              1));                            // Vertical span
        heading.addStyle(SGR.BOLD);
        notenPanel.addComponent(heading);
        for(final Score scoreSet: sortedMap.get(i)){
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
          Label            subject    = new Label(tabs + scoreSet.getSubject());
          ComboBox<String> scoreBox   = createScoreComboBox(scoreSet.hasSubScore());
          Label            attempts   = new Label(attemptsString);
          scoreBox.setReadOnly(true)
                  .addListener(changeScoreEvent(window, scoreSet, testMap, scoreBox, boxCount));
          notenPanel.addComponent(subject);
          notenPanel.addComponent(attempts);

          if(!scoreString.contains("-")){
            scoreBox.removeListener(changeScoreEventListener);
            scoreBox.setSelectedItem(scoreString);
            scoreBox.addListener(changeScoreEventListener);
          }
          notenPanel.addComponent(scoreBox);
          boxCount++;
          if(scoreSet.hasSubScore()){
            boxCount = drawTestScores(window, scoreSet.getSubScore(), notenPanel, indent+1, testMap, boxCount);
          }
        }
        boxCount = drawTestWpf(window, wpfMap, notenPanel, testMap, boxCount);
      }
    }
    return boxCount;
  }
  /**
   * Diese Methode fügt alle Untermodule hinzu.
   * @param window Ist das Fenster indem alle Module dargestellt werden.
   * @param scoreSet Hier sind alle Untermodule drin.
   * @param notenPanel Ist das Panel was für das neue Fenster erzeugt wurde. Alle Einträge müssen hierin gesetzt werden.
   * @param indent Gibt an wie weit ein Modul eingerückt werden muss.
   * @param testMap Hier sind alle Testnoten gespeichert.
   * @param boxCount Fügt jeder Box einen Index zu, damit sie beim Neuzeichnen des Fensters wiedergefunden werden kann.
   * @return Gibt den aktuellen Index der Boxen zurück
   */
  private int drawTestScores(Window window,
                              HashMap<String, Score> scoreSet,
                              Panel notenPanel,
                              int indent,
                              HashMap<String, Score> testMap,
                              int boxCount){
    for(Map.Entry<String,Score> subMap: scoreSet.entrySet()){
      final Score subSet = subMap.getValue();
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
      Label            subSubject  = new Label(tabs + subSet.getSubject());
      ComboBox<String> scoreBox    = createScoreComboBox(subSet.hasSubScore());
      Label            subAttempts = new Label(attemptsSubString);
      scoreBox.setReadOnly(true)
              .addListener(changeScoreEvent(window, subSet, testMap, scoreBox,boxCount));
      notenPanel.addComponent(subSubject);
      notenPanel.addComponent(subAttempts);
      if(!scoreSubString.contains("-")){
        scoreBox.removeListener(changeScoreEventListener);
        scoreBox.setSelectedItem(scoreSubString);
        scoreBox.addListener(changeScoreEventListener);
      }
      notenPanel.addComponent(scoreBox);
      boxCount++;
    }
    return boxCount;
  }
  /**
   * Diese Methode fügt alle Wahlpflichtfächer hinzu.
   * @param window Ist das Fenster indem alle Module dargestellt werden.
   * @param sortedMap Hier sind alle Module nach dem Semester geordnet. Semester sind der Key und Scores der Value.
   * @param notenPanel Ist das Panel was für das neue Fenster erzeugt wurde. Alle Einträge müssen hierin gesetzt werden.
   * @param indent Gibt an wie weit ein Modul eingerückt werden muss.
   * @param testMap Hier sind alle Testnoten gespeichert.
   * @param boxCount Fügt jeder Box einen Index zu, damit sie beim Neuzeichnen des Fensters wiedergefunden werden kann.
   * @return Gibt den aktuellen Index der Boxen zurück
   */
  private int drawTestWpf(Window window,
                           HashMap<String,
                           ArrayList<Score>> sortedMap,
                           Panel notenPanel,
                           HashMap<String, Score> testMap,
                           int boxCount){
    Iterator<Map.Entry<String,ArrayList<Score>>> it = sortedMap.entrySet().iterator();
    while(it.hasNext()){
      Map.Entry<String, ArrayList<Score>> pair = it.next();
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
        Label subject           = new Label(tabs + scoreSet.getSubject());
        ComboBox<String> scoreBox  = createScoreComboBox(scoreSet.hasSubScore());
        scoreBox.setReadOnly(true)
                .addListener(changeScoreEvent(window, scoreSet, testMap, scoreBox, boxCount));
        Label attempts       = new Label(attemptsString);
        notenPanel.addComponent(subject);
        notenPanel.addComponent(attempts);
        if(!scoreString.contains("-")){
          scoreBox.removeListener(changeScoreEventListener);
          scoreBox.setSelectedItem(scoreString);
          scoreBox.addListener(changeScoreEventListener);
        }
        notenPanel.addComponent(scoreBox);
        if(scoreSet.hasSubScore()){
          drawScores(scoreSet.getSubScore(), notenPanel, 3);
        }
        boxCount++;
      }
      it.remove();
    }
    return boxCount;
  }
  /**
   * Diese Methode stellt den Durchschnitt im Notentester dar
   * @param notenPanel Ist das Panel in dem die Elemente für das Fenster hinzugefügt werden
   */
  private void drawTestAverage(Panel notenPanel){
    Label durchschnitt = new Label("Durchschnitt: ");
    durchschnitt.addStyle(SGR.BOLD);
    durchschnitt.setLayoutData(GridLayout.createLayoutData(
                               GridLayout.Alignment.BEGINNING, // Horizontal alignment in the grid cell if the cell is larger than the component's preferred size
                               GridLayout.Alignment.BEGINNING, // Vertical alignment in the grid cell if the cell is larger than the component's preferred size
                               true,                           // Give the component extra horizontal space if available
                               false,                          // Give the component extra vertical space if available
                               2,                              // Horizontal span
                               1));
    Label note = new Label("" + DataHandler.getUser().getTestAverage());
    note.addStyle(SGR.BOLD);
    notenPanel.addComponent(durchschnitt);
    notenPanel.addComponent(note);
  }
  /**
   * Erstellt eine Liste mit möglichen Noten für Hauptmodule ohne Untermodule.
   * @return Gibt eine Liste an Noten als String in Form einer ArrayList zurück.
   */

  private ArrayList<String> createNonSubCandidates(){
    ArrayList<String> collection = new ArrayList<String>();
    collection.add("-");
    String candidate = "";
    for(int i = 1; i<6; i++){
      candidate = i + ".";
      if(i == 4 || i == 5){
        candidate += "0";
        collection.add(candidate);
      }else{
        for(int j = 0; j<3; j++){
          if(j == 2){
            candidate += ((j*3)+1);
            collection.add(candidate);
            candidate = i + ".";
          }else{
            candidate += j*3;
            collection.add(candidate);
            candidate = i + ".";
          }
        }
      }
    }
    return collection;
  }
  /**
   * Erstellt eine Liste mit möglichen Noten für Hauptmodule mit Untermodulen.
   * @return Gibt eine Liste an Noten als String in Form einer ArrayList zurück.
   */
  private ArrayList<String> createSubCandidates(){
    ArrayList<String> collection = new ArrayList<String>();
    collection.add("-");
    String candidate = "";
    for(int i = 1; i<6; i++){
      candidate = i + ".";
      if(i == 4 || i == 5){
        candidate += "0";
        collection.add(candidate);
      }else{
        for(int j = 0; j<10; j++){
            candidate += j;
            collection.add(candidate);
            candidate = i + ".";
        }
      }
    }
    return collection;
  }
  /**
   * Methode um das Programm zu starten. Es wird geprüft ob bereits Daten vorliegen.
   * Wenn keine Daten vorliegen, wird ein Login Screen eingeblendet, damit die Daten erstellt werden können
   */
  public void run(){
    try{
      screen.startScreen();
      if(DataHandler.mainfileExists()){
        createWelcomeWindows();
      }else{
        updateData(null, null);
        createWelcomeWindows();
      }

    }catch(IOException e){
      e.printStackTrace();
    }
  }
  /**
   * Beendet das Programm.
   */
  public void close(){
    if(screen != null){
      try{
        screen.stopScreen();
        System.exit(0);
      }catch(IOException e){
        e.printStackTrace();
      }
    }
  }
  /**
   * Sortiert alle Elemente der übergebenen Map nach den Semestern im Score Objekt.
   * @param syllabusMap Ist eine Map, die alle Kurse mit allen dazugehörigen Informationen enthält.
   * @return Es wird eine Map zurückgegeben, die als Key die Semester in Form von Integern hat und als Value eine ArrayList mit den dementsprechenden Modulen hat.
   */
  private HashMap<Integer, ArrayList<Score>> sortSemester(HashMap<String, Score> syllabusMap){
    HashMap <Integer, ArrayList<Score>> sorted = new HashMap<Integer, ArrayList<Score>>();
    for(Map.Entry<String,Score> score: syllabusMap.entrySet()){
      Score value = score.getValue();
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
  /**
   * Erzeugt eine Box(ComboBox), die für den Notentester mögliche Noten zur Auswahl hat.
   * @param hasSubScore gibt an ob eine Liste mit createNonSubCandidates oder mit createSubCandidates erstellt werden soll.
   * @return gibt eine ComboBox mit den Noten als Auswahloption zurück.
   */
  private ComboBox<String> createScoreComboBox(boolean hasSubScore){
    ComboBox<String> scoreBox;
    if(hasSubScore == false){
      scoreBox  = new ComboBox<String>(createNonSubCandidates());
    }else{
      scoreBox = new ComboBox<String>(createSubCandidates());
    }
    return scoreBox;
  }
  /**
   * Wenn eine Note im Notentester geändert wird, dann wird diese in der testMap in Form einer Datei gespeichert, sofern sich die Note geändert hat.
   * <br> Es wird ebenfalls überprüft, dass der User nicht mehr Wahlpflichtmodule testet, als es ihm vom Modulplan her möglich ist. Das wird mit updateTestWpf(...) realisiert.
   * <br> Zum Schluss wird das Fenster komplett geschlossen und alles neu gezeichnet. Dabei wird der boxCount übergeben, um den Fokus wieder auf die zuletzt aufgerufene Box zu setzen.
   * @param window Ist das TestScreen Fenster, was wieder geschlossen und neugezeichnet wird
   * @param score Das Score Objekt enthält die Daten um zu entscheiden ob es sich um ein Wahlpflichtmodul handelt oder nicht.
   * @param testMap Ist die Map, die alle Daten für den Notentester enthält
   * @param scoreBox Ist die Box, die gerade selectiert ist.
   * @param boxCount Gibt an welche Box selektiert wurde, damit diese dann wieder fokussiert werden kann.
   * @return Gibt einen ComboBox.Listener zurück, der alle Werte nach dem ändern aktualisiert hat.
   */
  private ComboBox.Listener changeScoreEvent(Window window, Score score, HashMap<String, Score> testMap, ComboBox<String> scoreBox, int boxCount){

    changeScoreEventListener = new ComboBox.Listener(){
      @Override public void onSelectionChanged(int selectedIndex, int previousSelection){
          if(!score.isWpf()){
            updateTestScore(score, testMap, scoreBox, selectedIndex, previousSelection);
            DataHandler.updateTestMap(testMap);
            DataHandler.updateTestAverage(Syllabus.updateAverage(testMap));

          }else{
            if(updateTestWpf(selectedIndex, previousSelection, score.getWpfTopic(), score)){
              updateTestScore(score, testMap, scoreBox, selectedIndex, previousSelection);
              DataHandler.updateTestMap(testMap);
              DataHandler.updateTestAverage(Syllabus.updateAverage(testMap));
            }else{
              scoreBox.removeListener(this);
              selectedIndex = previousSelection;
              scoreBox.setSelectedIndex(previousSelection);
              String module = "";
              if(score.getWpfWeight()[0] > 1){
                module = "Module";
              }else{
                module = "Modul";
              }
              MessageDialog.showMessageDialog(textGUI,
                                              "Achtung", "Du kannst maximal " + score.getWpfWeight()[0]
                                              + " " + module +" in " + score.getWpfTopic() + "auswählen!"
                                              , MessageDialogButton.OK);
              scoreBox.addListener(changeScoreEvent(window, score, testMap, scoreBox, boxCount));
            }
          }
          window.close();
          createTestScreen(boxCount);
        }
      };
      return changeScoreEventListener;
  }
  /**
   * Erzeugt einen Auswahlbildschirm, bei dem abgefragt, ob der Nutzer die Interaktion wirklich durchführen möchte.
   */
  private void createAreYouSureScreen(WindowBasedTextGUI textGUI){
    MessageDialog message = new MessageDialogBuilder()
                              .setTitle("Tester Zurücksetzen")
                              .setText("Sind Sie sicher, dass Sie den Tester zurücksetzen wollen?")
                              .addButton(MessageDialogButton.Cancel)
                              .addButton(MessageDialogButton.OK)
                              .build();
    Panel basePanel = (Panel) message.getComponent();
    Panel buttonPanel = null;
    for(Object obj: basePanel.getChildren()){
      if(obj instanceof Panel){
        buttonPanel = (Panel) obj;
      }
    }
    Button okayButton = null;
    for(Object obj: buttonPanel.getChildren()){
      Button btn = (Button) obj;
      if(btn.getLabel().equals("Ok")){
        okayButton = btn;
      }
    }
    okayButton.addListener(new Button.Listener(){
      @Override
      public void onTriggered(Button button){
        DataHandler.removeTestFile();
      }
    });
    message.showDialog(textGUI);
  }
  /**
   * Überprüft ob die maximale Anzahl von Wahlpflichmodulen schon erreicht ist.
   * @param selectedIndex Gibt an, welcher der neue gesetzte Index der Combobox is.
   * @param previousSelection Gibt an, welcher der zuletzt gesetzte Index der Combobox war.
   * @param wpfTopic Gibt an, unter welchem Thema das entsprechende Wahlpflichtmodul gezält wird.
   * @param score Gibt an, welcher der entsprechende Score/Modul ist, über den selektiert wurde.
   * @return Gibt "true" zurück, wenn ein Update durchgeführt wurde, ansonsten "false".
   */
  private boolean updateTestWpf(int selectedIndex, int previousSelection, String wpfTopic, Score score){
    boolean isUpdated = false;
    User user = DataHandler.getUser();
    if(user.getTestWpfCounter() == null){
      user.createTestWpfCounter();
    }
    if(user.getTestWpfCounter().get(wpfTopic) == null){
      user.getTestWpfCounter().put(wpfTopic, 0);
    }
    if(previousSelection != 0 && selectedIndex !=0){
      isUpdated = true;
    }else if((user.getTestWpfCounter().get(wpfTopic)<score.getWpfWeight()[0]) && previousSelection == 0 && previousSelection != selectedIndex){
      user.increaseTestWpfCounter(wpfTopic);
      isUpdated = true;
    }else if(selectedIndex == 0 && previousSelection != selectedIndex){
      user.decreaseTestWpfCounter(wpfTopic);
      isUpdated = true;
    }
    DataHandler.writeUser(user);
    return isUpdated;
  }
  /**
   * Updated die Combobox entsprechend der Auswahl, sofern eine neue Auswahl getroffen wurde.
   * <br> Wenn das Modul bisher ungetestet war, wird es als "gestestet" gesetzt und die Werte je nach dem geupdated.
   * <br> Wenn das Modul ein Übermodul von Untermodulen ist, werden die Untermodule zurückgesetzt.
   * <br> Wenn das Modul ein Untermodul ist, wird der Durchschnitt vom Übermodul angepasst, sofern alle Untermodule ihre Noten gesetzt haben.
   * @param score Ist das entsprechende Modul mit all seinen Werten.
   * @param testMap Ist die Map, die alle Module für den Notentester gespeichert hält.
   * @param scoreBox Ist die Box, die das Event auslöst.
   * @param selectedIndex Gibt an, welcher der neue gesetzte Index der Combobox is.
   * @param previousSelection Gibt an, welcher der zuletzt gesetzte Index der Combobox war.
   */
  private void updateTestScore(Score score, HashMap<String, Score> testMap, ComboBox<String> scoreBox, int selectedIndex, int previousSelection){
    if(!score.isTested() && selectedIndex != previousSelection){
      float testScore = 0.0f;
      if(!scoreBox.getSelectedItem().equals("-")){
        testScore = Float.parseFloat(scoreBox.getSelectedItem());
      }
      if(score.hasParentScore()){
        testMap.get(score.getParentStudienElement()).getSubScore().get(score.getStudienElement()).setIsTested();
        testMap.get(score.getParentStudienElement()).setIsTested();
        testMap.get(score.getParentStudienElement()).getSubScore().get(score.getStudienElement()).setScore(testScore);
        testMap.get(score.getParentStudienElement()).setScore(Syllabus.updateParentScore(testMap, score));
      }else{
        testMap.get(score.getStudienElement()).setIsTested();
        testMap.get(score.getStudienElement()).setScore(testScore);
      }
      if(score.hasSubScore()){
        Syllabus.resetSubScores(testMap, score);
      }
    }else if(selectedIndex != previousSelection){
      float testScore = 0.0f;
      if(!scoreBox.getSelectedItem().equals("-")){
        testScore = Float.parseFloat(scoreBox.getSelectedItem());
      }
      if(score.hasParentScore()){
        testMap.get(score.getParentStudienElement()).getSubScore().get(score.getStudienElement()).setScore(testScore);
        testMap.get(score.getParentStudienElement()).setScore(Syllabus.updateParentScore(testMap, score));
      }else{
        testMap.get(score.getStudienElement()).setScore(testScore);
      }
      if(score.hasSubScore()){
        Syllabus.resetSubScores(testMap, score);
      }
    }
  }
  /**
   * Diese Funktion öffnet ein Eingabefenster, um die Logindaten für die Hochschule einzugeben.
   * Dabei werden alle Fenster im Hintergrund geschlossen. Es wird nach Eingabe eine Verbindung zur Hochschule aufgebaut und versucht einen Modulplan(SyllabusMap) zu erstellen.
   * Je nach möglichen Fehler, wird eine Vermutung für die Ursache des Fehlers ausgegeben.
   * @param menueWindow Ist das Fenster, was das Menü enthält.
   * @param averageWindow Ist das Fenster, was die Durchschnitte enthält.
   */
  private void updateData(Window menueWindow, Window averageWindow){
    if(menueWindow != null && averageWindow != null){
      menueWindow.close();
      averageWindow.close();
    }
    final Window updateWindow = new BasicWindow("Login");
    Panel   panel             = new Panel(new GridLayout(2));
    Label   userLabel         = new Label("HSMW-Username:");
    Label   passwordLabel     = new Label("HSMW-Passwort:");
    TextBox userText          = new TextBox();
    TextBox passwordText      = new TextBox();
    GridLayout gridLayout = (GridLayout) panel.getLayoutManager();
    updateWindow.setPosition(new TerminalPosition(0,0));
    gridLayout.setHorizontalSpacing(4);
    passwordText.setMask('*');
    userText.setPreferredSize(new TerminalSize(25,1));
    passwordText.setPreferredSize(new TerminalSize(25,1));
    panel.addComponent(new EmptySpace().setLayoutData(
                        GridLayout.createHorizontallyFilledLayoutData(2)
                      ));
    panel.addComponent(userLabel);
    panel.addComponent(userText);
    panel.addComponent(passwordLabel);
    panel.addComponent(passwordText);
    panel.addComponent(new Button("Abbruch", new Runnable(){
      @Override
      public void run(){
        updateWindow.close();
        if(menueWindow != null && averageWindow != null){
          createWelcomeWindows();
        }
      }
    }));
    panel.addComponent(new Button("Okay", new Runnable(){
      @Override
      public void run(){
        String username = userText.getText();
        String password = passwordText.getText();
        try{
          Hsmw.createDataFromHSMW(username, password);
        }catch(MalformedURLException e){
          PrintWriter writer = null;
          try{
            writer = new PrintWriter("error.txt");
            writer.write(e.toString());
            e.printStackTrace(writer);
            writer.close();
          }catch(IOException ex){}
          MessageDialog.showMessageDialog(textGUI, "Ups", "Konnte die Internetseite nicht finden");

        }catch(IOException e){
          MessageDialog.showMessageDialog(textGUI, "Ups", "Das Lesen oder Schreiben ist schief gegangen");
          PrintWriter writer = null;
          try{
            writer = new PrintWriter("error.txt");
            writer.write(e.toString());
            e.printStackTrace(writer);
            writer.close();
          }catch(IOException ex){}
        }catch(StringIndexOutOfBoundsException e){
          MessageDialog.showMessageDialog(textGUI, "Ups", "Passwort vielleicht falsch?");
          PrintWriter writer = null;
          try{
            writer = new PrintWriter("error.txt");
            writer.write(e.toString());
            e.printStackTrace(writer);
            writer.close();
          }catch(IOException ex){}
        }catch(Exception e){
          MessageDialog.showMessageDialog(textGUI, "Ups", "Etwas ist schief gegangen");
          PrintWriter writer = null;
          try{
            writer = new PrintWriter("error.txt");
            writer.write(e.toString());
            e.printStackTrace(writer);
            writer.close();
          }catch(IOException ex){}
        }finally{
          updateWindow.close();
          if(menueWindow != null && averageWindow != null){
            createWelcomeWindows();
          }
        }
      }
    }));
    updateWindow.setHints(Arrays.asList(Window.Hint.CENTERED));
    updateWindow.setComponent(panel);
    textGUI.addWindowAndWait(updateWindow);

  }
  /**
   * Diese Funktion setzt den Fokus auf die zuletzt ausgewählte ComboBox, nachdem das Fenster neugezeichnet wurde.
   * @param panel Enthält alle Elemente, die im Fenster gezeichnet wurden.
   * @param focus Enthält die Nummer, welche Combobox zuletzt ausgewählt wurde.
   * @return Gibt die Combobox zurück, die fokussiert werden soll.
   */

  private ComboBox<String> setFocus(Panel panel, int focus){
    int i = 0;
    Collection<Component> components= panel.getChildren();
    ComboBox<String> focusBox = null;
    for(Component component: components){
      if(component instanceof ComboBox<?> && i == focus){
        @SuppressWarnings("unchecked")
        ComboBox<String> box = (ComboBox<String>)component;
        focusBox = box;
        break;
      }else if(component instanceof ComboBox<?>){
        i++;
      }
    }
    return focusBox;
  }
}
