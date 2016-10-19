package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.ejb.daos.DataServiceBean;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Team;
import com.nectp.jpa.entities.TeamForSeason;

@Stateless
public class TeamForSeasonServiceBean extends DataServiceBean<TeamForSeason> implements TeamForSeasonService {
	private static final long serialVersionUID = -7498663134306709096L;

	
	@Override
	public TeamForSeason selectTfsByTeamSeason(Team team, Season season) {
		Logger log = Logger.getLogger(TeamForSeasonServiceBean.class.getName());
		TeamForSeason tfs = null;
		
		if (team == null || season == null) {
			log.severe("Team or season not specified, can not select TeamForSeason!");
		}
		else {
			TypedQuery<TeamForSeason> tq = em.createNamedQuery("TeamForSeason.selectTfsByTeamSeason", TeamForSeason.class);
			tq.setParameter("franchiseId", team.getFranchiseId());
			tq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				tfs = tq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple TFS found for franchise: " + team.getFranchiseId() + " in season " + season.getSeasonNumber());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No TFS found for for franchise: " + team.getFranchiseId() + " in season " + season.getSeasonNumber());
				log.warning(e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.severe("Exception caught while retrieving TFS: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return tfs;
	}


	@Override
	public TeamForSeason selectTfsByAbbrSeason(String abbr, Season season) {
		Logger log = Logger.getLogger(TeamForSeasonServiceBean.class.getName());
		TeamForSeason tfs = null;
		
		if (abbr == null || season == null) {
			log.severe("Abbreviation or season not specified, can not select TeamForSeason!");
		}
		else {
			TypedQuery<TeamForSeason> tq = em.createNamedQuery("TeamForSeason.selectTfsByAbbrSeason", TeamForSeason.class);
			tq.setParameter("teamAbbr", abbr);
			tq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				tfs = tq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple TFS found for franchise: " + abbr + " in season " + season.getSeasonNumber());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No TFS found for for franchise: " + abbr + " in season " + season.getSeasonNumber());
				log.warning(e.getMessage());
			} catch (Exception e) {
				log.severe("Exception caught while retrieving TFS: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return tfs;
	}
	
	/** NOTE: if available, should use the ABBR query for performance. 
	 * 	City query performs a scan for UPPER command to work, whereas abbrs are stored in upper-case
	 * 
	 */
	@Override
	public TeamForSeason selectTfsByCitySeason(String city, Season season) {
		Logger log = Logger.getLogger(TeamForSeasonServiceBean.class.getName());
		TeamForSeason tfs = null;
		
		if (city == null || season == null) {
			log.severe("Abbreviation or season not specified, can not select TeamForSeason!");
		}
		else {
			TypedQuery<TeamForSeason> tq = em.createNamedQuery("TeamForSeason.selectTfsByAbbrSeason", TeamForSeason.class);
			tq.setParameter("teamCity", city.toUpperCase());
			tq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				tfs = tq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple TFS found for city: " + city + " in season " + season.getSeasonNumber());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No TFS found for for city: " + city + " in season " + season.getSeasonNumber());
				log.warning(e.getMessage());
			} catch (Exception e) {
				log.severe("Exception caught while retrieving TFS: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return tfs;
	}

}

