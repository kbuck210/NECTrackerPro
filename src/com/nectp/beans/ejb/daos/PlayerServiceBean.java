package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.PlayerService;
import com.nectp.jpa.entities.Player;

@Stateless
public class PlayerServiceBean extends DataServiceBean<Player> implements PlayerService {
	private static final long serialVersionUID = -366842462668067744L;

	@Override
	public Player selectUserByPrimaryEmail(String emailAddress) {
		Logger log = Logger.getLogger(PlayerServiceBean.class.getName());
		Player player = null;
		if (emailAddress == null) {
			log.severe("Player email address not specified! can not select player.");
		}
		else {
			TypedQuery<Player> pq = em.createNamedQuery("Email.selectUserByPrimaryEmail", Player.class);
			pq.setParameter("emailAddress", emailAddress);
			try {
				player = pq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple users found for primary email: " + emailAddress);
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No users found for primary email: " + emailAddress);
				log.warning(e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.severe("Exception caught retrieving user: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return player;
	}

	@Override
	public Player selectPlayerByName(String playerName) {
		Logger log = Logger.getLogger(PlayerServiceBean.class.getName());
		Player player = null;
		if (playerName == null) {
			log.severe("Player name not specified! can not select player.");
		}
		else {
			TypedQuery<Player> pq = em.createNamedQuery("Player.selectPlayerByName", Player.class);
			pq.setParameter("playerName", playerName);
			try {
				player = pq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple players found for name: " + playerName);
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No players found for name: " + playerName);
				log.warning(e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.severe("Exception caught retrieving player: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return player;
	}

}
