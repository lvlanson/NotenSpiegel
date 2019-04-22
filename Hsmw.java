import java.util.ArrayList;
import java.io.*;
import java.net.*;
//Diese Klasse verwaltet alle Operationen mit der HSMW Webseite
public class Hsmw{
  private static URL URLObj;
  private static URLConnection con;


  //private static void login(String username, String password){
  public static void login(){

    /*
     * Muss gegen Login System in der KonsolenGUI getauscht werden
    */
    System.out.println("Please Enter your Username:");
    String username = System.console().readLine();

    System.out.println("Please Enter your Password:");
    String password = String.copyValueOf(System.console().readPassword());

    try{
      URLObj = new URL("https://start.hs-mittweida.de");
      con = URLObj.openConnection();
      con.setDoOutput(true);


    }catch(MalformedURLException ex){
      System.out.println("Es gibt Probleme mit dem Aufruf der Loginseite der Hochschule");
      System.out.println(ex.getMessage());
      System.exit(1);
    }catch(IOException ex){
      System.out.println("Es gibt Probleme mit der Internetverbindung");
      System.out.println(ex.getMessage());
      System.exit(1);
    }
    File f = null;

    try{

      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
      writer.write("j_username="+username+"&j_password="+password+"submit=&_eventId_proceed");
      writer.close();
      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

      f = new File("html.txt");
      FileWriter fWriter = new FileWriter(f);
      String lineRead = "";
      while((lineRead = reader.readLine())!= null){
        fWriter.write(lineRead);
      }
      reader.close();
      fWriter.close();
    }catch(Exception ex){
      ex.printStackTrace();
    }



  }
  private static void logout(){

  }

/*  public static void writeScoresToFile(){
    ObjectOutputStream oos = null;
    try{
      //
      //
      //
      ArrayList<Score> scores = extractScores(new FileInputStream(new File("notentest.html")));
      //
      //
      //
      oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(data)));
      oos.writeObject(scores);
      oos.flush();
    }catch(FileNotFoundException e){
      e.printStackTrace();
    }catch(IOException e){
      e.printStackTrace();
    }finally{
      try{
        oos.close();
      }catch(Exception e){
        e.printStackTrace();
      }
    }
  }
  public static String getDataPath(){
    return data.getPath();
  }*/
}
