import java.util.ArrayList;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.StringBuilder;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
//Diese Klasse verwaltet alle Operationen mit der HSMW Webseite
public class Hsmw{
  private static URL notenAnzeigeURL; // = new URL("http://www.intranet.hs-mittweida.de/sportal/his/kpl/student.sve.asp");
  private static URL syllabusURL; // = new URL("http://www.intranet.hs-mittweida.de/sportal/his/studenten/student.ablauf.asp");
  private static URL basisDatenURL;
  private static HttpURLConnection con;

  public static void getDataFromHSMW(String username, String password){
    try{
      getBasicData("tdavies", "SuperStreber123!");
    }catch(MalformedURLException e){
      e.printStackTrace();
    }catch(IOException e){
      e.printStackTrace();
    }

  }

  private static void getBasicData(String username, String password) throws MalformedURLException, IOException{
    basisDatenURL = new URL("https://www.intranet.hs-mittweida.de/sportal/his/studenten/student.info.asp");
		HttpURLConnection.setFollowRedirects(true);
    Response response = getDocument(basisDatenURL, null, null);
    HttpURLConnection.setFollowRedirects(false);
    HashMap<String, String> params = new HashMap<>();
    params.put("j_username", username);
		params.put("j_password", password);
		params.put("_eventId_proceed", "");


    int referenceIndex = response.getContent().indexOf("<form action");
		int startIndex = response.getContent().indexOf("\"", referenceIndex) + 1;
		int endIndex = response.getContent().indexOf("\"", startIndex);
		String targetAction = response.getContent().substring(startIndex, endIndex);
		System.out.println("TargetAction: " + targetAction);
		targetAction = targetAction.replaceAll("&#x3a;", ":");
		targetAction = targetAction.replaceAll("&#x2f;", "/");
		System.out.println("TargetAction After: Edit" + targetAction);

		referenceIndex = response.getContent().indexOf("RelayState");
		startIndex = response.getContent().indexOf("value=", referenceIndex) + 7;
		endIndex = response.getContent().indexOf("\"", startIndex);
		String relayState = response.getContent().substring(startIndex, endIndex);
		System.out.println("Relaystate: " + relayState);
		relayState = relayState.replaceAll("&#x3a;", ":");
		System.out.println("Relaystate After:  Edit" + relayState);

		referenceIndex = response.getContent().indexOf("SAMLResponse");
		startIndex = response.getContent().indexOf("value=", referenceIndex) + 7;
		endIndex = response.getContent().indexOf("\"", startIndex);
		String samlResponse = response.getContent().substring(startIndex, endIndex);
		System.out.println("samlResponse: " + samlResponse);

    String cookies = response.getHeaderString("Set-Cookie");
		System.out.println("cookies: " + cookies);

		params.clear();
		params.put("RelayState", relayState);
		params.put("SAMLResponse", samlResponse);
		response = getDocument(new URL(targetAction), params, cookies);
		System.out.println(response);
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
		System.out.println("Response-Code:\n\t" + connection.getResponseCode() + " (" + connection.getResponseMessage() + ")");
		for (Map.Entry<String, List<String>> headerEntry : connection.getHeaderFields().entrySet()){
			System.out.println(headerEntry.getKey() + ":");
			for (String value : headerEntry.getValue()){
				System.out.println("\t" + value);
			}
		}

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
