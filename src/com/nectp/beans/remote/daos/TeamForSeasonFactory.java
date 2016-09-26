package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Team;
import com.nectp.jpa.entities.TeamForSeason;

public interface TeamForSeasonFactory extends TeamForSeasonService {

	public TeamForSeason createTeamForSeason(Team team, Season season, Division division, Stadium stadium, 
			String nickname, String excelPrintName, String homeHelmet, String awayHelmet);
	
}
