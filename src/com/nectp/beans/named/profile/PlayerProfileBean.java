package com.nectp.beans.named.profile;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.named.RecordDisplay;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Week.WeekStatus;

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
	
//	private String createChartSummary() {
//		StringBuilder sb = new StringBuilder();
//		int activeIndex = playerChart.getActiveIndex();
//		Week currentWeek = season.getCurrentWeek();
//		if (currentWeek != null) {
//			Subseason subseason = currentWeek.getSubseason();
//			//	Create the summary based on the selected tab
//			NEC activeScope;
//			RecordDisplay scoreDisplay;
//			switch(activeIndex) {
//			case 0:
//				activeScope = NEC.FIRST_HALF;
//				scoreDisplay = playerStats.getFirstHalfDisplay();
//				break;
//			case 1:
//				activeScope = NEC.SECOND_HALF;
//				scoreDisplay = playerStats.getSecondHalfDisplay();
//				break;
//			case 2:
//				activeScope = NEC.PLAYOFFS;
//				scoreDisplay = playerStats.getPlayoffsDisplay();
//				break;
//			case 3:
//				activeScope = NEC.SEASON;
//				scoreDisplay = playerStats.getSeasonDisplay();
//				break;
//			case 4:
//				activeScope = NEC.ALL_TIME;
//				scoreDisplay = null;
//				break;
//			default:
//				return "";
//			}
//			
//			//	If the current week is within the active scope, tailor the summary to that
//			if (subseason.getSubseasonType().equals(activeScope)) {
//				if (currentWeek.equals(subseason.getFirstWeek())) {
//					//	Check if the week is already over
//					if (currentWeek.getWeekStatus().equals(WeekStatus.COMPLETED)) {
//						if (scoreDisplay)
//					}
//					else {
//						
//					}
//				}
//			}
//		}
//	}
}
