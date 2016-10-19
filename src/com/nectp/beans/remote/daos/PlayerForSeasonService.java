package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;

public interface PlayerForSeasonService extends DataService<PlayerForSeason> {

	public PlayerForSeason selectPlayerInSeason(Player player, Season season);
	
	public PlayerForSeason selectPlayerByExcelName(String excelName, Season season);
	
	public PlayerForSeason selectPlayerByExcelCol(int excelCol, Season season);
	
	public PlayerForSeason selectCommishBySeason(Season season);
}

