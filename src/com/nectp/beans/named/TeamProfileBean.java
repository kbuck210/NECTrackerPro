package com.nectp.beans.named;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.context.FacesContext;

import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;

@Stateless
public class TeamProfileBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private TeamForSeasonService tfsService;
	
	private Season season;
	private TeamForSeason displayTeam;
	
	private Logger log;
	
	public TeamProfileBean() {
		log = Logger.getLogger(TeamProfileBean.class.getName());
	}
	
	@PostConstruct
	public void init() {
		Map<String, String> paramMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String teamAbbr = paramMap.get("teamAbbr");
		String seasonNum = paramMap.get("nec");
		Integer seasonNumber = null;
		try {
			seasonNumber = Integer.parseInt(seasonNum);
			season = seasonService.selectById(seasonNumber);
		} catch (NumberFormatException e) {
			log.warning("Invalid season number parameter, returning the current season.");
			log.warning(e.getMessage());
			season = seasonService.selectCurrentSeason();
		}
		try {
			displayTeam = tfsService.selectTfsByAbbrSeason(teamAbbr, season);
		} catch (NoExistingEntityException e) {
			//	Catch
		}
	}
	
	public TeamForSeason getDisplayTeam() {
		return displayTeam;
	}
	
//	public String getName() {
//		return displayTeam != null ? displayTeam.getName() : "N/a";
//	}
//	
//	public String getTeamCity() {
//		return displayTeam != null ? displayTeam.getTeamCity() : "N/a";
//	}
//	
//	public String getAvatar() {
//		return displayTeam != null ? displayTeam.getHomeHelmetUrl() : "img/home-helmet.png";
//	}
//	
//	public String getSeasonNumber() {
//		return season != null ? season.getSeasonNumber().toString() : "N/a";
//	}
}
