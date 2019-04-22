import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.HashMap;

public class DataHandler{
  private static String path         = System.getProperty("user.dir")
                                       + File.separator;
  private static String folderPath   = path + "data" + File.separator;
  private static String syllabusPath = folderPath + "syllabus.dat";
  private static String userPath = folderPath + "user.dat";
  private static void setupDirectory(){
    File folder = null;
    folder = new File(folderPath);
    if(!folder.exists()){
      folder.mkdirs();
    }
  }
  public static void run(){
    setupDirectory();
  }
  public static void writeSyllabus(HashMap<String, Score> syllabusMap){
    File syllabusFile = null;
    ObjectOutputStream oos = null;
    try{
      syllabusFile = new File(syllabusPath);
      oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(syllabusFile)));
      oos.writeObject(syllabusMap);
    }catch(IOException e){
      e.printStackTrace();
    }finally{
      if(oos != null){
        try{
          oos.close();
        }catch(IOException e){
          e.printStackTrace();
        }
      }
    }
  }
  public static void writeUser(User user){
    File userFile = null;
    ObjectOutputStream oos = null;
    try{
      userFile = new File(userPath);
      oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(userFile)));
      oos.writeObject(user);
    }catch(IOException e){
      e.printStackTrace();
    }finally{
      if(oos != null){
        try{
          oos.close();
        }catch(IOException e){
          e.printStackTrace();
        }
      }
    }
  }
  public static String getSurname(){
    File file = new File(userPath);
    String surname = "";
    if(file.exists()){
      ObjectInputStream ois = null;
      try{
        ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(userPath)));
        User user = (User) ois.readObject();
        String name = user.getName();
        surname = name.substring(name.indexOf(',')+2);
      }catch(IOException e){
        e.printStackTrace();
      }catch(ClassNotFoundException e){
        e.printStackTrace();
      }finally{
        if(ois != null){
          try{
            ois.close();
          }catch(IOException e){
            e.printStackTrace();
          }
        }
      }
    }
    return surname;
  }

  public static HashMap<String, Score> getSyllabus(){
    HashMap<String, Score> syllabusMap = null;
    ObjectInputStream ois = null;
    try{
        ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(syllabusPath))));
        syllabusMap = (HashMap<String, Score>) ois.readObject();
    }catch(IOException e){
      e.printStackTrace();
    }catch(ClassNotFoundException e){
      e.printStackTrace();
    }finally{
      if(ois != null){
        try{
          ois.close();
        }catch(IOException e){
          e.printStackTrace();
        }
      }
    }
    return syllabusMap;
  }
}
