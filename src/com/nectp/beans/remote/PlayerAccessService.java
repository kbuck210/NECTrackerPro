package com.nectp.beans.remote;

import javax.ejb.Remote;

import com.nectp.jpa.entities.Email;
import com.nectp.jpa.entities.Player;

@Remote
public interface PlayerAccessService {

	public Player getPlayer(String emailAddress);
	
	public Email getPrimaryEmail(Player player);
	
}
