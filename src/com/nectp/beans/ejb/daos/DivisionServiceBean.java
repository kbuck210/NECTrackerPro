package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.DivisionService;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Division.Region;

@Stateless
public class DivisionServiceBean extends DataServiceBean<Division> implements DivisionService {
	private static final long serialVersionUID = 3965068494859132010L;

	@Override
	public Division selectDivisionInConference(Region region, Conference conference) throws NoResultException {
		Logger log = Logger.getLogger(DivisionServiceBean.class.getName());
		Division division = null;
		if (region == null || conference == null) {
			log.severe("Region and/or conference not specified, can not get Division.");
		}
		else {
			TypedQuery<Division> dq = em.createNamedQuery("Division.selectByRegionConference", Division.class);
			dq.setParameter("region", region.ordinal());
			dq.setParameter("conferenceId", conference.getConferenceId());
			try {
				division = dq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple divisions found for region: " + region.name() + " in conference: " + conference.getConferenceType().name());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No division found for region: " + region.name() + " in conference: " + conference.getConferenceType().name());
				log.warning(e.getMessage());
				throw e;
			} catch (Exception e) {
				log.severe("Exception caught retrieving division: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return division;
	}

}
