package com.nectp.beans.remote.daos;

import java.util.Calendar;

import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Game.GameStatus;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

public interface GameFactory extends GameService {

	public Game createGameInWeek(Week week, 
								 TeamForSeason homeTeam, 
								 TeamForSeason awayTeam, 
								 Integer homeScore, 
								 Integer awayScore, 
								 String spread1, 
								 String spread2, 
								 Calendar gameDate,
								 GameStatus gameStatus,
								 Boolean homeFavoredSpread1,
								 Boolean homeFavoredSpread2,
								 String timeRemaining,
								 String possession,
								 boolean redzone,
								 Stadium stadium);
	
}