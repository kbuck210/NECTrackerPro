package com.nectp.beans.ejb.daos;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.ejb.daos.DataServiceBean;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Week;

@Stateless
public class SeasonServiceBean extends DataServiceBean<Season> implements SeasonService {
	private static final long serialVersionUID = -8737254199488482755L;

	@Override
	public Season selectCurrentSeason() {
		Logger log = Logger.getLogger(SeasonServiceBean.class.getName());
		Season currentSeason = null;
		TypedQuery<Season> sq = em.createNamedQuery("Season.selectCurrentSeason", Season.class);
		try {
			currentSeason = sq.getSingleResult();
		} catch (NonUniqueResultException e) {
			log.log(Level.SEVERE, "Multiple Seasons found for current year!");
			log.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
		} catch (NoResultException e) {
			log.log(Level.WARNING, "No Seasons found for currnet year!");
			log.log(Level.WARNING, e.getMessage());
		} catch (Exception e) {
			log.log(Level.SEVERE, "Exception retrieving current season: " + e.getMessage());
			e.printStackTrace();
		}
		
		return currentSeason;
	}
	
	@Override
	public Season selectSeasonByYear(String year) {
		Logger log = Logger.getLogger(SeasonServiceBean.class.getName());
		Season selectedSeason = null;
		TypedQuery<Season> sq = em.createNamedQuery("Season.selectByYear", Season.class);
		sq.setParameter("year", year);
		try {
			selectedSeason = sq.getSingleResult();
		} catch (NonUniqueResultException e) {
			log.log(Level.SEVERE, "Multiple Seasons found for year: " + year);
			log.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
		} catch (NoResultException e) {
			log.log(Level.WARNING, "No Seasons found for year: " + year);
			log.log(Level.WARNING, e.getMessage());
			throw new NoResultException();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Exception retrieving season: " + e.getMessage());
			e.printStackTrace();
		}
		
		return selectedSeason;
	}

	@Override
	public List<Season> listAll() {
		Logger log = Logger.getLogger(SeasonServiceBean.class.getName());
		List<Season> allSeasons = new LinkedList<Season>();
		TypedQuery<Season> sq = em.createNamedQuery("Season.findAll", Season.class);
		try {
			allSeasons = sq.getResultList();
		} catch (NoResultException e) {
			log.log(Level.WARNING, "No Seasons found!");
			log.log(Level.WARNING, e.getMessage());
		} catch (Exception e) {
			log.log(Level.SEVERE, "Exception retrieving season list: " + e.getMessage());
			e.printStackTrace();
		}
		
		return allSeasons;
	}

	@Override
	public boolean updateCurrentWeek(Week week) {
		Logger log = Logger.getLogger(SeasonService.class.getName());
		Season currentSeason = selectCurrentSeason();
		//	If the current season is undefined, return error
		if (currentSeason == null) {
			log.log(Level.SEVERE, "Could not retrieve current season! Can not update current week.");
			return false;
		}
		//	If the current week is the same as the update case, return no need to update
		else if (currentSeason.getCurrentWeek().equals(week)) { 
			log.log(Level.INFO, "Current week is already set. No need to update.");
			return true;
		}
		//	If the current week has changed, return the result of updating the season
		else {
			currentSeason.setCurrentWeek(week);
			return update(currentSeason);
		}
	}
}
