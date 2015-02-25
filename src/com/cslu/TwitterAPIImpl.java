package com.cslu;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.springframework.stereotype.Service;

@Service
public class TwitterAPIImpl implements TwitterAPI{
	public TwitterAPIImpl(){
		//Constructor
	}
	
	public String doRequestToken(){
		URL url = null;
		HttpURLConnection uc = null;
		try {
			url = new URL("https://api.twitter.com/oauth/request_token");
			uc = (HttpURLConnection) url.openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		uc.setDoOutput(true);
		uc.setUseCaches(false);
		
		String authorized_values = OAuthModel.getStepOneAuthParameters();
		
		try {
			uc.setRequestMethod("POST");
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
		uc.setRequestProperty("Authorization", authorized_values);

		OutputStreamWriter out = null;
		try {
			out = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
			out.flush();
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		StringBuilder sb = new StringBuilder();
		InputStream in = null;
		try {
			in = new BufferedInputStream(uc.getInputStream());
			Reader r = new InputStreamReader(in);
			int c;
			while ((c = r.read()) != -1) {
				sb.append((char) c);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}//end of doRequestToken()
	
	public String doAuthorize(String oauth_token) {
		String queryString = OAuthModel.getStepTwoQueryString(oauth_token);
		return "https://api.twitter.com/oauth/authorize" + queryString;
		
	}//end of doAuthorize()
	
	public String doAccessToken(String oauth_token, String oauth_verifier, String unAuthorizedTokenSecret){
		URL url = null;
		HttpURLConnection uc = null;
		StringBuilder queryString = new StringBuilder();
		queryString.append(OAuthModel.percentEncode("oauth_verifier"));
		queryString.append("=");
		queryString.append(OAuthModel.percentEncode(oauth_verifier));
		try {
			url = new URL("https://api.twitter.com/oauth/access_token");
			uc = (HttpURLConnection) url.openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		uc.setDoOutput(true);
		uc.setUseCaches(false);

		String authorized_values = OAuthModel.getStepThreeAuthParameters(oauth_token, oauth_verifier, unAuthorizedTokenSecret);
		
		try {
			uc.setRequestMethod("POST");
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
		uc.setRequestProperty("Authorization", authorized_values);

		OutputStreamWriter out = null;
		try {
			out = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
			out.write(queryString.toString());
			out.flush();
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		StringBuilder sb = new StringBuilder();
		InputStream in = null;
		try {
			in = new BufferedInputStream(uc.getInputStream());
			Reader r = new InputStreamReader(in);
			int c;
			while ((c = r.read()) != -1) {
				sb.append((char) c);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}//end of doAccessToken()
	
	public String doUserTimeLine(String access_token, String access_token_secret, String screen_name){
		URL url = null;
		HttpURLConnection uc = null;
		StringBuilder queryString = new StringBuilder();
		queryString.append("?");
		queryString.append(OAuthModel.percentEncode("screen_name"));
		queryString.append("=");
		queryString.append(OAuthModel.percentEncode(screen_name));
		try {
			url = new URL("https://api.twitter.com/1.1/statuses/user_timeline.json" + queryString.toString());
			uc = (HttpURLConnection) url.openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		uc.setUseCaches(false);

		String authorized_values = OAuthModel.getUserTimeLineAuthParameters(access_token, access_token_secret, screen_name);
		
		uc.setRequestProperty("Authorization", authorized_values);
		
		StringBuilder sb = new StringBuilder();
		InputStream in = null;
		try {
			in = new BufferedInputStream(uc.getInputStream());
			Reader r = new InputStreamReader(in);
			int c;
			while ((c = r.read()) != -1) {
				sb.append((char) c);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}//end of doUserTimeLine()
}//end of class
