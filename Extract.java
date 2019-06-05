public class Extract{
  public static String studienElement(String snippet){
    snippet = snippet.substring(snippet.indexOf('>', snippet.indexOf("pageLoad")));
    snippet = snippet.substring(snippet.lastIndexOf('.')+1,snippet.indexOf('<'));
    return snippet;
  }
  public static String subject(String snippet){
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    return snippet;
  }
  public static float score(String snippet){
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
    int attempt = 0;
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    if(snippet.matches(".*\\d.*")){
      attempt = Integer.parseInt(snippet);
    }
    return attempt;
  }

  public static String fieldOfStudy(String snippet){
    snippet = snippet.substring(snippet.indexOf('>')+1);
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    return snippet;
  }
  public static String name(String snippet){
    snippet = snippet.substring(snippet.indexOf("'Label'"));
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    return snippet;
  }
  public static String course(String snippet){
    snippet = snippet.substring(snippet.indexOf("'Label'"));
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    return snippet;
  }
  public static String syllabusStudienElement(String snippet){
    snippet = snippet.substring(snippet.lastIndexOf("onclick"));
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    return snippet;
  }
  public static String syllabusSubject(String snippet){
    snippet = snippet.substring(snippet.lastIndexOf("</a>")+"</a>".length());
    snippet = snippet.substring(1,snippet.indexOf('<'));
    if(snippet.contains("&amp;")){
      snippet = snippet.replace("&amp;", "&");
    }
    return snippet;
  }
  public static int[] syllabusWeight(String snippet){
    int denominator = Integer.parseInt(snippet.substring(snippet.indexOf('/')-1,snippet.indexOf('/')).replace("*",""));
    int numerator   = Integer.parseInt(snippet.substring(snippet.indexOf('/')+1,snippet.lastIndexOf('<')).replace("*",""));
    int[] weight = {denominator, numerator};
    return weight;
  }
  public static int[] wpfWeight(String snippet){
    snippet = snippet.substring(snippet.lastIndexOf("</a>"));
    int denominator = Integer.parseInt(snippet.substring(snippet.indexOf('(')+1, snippet.indexOf("aus")-1));
    int numerator = Integer.parseInt(snippet.substring(snippet.indexOf("aus ")+"aus ".length(),snippet.indexOf(')')));
    int[] wpfWeight = new int[2];
    wpfWeight[0] = denominator;
    wpfWeight[1] = numerator;
    return wpfWeight;
  }
  public static String wpfTopic(String snippet){
    snippet = snippet.substring(snippet.lastIndexOf("WPF ")+"WPF ".length(), snippet.lastIndexOf('('));
    snippet = snippet.trim();
    return snippet;
  }
  public static int semester(String snippet){
    int semester = 0;
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<')).trim();
    if(snippet.length()>0){
      semester = Integer.parseInt(snippet);
    }
    return semester;
  }
  public static int level(String snippet){
    snippet = snippet.substring(snippet.indexOf("SysTreeLevel")+"SysTreeLevel".length());
    snippet = snippet.substring(0, snippet.indexOf("'"));
    return Integer.parseInt(snippet);
  }
}
