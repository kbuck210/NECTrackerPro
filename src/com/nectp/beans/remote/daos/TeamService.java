package com.nectp.beans.remote.daos;

import java.util.List;

import com.nectp.jpa.entities.Team;

public interface TeamService extends DataService<Team> {

	public List<Team> selectTeamsByName(String teamName);
	
	public List<Team> selectTeamsByAbbr(String teamAbbr);
	
	public List<Team> selectTeamsByCity(String teamCity);
	
	public Team selectTeamByNameCity(String teamName, String teamCity);
}
