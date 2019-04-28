import java.io.Serializable;

public class User implements Serializable{
  private static final long serialVersionUID = 1;
  private String name;
  private String course;
  private String fieldOfStudy;
  private float average;
  private float testAverage;

  public User(String name, String course, String fieldOfStudy, float average){
    this.name = name;
    this.course = course;
    this.fieldOfStudy = fieldOfStudy;
    this.average = average;
  }
  public void setAverage(float average){
    this.average = average;
  }
  public void setTestAverage(float testAverage){
    this.testAverage = testAverage;
  }

  public String getName(){
    return this.name;
  }
  public String getCourse(){
    return this.course;
  }
  public String getFieldOfStudy(){
    return this.fieldOfStudy;
  }
  public float getAverage(){
    return this.average;
  }
  public float getTestAverage(){
    return this.testAverage;
  }
}
