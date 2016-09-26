package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.ejb.daos.DataServiceBean;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;

@Stateless
public class TeamForSeasonServiceBean extends DataServiceBean<TeamForSeason> implements TeamForSeasonService {
	private static final long serialVersionUID = -7498663134306709096L;

	
	@Override
	public TeamForSeason selectTfsByAbbr(String teamAbbr, Season season) {
		Logger log = Logger.getLogger(TeamForSeasonServiceBean.class.getName());
		TeamForSeason tfs = null;
		
		if (teamAbbr == null || season == null) {
			log.severe("Abbreviation or season not specified, can not select TeamForSeason!");
		}
		else {
			TypedQuery<TeamForSeason> tq = em.createNamedQuery("TeamForSeason.selectTfsByAbbr", TeamForSeason.class);
			tq.setParameter("teamAbbr", teamAbbr);
			tq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				tfs = tq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple TFS found for: " + teamAbbr + " in season " + season.getSeasonNumber());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No TFS found for: " + teamAbbr + " in season " + season.getSeasonNumber());
				log.warning(e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.severe("Exception caught while retrieving TFS: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return tfs;
	}

}
