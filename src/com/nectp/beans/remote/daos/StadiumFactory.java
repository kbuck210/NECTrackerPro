package com.nectp.beans.remote.daos;

import com.nectp.jpa.constants.Timezone;
import com.nectp.jpa.entities.Address;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Stadium.RoofType;

public interface StadiumFactory extends StadiumService {

	public Stadium createStadium(String stadiumName, Address address, long capacity, boolean international, RoofType roofType, Timezone timezone);
	
}
