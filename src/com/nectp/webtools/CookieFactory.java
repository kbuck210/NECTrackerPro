package com.nectp.webtools;

import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//	This class designed in style of CookieHelper by Vasil Lukach at
//	http://stackoverflow.com/questions/20934016/how-to-add-cookie-in-jsf

public class CookieFactory {

	private static Cookie createCookie(String name, String value, int maxage) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxage);
		return cookie;
	}
	
	private static Cookie[] getCookieJar(FacesContext cxt) {
		HttpServletRequest request = (HttpServletRequest) cxt.getExternalContext().getRequest();
		
		return request.getCookies();
	}
	
	public static void giveCookie(String name, String value, int maxage) {
		Cookie cookie = null;
		FacesContext cxt = FacesContext.getCurrentInstance();
		//	Check for existing cookie
		Cookie[] cookieJar = getCookieJar(cxt);
		for (Cookie c : cookieJar) {
			if (c.getName().equals(name)) {
				cookie = c;
				break;
			}
		}
		
		if (cookie != null) {
			cookie.setValue(value);
			cookie.setMaxAge(maxage);
		}
		else {
			cookie = createCookie(name, value, maxage);
		}
		
		HttpServletResponse response = (HttpServletResponse) cxt.getExternalContext().getResponse();
		response.addCookie(cookie);
	}
	
	public static Cookie recieveCookie(String name) {
		Cookie cookie = null;
		FacesContext cxt = FacesContext.getCurrentInstance();
		Cookie[] cookieJar = getCookieJar(cxt);
		if (cookieJar != null) {
			for (Cookie c : cookieJar) {
				if (c.getName().equals(name)) {
					cookie = c;
					break;
				}
			}
		}
		
		return cookie;
	}
}
