package com.nectp.beans.remote.daos;

import com.nectp.beans.remote.daos.DataService;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;

public interface TeamForSeasonService extends DataService<TeamForSeason> {

	public TeamForSeason selectTfsByAbbr(String teamAbbr, Season season);
	
}
