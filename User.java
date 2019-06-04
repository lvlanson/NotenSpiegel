import java.io.Serializable;
import java.util.HashMap;

public class User implements Serializable{
  private static final long serialVersionUID = 1;
  private String name;
  private String course;
  private String fieldOfStudy;
  private float average;
  private float testAverage;
  private HashMap<String, Integer> wpfCounter;
  private HashMap<String, Integer> testWpfCounter;

  public User(String name, String course, String fieldOfStudy, float average, float testAverage){
    this.name = name;
    this.course = course;
    this.fieldOfStudy = fieldOfStudy;
    this.average = average;
    this.testAverage = testAverage;
  }
  public void increaseWpfCounter(String wpfTopic){
    wpfCounter.put(wpfTopic, wpfCounter.get(wpfTopic)+1);
  }
  public void decreaseWpfCounter(String wpfTopic){
    if(wpfCounter.get(wpfTopic) != 0){
      wpfCounter.put(wpfTopic, wpfCounter.get(wpfTopic)-1);
    }
  }
  public void increaseTestWpfCounter(String wpfTopic){
    testWpfCounter.put(wpfTopic, testWpfCounter.get(wpfTopic)+1);
  }
  public void decreaseTestWpfCounter(String wpfTopic){
    if(testWpfCounter.get(wpfTopic) != 0){
      testWpfCounter.put(wpfTopic, testWpfCounter.get(wpfTopic)-1);
    }
  }
  public void setAverage(float average){
    this.average = average;
  }
  public void setTestAverage(float testAverage){
    this.testAverage = testAverage;
  }
  public void setWpfCounter(HashMap<String, Integer> wpfCounter){
    this.wpfCounter = wpfCounter;
  }
  public void setTestWpfCounter(HashMap<String, Integer> testWpfCounter){
    this.testWpfCounter = testWpfCounter;
  }
  public void createTestWpfCounter(){
    this.testWpfCounter = new HashMap<String, Integer>();
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
  public HashMap<String, Integer> getWpfCounter(){
    return this.wpfCounter;
  }
  public HashMap<String, Integer> getTestWpfCounter(){
    return this.testWpfCounter;
  }
}
