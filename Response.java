import java.net.URL;
import java.util.List;
import java.util.Map;

public class Response{
	private String content;
	private Map<String, List<String>> header;
	private URL url;

	public Response(String content, Map<String, List<String>> header, URL url){
		this.content = content;
		this.header = header;
		this.url = url;
	}
	public String getContent(){
		return content;
	}
	public Map<String, List<String>> getHeader(){
		return header;
	}
	public List<String> getHeader(String key){
		return header.get(key);
	}
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
	public URL getUrl(){
		return url;
	}

	@Override
	public String toString(){
		return content;
	}
}
