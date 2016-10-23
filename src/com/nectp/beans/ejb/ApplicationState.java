package com.nectp.beans.ejb;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.servlet.http.Cookie;

import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.webtools.CookieFactory;

import java.io.Serializable;

@Named
@SessionScoped
public class ApplicationState implements Serializable {

	private static final long serialVersionUID = -1652402098414950087L;

	private Player user;
	
	private Season currentSeason;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private PlayerForSeasonService pfsService;
	
	@Inject
	private LoginService loginService;
	
	public ApplicationState() {
	}
	
	@PostConstruct
	public void init() {
		//	TODO: check cookies for state variables
		currentSeason = seasonService.selectCurrentSeason();

		Cookie cookie = CookieFactory.recieveCookie("loginCookie");
		if (cookie != null) {
			user = loginService.login(cookie);
		}
	}

	/**
	 * @return the user
	 */
	public Player getUser() {
		return user;
	}
	
	public PlayerForSeason getUserInstance() {
		PlayerForSeason userInstance = null;
		if (user != null && currentSeason != null) {
			try {
				userInstance = pfsService.selectPlayerInSeason(user, currentSeason);
			} catch (NoResultException e) {
				/* eat the exception */
			}
		}
		return userInstance;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(Player user) {
		this.user = user;
		System.out.println("In appstate, user set to: " + user.getName());
	}

	public Season getCurrentSeason() {
		return currentSeason;
	}
	
	public void setCurrentSeason(Season currentSeason) {
		this.currentSeason = currentSeason;
	}
	
}
