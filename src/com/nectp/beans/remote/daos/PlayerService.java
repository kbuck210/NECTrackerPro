package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Player;

public interface PlayerService extends DataService<Player> {

	public Player selectUserByPrimaryEmail(String emailAddress);
	
	public Player selectPlayerByName(String playerName);
	
}
