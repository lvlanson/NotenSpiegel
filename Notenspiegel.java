public class Notenspiegel{
  public static void main(String args[]){
    Hsmw.writeScoresToFile();
    Visual vis = new Visual();
    vis.run();
    vis.close();
  }
}
