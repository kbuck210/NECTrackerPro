package com.nectp.beans.ejb;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;

import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.webtools.CookieFactory;

import java.io.Serializable;
import java.util.logging.Logger;

@Named(value="applicationStateBean")
@SessionScoped
public class ApplicationState implements Serializable {

	private static final long serialVersionUID = -1652402098414950087L;

	private Player user;
	
	private PlayerForSeason instance;
	
	private Season currentSeason;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private PlayerForSeasonService pfsService;
	
	@Inject
	private LoginService loginService;
	
	private Logger log;
	
	public ApplicationState() {
		log = Logger.getLogger(ApplicationState.class.getName());
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
		return instance;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(Player user) {
		this.user = user;
		if (user != null && currentSeason != null) {
			System.out.println("In appstate, user set to: " + user.getName());
			try {
				instance = pfsService.selectPlayerInSeason(user, currentSeason);
				System.out.println("Got an instance for " + instance.getNickname());
			} catch (NoExistingEntityException e) {
				System.out.println("no instance found for " + user.getName());
			}
		}
		else if (user == null) {
			instance = null;
		}
	}
	
	public void setUserInstance(PlayerForSeason instance) {
		if (instance == null || !instance.getPlayer().equals(user)) {
			log.severe("Specified player instance is null or does not match the logged in user!");
		}
		else {
			this.instance = instance;
		}
	}

	public Season getCurrentSeason() {
		return currentSeason;
	}
	
	public void setCurrentSeason(Season currentSeason) {
		this.currentSeason = currentSeason;
	}
	
	public String getSeasonNumber() {
		return currentSeason != null ? currentSeason.getSeasonNumber().toString() : "";
	}
	
	public String getInstanceId() {
		return instance != null ? instance.getAbstractTeamForSeasonId().toString() : "";
	}
}
