package com.nectp.beans.remote.daos;

import java.util.List;

import com.nectp.jpa.entities.Email;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.Season;

public interface EmailService extends DataService<Email> {

	public List<Email> selectByAddress(String emailAddress);
	
	public List<Email> selectAllByPlayer(Player player);
	
	public Email selectEmailForPlayer(String emailAddress, Player player);
	
	public List<Email> selectAllRecipientsBySeason(Season season);
}
