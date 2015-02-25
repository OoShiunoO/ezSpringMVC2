package com.cslu;

public interface TwitterAPI {
	public String doRequestToken();
	public String doAuthorize(String oauth_token);
	public String doAccessToken(String oauth_token, String oauth_verifier, String unAuthorizedTokenSecret);
	public String doUserTimeLine(String access_token, String access_token_secret, String screen_name);
}
