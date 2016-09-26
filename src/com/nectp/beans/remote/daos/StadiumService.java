package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Stadium;

public interface StadiumService extends DataService<Stadium> {
	
	public Stadium selectStadiumByName(String stadiumName);
	
}
