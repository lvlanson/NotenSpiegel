import java.io.Serializable;

public class Score implements Serializable{
  private static final long serialVersionUID = 1;
  private String studienElement;
  private String subject;
  private float score;
  private int attempts;

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
}
