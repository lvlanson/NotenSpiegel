import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Diese Klasse baut ein Objekt, was alle Daten aus einer Webserverantwort bündelt.
 * @author Thomas Davies
 * @version 1.0
*/

public class Response{
	private String content;
	private Map<String, List<String>> header;
	private URL url;

	/**
	 * Der Konstruktor nimmt den Inhalt als String, die Header als Map, und die URL des Seitenaufrufs auf.
	 * @param content Ist der Inhalt der Serverantwort
	 * @param header Sind die Header Informationen  der Serverantwort
	 * @param url Ist die URL der Serverantwort
	 */
	public Response(String content, Map<String, List<String>> header, URL url){
		this.content = content;
		this.header = header;
		this.url = url;
	}

	/**
	 * @return Gibt den Inhalt der Response als String zurück.
	 */
	public String getContent(){
		return content;
	}
	/**
	 * @return Gibt die Header in Form einer Map zurück.
	 */
	public Map<String, List<String>> getHeader(){
		return header;
	}
	/**
	 * @return Gibt die Header anhand eines bestimmten Keys in Form einer Liste zurück.
	 */
	public List<String> getHeader(String key){
		return header.get(key);
	}
	/**
	 * @return Gibt die Header anhand eines bestimmten Keys in Form eines Strings zurück.
	 */
	public String getHeaderString(String key){
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < header.get(key).size(); i++){
			builder.append(header.get(key).get(i));
			if (i < header.get(key).size() - 1){
        builder.append("; ");
      }
		}

		return builder.toString();
	}
	/**
	 * @return Gibt die URL der Respnse zurück.
	 */
	public URL getUrl(){
		return url;
	}

	/**
	 * @return Gibt den Content in Form eines Strings zurück.
	 */
	@Override
	public String toString(){
		return content;
	}
}
