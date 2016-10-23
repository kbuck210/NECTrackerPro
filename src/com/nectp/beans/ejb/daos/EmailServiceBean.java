package com.nectp.beans.ejb.daos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.EmailService;
import com.nectp.jpa.entities.Email;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.Season;

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
			emailAddresses.addAll(eq.getResultList());
		}
		
		return emailAddresses;
	}

	@Override
	public Email selectEmailForPlayer(String emailAddress, Player player) throws NoResultException {
		Email email = null;
		if (player != null) {
			TypedQuery<Email> eq = em.createNamedQuery("Email.selectByAddressName", Email.class);
			eq.setParameter("emailAddress", emailAddress);
			eq.setParameter("playerId", player.getAbstractTeamId());
			try {
				email = eq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple results found for: " + emailAddress + " - " + player.getName());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No Results found for: " + emailAddress + " - " + player.getName());
				log.warning(e.getMessage());
				throw e;
			} catch (Exception e) {
				log.severe("Exception caught retrieving email: " + e.getMessage());
				e.printStackTrace();
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
			allAddresses = eq.getResultList();
		}
		
		return allAddresses;
	}
	
	@Override
	public List<Email> selectAllRecipientsBySeason(Season season) {
		List<Email> recipients;
		if (season == null) {
			log.severe("No Season specified! Can not retrieve emails.");
			recipients = new ArrayList<Email>();
		}
		else {
			TypedQuery<Email> eq = em.createNamedQuery("Email.selectAllRecipientsBySeason", Email.class);
			eq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				recipients = eq.getResultList();
			} catch (Exception e) {
				log.severe("Exception retrieving emails for season: " + e.getMessage());
				e.printStackTrace();
				recipients = new ArrayList<Email>();
			}
		}
		
		return recipients;
	}
}
