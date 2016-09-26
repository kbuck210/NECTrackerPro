package com.nectp.beans.named;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.Season;

@Named(value="homeBean")
@SessionScoped
public class HomeBean implements Serializable {
	private static final long serialVersionUID = 8438655382115867835L;

	private Player user;
	
	private Season season;
	
	@EJB
	private SeasonService seasonService;
	
	//	Check for state cookies
	public HomeBean() {
		
	}
	
	public Player getUser() {
		return user;
	}
	
	public void setUser(Player user) {
		this.user = user;
	}
	
	public Season getCurrentSeason() {
		return season;
	}
}
