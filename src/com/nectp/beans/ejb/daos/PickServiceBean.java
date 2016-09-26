package com.nectp.beans.ejb.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.PickService;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Week;

@Stateless
public class PickServiceBean extends DataServiceBean<Pick> implements PickService {
	private static final long serialVersionUID = -8057607122514555934L;

	@Override
	public Pick selectPlayerPickForGame(PlayerForSeason player, Game game) {
		Logger log = Logger.getLogger(PickServiceBean.class.getName());
		Pick pick = null;
		if (player == null || game == null) {
			log.severe("Player/Game not defined, can not retrieve pick!");
		}
		else {
			TypedQuery<Pick> pq = em.createNamedQuery("Pick.selectPlayerPickForGame", Pick.class);
			pq.setParameter("playerId", player.getAbstractTeamForSeasonId());
			pq.setParameter("gameId", game.getGameId());
			try {
				pick = pq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple picks found for " + player.getNickname() + " for gameId: " + game.getGameId());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No picks found for " + player.getNickname() + " for gameId: " + game.getGameId());
				log.warning(e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.severe("Exception caught retrieving pick: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return pick;
	}

	@Override
	public List<Pick> selectPlayerPicksForWeek(PlayerForSeason player, Week week) {
		Logger log = Logger.getLogger(PickServiceBean.class.getName());
		List<Pick> picks = null;
		if (player == null || week == null) {
			log.severe("Player/Week not defined, can not retrieve pick!");
		}
		else {
			TypedQuery<Pick> pq = em.createNamedQuery("Pick.selectPlayerPicksForWeek", Pick.class);
			pq.setParameter("playerId", player.getAbstractTeamForSeasonId());
			pq.setParameter("weekId", week.getWeekId());
			try {
				picks = pq.getResultList();
			} catch (Exception e) {
				log.severe("Exception caught retrieving picks: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return picks != null ? picks : new ArrayList<Pick>();
	}

}
