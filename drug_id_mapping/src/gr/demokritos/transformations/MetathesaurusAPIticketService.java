package gr.demokritos.transformations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
import java.util.HashMap;
//import java.util.List;
import java.util.StringJoiner;

public class MetathesaurusAPIticketService {

	
	public static String getTicketGrantingTicket (String API_key) throws IOException {
		
		String TGT="";
		
		URL obj = new URL("https://utslogin.nlm.nih.gov/cas/v1/api-key");
	    URLConnection con = obj.openConnection();
	    HttpURLConnection http = (HttpURLConnection)con;
	    http.setRequestMethod("POST"); // PUT is another valid option
	    http.setDoOutput(true);
	    HashMap<String,String> arguments = new HashMap<>();
	    arguments.put("apikey", API_key);
	    StringJoiner sj = new StringJoiner("&");
	    for(HashMap.Entry<String,String> entry : arguments.entrySet())
	        sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" 
	             + URLEncoder.encode(entry.getValue(), "UTF-8"));
	    byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
	    int length = out.length;
	    //We can then attach our form contents to the http request with proper headers and send it.
	    http.setFixedLengthStreamingMode(length);
	    http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	    http.connect();
	    try(OutputStream os = http.getOutputStream()) {
	        os.write(out);
	    }
	    BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
	    String inputLine;
	    while ((inputLine = in.readLine()) != null) {
	    	//System.out.println(inputLine);
	    	int startPos =inputLine.indexOf("api-key/")+8;
	    	int endPos =inputLine.indexOf("\" method=");
	    	TGT=inputLine.substring(startPos, endPos);
	    	System.out.println("TGT="+TGT);
	    }	
	    in.close();
	    return TGT;
	}

	public static String getServiceTicket(String TGT) throws IOException {
	    
		String st="";
		URL obj = new URL("https://utslogin.nlm.nih.gov/cas/v1/tickets/"+TGT);
	    URLConnection con = obj.openConnection();
	    HttpURLConnection http = (HttpURLConnection)con;
	    http.setRequestMethod("POST"); // PUT is another valid option
	    http.setDoOutput(true);
	    HashMap<String,String> arguments = new HashMap<>();
	    arguments.put("service", "http://umlsks.nlm.nih.gov");
	    StringJoiner sj = new StringJoiner("&");
	    for(HashMap.Entry<String,String> entry : arguments.entrySet())
	        sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" 
	             + URLEncoder.encode(entry.getValue(), "UTF-8"));
	    byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
	    int length = out.length;
	    //We can then attach our form contents to the http request with proper headers and send it.
	    http.setFixedLengthStreamingMode(length);
	    http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	    http.connect();
	    try(OutputStream os = http.getOutputStream()) {
	        os.write(out);
	    }
	    BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
	    String inputLine;
	    if ((inputLine = in.readLine()) != null) {
	    	st=inputLine;
	    	//System.out.println("Service Ticket="+st);
	    }
	    else
	    	System.out.println("ERROR GETTING SERVICE TICKET!");
	    
	    in.close();
	    return st;
	}

	public static void main (String[] args) {
		//String TGT="TGT-1871590-QgQMc1Wom2TWIHaz5XU7lsmHNOCsSqUu3frBS2SbVXRNfqOMhI-cas";
		try {
			String TGT = getTicketGrantingTicket("5e3ab18f-17fa-43f6-9cb2-41cccacc4ad5");
			System.out.println("TGT="+TGT);
			System.out.println("ticket="+getServiceTicket(TGT));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
