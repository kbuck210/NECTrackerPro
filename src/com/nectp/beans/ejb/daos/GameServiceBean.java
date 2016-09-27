package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.GameService;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Stateless
public class GameServiceBean extends DataServiceBean<Game> implements GameService {
	private static final long serialVersionUID = 5822019567154362536L;

	@Override
	public Game selectGameByTeamsWeek(TeamForSeason homeTeam, TeamForSeason awayTeam, Week week) {
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
				log.severe("Multiple games found for " + awayTeam.getTeam().getTeamAbbr() + 
						" at: " + homeTeam.getTeam().getTeamAbbr() + " in week: " + week.getWeekNumber());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No games found for " + awayTeam.getTeam().getTeamAbbr() + 
						" at: " + homeTeam.getTeam().getTeamAbbr() + " in week: " + week.getWeekNumber());
				log.warning(e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.severe("Exception caught retrieving game: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return game;
	}

	@Override
	public Game selectGameByTeamWeek(TeamForSeason team, Week week) {
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
				log.severe("Multiple games found for " + team.getTeam().getTeamAbbr() + 
						" in week: " + week.getWeekNumber());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No games found for " + team.getTeam().getTeamAbbr() + 
						" in week: " + week.getWeekNumber());
				log.warning(e.getMessage());
				throw e;
			} catch (Exception e) {
				log.severe("Exception caught retrieving game: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return game;
	}

}
