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
  private static String testPath = folderPath + "testfile.dat";
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
  public static User getUser(){
    File file = new File(userPath);
    User user = null;
    if(file.exists()){
      ObjectInputStream ois = null;
      try{
        ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(userPath)));
        user = (User) ois.readObject();
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
    return user;
  }
  public static boolean testfileExists(){
    File file = null;
    boolean exists = false;
      file = new File(testPath);
      if(file.exists()){
        exists = true;
      }
    return exists;
  }
  public static void createTestfile(){
    File testFile = null;
    HashMap<String, Score> syllabusMap = null;
    ObjectOutputStream oos = null;
    try{
      testFile = new File(testPath);
      syllabusMap = getSyllabus();
      oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(testFile)));
      oos.writeObject(syllabusMap);
    }catch(IOException e){
      e.printStackTrace();
    }finally{
      try{
        if(oos != null){
          oos.close();
        }
      }catch(IOException e){



        e.printStackTrace();
      }
    }
  }
  public static HashMap<String,Score> getTestMap(){
    HashMap<String, Score> testMap = null;
    ObjectInputStream ois = null;
    try{
        ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(testPath))));
        testMap = (HashMap<String, Score>) ois.readObject();
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
    return testMap;
  }
  public static void updateTestMap(HashMap<String,Score> testMap){
    File testFile = null;
    ObjectOutputStream oos = null;
    try{
      testFile = new File(testPath);
      oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(testFile)));
      oos.writeObject(testMap);
    }catch(IOException e){
      e.printStackTrace();
    }finally{
      try{
        if(oos != null){
          oos.close();
        }
      }catch(IOException e){
        e.printStackTrace();
      }
    }
  }
  public static void updateTestAverage(float score){
    User user = getUser();
    user.setTestAverage(score);
    writeUser(user);
  }
}
