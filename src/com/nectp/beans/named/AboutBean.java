package com.nectp.beans.named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.ApplicationState;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Season;

@Named(value="aboutBean")
@RequestScoped
public class AboutBean implements Serializable {
	private static final long serialVersionUID = -1087836409653305546L;

	@Inject
	private ApplicationState appState;
	
	private Season season;
	
	private String firstHalfWeeks;
	private String secondHalfWeeks;
	private String minPicks;
	private String maxPicks;
	private String wltValue;
	
	private List<PrizeForSeason> prizes;
	
	@PostConstruct
	public void init() {
		season = appState.getCurrentSeason();
		
		if (season != null) {
			firstHalfWeeks = "1 - " + (season.getSecondHalfStartWeek() - 1);
			secondHalfWeeks = season.getSecondHalfStartWeek() + " - " + (season.getPlayoffStartWeek() - 1);
			minPicks = season.getMinPicks() != null ? minPicks = season.getMinPicks().toString() : "Pick 'em All!";
			maxPicks = season.getMaxPicks() != null ? maxPicks = season.getMaxPicks().toString() : "Max the Max!";
			wltValue = "+" + season.getWinValue() + "/-" + season.getLossValue() + "/" + season.getTieValue(); 
			
			prizes = season.getPrizes();
		}
		else {
			firstHalfWeeks = "N/a";
			secondHalfWeeks = "N/a";
			minPicks = "N/a";
			maxPicks = "N/a";
			wltValue = "N/a";
			prizes = new ArrayList<PrizeForSeason>();
		}
	}
	
	public String getFirstHalfWeeks() {
		return firstHalfWeeks;
	}
	
	public String getSecondHalfWeeks() {
		return secondHalfWeeks;
	}
	
	public String getMinPicks() {
		return minPicks;
	}
	
	public String getMaxPicks() {
		return maxPicks;
	}
	
	public String getWltValue() {
		return wltValue;
	}
	
	public List<PrizeForSeason> getPrizes() {
		return prizes;
	}
}
