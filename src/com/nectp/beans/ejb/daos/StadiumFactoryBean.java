package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.StadiumFactory;
import com.nectp.jpa.constants.Timezone;
import com.nectp.jpa.entities.Address;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Stadium.RoofType;

@Stateless
public class StadiumFactoryBean extends StadiumServiceBean implements StadiumFactory {
	private static final long serialVersionUID = 1610321741715288522L;

	@Override
	public Stadium createStadium(String stadiumName, Address address, long capacity, boolean international,
			RoofType roofType, Timezone timezone) 
	{
		Logger log = Logger.getLogger(StadiumFactoryBean.class.getName());
		Stadium stadium = null;
		if (stadiumName == null || address == null || roofType == null || timezone == null) {
			log.severe("Parameters not defined! can not create stadium.");
		}
		else {
			//	First check whether the specified stadium already exists
			try {
				stadium = selectStadiumByName(stadiumName);
				
				//	Check whether any of the stadium attributes need to be updated
				boolean update = false;
				if (!stadium.getAddress().equals(address)) {
					stadium.setAddress(address);
					update = true;
				}
				if (stadium.getCapacity().longValue() != capacity) {
					stadium.setCapacity(capacity);
					update = true;
				}
				if (stadium.getInternational() != international) {
					stadium.setInternational(international);
					update = true;
				}
				
				if (update) {
					update(stadium);
				}
			} 
			//	If no stadium found, create one
			catch (NoResultException e) {
				stadium = new Stadium();
				stadium.setAddress(address);
				stadium.setCapacity(capacity);
				stadium.setInternational(international);
				stadium.setRoofType(roofType);
				stadium.setStadiumName(stadiumName);
				stadium.setTimezone(timezone);
				
				boolean success = insert(stadium);
				if (!success) {
					stadium = null;
				}
			}
		}
		
		return stadium;
	}

}
