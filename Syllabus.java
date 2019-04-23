import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.HashMap;


public class Syllabus{
  private String fieldOfStudy;
  private String name;
  private String course;
  private HashMap<String, Score> syllabusMap;

  private int findSemester(BufferedReader reader) throws IOException{
    String line = reader.readLine();
    int semester = Extract.semester(line);
    while(semester == 0 && !line.contains("S8")){
      line = reader.readLine();
      semester = Extract.semester(line);
    }
    return semester;
  }
  private void findStudyInformation(InputStream in){
    //is found on https://www.intranet.hs-mittweida.de/sportal/his/studenten/student.info.asp?referer=&page_id=6527
    //Studentenportal -> Mein Studium
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new InputStreamReader(in));
      String  line              = "";
      boolean fieldOfStudyFound = false;
      boolean nameFound         = false;
      boolean courseFound       = false;
      boolean findFOS           = true;
      boolean findName          = true;
      boolean findCourse        = true;

      while((line = reader.readLine()) != null){
        if(findFOS && !fieldOfStudyFound && line.contains("Studienrichtung:")){
          fieldOfStudyFound = true;
          continue;
        }else if(findFOS && fieldOfStudyFound && line.contains("<p")){
          fieldOfStudy = Extract.fieldOfStudy(line);
          findFOS = false;
        }
        if(findName && !nameFound && line.contains("Name:")){
          nameFound = true;
          continue;
        }else if(findName && nameFound && line.contains("class=\"Label\"")){
          name = Extract.name(line);
          findName = false;
        }
        if(findCourse && !courseFound && line.contains("Studiengang:")){
          courseFound = true;
          continue;
        }else if(findCourse && courseFound && line.contains("class=\"Label\"")){
          course = Extract.course(line);
          findCourse = false;
        }
        if(!findFOS && !findName && !findCourse){
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
    boolean isSubScore = false;
    while(!(line = reader.readLine()).contains("typ8")){
      if(line.contains("EmSE MobileMain Name")){
        String studienElement = Extract.syllabusStudienElement(line);
        if(studienElement.contains(lastStudienElement) && lastStudienElement.length()>0){
          isSubScore = true;
        }
        String subject = Extract.syllabusSubject(line);
        int semester = findSemester(reader);
        int[] weight = new int[2];
        while(!(line = reader.readLine()).contains("PGew")){
        }
        weight = Extract.syllabusWeight(line);
        if(isSubScore){
          syllabusMap.get(lastStudienElement).setSubScore(studienElement, subject, semester, weight);
          isSubScore = false;
        }else{
          syllabusMap.put(studienElement, new Score(studienElement, subject, semester, weight));
          lastStudienElement = studienElement;
        }
      }
    }
    return true;
  }
  private boolean collectSpecialSyllabus(BufferedReader reader, String readLine) throws IOException{
    String line = "";
    String lastStudienElement = "";
    boolean isSubScore = false;
    readLine = readLine.substring(readLine.indexOf("SysTreeLevel")+"SysTreeLevel".length());
    int indentLevel = Integer.parseInt(readLine.substring(0,readLine.indexOf("\"")));
    boolean wpfFound = false;
    int[] wpfWeight = {0, 0};
    String wpfTopic = "";
    while(!(line = reader.readLine()).contains("SysTreeLevel"+indentLevel)){
      if(line.contains("EmSE MobileMain Name")){
        if(line.contains("WPF")){
            wpfFound = true;
            wpfWeight = Extract.wpfWeight(line);
            wpfTopic = Extract.wpfTopic(line);
            continue;
        }
        if(wpfFound){
          String studienElement = Extract.syllabusStudienElement(line);
          if(studienElement.contains(lastStudienElement) && lastStudienElement.length()>0){
            isSubScore = true;
          }
          String subject = Extract.syllabusSubject(line);
          int[] weight = new int[2];
          int semester = findSemester(reader);
          while(!(line = reader.readLine()).contains("PGew")){
          }
          weight = Extract.syllabusWeight(line);
          if(isSubScore){
            syllabusMap.get(lastStudienElement).setSubScore(studienElement, subject, semester, weight, true, wpfWeight, wpfTopic);
            isSubScore = false;
          }else{
            syllabusMap.put(studienElement, new Score(studienElement, subject, semester, weight, true, wpfWeight, wpfTopic));
            lastStudienElement = studienElement;
          }
        }else{
          String studienElement = Extract.syllabusStudienElement(line);
          if(studienElement.contains(lastStudienElement) && lastStudienElement.length()>0){
            isSubScore = true;
          }
          String subject = Extract.syllabusSubject(line);
          int[] weight = new int[2];
          int semester = findSemester(reader);
          while(!(line = reader.readLine()).contains("PGew")){
          }
          weight = Extract.syllabusWeight(line);
          if(isSubScore){
            syllabusMap.get(lastStudienElement).setSubScore(studienElement, subject, semester, weight);
            isSubScore = false;
          }else{
            syllabusMap.put(studienElement, new Score(studienElement, subject, semester, weight));
            lastStudienElement = studienElement;
          }
        }

      }
    }
    return true;
  }
  private void extractScores(InputStream input){
    BufferedReader  reader         = new BufferedReader(new InputStreamReader(input));
    String          line           = "";
    String          studienElement = "";
    String          subStuEl       = "";
    int             attempts       = 0;
    float           score          = 0;

    try{
      while((line = reader.readLine()) != null){
        if(line.contains("EmMNR Element") && !line.contains("th")){
          studienElement = Extract.studienElement(line);
          if(studienElement.length()>4){
            subStuEl = studienElement;
            studienElement = subStuEl.substring(0,4);
          }
        }else if(line.contains("Em200 SveStatus") && !line.contains("th")){
          score = Extract.score(line);
          if(subStuEl.length()>0){
            syllabusMap.get(studienElement).getSubScore().get(subStuEl).setScore(score);
          }else{
            syllabusMap.get(studienElement).setScore(score);
          }

        }else if(line.contains("Em150 Versuch SveStatus") && !line.contains("th")){
          attempts = Extract.attempts(line);
          if(subStuEl.length()>0){
            syllabusMap.get(studienElement).getSubScore().get(subStuEl).setAttempts(attempts);
          }else{
            syllabusMap.get(studienElement).setAttempts(attempts);
          }
        }
        subStuEl = "";
      }
    }catch(IOException e){
      e.printStackTrace();
    }
  }
  public void createSyllabus(InputStream basicStream, InputStream syllabusStream, InputStream scoreStream) throws Exception{
    //is found on https://www.intranet.hs-mittweida.de/sportal/his/studenten/student.ablauf.asp?referer=&page_id=6529
    //Studentenportal -> Mein Studium -> Mein Studienablauf
    //Settings: Anzeigemodus -> Studienablaufplan
    //          Ausgabe reduzieren -> auf Noten reduziert
    findStudyInformation(basicStream);
    if(fieldOfStudy == null){
      throw new Exception("Couldn't find field of study information");
    }
    syllabusMap = new HashMap<String, Score>();
    BufferedReader reader = null;
    try{

      reader = new BufferedReader(new InputStreamReader(syllabusStream));
      String line = "";
      boolean isMainSyllabusDone = false;
      boolean isSpecialSyllabusDone = false;
      boolean isFinalSyllabusDone = false;
      while(((line = reader.readLine()) != null)){
        if(!isMainSyllabusDone && line.contains(this.course)){
          isMainSyllabusDone = collectMainSyllabus(reader);
        }else if(!isSpecialSyllabusDone && (line.contains(this.fieldOfStudy))){
          isSpecialSyllabusDone = collectSpecialSyllabus(reader, line);

        }else if(isSpecialSyllabusDone && isMainSyllabusDone && isFinalSyllabusDone){
          break;
        }

      }
    }catch(IOException e){
      e.printStackTrace();
    }finally{
      extractScores(scoreStream);
      DataHandler.run();
      DataHandler.writeSyllabus(syllabusMap);
      DataHandler.writeUser(new User(name, course, fieldOfStudy));
      if(reader != null){
        try{
          reader.close();
        }catch(IOException e){
          e.printStackTrace();
        }
      }
    }


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
}
