package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Division.Region;

public interface DivisionFactory extends DivisionService {

	public Division createDivision(Region region, Conference conference);
	
}
