package com.nectp.beans.named;

import java.text.DecimalFormat;
import java.util.List;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.TeamForSeason;

public class TeamPickHistoryBean {

	private TeamForSeason team;
	
	private PlayerForSeason player;
	
	private List<Pick> picksForTeam;
	
	private int timesPicked;
	private int totalCovered;
	private int playerCovered;
	private double playerWinPct;
	
	private DecimalFormat format = new DecimalFormat(".##");
	
	private void calculate() {
		for (Pick p : picksForTeam) {
			Game g = p.getGame();
			boolean playerPicked = p.getPlayer().equals(player);
			//	** NOTE ** win % is based on SPREAD1 coverings only
			boolean teamCovered = g.getWinnerATS1().equals(team);
			
			if (playerPicked) {
				timesPicked += 1;
			}
			if (teamCovered) {
				totalCovered += 1;
			}
			if (playerPicked && teamCovered) {
				playerCovered += 1;
			}
		}
		
		if (timesPicked != 0) {
			playerWinPct = (double) playerCovered / (double) timesPicked;
		}
		else {
			playerWinPct = 0;
		}
	}
	
	public void setTeam(TeamForSeason team) {
		this.team = team;
		this.picksForTeam = team.getPicksForTeam();
		if (player != null) {
			calculate();
		}
	}
	
	public void setPlayer(PlayerForSeason player) {
		this.player = player;
		if (team != null) {
			calculate();
		}
	}
	
	public String getTeamName() {
		return team.getName();
	}
	
	public String getTeamRecord() {
		RecordAggregator ragg = new RecordAggregator(team, false);
		ragg.setRecords(team.getRecords());
		
		String record = "(" + ragg.getRawWins() + "-" + ragg.getRawLosses();
		if (ragg.getRawTies() > 0) {
			record += "-" + ragg.getRawTies();
		}
		record += ")";
		
		return record;
	}
	
	public String getTeamHelmet() {
		return team.getHomeHelmetUrl();
	}
	
	public int getTimesPicked() {
		return timesPicked;
	}
	
	public int getTotalCovered() {
		 return totalCovered;
	}
	
	public int getPlayerCovered() {
		return playerCovered;
	}
	
	public String getWinPct() {
		return format.format(playerWinPct);
	}
}
