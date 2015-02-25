package com.cslu;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64; //這個包要放在tomcat的lib資料夾裡面，如果放在別的地方會加載失敗。

class Encoded_pairs {
	private Map<String, String> pairs = null;
	public Encoded_pairs() {
		pairs = new TreeMap<String, String>();
	}
	
	public void addPairs(String key, String value) {
		pairs.put(OAuthModel.percentEncode(key), OAuthModel.percentEncode(value));
	}
	
	public Map<String, String> getPairs() {
		return this.pairs;
	}
}

public class OAuthModel {
	private static final String OAUTH_CONSUMER_KEY = "rKipCxkCfcd4BOIiHpJOiAZ6y";
	private static final String OAUTH_CONSUMER_SECRET = "UvDNRcTQUVCcZ3PkyBR9LYz9CHwRuBPXZYVAI3X9kY4ttlcPNo";
	private static final String CALLBACK_URL = "http://140.112.3.55:8080/ezSpringMVC2/handleCallBack";
	
	OAuthModel() {
		
	}
	
	public static String doRandomNonce(){
		Random r = new Random();
		String alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuilder nonce = new StringBuilder();
		for (int i=0; i<42; i++) {
			nonce.append(alphabet.charAt(r.nextInt(alphabet.length())));
		}
		return nonce.toString();
	}
	
	public static String percentEncode(String s) {
        if (s == null) {
            return "";
        }
        try {
            return URLEncoder.encode(s, "UTF-8")
                    // OAuth encodes some characters differently:
                    .replace("+", "%20").replace("*", "%2A")
                    .replace("%7E", "~");
            // This could be done faster with more hand-crafted code.
        } catch (UnsupportedEncodingException wow) {
            throw new RuntimeException(wow.getMessage(), wow);
        }
    }
	
	public static String generateSignature(String signing_key, String signature_base_string) {
		Base64 b64 = new Base64(); //使用了Apache Commons Codec的Base64模組
		byte[] result = null;
		try {
			Mac mac = Mac.getInstance("HmacSHA1"); //使用了javax.crypto.Mac套件
			SecretKeySpec secret = new SecretKeySpec(signing_key.getBytes(), mac.getAlgorithm());
			mac.init(secret);
			byte[] digest = mac.doFinal(signature_base_string.getBytes());
			result=b64.encode(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		StringBuilder signature = new StringBuilder();
		for (byte r:result) {
			signature.append((char)r);
		}
		
		return signature.toString();
	}
	
	public static String getAuthParameters_Template(String resourceURL, Map<String, String> additionalHeaders, Map<String, String> queryString, String method, String secret_for_signature) {
		Encoded_pairs ep = new Encoded_pairs();
		ep.addPairs("oauth_consumer_key",OAUTH_CONSUMER_KEY);
		ep.addPairs("oauth_nonce",doRandomNonce());
		ep.addPairs("oauth_timestamp",String.valueOf((new Date()).getTime()/1000)); //要回傳秒，不是毫秒!!!!!!!!
		ep.addPairs("oauth_version","1.0");
		ep.addPairs("oauth_signature_method","HMAC-SHA1");
		
		for (String key: additionalHeaders.keySet()) {
			ep.addPairs(key, additionalHeaders.get(key));
		}
		for (String key: queryString.keySet()) {
			ep.addPairs(key, queryString.get(key));
		}
		
		/*---------------以下是Collecting parameters這一步----------------*/
		StringBuilder collected_parameters = new StringBuilder();
		for (String key: ep.getPairs().keySet()) {
			collected_parameters.append(key);
			collected_parameters.append("=");
			collected_parameters.append(ep.getPairs().get(key));
			collected_parameters.append("&");
		}
		collected_parameters.deleteCharAt(collected_parameters.length()-1); //刪除最後一個&
		
		/*---------以下是Creating the signature base string這一步----------*/
		StringBuilder signature_base_string = new StringBuilder();
		signature_base_string.append(method);
		signature_base_string.append("&");
		signature_base_string.append(OAuthModel.percentEncode(resourceURL));
		signature_base_string.append("&");
		signature_base_string.append(OAuthModel.percentEncode(collected_parameters.toString()));
		
		/*----------------以下是Getting a signing key這一步----------------*/
		StringBuilder signing_key = new StringBuilder();
		signing_key.append(OAuthModel.percentEncode(OAUTH_CONSUMER_SECRET)); //我的consumer secret!!!
		signing_key.append("&");
		signing_key.append(OAuthModel.percentEncode(secret_for_signature));
		
		/*--------------以下是Calculating the signature這一步--------------*/
		String signature = generateSignature(signing_key.toString(), signature_base_string.toString());
		ep.addPairs("oauth_signature",signature.toString());
		
		Map<String, String> AuthorizationParameterPairs = ep.getPairs();
		for (String key: queryString.keySet()) {
			AuthorizationParameterPairs.remove(key);
		}
		
		StringBuilder DST = new StringBuilder();
		DST.append("OAuth ");
		for (String parameterKey: AuthorizationParameterPairs.keySet()) {
			DST.append(parameterKey);
			DST.append("=\"");
			DST.append(AuthorizationParameterPairs.get(parameterKey));
			DST.append("\", ");
		}
		DST.delete(DST.length()-2,DST.length());
		return DST.toString();
	}//end of getStepOneAuthParameters()
	
	public static String getStepOneAuthParameters() {
		String resourceURL = "https://api.twitter.com/oauth/request_token";
		Map<String, String> additionalHeaders = new HashMap<String, String>();
		additionalHeaders.put("oauth_callback", CALLBACK_URL);
		Map<String, String> queryString = new HashMap<String, String>();
		String method = "POST";
		String secret_for_signature = "";
		String AuthParameters = getAuthParameters_Template(resourceURL, additionalHeaders, queryString, method, secret_for_signature);
		return AuthParameters;
	}//end of getStepOneAuthParameters()
	
	public static String getStepTwoQueryString(String oauth_token) {
		StringBuilder sb = new StringBuilder();
		sb.append("?");
		sb.append(OAuthModel.percentEncode("oauth_token"));
		sb.append("=");
		sb.append(OAuthModel.percentEncode(oauth_token));
		return sb.toString();
	}//end of getStepTwoQueryString()
	
	public static String getStepThreeAuthParameters(String oauth_token, String oauth_verifier, String unAuthorizedTokenSecret) {
		String resourceURL = "https://api.twitter.com/oauth/access_token";
		Map<String, String> additionalHeaders = new HashMap<String, String>();
		additionalHeaders.put("oauth_token",oauth_token);
		Map<String, String> queryString = new HashMap<String, String>();
		queryString.put("oauth_verifier",oauth_verifier);
		String method = "POST";
		String secret_for_signature = unAuthorizedTokenSecret;
		String AuthParameters = getAuthParameters_Template(resourceURL, additionalHeaders, queryString, method, secret_for_signature);
		return AuthParameters;
	}//end of getStepThreeAuthParameters()
	
	public static String getUserTimeLineAuthParameters(String access_token, String access_token_secret, String screen_name) {
		String resourceURL = "https://api.twitter.com/1.1/statuses/user_timeline.json";
		Map<String, String> additionalHeaders = new HashMap<String, String>();
		additionalHeaders.put("oauth_token",access_token);
		Map<String, String> queryString = new HashMap<String, String>();
		queryString.put("screen_name",screen_name);
		String method = "GET";
		String secret_for_signature = access_token_secret;
		String AuthParameters = getAuthParameters_Template(resourceURL, additionalHeaders, queryString, method, secret_for_signature);
		return AuthParameters;
	}//end of getReTweetsOfMeAuthParameters()
}//end of class
