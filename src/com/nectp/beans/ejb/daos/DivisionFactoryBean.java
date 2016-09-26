package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.DivisionFactory;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Division.Region;

@Stateless
public class DivisionFactoryBean extends DivisionServiceBean implements DivisionFactory {
	private static final long serialVersionUID = -6276475007034640550L;

	@Override
	public Division createDivision(Region region, Conference conference) {
		Logger log = Logger.getLogger(DivisionFactoryBean.class.getName());
		Division division = null;
		if (region == null || conference == null) {
			log.severe("Region and/or conference not specified, can not create division.");
		}
		else {
			//	Check whether division already exists, if not, create it
			try {
				division = selectDivisionInConference(region, conference);
			} catch (NoResultException e) {
				division = new Division();
				division.setRegion(region);
				division.setConference(conference);
				
				boolean success = insert(division);
				if (!success) {
					division = null;
				}
			}
		}
		
		return division;
	}

}
