package com.nectp.beans.named.profile;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.ApplicationState;
import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.named.NextGameBean;
import com.nectp.beans.named.profile.ProfileBean;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.jpa.entities.TeamForSeason;

@Named(value="teamProfileBean")
@ViewScoped
public class TeamProfileBean extends ProfileBean<TeamForSeason> {
	private static final long serialVersionUID = 4833535029936688693L;

	private String teamAbbr;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private TeamForSeasonService tfsService;
	
	@Inject
	private TeamChartBean teamChart;
	
	@Inject
	private TeamStatsBean teamStats;
	
	@Inject
	private NextGameBean nextGame;
	
	@Inject
	private ApplicationState appState;
	
	/** Set teamAbbr is called from the f:viewParam pre-render method
	 * 
	 * @param teamAbbr the abbreviation of the TeamForSeason entity to select
	 */
	public void setTeamAbbr(String teamAbbr) {
		this.teamAbbr = teamAbbr;
	}
	
	public String getTeamAbbr() {
		return teamAbbr;
	}
	
	@Override
	public void setProfileEntity(TeamForSeason profileEntity) {
		this.profileEntity = profileEntity;
	}

	@Override
	public void initialize() {
		Integer seasonNumber = null;
		try {
			seasonNumber = Integer.parseInt(nec);
			season = seasonService.selectById(seasonNumber);
		} catch (NumberFormatException e) {
			log.warning("Invalid numerical format for season number: " + nec + " - defaulting to current season");
			log.warning(e.getMessage());
			season = appState.getCurrentSeason();
			if (season == null) {
				season = seasonService.selectCurrentSeason();
			}
		}
		
		try {
			TeamForSeason team = tfsService.selectTfsByAbbrSeason(teamAbbr, season);
			setProfileEntity(team);
			teamChart.setProfileEntity(team);
			teamStats.setProfileEntity(team);
			nextGame.setDisplayTeam(team);
		} catch (NoExistingEntityException e) {
			log.severe("No TeamForSeason entity found for abbr: " + teamAbbr + " in season: " + season);
			log.severe(e.getMessage());
		}
	}
	
}