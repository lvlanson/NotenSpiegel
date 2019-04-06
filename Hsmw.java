import java.util.HashMap;
import java.io.*;
import java.net.*;
//Diese Klasse verwaltet alle Operationen mit der HSMW Webseite
public class Hsmw{
  //Die Methode getMarks() gibt die Noten in Form einer Hashmap zurück.
  //HashMap<String Fach, Double Note>
  private static URL URLObj;
  private static URLConnection con;

  public static HashMap<String, Double> getMarks(){
    HashMap<String, Double> noten = new HashMap<String, Double>();

    return noten;
  }
  //private static void login(String username, String password){
  public static void login(String username, String password){


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
    }catch(Exception ex){
      ex.printStackTrace();
    }



  }
  private static void logout(){

  }
}
