package com.nectp.beans.remote.daos;

import com.nectp.beans.remote.daos.DataService;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Team;
import com.nectp.jpa.entities.TeamForSeason;

public interface TeamForSeasonService extends DataService<TeamForSeason> {

	public TeamForSeason selectTfsByTeamSeason(Team team, Season season);
	
	public TeamForSeason selectTfsByAbbrSeason(String abbr, Season season);
	
}

