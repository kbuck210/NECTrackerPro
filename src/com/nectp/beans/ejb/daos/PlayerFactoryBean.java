package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.PlayerFactory;
import com.nectp.jpa.entities.Player;

@Stateless
public class PlayerFactoryBean extends PlayerServiceBean implements PlayerFactory {
	private static final long serialVersionUID = -1508918084157632909L;

	@Override
	public Player createPlayer(String playerName, Integer sinceYear, String avatarUrl) {
		Logger log = Logger.getLogger(PlayerFactoryBean.class.getName());
		Player player = null;
		if (playerName == null) {
			log.severe("Name not specified! Can not create player");
		}
		else {
			//	Check whether player already exists before creating
			try {
				player = selectPlayerByName(playerName);
			} catch (NoResultException e) {
				player = new Player();
				player.setTeamName(playerName);
				player.setSinceYear(sinceYear);
				player.setAvatarUrl(avatarUrl);
				player.setPassword(playerName);	//	Default password is player name
				
				boolean success = insert(player);
				if (!success) {
					player = null;
				}
			}
		}
		
		return player;
	}

}
