public class Extract{
  public static String studienElement(String snippet){
    snippet = snippet.substring(snippet.indexOf('>', snippet.indexOf("pageLoad")));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    return snippet;
  }
  public static String subject(String snippet){
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    return snippet;
  }
  public static float score(String snippet){
    snippet = snippet.substring(snippet.indexOf('>')+1);
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'))
                     .replace(',','.');
    float score = 0.0f;
    if(snippet.contains("#") || snippet.length() == 0){

    }else{
      score=Float.parseFloat(snippet);
    }
    return score;
  }
  public static int attempts(String snippet){
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    return Integer.parseInt(snippet);
  }
}
