package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;

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
	public TeamForSeason createTeamForSeason(Team team, Season season, String teamAbbr, String teamName, 
			String teamCity, Division division, Stadium stadium, String nickname, 
			String excelPrintName, String homeHelmet, String awayHelmet) {

		Logger log = Logger.getLogger(TeamForSeasonFactoryBean.class.getName());
		TeamForSeason tfs = null;
		if (team == null || season == null || division == null || stadium == null || 
				nickname == null || excelPrintName == null || homeHelmet == null || awayHelmet == null) {
			log.severe("Parameters not defined, can not create TeamForSeason!");
		}
		else {
			//	Check to see if a TFS already exists, otherwise create one
			try {
				tfs = selectTfsByTeamSeason(team, season);
				
				//	Check whether the team for season needs to be updated
				boolean update = false;
				if (!tfs.getTeamAbbr().equals(teamAbbr)) {
					tfs.setTeamAbbr(teamAbbr);
					update = true;
				}
				if (!tfs.getTeamCity().equals(teamCity)) {
					tfs.setTeamCity(teamCity);
					update = true;
				}
				if (!tfs.getName().equals(teamName)) {
					tfs.setName(teamName);
					update = true;
				}
				if (!tfs.getDivision().equals(division)) {
					tfs.getDivision().removeTeamHistory(tfs);
					tfs.setDivision(division);
					division.addTeamHistory(tfs);
					update = true;
				}
				if (!tfs.getStadium().equals(stadium)) {
					tfs.getStadium().removeTeamUsingStadium(tfs);
					tfs.setStadium(stadium);
					stadium.addTeamUsingStadium(tfs);
					update = true;
				}
				if (!tfs.getNickname().equals(nickname)) {
					tfs.setNickname(nickname);
					update = true;
				}
				if (!tfs.getExcelPrintName().equals(excelPrintName)) {
					tfs.setExcelPrintName(excelPrintName);
					update = true;
				}
				if (!tfs.getHomeHelmetUrl().equals(homeHelmet)) {
					tfs.setHomeHelmetUrl(homeHelmet);
					update = true;
				}
				if (!tfs.getAwayHelmetUrl().equals(awayHelmet)) {
					tfs.setAwayHelmetUrl(awayHelmet);
					update = true;
				}
				
				if (update) {
					update(tfs);
				}
				
			} catch (NoExistingEntityException e) {
				tfs = new TeamForSeason();
				
				tfs.setTeamAbbr(teamAbbr);
				tfs.setTeamCity(teamCity);
				tfs.setName(teamName);
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

