import java.io.Serializable;
import java.util.HashMap;

public class Score implements Serializable{
  private static final long serialVersionUID = 1L;
  private String studienElement;
  private String subject;
  private float score;
  private int attempts;
  private int[] weight;
  private HashMap<String, Score> subScore;
  private boolean hasSubScore = false;
  private boolean isWpf = false;
  private String wpfTopic;
  private int[] wpfWeight;
  private int semester;
  private boolean isTested = false;
  private String parentStudienElement;
  private boolean hasParentScore = false;

  /**
   * Die Klasse Score enthält alle Informationen, die für ein Modul aufgenommen werden können.
   * @author Thomas Davies
   * @version 1.0
  */

  /**
   * Dieser Score Konstruktor erzeugt ein leeres Score Objekt.
   */
  public Score(){

  }

  /**
   * Der Score Konstruktor erzeugt ein neues Score Objekt gesetzt mit den Parametern, die ihm übergeben werden.
   * @param studEl Das Studienelement ist ein Code in der Form eines Strings, der jedem Modul eine unverwechselbare Bezeichnung gibt.
   * @param subject Ist der ausgeschriebene Name des Moduls.
   * @param semester Ist das Fachsemester, das für dieses Modul vorgesehen ist.
   * @param weight Ist die Gewichtung des Moduls. Es wird als Bruch angegeben. Weight[0] ist der Zähler und Weight[1] der Nenner.
   * @param score Ist die Note, die in diesem Modul erreicht wurde. Wenn die Note 0.0f gesetzt ist, heißt es, dass bisher keine Note darin erreicht wurde.
   * @param attempt Gibt die Anzahl der Versuche für das Semester an.
   */
  public Score(String studEl, String subject,int semester, int[] weight, float score, int attempt){
    this.studienElement = studEl;
    this.subject = subject;
    this.weight = new int[2];
    this.weight[0] = weight[0];
    this.weight[1] = weight[1];
    this.semester = semester;
    this.score = score;
    this.attempts = attempt;
  }

  /**
   * Der Score Konstruktor erzeugt ein neues Score Objekt gesetzt mit den Parametern, die ihm übergeben werden. Dieses Scoreobjekt ist typischerweise ein Untermodul(Subscore).
   * @param studEl Das Studienelement ist ein Code in der Form eines Strings, der jedem Modul eine unverwechselbare Bezeichnung gibt.
   * @param subject Ist der ausgeschriebene Name des Moduls.
   * @param semester Ist das Fachsemester, das für dieses Modul vorgesehen ist.
   * @param weight Ist die Gewichtung des Moduls. Es wird als Bruch angegeben. Weight[0] ist der Zähler und Weight[1] der Nenner.
   * @param parentStudienElement Gibt das Übermodul an in der Codierung der Studienelemente.
   * @param score Ist die Note, die in diesem Modul erreicht wurde. Wenn die Note 0.0f gesetzt ist, heißt es, dass bisher keine Note darin erreicht wurde.
   * @param attempt Gibt die Anzahl der Versuche für das Semester an.
   */
  public Score(String studEl, String subject,int semester, int[] weight, String parentStudienElement, float score, int attempt){
    this.studienElement = studEl;
    this.subject = subject;
    this.weight = new int[2];
    this.weight[0] = weight[0];
    this.weight[1] = weight[1];
    this.semester = semester;
    this.parentStudienElement = parentStudienElement;
    this.hasParentScore = true;
    this.score = score;
    this.attempts = attempt;
  }

  /**
   * Der Score Konstruktor erzeugt ein neues Score Objekt gesetzt mit den Parametern, die ihm übergeben werden. Dieses Scoreobjekt ist typischerweise ein Wahlpflichtmodul.
   * @param studEl Das Studienelement ist ein Code in der Form eines Strings, der jedem Modul eine unverwechselbare Bezeichnung gibt.
   * @param subject Ist der ausgeschriebene Name des Moduls.
   * @param semester Ist das Fachsemester, das für dieses Modul vorgesehen ist.
   * @param weight Ist die Gewichtung des Moduls. Es wird als Bruch angegeben. Weight[0] ist der Zähler und Weight[1] der Nenner.
   * @param isWpf Gibt an ob das Modul ein Wahlpflichtmodul ist. True bedeutet es handelt sich um ein Wahlpflichtmodul.
   * @param wpfWeight Gibt die Gewichtung des Wahlpflichtmoduls im Bereich des Wahlpflichtthemas an. wpfWeight[0] entspricht der Gewichtung
   *                  des Moduls und wpfWeight[1] entspricht der erwarteten Anzahl, die das Wahlpflichtthema benötigt um es abzuschließen.
   * @param wpfTopic Ist das Wahlpflichtthema des Wahlpflichtmoduls.
   * @param score Ist die Note, die in diesem Modul erreicht wurde. Wenn die Note 0.0f gesetzt ist, heißt es, dass bisher keine Note darin erreicht wurde.
   * @param attempt Gibt die Anzahl der Versuche für das Semester an.
   */
  public Score(String studEl, String subject, int semester, int[] weight, boolean isWpf, int[] wpfWeight, String wpfTopic, float score){
    this.studienElement = studEl;
    this.subject = subject;
    this.weight = new int[2];
    this.weight[0] = weight[0];
    this.weight[1] = weight[1];
    this.isWpf = isWpf;
    this.wpfWeight = new int[2];
    this.wpfWeight[0] = wpfWeight[0];
    this.wpfWeight[1] = wpfWeight[1];
    this.wpfTopic = wpfTopic;
    this.semester = semester;
    this.score = score;
  }

  /**
   * Der Score Konstruktor erzeugt ein neues Score Objekt gesetzt mit den Parametern, die ihm übergeben werden. Dieses Scoreobjekt ist typischerweise ein Wahlpflichtmodul und ein Untermodul.
   * @param studEl Das Studienelement ist ein Code in der Form eines Strings, der jedem Modul eine unverwechselbare Bezeichnung gibt.
   * @param subject Ist der ausgeschriebene Name des Moduls.
   * @param semester Ist das Fachsemester, das für dieses Modul vorgesehen ist.
   * @param weight Ist die Gewichtung des Moduls. Es wird als Bruch angegeben. Weight[0] ist der Zähler und Weight[1] der Nenner.
   * @param isWpf Gibt an ob das Modul ein Wahlpflichtmodul ist. True bedeutet es handelt sich um ein Wahlpflichtmodul.
   * @param wpfWeight Gibt die Gewichtung des Wahlpflichtmoduls im Bereich des Wahlpflichtthemas an. wpfWeight[0] entspricht der Gewichtung
   *                  des Moduls und wpfWeight[1] entspricht der erwarteten Anzahl, die das Wahlpflichtthema benötigt um es abzuschließen.
   * @param wpfTopic Ist das Wahlpflichtthema des Wahlpflichtmoduls.
   * @param parentStudienElement Gibt das Übermodul an in der Codierung der Studienelemente.
   * @param score Ist die Note, die in diesem Modul erreicht wurde. Wenn die Note 0.0f gesetzt ist, heißt es, dass bisher keine Note darin erreicht wurde.
   * @param attempt Gibt die Anzahl der Versuche für das Semester an.
   */
  public Score(String studEl, String subject, int semester, int[] weight, boolean isWpf, int[] wpfWeight, String wpfTopic, String parentStudienElement, float score){
    this.studienElement = studEl;
    this.subject = subject;
    this.weight = new int[2];
    this.weight[0] = weight[0];
    this.weight[1] = weight[1];
    this.isWpf = isWpf;
    this.wpfWeight = new int[2];
    this.wpfWeight[0] = wpfWeight[0];
    this.wpfWeight[1] = wpfWeight[1];
    this.wpfTopic = wpfTopic;
    this.semester = semester;
    this.parentStudienElement = parentStudienElement;
    this.hasParentScore = true;
    this.score = score;
  }
  /**
   * @return Gibt das Studienelement zurück.
   */
  public String getStudienElement(){
    return this.studienElement;
  }
  /**
   * @return Gibt den Namen des Moduls zurück.
   */
  public String getSubject(){
    return this.subject;
  }
  /**
   * @return Gibt die erreichte Note zurück. Ist 0.0 falls keine Note erreicht wurde
   */
  public float getScore(){
    return this.score;
  }
  /**
   * @return Gibt die Anzahl der Versuche zurück.
   */
  public int getAttempts(){
    return this.attempts;
  }
  /**
   * @return Gibt die Gewichtung des Moduls zurück. weight[0] entspricht der Gewichtung des Moduls und weight[1] die Gesamtgewichtung.
   */
  public int[] getWeight(){
    return this.weight;
  }
  /**
   * @return Gibt die Untermodule eines Moduls in Form einer HashMap zurück zurück.
   */
  public HashMap<String, Score> getSubScore(){
    return this.subScore;
  }
  /**
   * @return Gibt zurück, ob ein Modul ein Untermodul besitzt. Wenn es eins besitzt, gibt die Methode true zurück.
   */
  public boolean hasSubScore(){
    return this.hasSubScore;
  }
  /**
   * @return Gibt zurück, ob ein Modul ein Wahlfplichtmodul ist. Wenn es eins ist, gibt die Methode true zurück.
   */
  public boolean isWpf(){
    return this.isWpf;
  }
  /**
   * @return Gibt das Wahlpflichtthema zurück.
   */
  public String getWpfTopic(){
    return this.wpfTopic;
  }
  /**
   * @return Gibt die Gewichtung des Wahlpflichtthemas zurück. wpfWeight[0] entspricht der Gewichtung des Moduls und wpfWeight[1] die Gesamtgewichtung.
   */
  public int[] getWpfWeight(){
    return this.wpfWeight;
  }
  /**
   * @return Gibt das vorgesehene Semester des Moduls zurück.
   */
  public int getSemester(){
    return this.semester;
  }
  /**
   * @return Gibt zurück ob ein Modul im Notentester gestestet wurde.
   */
  public boolean isTested(){
    return this.isTested;
  }
  /**
   * @return Gibt zurück, ob ein Modul ein Übermodul hat. Gibt true zurück, wenn das der Fall ist.
   */
  public boolean hasParentScore(){
    return this.hasParentScore;
  }
  /**
   * @return Gibt das Studienelemnt des Übermoduls zurück.
   */
  public String getParentStudienElement(){
    return this.parentStudienElement;
  }
  /**
   * @param el Setzt im Objekt den Parameter studienElement zum übergebenen Parameter.
   */
  public void setStudienElement(String el){
    this.studienElement = el;
  }
  /**
   * @param subject Setzt im Objekt den Parameter subject zum übergebenen Parameter.
   */
  public void setSubject(String subject){
    this.subject = subject;
  }
  /**
   * @param score Setzt im Objekt den Parameter score zum übergebenen Parameter.
   */
  public void setScore(float score){
    if(this.isWpf){

    }
    this.score = score;
  }
  /**
   * @param attempts Setzt im Objekt den Parameter attempts zum übergebenen Parameter.
   */
  public void setAttempts(int attempts){
    this.attempts = attempts;
  }
  /**
   * Kennzeichnet das Objekt als Untermodul.
   */
  public void setSubScore(){
    this.hasSubScore = true;
  }
  /**
   * Kennzeichnet das Modul als Wahlpflichtmodul
   */
  public void setWpf(){
    this.isWpf = true;
  }
  /**
   * setWeight setzt die Gewichtung des Moduls.
   * @param denominaor Setzt im Objekt den Parameter weight[0] zum übergebenen Parameter.
   * @param numerator Setzt im Objekt den Parameter weight[1] zum übergebenen Parameter.
   */
  public void setWeight(int denominator, int numerator){
    this.weight = new int[2];
    this.weight[0] = denominator;
    this.weight[1] = numerator;
  }
  /**
   * Diese Methode setzt ein Untermodul in dem aufgerufenen Objekt.
   * @param studEl Das Studienelement ist ein Code in der Form eines Strings, der jedem Modul eine unverwechselbare Bezeichnung gibt.
   * @param subject Ist der ausgeschriebene Name des Moduls.
   * @param semester Ist das Fachsemester, das für dieses Modul vorgesehen ist.
   * @param weight Ist die Gewichtung des Moduls. Es wird als Bruch angegeben. Weight[0] ist der Zähler und Weight[1] der Nenner.
   * @param parentStudienElement Gibt das Übermodul an in der Codierung der Studienelemente.
   * @param score Ist die Note, die in diesem Modul erreicht wurde. Wenn die Note 0.0f gesetzt ist, heißt es, dass bisher keine Note darin erreicht wurde.
   * @param attempt Gibt die Anzahl der Versuche für das Semester an.
   */
  public void setSubScore(String studEl, String subject,int semester, int[] weight, String parentStudienElement, float score, int attempt){
    if(!hasSubScore){
      this.subScore = new HashMap<String, Score>();
      this.subScore.put(studEl, new Score(studEl, subject, semester, weight, parentStudienElement, score, attempt));
      this.setSubScore();
    }else{
      this.subScore.put(studEl, new Score(studEl, subject, semester, weight, parentStudienElement, score, attempt));
    }
  }
  /**
   * Diese Methode setzt ein Untermodul in dem aufgerufenen Objekt. Hierbei handelt es sich um ein Wahlpflichtmodul
   * @param studEl Das Studienelement ist ein Code in der Form eines Strings, der jedem Modul eine unverwechselbare Bezeichnung gibt.
   * @param subject Ist der ausgeschriebene Name des Moduls.
   * @param semester Ist das Fachsemester, das für dieses Modul vorgesehen ist.
   * @param weight Ist die Gewichtung des Moduls. Es wird als Bruch angegeben. Weight[0] ist der Zähler und Weight[1] der Nenner.
   * @param isWpf Gibt an ob das Modul ein Wahlpflichtmodul ist. True bedeutet es handelt sich um ein Wahlpflichtmodul.
   * @param wpfWeight Gibt die Gewichtung des Wahlpflichtmoduls im Bereich des Wahlpflichtthemas an. wpfWeight[0] entspricht der Gewichtung
   *                  des Moduls und wpfWeight[1] entspricht der erwarteten Anzahl, die das Wahlpflichtthema benötigt um es abzuschließen.
   * @param wpfTopic Ist das Wahlpflichtthema des Wahlpflichtmoduls.
   * @param parentStudienElement Gibt das Übermodul an in der Codierung der Studienelemente.
   * @param score Ist die Note, die in diesem Modul erreicht wurde. Wenn die Note 0.0f gesetzt ist, heißt es, dass bisher keine Note darin erreicht wurde.
   */
  public void setSubScore(String studEl, String subject, int semester, int[] weight, boolean isWpf, int[] wpfWeight, String wpfTopic, String parentStudienElement, float score){
    if(!hasSubScore){
      this.subScore = new HashMap<String, Score>();
      this.subScore.put(studEl, new Score(studEl, subject, semester, weight, isWpf, wpfWeight, wpfTopic, parentStudienElement, score));
      this.setSubScore();
    }else{
      this.subScore.put(studEl, new Score(studEl, subject, semester, weight, isWpf, wpfWeight, wpfTopic, parentStudienElement, score));
    }
  }
  /**
   * @param wpfTopic Setzt im Objekt den Parameter wpfTopic zum übergebenen Parameter
   */
  public void setWpfTopic(String wpfTopic){
    this.wpfTopic = wpfTopic;
  }
  /**
   * setWpfWeight setzt die Gewichtung des Moduls.
   * @param denominaor Setzt im Objekt den Parameter wpfWeight[0] zum übergebenen Parameter.
   * @param numerator Setzt im Objekt den Parameter wpfWeight[1] zum übergebenen Parameter.
   */
  public void setWpfWeight(int denominator, int numerator){
    this.wpfWeight = new int[2];
    this.wpfWeight[0] = denominator;
    this.wpfWeight[1] = numerator;
  }
  /**
   * @param semester Setzt im Objekt den Parameter semester zum übergebenen Parameter
   */
  public void setSemester(int semester){
    this.semester = semester;
  }
  /**
   * Kennzeichnet das Modul als getestet vom Notentester.
   */
  public void setIsTested(){
    this.isTested = true;
  }
  /**
   * Kennzeichnet das Modul als ungetestet vom Notentester
   */
  public void resetIsTested(){
    this.isTested = false;
  }

  /**
   * Gibt den Score als String aus.
   */
  public String toString(){
    String score = "studienElement: " + "\t"    + this.studienElement                     + "\n"
                 + "subject: "        + "\t\t"  + this.subject                            + "\n"
                 + "score: "          + "\t\t\t"+ this.score                              + "\n"
                 + "attempts: "       + "\t\t"  + this.attempts                           + "\n"
                 + "weight: "         + "\t\t[" + this.weight[0] + "][" + this.weight[1]  + "]\n"
                 + "semester: "       + "\t\t"  + this.semester                           + "\n"
                 + "hasSubScore: "    + "\t\t"  + this.hasSubScore                        + "\n"
                 + "hasParentScore:"  + "\t\t"  + this.hasParentScore                     + "\n"
                 + "parentStudienElement" + "\t"+ this.parentStudienElement               + "\n";
    score += "isWpf: " + "\t\t\t" + this.isWpf + "\n";
    if(this.isWpf){
      score+="wpfTopic: " + "\t\t" + this.wpfTopic + "\n"
            +"wpfWeight: " + "\t\t[" + this.wpfWeight[0] + "][" + this.wpfWeight[1] + "]\n";
    }
    score += "isTested: " + this.isTested + "\n";
    if(this.hasSubScore){
      score += "\t ===Subscore:===\n";
      for(Score subSet: this.subScore.values()){
        score += "\n" + subSet.toString();
      }
      score += "\n";

    }
    if(this.studienElement.length()>4){
      score+="\t========================================";
    }else{
      score+= "================================================";
    }

    return score;
  }
}
