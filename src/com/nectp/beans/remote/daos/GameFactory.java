package com.nectp.beans.remote.daos;

import java.math.BigDecimal;
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
								 BigDecimal spread1, 
								 BigDecimal spread2, 
								 Calendar gameDate,
								 GameStatus gameStatus,
								 Boolean homeFavoredSpread1,
								 Boolean homeFavoredSpread2,
								 String timeRemaining,
								 Stadium stadium);
	
}
