package com.nectp.beans.named.profile;

import java.util.List;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.ApplicationState;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.entities.Email;
import com.nectp.jpa.entities.PlayerForSeason;

@Named(value="playerProfileBean")
@ViewScoped
public class PlayerProfileBean extends ProfileBean<PlayerForSeason> {
	private static final long serialVersionUID = 3128743401536700273L;

	private String pfsId;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private PlayerForSeasonService pfsService;
	
	@Inject
	private PlayerChartBean playerChart;
	
	@Inject
	private PlayerStatsBean playerStats;
	
	@Inject
	private PlayerHistoryBean playerHistory;
	
	@Inject
	private ApplicationState appState;
	
	@Override
	public void setProfileEntity(PlayerForSeason profileEntity) {
		this.profileEntity = profileEntity;
	}

	@Override
	public void initialize() {
		Integer seasonNumber = null;
		try {
			seasonNumber = Integer.parseInt(nec);
			season = seasonService.selectById(seasonNumber);
		} catch (NumberFormatException e) {
			log.warning("Invalid numerical format for season number: " + nec + " - defaulting to current season.");
			log.warning(e.getMessage());
			season = seasonService.selectCurrentSeason();
		}
		
		Long playerId = null;
		try {
			playerId = Long.parseLong(pfsId);
		} catch (NumberFormatException e) {
			log.warning("Invalid numerical format for player id: " + pfsId + " - can't get player profile!");
			log.warning(e.getMessage());
			//	TODO: somehow redirect
		}
		PlayerForSeason player = pfsService.selectById(playerId);
		if (player != null) {
			setProfileEntity(player);
			playerChart.setProfileEntity(player);
			playerChart.setSummary("Summary Placeholder - it'll be really cool hopefully.");
			playerStats.setProfileEntity(player);
			playerHistory.setProfileEntity(player);
		}
		else {
			log.severe("No PlayerForSeason found for id: " + pfsId + " in season " + nec);
		}
	}
	
	public void setPfsId(String pfsId) {
		this.pfsId = pfsId;
	}

	public String getPfsId() {
		return pfsId;
	}
	
	public boolean getEditable() {
		PlayerForSeason user = appState.getUserInstance();
		if (user != null) {
			return user.getPlayer().equals(profileEntity.getPlayer());
		}
		else return false;
	}
	
	public String getDisableEdits() {
		return getEditable() ? "" : "disabled";
	}
	
	public String getPrimaryEmail() {
		if (profileEntity != null) {
			List<Email> emails = profileEntity.getPlayer().getEmails();
			if (!emails.isEmpty()) {
				return emails.get(0).getEmailAddress();
			}
		}
		return "";
	}
}
