import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class Tester{
  public static void main(String[] args){
    InputStream in = null;
    InputStream in2 = null;
    InputStream in3 = null;
    try{
      File f = new File("fieldOfStudy.html");
      File f2 = new File("Syllabus.html");
      File f3 = new File("notentest.html");
      in = new FileInputStream(f);
      in2 = new FileInputStream(f2);
      in3 = new FileInputStream(f3);
      Syllabus s = new Syllabus();
      try{
        s.createSyllabus(in, in2, in3);
      }catch(Exception e){
        e.printStackTrace();
      }

      HashMap<String, Score> syllabusMap = DataHandler.getTestMap();
      System.out.println("Testmap:");
      System.out.println(syllabusMap.get("2120").getSubScore().get("2120(T1)").hasParentScore());
      Iterator it = s.getSyllabusMap().entrySet().iterator();
      while(it.hasNext()){
        Map.Entry pair = (Map.Entry)it.next();
        Score sc = (Score) pair.getValue();
        if(sc.hasSubScore()){
          for(Score ns : sc.getSubScore().values()){
            if(ns.hasParentScore()){

            }
          }

        }


        it.remove();
      }

      System.out.println(s.getAverage());
    }catch(IOException e){
      e.printStackTrace();
    }finally{
      if(in != null){
        try{
          in.close();
        }catch(IOException e){
          e.printStackTrace();
        }
      }
      if(in2 != null){
        try{
          in2.close();
        }catch(IOException e){
          e.printStackTrace();
        }
      }
    }
  }
}
