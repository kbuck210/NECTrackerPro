package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;

import com.nectp.beans.remote.daos.TeamFactory;
import com.nectp.jpa.entities.Team;

@Stateless
public class TeamFactoryBean extends TeamServiceBean implements TeamFactory {
	private static final long serialVersionUID = -4296204769641649399L;

	@Override
	public Team createTeam(Integer franchiseId) {
		Logger log = Logger.getLogger(TeamFactoryBean.class.getName());
		Team team = null;
		if (franchiseId == null) {
			log.severe("Franchise ID not defined, can not create Team!");
		}
		else {
			//	Check whether the specified team exists
			try {
				team = selectTeamByFranchiseId(franchiseId);
				log.info("Franchise exists for: " + franchiseId.toString());
			} 
			//	If no results found, create new team
			catch (NoExistingEntityException e) {
				team = new Team();
				team.setFranchiseId(franchiseId);
				
				boolean success = insert(team);
				if (!success) {
					team = null;
				}
			}
		}
		
		return team;
	}

}