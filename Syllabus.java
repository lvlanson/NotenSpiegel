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
    while(!(line = reader.readLine()).contains("typ8")){
      if(line.contains("EmSE MobileMain Name")){
        String studienElement = Extract.syllabusStudienElement(line);
        String subject = Extract.syllabusSubject(line);
        int[] weight = new int[2];
        while(!(line = reader.readLine()).contains("PGew")){
        }
        weight = Extract.syllabusWeight(line);
        syllabusMap.put(studienElement, new Score(studienElement, subject, weight));
      }
    }
    return true;
  }

  private boolean collectSpecialSyllabus(BufferedReader reader, String readLine) throws IOException{
    System.out.println("Collecting Special Syllabus");
    String line = "";
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
          String subject = Extract.syllabusSubject(line);
          int[] weight = new int[2];

          while(!(line = reader.readLine()).contains("PGew")){
          }
          weight = Extract.syllabusWeight(line);
          syllabusMap.put(studienElement, new Score(studienElement, subject, weight, true, wpfWeight, wpfTopic));
        }else{
          String studienElement = Extract.syllabusStudienElement(line);
          String subject = Extract.syllabusSubject(line);
          int[] weight = new int[2];

          while(!(line = reader.readLine()).contains("PGew")){
          }
          weight = Extract.syllabusWeight(line);
          syllabusMap.put(studienElement, new Score(studienElement, subject, weight));
        }

      }
    }
    return true;
  }
  private void extractScores(InputStream input){
    BufferedReader  reader         = new BufferedReader(new InputStreamReader(input));
    String          line           = "";
    String          studienElement = null;
    int             attempts       = 0;
    float           score          = 0;

    try{
      while((line = reader.readLine()) != null){
        if(line.contains("EmMNR Element") && !line.contains("th")){
          studienElement = Extract.studienElement(line);
        }else if(line.contains("Em200 SveStatus") && !line.contains("th")){
          score = Extract.score(line);
          syllabusMap.get(studienElement).setScore(score);
        }else if(line.contains("Em150 Versuch SveStatus") && !line.contains("th")){
          attempts = Extract.attempts(line);
          syllabusMap.get(studienElement).setAttempts(attempts);
        }
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
