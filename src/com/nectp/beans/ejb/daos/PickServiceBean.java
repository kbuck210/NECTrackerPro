package com.nectp.beans.ejb.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.PickService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Week;

@Stateless
public class PickServiceBean extends DataServiceBean<Pick> implements PickService {
	private static final long serialVersionUID = -8057607122514555934L;

	@Override
	public Pick selectPlayerPickForGameForType(PlayerForSeason player, Game game, NEC pickFor) {
		Logger log = Logger.getLogger(PickServiceBean.class.getName());
		Pick pick = null;
		if (player == null || game == null) {
			log.severe("Player/Game not defined, can not retrieve pick!");
		}
		else {
			TypedQuery<Pick> pq = em.createNamedQuery("Pick.selectPlayerPickForGameForType", Pick.class);
			pq.setParameter("playerId", player.getAbstractTeamForSeasonId());
			pq.setParameter("gameId", game.getGameId());
			pq.setParameter("pickType", pickFor.ordinal());
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
	public List<Pick> selectPlayerPicksForWeekForType(PlayerForSeason player, Week week, NEC pickFor) {
		Logger log = Logger.getLogger(PickServiceBean.class.getName());
		List<Pick> picks = null;
		if (player == null || week == null) {
			log.severe("Player/Week not defined, can not retrieve pick!");
		}
		else {
			TypedQuery<Pick> pq = em.createNamedQuery("Pick.selectPlayerPicksForWeekForType", Pick.class);
			pq.setParameter("playerId", player.getAbstractTeamForSeasonId());
			pq.setParameter("weekId", week.getWeekId());
			pq.setParameter("pickType", pickFor.ordinal());
			try {
				picks = pq.getResultList();
			} catch (Exception e) {
				log.severe("Exception caught retrieving picks: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return picks != null ? picks : new ArrayList<Pick>();
	}

	@Override
	public List<Pick> selectPicksForGameByType(Game game, NEC pickType) {
		Logger log = Logger.getLogger(PickServiceBean.class.getName());
		List<Pick> picks;
		if (game == null || pickType == null) {
			log.severe("Game/Type not defined, can not retrieve picks!");
			picks = new ArrayList<Pick>();
		}
		else {
			TypedQuery<Pick> pq = em.createNamedQuery("Pick.selectPicksForGameByType", Pick.class);
			pq.setParameter("gameId", game.getGameId());
			pq.setParameter("pickType", pickType.ordinal());
			try {
				picks = pq.getResultList();
			} catch (Exception e) {
				log.severe("Exception caught retrieving list of picks: " + e.getMessage());
				e.printStackTrace();
				picks = new ArrayList<Pick>();
			}
		}
		
		return picks;
	}

	@Override
	public List<Pick> selectPlayerPicksForType(PlayerForSeason player, NEC pickType) {
		Logger log = Logger.getLogger(PickServiceBean.class.getName());
		List<Pick> picks;
		if (player == null || pickType == null) {
			log.severe("Player/Type not defined, can not retrieve picks!");
			picks = new ArrayList<Pick>();
		}
		else {
			TypedQuery<Pick> pq = em.createNamedQuery("Pick.selectPlayerPicksForType", Pick.class);
			pq.setParameter("atfsId", player.getAbstractTeamForSeasonId());
			pq.setParameter("pickType", pickType.ordinal());
			try {
				picks = pq.getResultList();
			} catch (Exception e) {
				log.severe("Exception caught retrieving list of picks: " + e.getMessage());
				e.printStackTrace();
				picks = new ArrayList<Pick>();
			}
		}
		
		return picks;
	}
}