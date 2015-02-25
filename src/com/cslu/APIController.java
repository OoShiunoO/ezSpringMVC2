package com.cslu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@SessionAttributes({"oauth_token","oauth_token_secret"})
public class APIController {
	
	@Autowired
	TwitterAPI twApi;
	
	@RequestMapping(value = "/redirectToTwitter", method = RequestMethod.GET)
	public ModelAndView redirectToTwitter() {
		//System.out.println("This is redirectTOTwitter method!!!");
		String the_token_information = twApi.doRequestToken();
		String[] split_information = the_token_information.split("&");
		String oauth_token = null;
		String oauth_token_secret = null;
		if ((split_information[2].split("=")[1]).equals("true")) {
			oauth_token = split_information[0].split("=")[1];
			oauth_token_secret = split_information[1].split("=")[1];
		} else {
			throw new RuntimeException("oauth_callback_confirmed=false");
		}
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setView(new RedirectView(twApi.doAuthorize(oauth_token)));
		modelAndView.addObject("oauth_token",oauth_token);
		modelAndView.addObject("oauth_token_secret",oauth_token_secret);
		
		return modelAndView;
	}//end of redirectToTwitter()
	
}//end of class
