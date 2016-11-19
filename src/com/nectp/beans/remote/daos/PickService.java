package com.nectp.beans.remote.daos;

import java.util.List;

import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Week;

public interface PickService extends DataService<Pick> {

	public Pick selectPlayerPickForGameForType(PlayerForSeason player, Game game, NEC pickFor);
	
	public List<Pick> selectPlayerPicksForWeekForType(PlayerForSeason player, Week week, NEC pickFor);

	public List<Pick> selectPicksForGameByType(Game game, NEC pickType);

	public List<Pick> selectPlayerPicksForType(PlayerForSeason player, NEC pickType);
	
	public List<Pick> selectAllPicksInWeek(Week week);
	
	public void lockPicksInWeek(Week week);
}

