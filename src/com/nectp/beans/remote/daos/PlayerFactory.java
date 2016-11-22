package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Player;

public interface PlayerFactory extends PlayerService {

	public Player createPlayer(String playerName, boolean admin, Integer sinceYear, String avatarUrl);
	
}
