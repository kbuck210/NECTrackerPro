package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Email;
import com.nectp.jpa.entities.Player;

public interface EmailFactory extends EmailService {

	public Email createEmailForPlayer(Player player, String emailAddress, boolean primary, boolean emailsRequested);
	
}
