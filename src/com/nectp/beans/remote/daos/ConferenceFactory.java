package com.nectp.beans.remote.daos;


import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Conference.ConferenceType;

public interface ConferenceFactory extends ConferenceService {

	public Conference createConference(ConferenceType confType);
	
}
