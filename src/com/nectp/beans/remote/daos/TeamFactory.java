package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Team;

public interface TeamFactory extends TeamService {

	public Team createTeam(Integer franchiseId);
	
}
