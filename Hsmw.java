import java.util.ArrayList;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.lang.StringBuilder;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
//Diese Klasse verwaltet alle Operationen mit der HSMW Webseite
public class Hsmw{
  private static final String syllabus = "https://www.intranet.hs-mittweida.de/sportal/his/studenten/student.ablauf.asp";
  private static final String basisDaten = "https://www.intranet.hs-mittweida.de/sportal/his/studenten/student.info.asp";


  public static void createDataFromHSMW(String username, String password)throws MalformedURLException, IOException, StringIndexOutOfBoundsException, Exception{
      ArrayList<InputStream> streams = getInputStreams(username, password);
      Syllabus s = new Syllabus();
      s.createSyllabus(streams.get(0), streams.get(1));
  }

  private static ArrayList<InputStream> getInputStreams(String username, String password) throws MalformedURLException, IOException, StringIndexOutOfBoundsException{
		// Request initial page
    ArrayList<InputStream> inputstreams = new ArrayList<InputStream>();
		HttpURLConnection.setFollowRedirects(true);
		Response response = getDocument(new URL(basisDaten), null, null);

		HttpURLConnection.setFollowRedirects(false);

		// Login
		Map<String, String> params = new HashMap<>();
		params.put("j_username", username);
		params.put("j_password", password);
		params.put("_eventId_proceed", "");
		response = getDocument(response.getUrl(), params, null);


		int referenceIndex = response.getContent().indexOf("<form action");
		int startIndex = response.getContent().indexOf("\"", referenceIndex) + 1;
		int endIndex = response.getContent().indexOf("\"", startIndex);
    if(response.getContent().length() <= 0){
      throw new StringIndexOutOfBoundsException();
    }
		String targetAction = response.getContent().substring(startIndex, endIndex);
		targetAction = targetAction.replaceAll("&#x3a;", ":");
		targetAction = targetAction.replaceAll("&#x2f;", "/");

		referenceIndex = response.getContent().indexOf("RelayState");
		startIndex = response.getContent().indexOf("value=", referenceIndex) + 7;
		endIndex = response.getContent().indexOf("\"", startIndex);
		String relayState = response.getContent().substring(startIndex, endIndex);
		relayState = relayState.replaceAll("&#x3a;", ":");

		referenceIndex = response.getContent().indexOf("SAMLResponse");
		startIndex = response.getContent().indexOf("value=", referenceIndex) + 7;
		endIndex = response.getContent().indexOf("\"", startIndex);
		String samlResponse = response.getContent().substring(startIndex, endIndex);

		// IDP-Cookie
		String cookies = response.getHeaderString("Set-Cookie");

		params.clear();
		params.put("RelayState", relayState);
		params.put("SAMLResponse", samlResponse);
		response = getDocument(new URL(targetAction), params, cookies);

		 // Session Cookie
		String sessionCookie = response.getHeaderString("Set-Cookie");
		String location = response.getHeaderString("Location");

		cookies += "; " + sessionCookie;
		response = getDocument(new URL(location), null, cookies);

    String baseDataHTML = response.toString();
    InputStream baseStream = new ByteArrayInputStream(baseDataHTML.getBytes(Charset.forName("UTF-8")));
    inputstreams.add(baseStream);

    HttpURLConnection.setFollowRedirects(true);
		response = getDocument(new URL(syllabus), null, null);

		HttpURLConnection.setFollowRedirects(false);

		// Login
		params = new HashMap<>();
		params.put("j_username", username);
		params.put("j_password", password);
		params.put("_eventId_proceed", "");
		response = getDocument(response.getUrl(), params, null);


		referenceIndex = response.getContent().indexOf("<form action");
		startIndex = response.getContent().indexOf("\"", referenceIndex) + 1;
		endIndex = response.getContent().indexOf("\"", startIndex);
		targetAction = response.getContent().substring(startIndex, endIndex);
		targetAction = targetAction.replaceAll("&#x3a;", ":");
		targetAction = targetAction.replaceAll("&#x2f;", "/");

		referenceIndex = response.getContent().indexOf("RelayState");
		startIndex = response.getContent().indexOf("value=", referenceIndex) + 7;
		endIndex = response.getContent().indexOf("\"", startIndex);
		relayState = response.getContent().substring(startIndex, endIndex);
		relayState = relayState.replaceAll("&#x3a;", ":");

		referenceIndex = response.getContent().indexOf("SAMLResponse");
		startIndex = response.getContent().indexOf("value=", referenceIndex) + 7;
		endIndex = response.getContent().indexOf("\"", startIndex);
		samlResponse = response.getContent().substring(startIndex, endIndex);

		// IDP-Cookie
		cookies = response.getHeaderString("Set-Cookie");

		params.clear();
		params.put("RelayState", relayState);
		params.put("SAMLResponse", samlResponse);
		response = getDocument(new URL(targetAction), params, cookies);

		 // Session Cookie
		sessionCookie = response.getHeaderString("Set-Cookie");
		location = response.getHeaderString("Location");

		cookies += "; " + sessionCookie;
		response = getDocument(new URL(location), null, cookies);

    String syllabusHTML = response.toString();
    InputStream syllabusStream = new ByteArrayInputStream(syllabusHTML.getBytes(Charset.forName("UTF-8")));
    inputstreams.add(syllabusStream);


    return inputstreams;

  }

  private static Response getDocument(URL url, Map<String, String> params, String cookies) throws IOException
	{
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		connection.setRequestMethod("POST");

		// Cookies setzen
		if (cookies != null)
		{ connection.setRequestProperty("Cookie", cookies); }

		// Parameter übergeben
		if (params != null)
		{
			byte[] postData = getPostData(params);
			connection.setRequestProperty("Content-Length", String.valueOf(postData.length));

			connection.setDoOutput(true);
			try (OutputStream output = connection.getOutputStream())
			{
				output.write(postData);
				output.flush();
			}
		}

//		connection.addRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3650.1 Iron Safari/537.36");
		/*System.out.println("Response-Code:\n\t" + connection.getResponseCode() + " (" + connection.getResponseMessage() + ")");
		for (Map.Entry<String, List<String>> headerEntry : connection.getHeaderFields().entrySet()){
			System.out.println(headerEntry.getKey() + ":");
			for (String value : headerEntry.getValue()){
				System.out.println("\t" + value);
			}
		}*/

		// Inhalt holen
		StringBuilder builder = new StringBuilder();
		try (Scanner s = new Scanner(connection.getInputStream())){
			while (s.hasNext()){
        builder.append(s.nextLine()).append("\n"); }
		}

		return new Response(builder.toString(), connection.getHeaderFields(), connection.getURL());
	}
  private static byte[] getPostData(Map<String, String> params) throws UnsupportedEncodingException{
		StringBuilder postDataBuilder = new StringBuilder();
		for (Map.Entry<String, String> param : params.entrySet()){
			if (postDataBuilder.length() != 0){
        postDataBuilder.append('&');
      }
			postDataBuilder.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postDataBuilder.append('=');
			postDataBuilder.append(URLEncoder.encode(param.getValue(), "UTF-8"));
		}
		return postDataBuilder.toString().getBytes("UTF-8");
	}
}
