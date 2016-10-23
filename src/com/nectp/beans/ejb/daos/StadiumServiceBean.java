package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.StadiumService;
import com.nectp.jpa.entities.Stadium;

@Stateless
public class StadiumServiceBean extends DataServiceBean<Stadium> implements StadiumService {
	private static final long serialVersionUID = -6093819600438060287L;

	@Override
	public Stadium selectStadiumByName(String stadiumName) throws NoResultException {
		Logger log = Logger.getLogger(StadiumServiceBean.class.getName());
		Stadium stadium = null;
		if (stadiumName == null) {
			log.severe("Stadium name not specified! can not retrieve stadium.");
		}
		else {
			TypedQuery<Stadium> sq = em.createNamedQuery("Stadium.selectStadiumByName", Stadium.class);
			sq.setParameter("stadiumName", stadiumName);
			try {
				stadium = sq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple stadiums found for name: " + stadiumName);
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No stadium found for name: " + stadiumName);
				log.warning(e.getMessage());
				throw e;
			} catch (Exception e) {
				log.severe("Exception caught retrieving stadium: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return stadium;
	}

}
