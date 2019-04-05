import java.io.Console;

public class Notenspiegel{
  public static void main(String args[]){

      System.out.println("Please Enter your Username:");
      String username = System.console().readLine();

      System.out.println("Please Enter your Password:");
      String password = String.copyValueOf(System.console().readPassword());

      Hsmw.login(username, password);
  }
}
