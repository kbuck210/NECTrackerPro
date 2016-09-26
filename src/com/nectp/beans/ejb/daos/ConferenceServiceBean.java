package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.ConferenceService;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Conference.ConferenceType;

@Stateless
public class ConferenceServiceBean extends DataServiceBean<Conference> implements ConferenceService {
	private static final long serialVersionUID = -4384443912640043470L;

	@Override
	public Conference selectConferenceByType(ConferenceType confType) {
		Logger log = Logger.getLogger(ConferenceServiceBean.class.getName());
		Conference conference = null;
		if (confType == null) {
			log.severe("Conference type not specified, can not select conference.");
		}
		else {
			TypedQuery<Conference> cq = em.createNamedQuery("Conference.selectByConfType", Conference.class);
			cq.setParameter("confType", confType.ordinal());
			try {
				conference = cq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple conferences found for type: " + confType.name());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No results found for type: " + confType.name());
				log.warning(e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.severe("Exception caught retrieving conference: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return conference;
	}

}
