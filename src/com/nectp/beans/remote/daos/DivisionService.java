package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Division.Region;

public interface DivisionService extends DataService<Division> {

	public Division selectDivisionInConference(Region region, Conference conference);
	
}
