package com.nectp.beans.ejb.daos;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.ejb.daos.DataServiceBean;
import com.nectp.beans.remote.daos.SubseasonService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;

@Stateless
public class SubseasonServiceBean extends DataServiceBean<Subseason> implements SubseasonService {
	private static final long serialVersionUID = -2007513002724168161L;
	
	@Override
	public Subseason selectSubseasonInSeason(NEC subseasonType, Season season) {
		Logger log = Logger.getLogger(SubseasonService.class.getName());
		Subseason subseason = null;
		TypedQuery<Subseason> sq = em.createNamedQuery("Subseason.selectSubseasonByTypeInSeason", Subseason.class);
		sq.setParameter("subseasonType", subseasonType.ordinal());
		sq.setParameter("seasonNumber", season.getSeasonNumber());
		try {
			subseason = sq.getSingleResult();
		} catch (NonUniqueResultException e) {
			log.log(Level.SEVERE, "Multiple subseasons found for type: " + subseasonType + " in season " + season.getSeasonNumber());
			log.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
		} catch (NoResultException e) {
			log.log(Level.WARNING, "No subseason found for type: " + subseasonType + " in season " + season.getSeasonNumber());
			log.log(Level.WARNING, e.getMessage());
			throw new NoResultException();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Exception caught retrieving subseason: " + e.getMessage());
			e.printStackTrace();
		}
		
		return subseason;
	}

}
