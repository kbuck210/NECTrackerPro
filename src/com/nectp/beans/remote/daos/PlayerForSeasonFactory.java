package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;

public interface PlayerForSeasonFactory extends PlayerForSeasonService {

	public PlayerForSeason createPlayerForSeason(Player player, Season season, String nickname,
			String excelPrintName, Integer excelCol, boolean commish);
	
}