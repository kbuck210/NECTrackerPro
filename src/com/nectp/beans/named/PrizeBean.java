package com.nectp.beans.named;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.PrizeForSeason;

@Named(value="prizeBean")
@RequestScoped
public class PrizeBean implements Serializable {
	private static final long serialVersionUID = 1205966631688975355L;
	
	private PrizeForSeason prize;
	private PlayerForSeason winner;
	
	private String title;
	private String id;
	
	private String winnerIcon;
	private String nonWinnerIcon;
	
	public PrizeForSeason getPrize() {
		return prize;
	}
	
	public void setPrize(PrizeForSeason prize) {
		this.prize = prize;
		this.title = prize.getPrize().getPrizeType().toString();
		this.id = "prize" + prize.getPrizeForSeasonId();
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getId() {
		return id;
	}
	
	public PlayerForSeason getWinner() {
		return winner;
	}
	
	public void setWinner(PlayerForSeason winner) {
		this.winner = winner;
	}

	public String getWinnerIcon() {
		return winnerIcon;
	}

	public void setWinnerIcon(String winnerIcon) {
		this.winnerIcon = winnerIcon;
	}

	public String getNonWinnerIcon() {
		return nonWinnerIcon;
	}

	public void setNonWinnerIcon(String nonWinnerIcon) {
		this.nonWinnerIcon = nonWinnerIcon;
	}
	
	public String getIcon(PlayerForSeason player) {
		String icon = nonWinnerIcon;
		if (player != null && player.equals(winner)) {
			icon = winnerIcon;
		}
		
		return icon;
	}
}
