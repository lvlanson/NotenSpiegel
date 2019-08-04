import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Die Klasse Syllabus enthält alle Funktionen um von der HSMW Seite alle möglichen Module und die Basisinformationen eines Studenten
 * herunterzuladen. Diese Daten werden in den globalen Variablen der Klasse gespeichert.
 * @author Thomas Davies
 * @version 1.0
*/
public class Syllabus{
  private String fieldOfStudy;
  private String name;
  private String course;
  private HashMap<String, Score> syllabusMap;
  private float average;
  private boolean fieldOfStudyFound = false;

  /**
   * Diese Methode findet die Basisinformationen eines Studenten. Dazu zählen der vollständige Name(name), der Studienschwerpunkt(fieldOfStudy),
   * und den Studiengang(course):
   * @param in Dieser Inputstream hat die Verbindung zur Webseite der HSMW auf der die Basisinformationen zum Studium stehen.
   */
  private void findStudyInformation(InputStream in){
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new InputStreamReader(in));
      String  line              = "";
      boolean nameFound         = false;
      boolean courseFound       = false;

      while((line = reader.readLine()) != null){
        if(!nameFound && line.contains("Name:")){
          line = skipTo(reader, "Label");
          name = Extract.name(line);
          nameFound = true;
        }else if(!fieldOfStudyFound && line.contains("Studienrichtung:")){
          line = skipTo(reader, "SysButtonIconText");
          fieldOfStudy = Extract.fieldOfStudy(line);
          fieldOfStudyFound = true;
        }else if(!courseFound && line.contains("Studiengang:")){
          line = skipTo(reader, "SysButtonIconText");
          course = Extract.course(line);
          courseFound = true;
        }

        if(courseFound && fieldOfStudyFound && nameFound){
          break;
        }

      }
    }catch(IOException e){
      e.printStackTrace();
    }finally{
      if(reader != null){
        try{
          reader.close();
        }catch(IOException e){
          e.printStackTrace();
        }
      }
    }
  }
  /**
   * Diese Methode sammelt alle Basismodule des Studiengangs. Die Suche läuft so lange bis eine Zeile im HTML Dokument den String
   * "typ8" enthält. Dieser signalisiert das Ende der Basismodule.
   * @param reader Enthält Stream zum Modulplan.
   * @return Wenn der Rückgabewert "true" zurückgegeben wird, signalisiert es das Ende der Funktion.
   * @throws IOException Die Exception wird von reader.readLine() erzeugt und weitergegeben.
   */
  private boolean collectMainSyllabus(BufferedReader reader) throws IOException{
    String line = "";
    String lastStudienElement = "";
    Float score = 0.0f;

    boolean isSubScore = false;
    while(!(line = reader.readLine()).contains("typ8")){
      if(line.contains("EmSE EmFlex Name")){
        String studienElement = Extract.syllabusStudienElement(line);
        if(studienElement.contains(lastStudienElement) && lastStudienElement.length()>0){
          isSubScore = true;
        }
        String subject = Extract.syllabusSubject(line);

        int[] weight = new int[2];
        line = skipTo(reader, "pruefung PGew");
        weight = Extract.syllabusWeight(line);

        line = skipTo(reader, "pruefung RF");
        int semester = Extract.semester(line);

        line = skipTo(reader, "note PVAnz");
        int attempt = Extract.attempts(line);

        line = skipTo(reader, "note GNote");
        score = Extract.score(line);

        if(isSubScore){
          syllabusMap.get(lastStudienElement).setSubScore(studienElement, subject, semester, weight, lastStudienElement, score, attempt);
          isSubScore = false;
        }else{
          syllabusMap.put(studienElement, new Score(studienElement, subject, semester, weight, score, attempt));
          lastStudienElement = studienElement;
        }
      }
    }
    return true;
  }
  /**
   * Diese Methode sammelt alle Module aus dem Studienschwerpunkt. Die Suche läuft solange, bis die Identation auf der
   * HSMW Seite wieder um eins verringert wird. Die Studienschwerpunkte sind um eine Identation eingerückt.
   * @param reader Enthält Stream zum Modulplan.
   * @param readLine Enthält die zuletzt eingelesene Zeile.
   * @throws IOException Die Exception wird von reader.readLine() erzeugt und weitergegeben.
   */
  private boolean collectSpecialSyllabus(BufferedReader reader, String readLine) throws IOException{
    String      line                = "";
    String      lastStudienElement  = "";
    boolean     isSubScore          = false;
    boolean     wpfFound            = false;
    int[]       wpfWeight           = {0, 0};
    int         wpfTreeLevel        = 0;
    String      wpfTopic            = "";
    float       score               = 0.0f;
    readLine =  readLine.substring(readLine.indexOf("SysTreeLevel")+"SysTreeLevel".length());
    int         indentLevel         = Integer.parseInt(readLine.substring(0,readLine.indexOf("'")));

    while(!(line = reader.readLine()).contains("SysTreeLevel"+indentLevel)){
      if(line.contains("strike")){
        skipTo(reader, "</tr>");
      }
      if(line.contains("EmSE EmFlex Name")){
        if(line.contains("WPF")){
          wpfFound = true;
          wpfWeight = Extract.wpfWeight(line);
          wpfTopic = Extract.wpfTopic(line);
          continue;
        }
        if(wpfFound && wpfTreeLevel>Extract.level(line) && Extract.syllabusStudienElement(line).length()<=4){
          wpfFound = false;
          wpfTreeLevel = 0;
        }
        if(wpfFound){
          if(Extract.syllabusStudienElement(line).length() <= 4){
            wpfTreeLevel = Extract.level(line);
          }
          String studienElement = Extract.syllabusStudienElement(line);
          if(studienElement.contains(lastStudienElement) && lastStudienElement.length()>0){
            isSubScore = true;
          }
          String subject = Extract.syllabusSubject(line);
          int[] weight = new int[2];
          line = skipTo(reader, "pruefung PGew");
          weight = Extract.syllabusWeight(line);

          line = skipTo(reader, "pruefung RF");
          int semester = Extract.semester(line);

          line = skipTo(reader, "note GNote");
          score = Extract.score(line);

          if(isSubScore){
            syllabusMap.get(lastStudienElement).setSubScore(studienElement, subject, semester, weight, true, wpfWeight, wpfTopic, lastStudienElement, score);
            if(syllabusMap.get(lastStudienElement).getSemester() == 0){
              syllabusMap.get(lastStudienElement).setSemester(semester);
            }
            isSubScore = false;
          }else{
            syllabusMap.put(studienElement, new Score(studienElement, subject, semester, weight, true, wpfWeight, wpfTopic, score));
            lastStudienElement = studienElement;
          }
        }else{
          String studienElement = Extract.syllabusStudienElement(line);
          if(studienElement.contains(lastStudienElement) && lastStudienElement.length()>0){
            isSubScore = true;
          }
          String subject = Extract.syllabusSubject(line);

          int[] weight = new int[2];
          line = skipTo(reader, "pruefung PGew");
          weight = Extract.syllabusWeight(line);

          line = skipTo(reader, "pruefung RF");
          int semester = Extract.semester(line);

          line = skipTo(reader, "note PVAnz");
          int attempt = Extract.attempts(line);

          line = skipTo(reader, "note GNote");
          score = Extract.score(line);

          if(isSubScore){
            syllabusMap.get(lastStudienElement).setSubScore(studienElement, subject, semester, weight, lastStudienElement, score, attempt);
            isSubScore = false;
          }else{
            syllabusMap.put(studienElement, new Score(studienElement, subject, semester, weight, score, attempt));
            lastStudienElement = studienElement;
          }
        }

      }
    }
    return true;
  }
  /**
   * Diese Methode verwaltet den kompletten Sammelablauf und startet je nach Position des Pointers im Stream
   * die entsprechende Methode um die Daten zu sammeln.
   * @param reader Ist der Stream zum Modulplan.
   * @param readLine Ist die zuletzt ausgelesen Zeile.
   * @throws IOException Die Exception wird von reader.readLine() erzeugt und weitergegeben.
   */
  private boolean collectCompleteSyllabus(BufferedReader reader, String readLine) throws IOException{
    String      line                = "";
    String      lastStudienElement  = "";
    boolean     isSubScore          = false;
    boolean     wpfFound            = false;
    int[]       wpfWeight           = {0, 0};
    int         wpfTreeLevel        = 0;
    String      wpfTopic            = "";
    float       score               = 0.0f;
    readLine =  readLine.substring(readLine.indexOf("SysTreeLevel")+"SysTreeLevel".length());
    int         indentLevel         = Integer.parseInt(readLine.substring(0,readLine.indexOf("'")));

    while(!(line = reader.readLine()).contains("SysTreeLevel"+indentLevel) && !(line = reader.readLine()).contains("/table")){

      if(line.contains("strike")){
        skipTo(reader, "</tr>");
      }
      if(line.contains("EmSE EmFlex Name")){
        if(line.contains("WPF") || line.contains("Spezialisierung") || line.contains("Wahlpflicht")){
          wpfFound = true;
          wpfWeight = Extract.wpfWeight(line);
          wpfTopic = Extract.wpfTopic(line);
          continue;
        }
        if(wpfFound && wpfTreeLevel>Extract.level(line) && Extract.syllabusStudienElement(line).length()<=4){
          wpfFound = false;
          wpfTreeLevel = 0;
        }
        if(wpfFound){
          if(Extract.syllabusStudienElement(line).length() <= 4){
            wpfTreeLevel = Extract.level(line);
          }
          String studienElement = Extract.syllabusStudienElement(line);
          if(studienElement.contains(lastStudienElement) && lastStudienElement.length()>0){
            isSubScore = true;
          }
          String subject = Extract.syllabusSubject(line);
          int[] weight = new int[2];
          line = skipTo(reader, "pruefung PGew");
          weight = Extract.syllabusWeight(line);

          line = skipTo(reader, "pruefung RF");
          int semester = Extract.semester(line);

          line = skipTo(reader, "note GNote");
          score = Extract.score(line);

          if(isSubScore){
            syllabusMap.get(lastStudienElement).setSubScore(studienElement, subject, semester, weight, true, wpfWeight, wpfTopic, lastStudienElement, score);
            if(syllabusMap.get(lastStudienElement).getSemester() == 0){
              syllabusMap.get(lastStudienElement).setSemester(semester);
            }
            isSubScore = false;
          }else{
            syllabusMap.put(studienElement, new Score(studienElement, subject, semester, weight, true, wpfWeight, wpfTopic, score));
            lastStudienElement = studienElement;
          }
        }else{
          String studienElement = Extract.syllabusStudienElement(line);
          if(studienElement.contains(lastStudienElement) && lastStudienElement.length()>0){
            isSubScore = true;
          }
          String subject = Extract.syllabusSubject(line);

          int[] weight = new int[2];
          line = skipTo(reader, "pruefung PGew");
          weight = Extract.syllabusWeight(line);

          line = skipTo(reader, "pruefung RF");
          int semester = Extract.semester(line);

          line = skipTo(reader, "note PVAnz");
          int attempt = Extract.attempts(line);

          line = skipTo(reader, "note GNote");
          score = Extract.score(line);

          if(isSubScore){
            syllabusMap.get(lastStudienElement).setSubScore(studienElement, subject, semester, weight, lastStudienElement, score, attempt);
            isSubScore = false;
          }else{
            syllabusMap.put(studienElement, new Score(studienElement, subject, semester, weight, score, attempt));
            lastStudienElement = studienElement;
          }
        }

      }
    }
    return true;
  }
  /**
   * Diese Methode sammelt alle Daten die zum abschließenden Modulplan gehört. Dazu gehören für gewöhnlich das Praxismodul und die Bachelorarbeit.
   * @param reader Ist der Stream zum Modulplan.
   * @param line Ist die zuletzt ausgelesen Zeile.
   * @throws IOException Die Exception wird von reader.readLine() erzeugt und weitergegeben.
   */
  private boolean collectFinalSyllabus(BufferedReader reader, String line) throws IOException{
    String  lastStudienElement = "";
    boolean isSubScore         = false;
    float   score              = 0.0f;
    while(!(line.contains("ControlsLegend"))){
      if(line.contains("EmSE EmFlex Name")){
        String studienElement = Extract.syllabusStudienElement(line);
        if(studienElement.contains(lastStudienElement) && lastStudienElement.length()>0){
          isSubScore = true;
        }
        String subject = Extract.syllabusSubject(line);

        int[] weight = new int[2];
        line = skipTo(reader, "PGew");
        weight = Extract.syllabusWeight(line);

        line = skipTo(reader, "pruefung RF");
        int semester = Extract.semester(line);

        line = skipTo(reader, "note PVAnz");
        int attempt = Extract.attempts(line);

        if(isSubScore){
          syllabusMap.get(lastStudienElement).setSubScore(studienElement, subject, semester, weight, lastStudienElement, score, attempt);
          isSubScore = false;
        }else{
          syllabusMap.put(studienElement, new Score(studienElement, subject, semester, weight, score, attempt));
          lastStudienElement = studienElement;
        }
      }
      line = reader.readLine();
    }
    return true;
  }
  /**
   * Diese Methode berechnet den Durchschnitt für die übergebene Menge an Modulen.
   * @param scoreSet Enthält alle Module aus denen ein Durchschnitt errechnet werden soll. Die Module sind vom Typ "Score".
   * @return Gibt einen Durchschnitt vom Typ float zurück mit der Formatierung "#.#".
   */
  private float calculateAverage(Collection<Score> scoreSet){
    float average = 0.0f;
    int denominator = 0;
    for(Score score: scoreSet){
      if(score.getScore() != 0){
        average += score.getScore()*score.getWeight()[0];
        denominator += score.getWeight()[0];
      }
    }
    average = (float)((int)((average/denominator)*10))/10;
    return average;
  }

  /**
   * Diese Methode aktualisiert den Zähler für die Wahlpflichtfächer. Der Zähler wird im Userobjekt gespeichert und in die Userfile geschrieben.
   * @param user Ist der User, indem der Zähler gespeichert wird.
   */
  private void updateWpfCounter(User user){
    for(Score score: syllabusMap.values()){
      if(score.hasSubScore()){
        for(Score subScore: score.getSubScore().values()){
          if(subScore.isWpf() && subScore.getScore() != 0){
            if(user.getWpfCounter() == null){
              user.setWpfCounter(new HashMap<String, Integer>());
            }
            user.increaseWpfCounter(subScore.getStudienElement());
          }
        }
      }
      if(score.isWpf()){
        if(score.isWpf() && score.getScore() != 0){
          if(user.getWpfCounter() == null){
            user.setWpfCounter(new HashMap<String, Integer>());
          }
          user.increaseWpfCounter(score.getStudienElement());
        }
      }
    }
    DataHandler.writeUser(user);
  }
  /**
   * Diese Methode berechnet den Durchschnitt für eine Menge an Modulen.
   * @param syllabusMap Ist eine HashMap, die als Value die Module(Score) enthält.
   * @return Gibt einen Durchschnitt vom Typ float zurück mit der Formatierung "#.#".
   */
  public static float updateAverage(HashMap<String, Score> syllabusMap){
    float average = 0.0f;
    int denominator = 0;
    for(Score score: syllabusMap.values()){
      if(score.getScore() != 0){
        average += score.getScore()*score.getWeight()[0];
        denominator += score.getWeight()[0];
      }
    }
    return (float)((int)((average/denominator)*10))/10;
  }
  /**
   * Sofern ein Modul ein Übermodul hat, berechnet diese Methode den Durchschnitt des Übermoduls neu.
   * @param syllabusMap Enthält alle Module.
   * @param score Ist ein Untermodul, dessen Übermoduls Durchschnitt neu berechnet werden soll.
   * @return Gibt einen Durchschnitt vom Typ float zurück mit der Formatierung "#.#".
   */
  public static float updateParentScore(HashMap<String, Score> syllabusMap, Score score){
    float parentAverage = 0.0f;
    for(Map.Entry<String, Score> entry: syllabusMap.get(score.getParentStudienElement()).getSubScore().entrySet()){
      parentAverage += entry.getValue().getScore() * entry.getValue().getWeight()[0] / entry.getValue().getWeight()[1];
    }
    parentAverage = (float)((int)((parentAverage)*10))/10;
    return parentAverage;
  }
  /**
   * Sofern ein Übermodul seinen Score geändert hat, werden mit dieser Methode die Noten der Untermodule alle
   * zurückgesetzt.
   */
  public static void resetSubScores(HashMap<String, Score> syllabusMap, Score score){
    for(Score subScore:syllabusMap.get(score.getStudienElement()).getSubScore().values()){
      subScore.setScore(0.0f);
    }
  }
  /**
   * Ist die Methode, die die ganze Erstellung aller Informationen eines Users koordiniert. Dazu gehören die Basisuserdaten zu erzeugen
   * als auch den ganzen Modulplan auszuwerten und alle Module aufzusammeln.
   * @param basicStream Enthält den Stream zur Seite auf der HSMW Seite zu den Basisinformationen.
   * @param syllabusStream Enthält den Stream zur Seite auf der HSMW Seite mit dem Modulplan.
   * @throws IOException Wird von divsersion In- und Output Operationen geworfen und an die aufrufende Methode weitergegeben.
   */
  public void createSyllabus(InputStream basicStream, InputStream syllabusStream) throws IOException{
    findStudyInformation(basicStream);
    syllabusMap = new HashMap<String, Score>();
    BufferedReader reader = null;
    try{

      reader = new BufferedReader(new InputStreamReader(syllabusStream));
      String line = "";
      boolean isMainSyllabusDone = false;
      boolean isSpecialSyllabusDone = false;
      boolean isFinalSyllabusDone = false;
      if(fieldOfStudyFound){
        while(((line = reader.readLine()) != null)){
          if(!isMainSyllabusDone && line.contains(this.course)){
            isMainSyllabusDone = collectMainSyllabus(reader);
          }else if(!isSpecialSyllabusDone && (line.contains(this.fieldOfStudy))){
            isSpecialSyllabusDone = collectSpecialSyllabus(reader, line);
          }else if(isSpecialSyllabusDone && !isFinalSyllabusDone && (line.contains("SysTreeLevel1"))){
            isFinalSyllabusDone = collectFinalSyllabus(reader, line);
          }else if(isSpecialSyllabusDone && isMainSyllabusDone && isFinalSyllabusDone){
            break;
          }
        }
      }else{
        while(((line = reader.readLine()) != null)){
          if(!isSpecialSyllabusDone && line.contains(this.course)){
            isSpecialSyllabusDone = collectCompleteSyllabus(reader, line);
          }else if(isSpecialSyllabusDone){
            break;
          }
        }
      }
    }catch(IOException e){
      e.printStackTrace();
    }finally{
      updateSemesters();
      average = calculateAverage(syllabusMap.values());
      float testAverage = 0.0f;
      if(DataHandler.userfileExists()){
        testAverage = DataHandler.getUser().getTestAverage();
      }

      User user = new User(name, course, fieldOfStudy, average, testAverage,
                           calculateBestAverage(syllabusMap),
                           calculateWorstAverage(syllabusMap));
      updateWpfCounter(user);
      DataHandler.run();
      DataHandler.writeSyllabus(syllabusMap);
      DataHandler.writeUser(user);
      if(reader != null){
        try{
          reader.close();
        }catch(IOException e){
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Diese Methode rückt den Streampointer bis eine Zeile den Ausdruck von string enthält.
   * @param reader Enthält den Stream, den es zu durchsuchen gilt.
   * @param string Ist der String, bis zu dem durchsucht werden soll.
   * @return Gibt die Zeile zuurück, in der string enthalten ist.
   * @throws IOException Die Exception wird von reader.readLine() erzeugt und weitergegeben.
   */
  private String skipTo(BufferedReader reader, String string) throws IOException{
    String line = "";
    while(!(line = reader.readLine()).contains(string)){
    }
    return line;
  }

  /**
   * Diese Methode aktualisiert für die Untermodule die Semester, sofern sie nicht eingetragen sind.
   */
  private void updateSemesters(){
    for(Map.Entry<String, Score> entry: syllabusMap.entrySet()){
      Score entryScore = entry.getValue();
      if(entryScore.hasSubScore() && entryScore.getSemester() != 0){
        for(Map.Entry<String,Score> subEntry: entryScore.getSubScore().entrySet()){
          Score subScore = subEntry.getValue();
          if(subScore.getSemester()==0){
            subScore.setSemester(entryScore.getSemester());
          }
        }
      }else if(entryScore.hasSubScore() && entryScore.getSemester() == 0){
        for(Map.Entry<String,Score> subEntry: entryScore.getSubScore().entrySet()){
          Score subScore = subEntry.getValue();
          if(subScore.getSemester()!=0){
            entryScore.setSemester(subScore.getSemester());
          }
        }
      }
    }
  }
  /**
   * Diese Methode berechnet die noch bestmöglichste Note, die erreichbar ist. Dabei wird ebenfalls berücksichtigt, dass eine 5.0 noch zur 1.0 werden kann
   * @param syllabusMap Enthält alle Module.
   * @return Gibt den bestmöglichsten Durchschnitt vom Typ float zurück mit der Formatierung "#.#".
   */
  private float calculateBestAverage(HashMap<String, Score> syllabusMap){
    float average = 0.0f;
    int numerator = 0;
    int denominator = 0;
    for(Score score: syllabusMap.values()){
      if(score.getScore() == 5.0f){
        numerator+=score.getWeight()[0];
        average += 1.0 * score.getWeight()[0] / score.getWeight()[1];
      }else if(score.getScore() != 0){
        numerator+=score.getWeight()[0];
        average += score.getScore() * score.getWeight()[0] / score.getWeight()[1];
      }
      denominator = score.getWeight()[1];
    }
    average += ((float)(denominator-numerator)*1.0f)/denominator;
    average = (float)((int)((average)*10))/10;
    return average;
  }
  /**
   * Diese Methode berechnet die noch schlechtmöglichste Note, die erreichbar ist. Dabei wird ebenfalls berücksichtigt, dass eine 5.0 noch zur 4.0 werden kann
   * @param syllabusMap Enthält alle Module.
   * @return Gibt den schlechtmöglichsten Durchschnitt vom Typ float zurück mit der Formatierung "#.#".
   */
  private float calculateWorstAverage(HashMap<String, Score> syllabusMap){
    float average = 0.0f;
    int numerator = 0;
    int denominator = 0;
    for(Score score: syllabusMap.values()){
      if(score.getScore() == 5.0){
        numerator+=score.getWeight()[0];
        average += 4.0 * score.getWeight()[0] / score.getWeight()[1];
      }else if(score.getScore() != 0){
        numerator+=score.getWeight()[0];
        average += score.getScore() * score.getWeight()[0] / score.getWeight()[1];
      denominator = score.getWeight()[1];
      }
    }
    average += ((float)(denominator-numerator)*4.0f)/denominator;
    average = (float)((int)((average)*10))/10;
    return average;
  }
  /**
   * @return Gibt den Studienschwerpunkt zurück, der in fieldOfStudy gespeichert ist.
   */
  public String getFieldOfStudy(){
    return this.fieldOfStudy;
  }
  /**
   * @return Gibt den Namen zurück, der in name gespeichert ist.
   */
  public String getName(){
    return this.name;
  }
  /**
   * @return Gibt den Studiengang zurück, der in course gespeichert ist.
   */
  public String getCourse(){
    return this.course;
  }
  /**
   * @return Gibt die Map mit sämtlichen Modulen zurück, die in syllabusMap gespeichert ist.
   */
  public HashMap<String, Score> getSyllabusMap(){
    return syllabusMap;
  }
  /**
   * @return Gibt den Durchschnitt zurück, der in average gespeichert ist.
   */
  public float getAverage(){
    return this.average;
  }
}
