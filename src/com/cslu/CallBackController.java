package com.cslu;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller
@SessionAttributes({"oauth_token","oauth_token_secret"})
public class CallBackController {
	
	@Autowired
	TwitterAPI twApi;
	
	@RequestMapping(value = "/handleCallBack", method = RequestMethod.GET)
	public ModelAndView handleCallBack(HttpServletRequest request) {
		//System.out.println("This is handleCallBack method!!!");
		String oauth_token = request.getParameter("oauth_token");
		String oauth_verifier = request.getParameter("oauth_verifier");
		System.out.println("oauth_token: "+oauth_token);
		System.out.println("oauth_verifier: "+oauth_verifier);
		//接下來需要驗證oauth_token是否與第一步收到的是一樣的
		HttpSession session = request.getSession();
		String unAuthorizedToken = (String) session.getAttribute("oauth_token");
		String unAuthorizedTokenSecret = (String) session.getAttribute("oauth_token_secret");
		System.out.println("unAuthorizedToken: "+ unAuthorizedToken);
		System.out.println("unAuthorizedTokenSecret: "+ unAuthorizedTokenSecret);
		String AccessTokenInformation = null;
		if (unAuthorizedToken.equals(oauth_token)) {
			AccessTokenInformation = twApi.doAccessToken(oauth_token, oauth_verifier, unAuthorizedTokenSecret);
		} else {
			throw new RuntimeException("Authorized Token is different from request token!");
		}
		System.out.println("AccessTokenInformation is: "+ AccessTokenInformation);
		String[] informationArray = AccessTokenInformation.split("&");
		String access_token = informationArray[0].split("=")[1];
		String access_token_secret = informationArray[1].split("=")[1];
		String user_id = informationArray[2].split("=")[1];
		String screen_name = informationArray[3].split("=")[1];
		
		String UserTimeLine = twApi.doUserTimeLine(access_token, access_token_secret, screen_name);
		request.setAttribute("Message", UserTimeLine);
		request.setAttribute("screen_name", screen_name);
		
		return new ModelAndView("UserTimeLine");
	}//end of handleCallBack()
}//end of class
