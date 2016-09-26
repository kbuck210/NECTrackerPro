package com.nectp.beans.remote.daos;

import java.util.List;

import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Week;

public interface PickService extends DataService<Pick> {

	public Pick selectPlayerPickForGame(PlayerForSeason player, Game game);
	
	public List<Pick> selectPlayerPicksForWeek(PlayerForSeason player, Week week);
	
}
