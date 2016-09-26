package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.ConferenceFactory;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Conference.ConferenceType;

@Stateless
public class ConferenceFactoryBean extends ConferenceServiceBean implements ConferenceFactory {
	private static final long serialVersionUID = 6197561921452005314L;

	@Override
	public Conference createConference(ConferenceType confType) {
		Logger log = Logger.getLogger(ConferenceFactoryBean.class.getName());
		Conference conference = null;
		if (confType == null) {
			log.severe("Conference type not specified, can not create conference");
		}
		else {
			//	Check whether conference already exists, if not, create it
			try {
				conference = selectConferenceByType(confType);
			} catch (NoResultException e) {
				conference = new Conference();
				conference.setConferenceType(confType);
				
				boolean success = insert(conference);
				if (!success) {
					conference = null;
				}
			}
		}
		
		return conference;
	}

}
