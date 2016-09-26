package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;

public interface PlayerForSeasonService extends DataService<PlayerForSeason> {

	public PlayerForSeason selectPlayerInSeason(Player player, Season season);
	
}
