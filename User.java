import java.io.Serializable;

public class User implements Serializable{
  private static final long serialVersionUID = 1;
  private String name;
  private String course;
  private String fieldOfStudy;

  public User(String name, String course, String fieldOfStudy){
    this.name = name;
    this.course = course;
    this.fieldOfStudy = fieldOfStudy;
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
}
