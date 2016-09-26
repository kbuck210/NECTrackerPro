package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.TeamForSeasonFactory;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Team;
import com.nectp.jpa.entities.TeamForSeason;

@Stateless
public class TeamForSeasonFactoryBean extends TeamForSeasonServiceBean implements TeamForSeasonFactory {
	private static final long serialVersionUID = -6583194394407751179L;

	@Override
	public TeamForSeason createTeamForSeason(Team team, Season season, Division division, Stadium stadium, String nickname, String excelPrintName, String homeHelmet,
			String awayHelmet) {

		Logger log = Logger.getLogger(TeamForSeasonFactoryBean.class.getName());
		TeamForSeason tfs = null;
		if (team == null || season == null || division == null || stadium == null || 
				nickname == null || excelPrintName == null || homeHelmet == null || awayHelmet == null) {
			log.severe("Parameters not defined, can not create TeamForSeason!");
		}
		else {
			//	Check to see if a TFS already exists, otherwise create one
			try {
				tfs = selectTfsByAbbr(team.getTeamAbbr(), season);
			} catch (NoResultException e) {
				tfs = new TeamForSeason();
				
				tfs.setNickname(nickname);
				tfs.setExcelPrintName(excelPrintName);
				tfs.setHomeHelmetUrl(homeHelmet);
				tfs.setAwayHelmetUrl(awayHelmet);
				
				tfs.setTeam(team);
				team.addTeamInstance(tfs);
				
				tfs.setSeason(season);
				season.addTeam(tfs);
				
				tfs.setDivision(division);
				division.addTeamHistory(tfs);
				
				tfs.setStadium(stadium);
				stadium.addTeamUsingStadium(tfs);
				
				boolean success = insert(tfs);
				if (!success) {
					tfs = null;
				}
			}
		}
		
		return tfs;
	}

}
