import java.io.Console;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class Notenspiegel{
  public static void main(String args[]){

      /*System.out.println("Please Enter your Username:");
      String username = System.console().readLine();

      System.out.println("Please Enter your Password:");
      String password = String.copyValueOf(System.console().readPassword());

      Hsmw.login(username, password);*/
      File file;

      try{
        file = new File("notentest.html");
        ArrayList<Score> scores = Hsmw.extractScores((InputStream)new FileInputStream(file));
        for(Score score : scores){
          System.out.println("CourseElement:" + score.getStudienElement());
          System.out.println("Subject:" + score.getSubject());
          System.out.println("Reached Score:" + score.getScore());
          System.out.println("Attempts:" + score.getAttempts());
          System.out.println("=========================================");
        }
      }catch(Exception e){
        e.printStackTrace();
      }
  }
}
