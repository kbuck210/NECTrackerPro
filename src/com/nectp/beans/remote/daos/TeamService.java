package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Team;

public interface TeamService extends DataService<Team> {

	public Team selectTeamByFranchiseId(int franchiseId);
	
}

