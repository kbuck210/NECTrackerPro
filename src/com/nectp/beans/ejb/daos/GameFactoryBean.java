package com.nectp.beans.ejb.daos;

import java.util.Calendar;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.nectp.beans.remote.daos.GameFactory;
import com.nectp.beans.remote.daos.RecordFactory;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Game.GameStatus;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Stateless
public class GameFactoryBean extends GameServiceBean implements GameFactory {
	private static final long serialVersionUID = -6326641654266855469L;

	@EJB
	private RecordFactory recordFactory;
	
	@Override
	public Game createGameInWeek(Week week, TeamForSeason homeTeam, TeamForSeason awayTeam, Integer homeScore,
			Integer awayScore, String spread1, String spread2, Calendar gameDate, GameStatus gameStatus,
			Boolean homeFavoredSpread1, Boolean homeFavoredSpread2, String timeRemaining, 
			String possession, boolean redzone, Stadium stadium) 
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
				
				//	Check whether the game needs to be updated
				boolean update = false;
				if (game.getHomeScore() != homeScore) {
					game.setHomeScore(homeScore);
					update = true;
				}
				if (game.getAwayScore() != awayScore) {
					game.setAwayScore(awayScore);
					update = true;
				}
				if (!game.getSpread1().equals(spread1)) {
					game.setSpread1(spread1);
					update = true;
				}
				if (game.getSpread2() != null) {
					if (!game.getSpread2().equals(spread2)) {
						game.setSpread2(spread2);
						update = true;
					}
					else if (spread2 == null) {
						game.setSpread2(null);
						update = true;
					}
				}
				else if (spread2 != null) {
					game.setSpread2(spread2);
					update = true;
				}
				if (!game.getGameDate().equals(gameDate)) {
					game.setGameDate(gameDate);
					update = true;
				}
				if (!game.getGameStatus().equals(gameStatus)) {
					game.setGameStatus(gameStatus);
					update = true;
				}
				if (game.getHomeFavoredSpread1() != null && homeFavoredSpread1 != null) {
					if (!game.getHomeFavoredSpread1().equals(homeFavoredSpread1)) {
						game.setHomeFavoredSpread1(homeFavoredSpread1);
						update = true;
					}
				}
				//	Use XOR to determine if one or the other has null spread, if both null, do not update
				else if (game.getHomeFavoredSpread1() == null ^ homeFavoredSpread1 == null) {
					game.setHomeFavoredSpread1(homeFavoredSpread1);
					update = true;
				}
				if (game.getHomeFavoredSpread2() != null && homeFavoredSpread2 != null) {
					if (!game.getHomeFavoredSpread2().equals(homeFavoredSpread2)) {
						game.setHomeFavoredSpread2(homeFavoredSpread2);
						update = true;
					}
				}
				//	Use XOR to determine if one or the other has null spread, if both null, do not update
				else if (game.getHomeFavoredSpread2() == null ^ homeFavoredSpread2 == null) {
					game.setHomeFavoredSpread2(homeFavoredSpread2);
					update = true;
				}
				if (!game.getTimeRemaining().equals(timeRemaining)) {
					game.setTimeRemaining(timeRemaining);
					update = true;
				}
				if (possession != null && game.getPossession() == null) {
					game.setPossession(possession);
					update = true;
				}
				else if (possession == null && game.getPossession() != null) {
					game.setPossession(possession);
					update = true;
				}
				else if (possession != null && game.getPossession() != null && !game.getPossession().equals(possession)) {
					game.setPossession(possession);
					update = true;
				}
				if (game.getRedZone() != redzone) {
					game.setRedZone(redzone);
					update = true;
				}
				if (!game.getStadium().equals(stadium)) {
					game.getStadium().removeGamePlayedInStadium(game);
					game.setStadium(stadium);
					stadium.addGamePlayedInStadium(game);
					update = true;
				}
				
				recordFactory.createWeekRecordForGame(game, homeTeam);
				recordFactory.createWeekRecordForGame(game, awayTeam);
				
				if (update) {
					boolean updateSuccess = update(game);
					return updateSuccess ? game : null;
				}
				
			} catch (NoExistingEntityException e) {
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
				
				recordFactory.createWeekRecordForGame(game, homeTeam);
				recordFactory.createWeekRecordForGame(game, awayTeam);
				
				boolean success = insert(game);
				if (!success) {
					game = null;
				}
			}
		}
		
		return game;
	}

}

