package com.nectp.beans.ejb.daos;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.GameService;
import com.nectp.beans.remote.daos.PickService;
import com.nectp.beans.remote.daos.RecordFactory;
import com.nectp.beans.remote.daos.WeekFactory;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Game.GameStatus;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Stateless
public class GameServiceBean extends DataServiceBean<Game> implements GameService {
	private static final long serialVersionUID = 5822019567154362536L;
	
	@EJB
	private RecordFactory recordFactory;
	
	@EJB
	private WeekFactory weekFactory;
	
	@EJB
	private PickService pickService;

	@Override
	public Game selectGameByTeamsWeek(TeamForSeason homeTeam, TeamForSeason awayTeam, Week week) throws NoExistingEntityException {
		Logger log = Logger.getLogger(GameServiceBean.class.getName());
		Game game = null;
		
		//	If any of the supplied parameters are null, can't run the query
		if (homeTeam == null || awayTeam == null || week == null) {
			log.severe("Teams/week not specified, can not retrieve game!");
		}
		else {
			TypedQuery<Game> gq = em.createNamedQuery("Game.selectGameByTeamsWeek", Game.class);
			gq.setParameter("homeTfsId", homeTeam.getAbstractTeamForSeasonId());
			gq.setParameter("awayTfsId", awayTeam.getAbstractTeamForSeasonId());
			gq.setParameter("weekId", week.getWeekId());
			try {
				game = gq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple games found for " + awayTeam.getTeamAbbr() + 
						" at: " + homeTeam.getTeamAbbr() + " in week: " + week.getWeekNumber());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No games found for " + awayTeam.getTeamAbbr() + 
						" at: " + homeTeam.getTeamAbbr() + " in week: " + week.getWeekNumber());
				log.warning(e.getMessage());
				throw new NoExistingEntityException(e);
			} catch (Exception e) {
				log.severe("Exception caught retrieving game: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return game;
	}

	@Override
	public Game selectGameByTeamWeek(TeamForSeason team, Week week) throws NoExistingEntityException {
		Logger log = Logger.getLogger(GameServiceBean.class.getName());
		Game game = null;
		
		//	If any of the supplied parameters are null, can't run the query
		if (team == null || week == null) {
			log.severe("Team/week not specified, can not retrieve game!");
		}
		else {
			TypedQuery<Game> gq = em.createNamedQuery("Game.selectGameByTeamWeek", Game.class);
			gq.setParameter("atfsId", team.getAbstractTeamForSeasonId());
			gq.setParameter("weekId", week.getWeekId());
			try {
				game = gq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple games found for " + team.getTeamAbbr() + 
						" in week: " + week.getWeekNumber());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No games found for " + team.getTeamAbbr() + 
						" in week: " + week.getWeekNumber());
				log.warning(e.getMessage());
				throw new NoExistingEntityException(e);
			} catch (Exception e) {
				log.severe("Exception caught retrieving game: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return game;
	}

	@Override
	public void updateGameComplete(Game game) {
		Logger log = Logger.getLogger(GameServiceBean.class.getName());
		if (game == null) {
			log.warning("No game specifed! can not update game.");
		}
		else {
			//	Update the status attributes
			game.setGameStatus(GameStatus.FINAL);
			game.setTimeRemaining("FINAL");
			
			Week week = game.getWeek();
			Subseason subseason = week.getSubseason();
			
			Boolean homeCoveredSpread1 = game.homeTeamCoveringSpread1();
			Boolean homeCoveredSpread2 = game.homeTeamCoveringSpread2();
			
			//	Get/Create & update the team records associated with the game
			TeamForSeason homeTeam = game.getHomeTeam();
			Record homeRecordForWeek = recordFactory.createWeekRecordForAtfs(week, homeTeam, subseason.getSubseasonType());
			
			TeamForSeason awayTeam = game.getAwayTeam();
			Record awayRecordForWeek = recordFactory.createWeekRecordForAtfs(week, awayTeam, subseason.getSubseasonType());
			
			List<Pick> picksForGame = game.getPicks();
			
			//	Update the records for the raw win/loss for each team
			TeamForSeason rawWinner = game.getWinner();
			
			if (rawWinner == null) {
				homeRecordForWeek.addTie();
				awayRecordForWeek.addTie();
			}
			else if (rawWinner.equals(homeTeam)) {
				homeRecordForWeek.addWin();
				awayRecordForWeek.addLoss();
			}
			else if (rawWinner.equals(awayTeam)) {
				homeRecordForWeek.addLoss();
				awayRecordForWeek.addWin();
			}
			
			//	Update the records against the spread1 for each team
			if (homeCoveredSpread1 == null) {
				homeRecordForWeek.addTieATS1();
				awayRecordForWeek.addTieATS1();
			}
			else if (homeCoveredSpread1) {
				homeRecordForWeek.addWinATS1();
				awayRecordForWeek.addLossATS1();
			}
			else {
				homeRecordForWeek.addLossATS1();
				awayRecordForWeek.addWinATS1();
			}
			
			//	Update the records against the spread2 for each team if a spread 2 exists
			if (game.getSpread2() != null) {
				if (homeCoveredSpread2 == null) {
					homeRecordForWeek.addTieATS2();
					awayRecordForWeek.addTieATS2();
				}
				else if (homeCoveredSpread2) {
					homeRecordForWeek.addWinATS2();
					awayRecordForWeek.addLossATS2();
				}
				else {
					homeRecordForWeek.addLossATS2();
					awayRecordForWeek.addWinATS2();
				}
			}
			
			
			//	Update the Team records
			recordFactory.update(homeRecordForWeek);
			recordFactory.update(awayRecordForWeek);
			
			//	Update player picks
			for (Pick p : picksForGame) {
				recordFactory.updateRecordForPlayerPick(p);
			}
			
			//	Update the game
			update(game);
			
			//	Update the week with the completed game
			weekFactory.updateWeekForGameComplete(week);
		}
	}
}
