/**
 * Diese Klasse Extract enthält statische Methoden für das Herausfiltern von Informationen aus dem HTML Dokument.
 * @author Thomas Davies
 * @version 1.0
*/

public class Extract{
  /**
   * Filtert das Studienelement aus der entsprechenden HTML-Zeile heraus.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt das Studienelement in Form eines Strings zurück.
   */
  public static String studienElement(String snippet){
    snippet = snippet.substring(snippet.indexOf('>', snippet.indexOf("pageLoad")));
    snippet = snippet.substring(snippet.lastIndexOf('.')+1,snippet.indexOf('<'));
    return snippet;
  }

  /**
   * Filtert den Modulnamen aus der entsprechenden HTML-Zeile heraus.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt den Modulnamen in Form eines Strings zurück.
   */
  public static String subject(String snippet){
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    return snippet;
  }

  /**
   * Filtert die Note aus der entsprechenden HTML-Zeile heraus.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt die Note in Form eines Floats zurück.
   */
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

  /**
   * Filtert die Anzahl der Versuche für ein Modul aus der entsprechenden HTML-Zeile heraus.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt di Versuche in Form eines int zurück.
   */
  public static int attempts(String snippet){
    int attempt = 0;
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    if(snippet.matches(".*\\d.*")){
      snippet = snippet.replaceAll("\\D+","");
      attempt = Integer.parseInt(snippet);
    }
    return attempt;
  }

  /**
   * Filter den Studienschwerpunkt aus der entsprechenden HTML-Zeile heraus.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt den Studienschwerpunkt in Form eines Strings zurück.
   */
  public static String fieldOfStudy(String snippet){
    snippet = snippet.substring(snippet.indexOf('>')+1);
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    return snippet;
  }

  /**
   * Filtert den Namen des Studenten aus der entsprechenden HTML-Zeile heraus.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt den Namen in Form eines Strings zurück.
   */
  public static String name(String snippet){
    snippet = snippet.substring(snippet.indexOf("'Label'"));
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    return snippet;
  }

  /**
   * Filtert den Studiengang aus der entsprechenden HTML-Zeile heraus.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt den Studiengang in Form eines Strings zurück.
   */
  public static String course(String snippet){
    snippet = snippet.substring(snippet.indexOf("'Label'"));
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    return snippet;
  }

  /**
   * Filtert das Studienelement aus der entsprechenden HTML-Zeile heraus.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt das Studienelement in Form eines Strings zurück.
   */
  public static String syllabusStudienElement(String snippet){
    snippet = snippet.substring(snippet.lastIndexOf("onclick"));
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<'));
    return snippet;
  }

  /**
   * Filtert den Modulnamen aus der entsprechenden HTML-Zeile heraus.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt den Modulnamen in Form eines Strings zurück.
   */
  public static String syllabusSubject(String snippet){
    snippet = snippet.substring(snippet.lastIndexOf("</a>")+"</a>".length());
    snippet = snippet.substring(1,snippet.indexOf('<'));
    if(snippet.contains("&amp;")){
      snippet = snippet.replace("&amp;", "&");
    }
    return snippet;
  }

  /**
   * Filtert die Gewichtung eines Moduls aus der entsprechenden HTML-Zeile heraus.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt die Gewichtung in Form eines int[] zurück.
   */
  public static int[] syllabusWeight(String snippet){
    int denominator = Integer.parseInt(snippet.substring(snippet.indexOf('/')-1,snippet.indexOf('/')).replace("*",""));
    int numerator   = Integer.parseInt(snippet.substring(snippet.indexOf('/')+1,snippet.lastIndexOf('<')).replace("*",""));
    int[] weight = {denominator, numerator};
    return weight;
  }

  /**
   * Filtert die Gewichtung eines Wahlpflichtmoduls aus der entsprechenden HTML-Zeile heraus.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt die Gewichtung in Form eines int[] zurück.
   */
  public static int[] wpfWeight(String snippet){
    snippet = snippet.substring(snippet.lastIndexOf("</a>"));
    int denominator = Integer.parseInt(snippet.substring(snippet.indexOf('(')+1, snippet.indexOf("aus")-1));
    int numerator = Integer.parseInt(snippet.substring(snippet.indexOf("aus ")+"aus ".length(),snippet.indexOf(')')));
    int[] wpfWeight = new int[2];
    wpfWeight[0] = denominator;
    wpfWeight[1] = numerator;
    return wpfWeight;
  }

  /**
   * Filtert das Wahlpflichtthema aus der entsprechenden HTML-Zeile heraus.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt das Wahlpflichtthema in Form eines Strings zurück.
   */
  public static String wpfTopic(String snippet){
    if(snippet.contains("WPF ")){
      snippet = snippet.substring(snippet.lastIndexOf("WPF ")+"WPF ".length(), snippet.lastIndexOf('('));
    }else if(snippet.contains("WPF ")){
      snippet = snippet.substring(snippet.lastIndexOf("Spezialisierung "), snippet.lastIndexOf('('));
    }else if(snippet.contains("Wahlpflicht_")){
      snippet = snippet.substring(snippet.lastIndexOf("Wahlpflicht_")+"Wahlpflicht_".length(), snippet.lastIndexOf('('));
    }else{
      snippet = snippet.substring(snippet.lastIndexOf("Wahlpflicht ")+"Wahlpflicht ".length(), snippet.lastIndexOf('('));
    }
    snippet = snippet.trim();
    return snippet;
  }

  /**
   * Filtert das Semester aus der entsprechenden HTML-Zeile heraus.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt das entsprechende Semster in Form eines int zurück.
   */
  public static int semester(String snippet){
    int semester = 0;
    snippet = snippet.substring(snippet.indexOf('>'));
    snippet = snippet.substring(1,snippet.indexOf('<')).trim();
    if(snippet.length()>0){
      semester = Integer.parseInt(snippet);
    }
    return semester;
  }
  /**
   * Filtert das Systree Level aus der entsprechenden HTML-Zeile heraus. Diese Methode hilft nur zur Koordinierung im HTML Dokument.
   * @param snippet Enthält die ausgelesene Zeile des HTML Dokuments.
   * @return Gibt das Systreelevel in Form eines int zurück.
   */
  public static int level(String snippet){
    snippet = snippet.substring(snippet.indexOf("SysTreeLevel")+"SysTreeLevel".length());
    snippet = snippet.substring(0, snippet.indexOf("'"));
    return Integer.parseInt(snippet);
  }
}
