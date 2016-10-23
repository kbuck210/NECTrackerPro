package com.nectp.beans.ejb;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.servlet.http.Cookie;

import com.nectp.beans.remote.daos.PlayerService;
import com.nectp.jpa.entities.Player;
import com.nectp.webtools.CookieFactory;

/**
 * Session Bean implementation class LoginService
 */
@Stateless
public class LoginService implements Serializable {
	private static final long serialVersionUID = -7299567040921684375L;

	@EJB
	private PlayerService playerService;
	
	@Inject
	private ApplicationState appState;
	
	private String messages;
	
	private Logger log = Logger.getLogger(LoginService.class.getName());
	
	public Player login(String email, String password) {
		return login(email, password, false);
	}
	
	public Player login(Cookie cookie) {
		String loginInfo = cookie.getValue();
		String[] loginParts = loginInfo.split(" ");
		String username = null;
		String password = null;
		if (loginParts.length == 2) {
			username = loginParts[0];
			if (username.length() > "username:".length()) {
				username = username.substring("username:".length());
			}
			password = loginParts[1];
			if (password.length() > "password:".length()) {
				password = password.substring("password:".length());
			}
		}
		
		return login(username, password, true);
	}
    
	private Player login(String email, String password, boolean fromCookie) {
		Player player = null;
		if (email == null || password == null) {
			log.severe("Email/Password not defined! can not login.");
		}
		else {
			try {
				player = playerService.selectUserByPrimaryEmail(email);
			} catch (NoResultException e) {
				messages = "Invalid Username/Email. Please try again";
			}
			if (player != null) {
				if (player.getPassword().equals(password)) {
					//	Sets the found user as the currently logged in user
					appState.setUser(player);
					log.info("User: " + player.getName() + " set in appState");
					//	Save a persistent cookie to keep the player logged in until log out selected (or max age - 30 weeks - reached)
					int seconds30weeks = 30 * 7 * 24 * 60 * 60;
					String cookieVal = "username:" + email + " password:" + password;
					CookieFactory.giveCookie("loginCookie", cookieVal, seconds30weeks);
				}
			}
		}
		
		return player;
	}
	
	public void logout() {
		appState.setUser(null);
		Cookie cookie = CookieFactory.recieveCookie("loginCookie");
		if (cookie != null) {
			cookie.setMaxAge(0);
		}
	}
	
	public String getMessages() {
		return messages;
	}
}
