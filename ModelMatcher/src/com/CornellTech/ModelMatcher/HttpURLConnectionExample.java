package com.CornellTech.ModelMatcher;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
 
public class HttpURLConnectionExample {
 
	private final String USER_AGENT = "Mozilla/5.0";
	
	// HTTP GET request
	public byte[] getFile(String url) throws IOException {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		// get length
		int len = con.getContentLength();
		
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		InputStream is = new BufferedInputStream(con.getInputStream());
		
	    try {
	        byte[] data = new byte[4096];
	        int offset = 0;
	        ByteArrayOutputStream bs = new ByteArrayOutputStream();
	        
	        while((offset=is.read(data))!=-1 ){
	        	bs.write(data,0,offset);
	        }
	        System.out.println("Byte length:" + bs.size());
	        return bs.toByteArray();
	    } finally {
	        is.close();
	    }
 
		//print result
		//System.out.println(response.toString());
 
	}
	
	// HTTP GET request
	public void sendGet(String url) throws Exception {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		//System.out.println(response.toString());
 
	}
 
	public static byte[] download(URL url) throws IOException {
	    URLConnection uc = url.openConnection();
	    int len = uc.getContentLength();
	    InputStream is = new BufferedInputStream(uc.getInputStream());
	    try {
	        byte[] data = new byte[len];
	        int offset = 0;
	        while (offset < len) {
	            int read = is.read(data, offset, data.length - offset);
	            if (read < 0) {
	                break;
	            }
	          offset += read;
	        }
	        if (offset < len) {
	            throw new IOException(
	                String.format("Read %d bytes; expected %d", offset, len));
	        }
	        return data;
	    } finally {
	        is.close();
	    }
	}
	
	// HTTP POST request
	public void sendPost(String url) throws Exception {
 
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
		String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
 
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
 
	}
 
}