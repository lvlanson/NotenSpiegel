import java.io.Serializable;
import java.util.HashMap;

/**
 * Die Klasse User enthält alle Informationen, die ein User während der Benutzung des Programms anhäuft.
 * Dazu zählen die jeweiligen Durchschnitte, der Name, der Kurs und der möglichen Vertiefung.
 * Es werden auch Daten für den korrekten Datenablauf gespeichert. Das sind der wpfCounter und der testWpfCounter, die jeweils
 * Buch darüber führen, wieviele Wahlpflichtmodule bereits in den Wahlpflichtthemen belegt wurden.
 * @author Thomas Davies
 * @version 1.0
*/
public class User implements Serializable{
  private static final long serialVersionUID = 1;
  private String name;
  private String course;
  private String fieldOfStudy;
  private String credentials;
  private float average;
  private float testAverage;
  private float bestAverage;
  private float worstAverage;
  private HashMap<String, Integer> wpfCounter;
  private HashMap<String, Integer> testWpfCounter;
  /**
   * Der Konstruktor erzeugt einen user mit allen Daten, bis auf wpfCounter und testWpfCounter.
   * @param name Ist der Name.
   * @param course Ist der Kurs.
   * @param fieldOfStudy Ist die Vertiefung.
   * @param average Ist der aktuelle Durchschnitt.
   * @param testAverage Ist der Durchschnitt im Notentester.
   * @param bestAverage Ist der noch bestmöglichste Durchschnitt.
   * @param worstAverage Ist der noch schlechtmöglichste Durchschnitt.
   */
  public User(String name, String course, String fieldOfStudy, float average, float testAverage, float bestAverage, float worstAverage, String credentials){
    this.name = name;
    this.course = course;
    this.fieldOfStudy = fieldOfStudy;
    this.average = average;
    this.testAverage = testAverage;
    this.bestAverage = bestAverage;
    this.worstAverage = worstAverage;
    this.credentials = credentials;
  }
  /**
   * Erhöht den Wahlpflichtcounter um 1.
   * @param wpfTopic Ist das Modul, dessen Wahlpflichtcounter es um 1 zu erhöhen gilt.
   */
  public void increaseWpfCounter(String wpfTopic){
    wpfCounter.put(wpfTopic, wpfCounter.get(wpfTopic)+1);
  }
  /**
   * Reduziert den Wahlpflichtcounter um 1.
   * @param wpfTopic Ist das Modul, dessen Wahlpflichtcounter es um 1 zu reduzieren gilt.
   */
  public void decreaseWpfCounter(String wpfTopic){
    if(wpfCounter.get(wpfTopic) != 0){
      wpfCounter.put(wpfTopic, wpfCounter.get(wpfTopic)-1);
    }
  }
  /**
   * Erhöht den Wahlpflichtcounter der Testmap um 1.
   * @param wpfTopic Ist das Modul, dessen Wahlpflichtcounter der Testmap es um 1 zu erhöhen gilt.
   */
  public void increaseTestWpfCounter(String wpfTopic){
    testWpfCounter.put(wpfTopic, testWpfCounter.get(wpfTopic)+1);
  }
  /**
   * Reduziert den Wahlpflichtcounter der Testmap um 1.
   * @param wpfTopic Ist das Modul, dessen Wahlpflichtcounter der Testmap es um 1 zu erhöhen gilt.
   */
  public void decreaseTestWpfCounter(String wpfTopic){
    if(testWpfCounter.get(wpfTopic) != 0){
      testWpfCounter.put(wpfTopic, testWpfCounter.get(wpfTopic)-1);
    }
  }
  /**
   * Setzt den Wert des Durchschnitts.
   * @param average Ist der Durchschnitt, der gespeichert werden soll.
   */
  public void setAverage(float average){
    this.average = average;
  }
  /**
   * Setzt den Wert des bestmöglichsten Durchschnitts.
   * @param bestAverage Ist der Durchschnitt, der gespeichert werden soll.
   */
  public void setBestAverage(float bestAverage){
    this.bestAverage = bestAverage;
  }
  /**
   * Setzt den Wert des schlechtmöglichsten Durchschnitts.
   * @param worstAverage Ist der Durchschnitt, der gespeichert werden soll.
   */
  public void setWorstAverage(float worstAverage){
    this.worstAverage = worstAverage;
  }
  /**
   * Setzt den Wert des Durchschnitts des Notentesters.
   * @param testAverage Ist der Durchschnitt, der gespeichert werden soll.
   */
  public void setTestAverage(float testAverage){
    this.testAverage = testAverage;
  }
  /**
   * Setzt den Wert des wpfCounters. Dieser hält Überblick über die Anzahl der Module von gewählten Wahlfplichtbereichen.
   * @param wpfCounter Setzt den übergebenen wpfCounter im User Objekt.
   */
  public void setWpfCounter(HashMap<String, Integer> wpfCounter){
    this.wpfCounter = wpfCounter;
  }
  /**
   * Setzt den Wert des testWpfCounters für den Notentester. Dieser hält Überblick über die Anzahl der Module von gewählten Wahlfplichtbereichen.
   * @param wpfCounter Setzt den übergebenen testWpfCounter im User Objekt.
   */
  public void setTestWpfCounter(HashMap<String, Integer> testWpfCounter){
    this.testWpfCounter = testWpfCounter;
  }
  /**
   * Erzeugt einen neuen testWpfCounter. Dieser hält Überblick über die Anzahl der Module von gewählten Wahlfplichtbereichen. Ist allerdings komplett zurückgesetzt, wenn diese Methode aufgerufen wird.
   */
  public void createTestWpfCounter(){
    this.testWpfCounter = new HashMap<String, Integer>();
  }
  /**
   * Hinterlegt den Loginnamen des Users, damit überprüft werden kann, ob diesem die Daten aus dem Notentester entsprechen.
   * @param credentials Ist der Hochschul-Login Name.
   */
  public void setCredentials(String credentials){
    this.credentials = credentials;
  }
  /**
   * Gibt den Hochschul-Login Name zurück.
   * @return Gibt Hochschul-Login Name zurück.
   */
  public String getCredentials(){
    return this.credentials;
  }
  /**
   * Gibt Namen des Users zurück.
   * @return Gibt name zurück.
   */
  public String getName(){
    return this.name;
  }
  /**
   * Gibt den Kurs des Users zurück.
   * @return Gibt course zurück.
   */
  public String getCourse(){
    return this.course;
  }
  /**
   * Gibt den Vertiefung des Users zurück.
   * @return Gibt fieldOfStudy zurück.
   */
  public String getFieldOfStudy(){
    return this.fieldOfStudy;
  }
  /**
   * Gibt den Durchschnitt des Users zurück.
   * @return Gibt average zurück.
   */
  public float getAverage(){
    return this.average;
  }
  /**
   * Gibt den Durchschnitt des Notentesters des Users zurück.
   * @return Gibt testAverage zurück.
   */
  public float getTestAverage(){
    return this.testAverage;
  }
  /**
   * Gibt die Map mit dem Zähler für die Wahlpflichtbereiche des Users zurück.
   * @return Gibt wpfCounter zurück.
   */
  public HashMap<String, Integer> getWpfCounter(){
    return this.wpfCounter;
  }
  /**
   * Gibt die Map mit dem Zähler für die Wahlpflichtbereiche des Notentests für den User zurück.
   * @return Gibt testWpfCounter zurück.
   */
  public HashMap<String, Integer> getTestWpfCounter(){
    return this.testWpfCounter;
  }
  /**
   * Gibt den bestmöglichsten Durchschnitt des Users zurück.
   * @return Gibt bestAverage zurück.
   */
  public float getBestAverage(){
    return bestAverage;
  }
  /**
   * Gibt den schlechtmöglichsten Durchschnitt des Users zurück.
   * @return Gibt worstAverage zurück.
   */
  public float getWorstAverage(){
    return worstAverage;
  }
}
