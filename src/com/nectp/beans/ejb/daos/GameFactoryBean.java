package com.nectp.beans.ejb.daos;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.GameFactory;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Game.GameStatus;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Stateless
public class GameFactoryBean extends GameServiceBean implements GameFactory {
	private static final long serialVersionUID = -6326641654266855469L;

	@Override
	public Game createGameInWeek(Week week, TeamForSeason homeTeam, TeamForSeason awayTeam, Integer homeScore,
			Integer awayScore, BigDecimal spread1, BigDecimal spread2, Calendar gameDate, GameStatus gameStatus,
			Boolean homeFavoredSpread1, Boolean homeFavoredSpread2, String timeRemaining, Stadium stadium) 
	{
		Logger log = Logger.getLogger(GameFactoryBean.class.getName());
		Game game = null;
		if (week == null || homeTeam == null || awayTeam == null || 
				gameDate == null || stadium == null || timeRemaining == null) {
			log.severe("Parameters not specified, can not create Game!");
		}
		else {
			//	Attempt to select the game from the week with the two teams
			try {
				game = selectGameByTeamsWeek(homeTeam, awayTeam, week);
			} catch (NoResultException e) {
				game = new Game();
				game.setHomeTeam(homeTeam);
				homeTeam.addHomeGame(game);
				
				game.setAwayTeam(awayTeam);
				awayTeam.addAwayGame(game);
				
				game.setWeek(week);
				week.addGame(game);
				
				game.setStadium(stadium);
				stadium.addGamePlayedInStadium(game);
				
				game.setHomeScore(homeScore);
				game.setAwayScore(awayScore);
				game.setSpread1(spread1);
				game.setSpread2(spread2);
				game.setGameDate(gameDate);
				game.setGameStatus(gameStatus);
				game.setHomeFavoredSpread1(homeFavoredSpread1);
				game.setHomeFavoredSpread2(homeFavoredSpread2);
				game.setTimeRemaining(timeRemaining);
				
				boolean success = insert(game);
				if (!success) {
					game = null;
				}
			}
		}
		
		return game;
	}

}
