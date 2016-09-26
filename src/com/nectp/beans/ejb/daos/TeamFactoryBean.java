package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.TeamFactory;
import com.nectp.jpa.entities.Team;

@Stateless
public class TeamFactoryBean extends TeamServiceBean implements TeamFactory {
	private static final long serialVersionUID = -4296204769641649399L;

	@Override
	public Team createTeam(String teamName, String teamAbbr, String teamCity) {
		Logger log = Logger.getLogger(TeamFactoryBean.class.getName());
		Team team = null;
		if (teamName == null || teamAbbr == null || teamCity == null) {
			log.severe("Parameters not defined, can not create Team!");
		}
		else {
			//	Check whether the specified team exists
			try {
				team = selectTeamByNameCity(teamName, teamCity);
				log.info("Team exists for: " + teamCity + " " + teamName);
			} 
			//	If no results found, create new team
			catch (NoResultException e) {
				team = new Team();
				team.setTeamAbbr(teamAbbr);
				team.setTeamCity(teamCity);
				team.setTeamName(teamName);
				
				boolean success = insert(team);
				if (!success) {
					team = null;
				}
			}
		}
		
		return team;
	}

}
