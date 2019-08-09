import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.HashMap;

/**
 * Diese Klasse übernimmt alle Ein- und Ausgabeoperationen. Alle Methoden und Attribute sind static und über die Klasse aufrufbar.
 * @author Thomas Davies
 * @version 1.0
*/
public class DataHandler{
  private static String path         = System.getProperty("user.dir")
                                       + File.separator;
  private static String folderPath   = path + "data" + File.separator;
  private static String syllabusPath = folderPath + "syllabus.dat";
  private static String userPath = folderPath + "user.dat";
  private static String testPath = folderPath + "testfile.dat";
  /**
   * Diese Methode erstellt einen "Data" Ordner, sofern noch keiner vorhanden ist.
   */
  private static void setupDirectory(){
    File folder = null;
    folder = new File(folderPath);
    if(!folder.exists()){
      folder.mkdirs();
    }
  }
  /**
   * Diese Methode ruft die setupDirectory Methode auf.
   */
  public static void run(){
    setupDirectory();
  }
  /**
   * Diese Methode schreibt die Map mit allen Modulen(Scores), als ein Objekt in eine Datei in den Data Ordner.
   * @param syllabusMap Ist die Map, die es in eine Datei zu schreiben gilt.
   */
  public static void writeSyllabus(HashMap<String, Score> syllabusMap){
    File syllabusFile = null;
    ObjectOutputStream oos = null;
    try{
      syllabusFile = new File(syllabusPath);
      oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(syllabusFile)));
      oos.writeObject(syllabusMap);
    }catch(IOException e){
      PrintWriter writer = null;
      try{
        writer = new PrintWriter("error.txt");
        writer.write(e.toString());
        e.printStackTrace(writer);
        writer.close();
      }catch(IOException ex){}
    }finally{
      if(oos != null){
        try{
          oos.close();
        }catch(IOException e){
          PrintWriter writer = null;
          try{
            writer = new PrintWriter("error.txt");
            writer.write(e.toString());
            e.printStackTrace(writer);
            writer.close();
          }catch(IOException ex){}
        }
      }
    }
  }
  /**
   * Diese Methode schreibt das User Objekt in eine Datei in den Data Ordner.
   * @param user Enthält das User Objekt mit all seinen User-Daten.
   */
  public static void writeUser(User user){
    File userFile = null;
    ObjectOutputStream oos = null;
    try{
      userFile = new File(userPath);
      oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(userFile)));
      oos.writeObject(user);
    }catch(IOException e){
      PrintWriter writer = null;
      try{
        writer = new PrintWriter("error.txt");
        writer.write(e.toString());
        e.printStackTrace(writer);
        writer.close();
      }catch(IOException ex){}
    }finally{
      if(oos != null){
        try{
          oos.close();
        }catch(IOException e){
          PrintWriter writer = null;
          try{
            writer = new PrintWriter("error.txt");
            writer.write(e.toString());
            e.printStackTrace(writer);
            writer.close();
          }catch(IOException ex){}
        }
      }
    }
  }
  /**
   * Diese Methode holt den Vornamen aus dem User Objekt aus "user.dat".
   * @return Gibt den Vornamen als String zurück.
   */
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
        PrintWriter writer = null;
        try{
          writer = new PrintWriter("error.txt");
          writer.write(e.toString());
          e.printStackTrace(writer);
          writer.close();
        }catch(IOException ex){}
      }catch(ClassNotFoundException e){
        PrintWriter writer = null;
        try{
          writer = new PrintWriter("error.txt");
          writer.write(e.toString());
          e.printStackTrace(writer);
          writer.close();
        }catch(IOException ex){}
      }finally{
        if(ois != null){
          try{
            ois.close();
          }catch(IOException e){
            PrintWriter writer = null;
            try{
              writer = new PrintWriter("error.txt");
              writer.write(e.toString());
              e.printStackTrace(writer);
              writer.close();
            }catch(IOException ex){}
          }
        }
      }
    }
    return surname;
  }
  /**
   * Diese Methode holt die Map mit all den Modulen(Scores) aus der Datei "syllabus.dat".
   * @return Gibt die Map als HashMap zurück, wobei als Key das Studienelement in Form eines Strings ist, und als Value das Modul(Score).
   */
  public static HashMap<String, Score> getSyllabus(){
    HashMap<String, Score> syllabusMap = null;
    ObjectInputStream ois = null;
    try{
        ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(syllabusPath))));
        @SuppressWarnings("unchecked")
        HashMap<String, Score> read = (HashMap<String, Score>) ois.readObject();
        syllabusMap = read;
    }catch(IOException e){
      PrintWriter writer = null;
      try{
        writer = new PrintWriter("error.txt");
        writer.write(e.toString());
        e.printStackTrace(writer);
        writer.close();
      }catch(IOException ex){}
    }catch(ClassNotFoundException e){
      PrintWriter writer = null;
      try{
        writer = new PrintWriter("error.txt");
        writer.write(e.toString());
        e.printStackTrace(writer);
        writer.close();
      }catch(IOException ex){}
    }finally{
      if(ois != null){
        try{
          ois.close();
        }catch(IOException e){
          PrintWriter writer = null;
          try{
            writer = new PrintWriter("error.txt");
            writer.write(e.toString());
            e.printStackTrace(writer);
            writer.close();
          }catch(IOException ex){}
        }
      }
    }
    return syllabusMap;
  }
  /**
   * Diese Methode holt den User als User Objekt aus der Datei "syllabus.dat".
   * @return Gibt den User aus der User Datei zurück.
   */
  public static User getUser(){
    File file = new File(userPath);
    User user = null;
    if(file.exists()){
      ObjectInputStream ois = null;
      try{
        ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
        user = (User) ois.readObject();
      }catch(IOException e){
        PrintWriter writer = null;
        try{
          writer = new PrintWriter("error.txt");
          writer.write(e.toString());
          e.printStackTrace(writer);
          writer.close();
        }catch(IOException ex){}
      }catch(ClassNotFoundException e){
        PrintWriter writer = null;
        try{
          writer = new PrintWriter("error.txt");
          writer.write(e.toString());
          e.printStackTrace(writer);
          writer.close();
        }catch(IOException ex){}
      }finally{
        if(ois != null){
          try{
            ois.close();
          }catch(IOException e){
            PrintWriter writer = null;
            try{
              writer = new PrintWriter("error.txt");
              writer.write(e.toString());
              e.printStackTrace(writer);
              writer.close();
            }catch(IOException ex){}
          }
        }
      }
    }
    return user;
  }
  /**
   * Diese Methode überprüft ob schon eine Datei existiert, in der die Testdateien des Notentesters existieren.
   * @return Gibt wahr zurück, wenn die Testdatei existiert.
   */
  public static boolean testfileExists(){
    File file = null;
    boolean exists = false;
      file = new File(testPath);
      if(file.exists()){
        exists = true;
      }
    return exists;
  }
  /**
   * Diese Methode überprüft ob schon eine User Datei existiert.
   * @return Gibt wahr zurück, wenn die Userdatei existiert.
   */
  public static boolean userfileExists(){
    File file = null;
    boolean exists = false;
      file = new File(userPath);
      if(file.exists()){
        exists = true;
      }
    return exists;
  }
  /**
   * Diese Methode ob die Modulplan Datei existiert.
   * @return Gibt wahr zurück, wenn die Moduplandatei existiert.
   */
  public static boolean mainfileExists(){
    File file = null;
    boolean exists = false;
      file = new File(syllabusPath);
      if(file.exists()){
        exists = true;
      }
    return exists;
  }
  /**
   * Diese Methode erzeugt eine Testdatei, in der die Noten des Notentesters gespeichert werden.
   */
  public static void createTestfile(){
    File testFile = null;
    HashMap<String, Score> syllabusMap = null;
    ObjectOutputStream oos = null;
    try{
      testFile = new File(testPath);
      syllabusMap = getSyllabus();
      oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(testFile)));
      oos.writeObject(syllabusMap);
      User user = getUser();
      user.setTestAverage(user.getAverage());
      writeUser(user);
    }catch(IOException e){
      PrintWriter writer = null;
      try{
        writer = new PrintWriter("error.txt");
        writer.write(e.toString());
        e.printStackTrace(writer);
        writer.close();
      }catch(IOException ex){}
    }finally{
      try{
        if(oos != null){
          oos.close();
        }
      }catch(IOException e){
        PrintWriter writer = null;
        try{
          writer = new PrintWriter("error.txt");
          writer.write(e.toString());
          e.printStackTrace(writer);
          writer.close();
        }catch(IOException ex){}
      }
      User user = getUser();
      user.setTestAverage(user.getAverage());
    }
  }
  /**
   * Diese Methode liest die Map mit den Noten aus dem Notentester aus.
   * @return Gibt die Map des Notentesters als HashMap zurück, wobei als Key das Studienelement in Form eines Strings ist, und als Value das Modul(Score).
   */
  public static HashMap<String,Score> getTestMap(){
    HashMap<String, Score> testMap = null;
    ObjectInputStream ois = null;
    try{
        ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(testPath))));
        @SuppressWarnings("unchecked")
        HashMap<String, Score> read =(HashMap<String, Score>) ois.readObject();
        testMap = read;
    }catch(IOException e){
      PrintWriter writer = null;
      try{
        writer = new PrintWriter("error.txt");
        writer.write(e.toString());
        e.printStackTrace(writer);
        writer.close();
      }catch(IOException ex){}
    }catch(ClassNotFoundException e){
      PrintWriter writer = null;
      try{
        writer = new PrintWriter("error.txt");
        writer.write(e.toString());
        e.printStackTrace(writer);
        writer.close();
      }catch(IOException ex){}
    }finally{
      if(ois != null){
        try{
          ois.close();
        }catch(IOException e){
          PrintWriter writer = null;
          try{
            writer = new PrintWriter("error.txt");
            writer.write(e.toString());
            e.printStackTrace(writer);
            writer.close();
          }catch(IOException ex){}
        }
      }
    }
    return testMap;
  }
  /**
   * Diese Methode updated die Testmap mit den aktuellen Veränderungen aus dem Notentester.
   * @param testMap Enthält alle Noten des Notentesters, wobei als Key das Studienelement in Form eines Strings ist, und als Value das Modul(Score).
   */
  public static void updateTestMap(HashMap<String,Score> testMap){
    File testFile = null;
    ObjectOutputStream oos = null;
    try{
      testFile = new File(testPath);
      oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(testFile)));
      oos.writeObject(testMap);
    }catch(IOException e){
      PrintWriter writer = null;
      try{
        writer = new PrintWriter("error.txt");
        writer.write(e.toString());
        e.printStackTrace(writer);
        writer.close();
      }catch(IOException ex){}
    }finally{
      try{
        if(oos != null){
          oos.close();
        }
      }catch(IOException e){
        PrintWriter writer = null;
        try{
          writer = new PrintWriter("error.txt");
          writer.write(e.toString());
          e.printStackTrace(writer);
          writer.close();
        }catch(IOException ex){}
      }
    }
  }

  /**
   * Updated den Durchschnitt des Users
   * @param score Ist der Durchschnitt, der in das Userobjekt geschrieben wird.
   */
  public static void updateTestAverage(float score){
    User user = getUser();
    user.setTestAverage(score);
    writeUser(user);
  }

  /**
   * Erzeugt einen Wahlpflicht Zähler, der Überblick über die maximale Anzahl der Wahlpflichtfächer gibt.
   */
  public static void createTestWpfCounter(){
    File file = new File(userPath);
    User user = null;
    if(!file.exists()){
      user = getUser();
      user.setTestWpfCounter(user.getWpfCounter());
      writeUser(user);
    }
  }
  /**
   * Löscht die Testdatei des Notentesters. Diese Funktion dient dazu um den Notentester zurückzusetzen.
   */
  public static void removeTestFile(){
    File file = new File(testPath);
    file.delete();
  }
}
