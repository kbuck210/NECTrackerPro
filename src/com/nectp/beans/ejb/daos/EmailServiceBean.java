package com.nectp.beans.ejb.daos;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.EmailService;
import com.nectp.jpa.entities.Email;
import com.nectp.jpa.entities.Player;

@Stateless
public class EmailServiceBean extends DataServiceBean<Email> implements EmailService {
	private static final long serialVersionUID = -3242994849932595929L;

	private Logger log;
	
	public EmailServiceBean() {
		log = Logger.getLogger(this.getClass().getName());
	}
	
	@Override
	public List<Email> selectByAddress(String emailAddress) {
		List<Email> emailAddresses = new LinkedList<Email>();
		if (emailAddress != null) {
			TypedQuery<Email> eq = em.createNamedQuery("Email.selectByAddress", Email.class);
			eq.setParameter("emailAddress", emailAddress);
			try {
				emailAddresses.addAll(eq.getResultList());
			} catch (NoResultException e) {
				log.log(Level.WARNING, "No Results found: " + e.getMessage());
			}
		}
		
		return emailAddresses;
	}

	@Override
	public Email selectEmailForPlayer(String emailAddress, Player player) {
		Email email = null;
		if (player != null) {
			TypedQuery<Email> eq = em.createNamedQuery("Email.selectByAddressName", Email.class);
			eq.setParameter("emailAddress", emailAddress);
			eq.setParameter("playerId", player.getAbstractTeamId());
			try {
				email = eq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.log(Level.SEVERE, "Multiple results found for: " + emailAddress + " - " + player.getName());
				log.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.log(Level.WARNING, "No Results found for: " + emailAddress + " - " + player.getName());
				log.log(Level.WARNING, e.getMessage());
				throw new NoResultException();
			}
		}
		return email;
	}

	@Override
	public List<Email> selectAllByPlayer(Player player) {
		List<Email> allAddresses = new LinkedList<Email>();
		if (player != null) {
			TypedQuery<Email> eq = em.createNamedQuery("Email.selectAllByPlayer", Email.class);
			eq.setParameter("playerId", player.getAbstractTeamId());
			try {
				allAddresses = eq.getResultList();
			} catch (NoResultException e) {
				log.log(Level.WARNING, "No Results found for: " + player.getName());
				log.log(Level.WARNING, e.getMessage());
			}
		}
		
		return allAddresses;
	}
}
