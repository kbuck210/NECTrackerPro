package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Conference.ConferenceType;

public interface ConferenceService extends DataService<Conference> {

	public Conference selectConferenceByType(ConferenceType confType);
	
}
