import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;

public class Syllabus{
  private String fieldOfStudy;
  private String name;
  private String course;
  private HashMap<String, Score> syllabusMap;
  private float average;
  private boolean fieldOfStudyFound = false;

  private void findStudyInformation(InputStream in){
    //is found on https://www.intranet.hs-mittweida.de/sportal/his/studenten/student.info.asp?referer=&page_id=6527
    //Studentenportal -> Mein Studium
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
        if(line.contains("WPF") || line.contains("Spezialisierung")){
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
  public static float updateParentScore(HashMap<String, Score> syllabusMap, Score score){
    float parentAverage = 0.0f;
    for(Map.Entry<String, Score> entry: syllabusMap.get(score.getParentStudienElement()).getSubScore().entrySet()){
      parentAverage += entry.getValue().getScore() * entry.getValue().getWeight()[0] / entry.getValue().getWeight()[1];
    }
    parentAverage = (float)((int)((parentAverage)*10))/10;
    return parentAverage;
  }
  public static void resetSubScores(HashMap<String, Score> syllabusMap, Score score){
    for(Score subScore:syllabusMap.get(score.getStudienElement()).getSubScore().values()){
      subScore.setScore(0.0f);
    }
  }
  public void createSyllabus(InputStream basicStream, InputStream syllabusStream) throws Exception{
    //is found on https://www.intranet.hs-mittweida.de/sportal/his/studenten/student.ablauf.asp?referer=&page_id=6529
    //Studentenportal -> Mein Studium -> Mein Studienablauf
    //Settings: Anzeigemodus -> Studienablaufplan
    //          Ausgabe reduzieren -> auf Noten reduziert
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
  private String skipTo(BufferedReader reader, String string) throws IOException{
    String line = "";
    while(!(line = reader.readLine()).contains(string)){
    }
    return line;
  }
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
  private float calculateWorstAverage(HashMap<String, Score> syllabusMap){
    float average = 0.0f;
    int numerator = 0;
    int denominator = 0;
    for(Score score: syllabusMap.values()){
      if(score.getScore() == 5.0){
        System.out.println("Jup");
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

  public String getFieldOfStudy(){
    return this.fieldOfStudy;
  }
  public String getName(){
    return this.name;
  }
  public String getCourse(){
    return this.course;
  }
  public HashMap<String, Score> getSyllabusMap(){
    return syllabusMap;
  }
  public float getAverage(){
    return this.average;
  }
}
