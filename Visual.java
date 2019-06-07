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
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;
import java.net.MalformedURLException;


public class Visual{
  private DefaultTerminalFactory terminalFactory = null;
  private Screen screen = null;
  private WindowBasedTextGUI textGUI = null;
  private ComboBox.Listener changeScoreEventListener;

  public Visual(){
    try{
      terminalFactory = new DefaultTerminalFactory();
      screen = terminalFactory.createScreen();
      textGUI = new MultiWindowTextGUI(screen);
    }catch(IOException e){
      e.printStackTrace();
    }
  }
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
                              false,                          // Give the component extra vertical space if available
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
  private ComboBox<String> createScoreComboBox(boolean hasSubScore){
    ComboBox<String> scoreBox;
    if(hasSubScore == false){
      scoreBox  = new ComboBox<String>(createNonSubCandidates());
    }else{
      scoreBox = new ComboBox<String>(createSubCandidates());
    }
    return scoreBox;
  }
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
          MessageDialog.showMessageDialog(textGUI, "Ups", "Konnte die Internetseite nicht finden");
        }catch(IOException e){
          MessageDialog.showMessageDialog(textGUI, "Ups", "Das Lesen oder Schreiben ist schief gegangen");
        }catch(StringIndexOutOfBoundsException e){
          MessageDialog.showMessageDialog(textGUI, "Ups", "Passwort vielleicht falsch?");
          e.printStackTrace();
        }catch(Exception e){
          MessageDialog.showMessageDialog(textGUI, "Ups", "Etwas ist schief gegangen");
          e.printStackTrace();
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
