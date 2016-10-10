package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.EmailFactory;
import com.nectp.jpa.entities.Email;
import com.nectp.jpa.entities.Player;

@Stateless
public class EmailFactoryBean extends EmailServiceBean implements EmailFactory {
	private static final long serialVersionUID = 4198398562301600167L;

	@Override
	public Email createEmailForPlayer(Player player, String emailAddress, boolean primary, boolean emailsRequested) {
		Logger log = Logger.getLogger(EmailFactoryBean.class.getName());
		Email email = null;
		
		if (player == null || emailAddress == null) {
			log.severe("No player/address specified, can not create email.");
		}
		else {
			//	Check whether the specified email already exists
			try {
				email = selectEmailForPlayer(emailAddress, player);
				
				//	Check whether any options have been updated
				boolean update = false;
				if (email.isPrimaryAddress() != primary) {
					email.setPrimaryAddress(primary);
					update = true;
				}
				if (email.isEmailsRequested() != emailsRequested) {
					email.setEmailsRequested(emailsRequested);
					update = true;
				}
				
				if (update) {
					update(email);
				}
			} 
			//	If no email already exists, create one
			catch (NoResultException e) {
				email = new Email();
				email.setEmailAddress(emailAddress);
				email.setEmailsRequested(emailsRequested);
				email.setPrimaryAddress(primary);
				email.setPlayer(player);
				player.addEmail(email);
				
				boolean success = insert(email);
				if (!success) {
					email = null;
				}
			}
		}
		
		return email;
	}

}
