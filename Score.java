import java.io.Serializable;

public class Score implements Serializable{
  private static final long serialVersionUID = 1L;
  private String studienElement;
  private String subject;
  private float score;
  private int attempts;
  private int[] weight;
  private Score subScore;
  private boolean hasSubScore = false;
  private boolean isWpf = false;
  private String wpfTopic;
  private int[] wpfWeight;
  private int semester;

  public Score(){

  }

  public Score(String studEl, String subject,int semester, int[] weight){
    this.studienElement = studEl;
    this.subject = subject;
    this.weight = new int[2];
    this.weight[0] = weight[0];
    this.weight[1] = weight[1];
    this.semester = semester;
  }
  public Score(String studEl, String subject, int semester, int[] weight, boolean isWpf, int[] wpfWeight, String wpfTopic){
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
  }
  public String getStudienElement(){
    return this.studienElement;
  }
  public String getSubject(){
    return this.subject;
  }
  public float getScore(){
    return this.score;
  }
  public int getAttempts(){
    return this.attempts;
  }
  public int[] getWeight(){
    return this.weight;
  }
  public Score getSubScore(){
    return this.subScore;
  }
  public boolean hasSubScore(){
    return this.hasSubScore;
  }
  public boolean isWpf(){
    return this.isWpf;
  }
  public String getWpfTopic(){
    return this.wpfTopic;
  }
  public int[] getWpfWeight(){
    return this.wpfWeight;
  }
  public int getSemester(){
    return this.semester;
  }

  public void setStudienElement(String el){
    this.studienElement = el;
  }
  public void setSubject(String subject){
    this.subject = subject;
  }
  public void setScore(float score){
    this.score = score;
  }
  public void setAttempts(int attempts){
    this.attempts = attempts;
  }
  public void setSubScore(){
    this.hasSubScore = true;
  }
  public void setWpf(){
    this.isWpf = true;
  }
  public void setWeight(int denominator, int numerator){
    this.weight = new int[2];
    this.weight[0] = denominator;
    this.weight[1] = numerator;
  }
  public void setSubScore(String studEl, String subject,int semester, int[] weight){
    this.subScore = new Score(studEl, subject, semester, weight);
  }
  public void setWpfTopic(String wpfTopic){
    this.wpfTopic = wpfTopic;
  }
  public void setWpfWeight(int denominator, int numerator){
    this.wpfWeight = new int[2];
    this.wpfWeight[0] = denominator;
    this.wpfWeight[1] = numerator;
  }
}
