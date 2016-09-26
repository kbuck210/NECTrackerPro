package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Player;

public interface PlayerFactory extends PlayerService {

	public Player createPlayer(String playerName, Integer sinceYear, String avatarUrl);
	
}
