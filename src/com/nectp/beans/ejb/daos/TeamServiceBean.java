package com.nectp.beans.ejb.daos;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.TeamService;
import com.nectp.jpa.entities.Team;

@Stateless
public class TeamServiceBean extends DataServiceBean<Team> implements TeamService {
	private static final long serialVersionUID = 7381216334640961056L;

	@Override
	public List<Team> selectTeamsByName(String teamName) {
		Logger log = Logger.getLogger(TeamServiceBean.class.getName());
		List<Team> teams = new LinkedList<Team>();
		if (teamName == null) {
			log.severe("Team name not specified! Can not select team.");
		}
		else {
			TypedQuery<Team> tq = em.createNamedQuery("Team.selectTeamByName", Team.class);
			tq.setParameter("teamName", teamName);
			try {
				teams = tq.getResultList();
			} catch (NoResultException e) {
				log.warning("No teams found for name: " + teamName);
				log.warning(e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.severe("Exception caught retrieving teams: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return teams;
	}

	@Override
	public List<Team> selectTeamsByAbbr(String teamAbbr) {
		Logger log = Logger.getLogger(TeamServiceBean.class.getName());
		List<Team> teams = new LinkedList<Team>();
		if (teamAbbr == null) {
			log.severe("Team name not specified! Can not select team.");
		}
		else {
			TypedQuery<Team> tq = em.createNamedQuery("Team.selectTeamByAbbr", Team.class);
			tq.setParameter("teamAbbr", teamAbbr);
			try {
				teams = tq.getResultList();
			} catch (NoResultException e) {
				log.warning("No teams found for abbr: " + teamAbbr);
				log.warning(e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.severe("Exception caught retrieving teams: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return teams;
	}

	@Override
	public List<Team> selectTeamsByCity(String teamCity) {
		Logger log = Logger.getLogger(TeamServiceBean.class.getName());
		List<Team> teams = new LinkedList<Team>();
		if (teamCity == null) {
			log.severe("Team name not specified! Can not select team.");
		}
		else {
			TypedQuery<Team> tq = em.createNamedQuery("Team.selectTeamByCity", Team.class);
			tq.setParameter("teamCity", teamCity);
			try {
				teams = tq.getResultList();
			} catch (NoResultException e) {
				log.warning("No teams found for city: " + teamCity);
				log.warning(e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.severe("Exception caught retrieving teams: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return teams;
	}

	@Override
	public Team selectTeamByNameCity(String teamName, String teamCity) {
		Logger log = Logger.getLogger(TeamServiceBean.class.getName());
		Team team = null;
		if (teamName == null || teamCity == null) {
			log.severe("Name and/or city not defined! Can't select team.");
		}
		else {
			TypedQuery<Team> tq = em.createNamedQuery("Team.selectTeamByNameCity", Team.class);
			tq.setParameter("teamName", teamName);
			tq.setParameter("teamCity", teamCity);
			try {
				team = tq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple teams found for name: " + teamName + " city: " + teamCity);
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No team found for name: " + teamName + " city: " + teamCity);
				log.warning(e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.severe("Exception caught retrieving team: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return team;
	}

}
