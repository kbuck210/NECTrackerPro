package com.nectp.beans.ejb.daos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.ejb.daos.DataServiceBean;
import com.nectp.beans.remote.daos.WeekService;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.Week;

@Stateless
public class WeekServiceBean extends DataServiceBean<Week> implements WeekService {
	private static final long serialVersionUID = -496348214173792532L;
	
	@Override
	public Week selectCurrentWeekInSeason(Season season) {
		Logger log = Logger.getLogger(WeekServiceBean.class.getName());
		Week nextWeek = null;
		if (season == null) {
			log.log(Level.SEVERE, "No Season sepecified! can't get next week in season.");
		}
		else {
			TypedQuery<Week> wq = em.createNamedQuery("Week.selectCurrentWeekInSeason", Week.class);
			wq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				nextWeek = wq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.log(Level.SEVERE, "Multiple weeks found for next week in season: " + season.getSeasonNumber());
				log.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.log(Level.WARNING, "No week found for next week in season: " + season.getSeasonNumber());
				log.log(Level.WARNING, e.getMessage());
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception caught in retrieving next week: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return nextWeek;
	}
	
	@Override
	public Week selectWeekByNumberInSeason(int weekNumber, Season season) {
		Logger log = Logger.getLogger(WeekServiceBean.class.getName());
		Week selectedWeek = null;
		if (season == null) {
			log.log(Level.WARNING, "No Season specified! can't get week in season.");
		}
		else {
			TypedQuery<Week> wq = em.createNamedQuery("Week.selectWeekByNumberInSeason", Week.class);
			wq.setParameter("weekNumber", weekNumber);
			wq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				selectedWeek = wq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.log(Level.SEVERE, "Multiple weeks found for week: " + weekNumber + " in season " + season.getSeasonNumber());
				log.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.log(Level.WARNING, "No weeks found for week: " + weekNumber + " in season " + season.getSeasonNumber());
				log.log(Level.WARNING, e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception caught in retrieving week: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return selectedWeek;
	}

	@Override
	public List<Week> listAllWeeksInSeason(Season season) {
		Logger log = Logger.getLogger(WeekServiceBean.class.getName());
		List<Week> weeks = new LinkedList<Week>();
		if (season == null) {
			log.log(Level.WARNING, "No season specified! can't get list of weeks in season.");
		}
		else {
			TypedQuery<Week> wq = em.createNamedQuery("Week.selectWeeksInSeason", Week.class);
			wq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				weeks = wq.getResultList();
			} catch (NoResultException e) {
				log.log(Level.WARNING, "No weeks found in season " + season.getSeasonNumber());
				log.log(Level.WARNING, e.getMessage());
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception caught in retrieving weeks: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return weeks;
	}

	@Override
	public List<Week> selectWeeksThroughCurrentWeekInSeason(Season season) {
		Logger log = Logger.getLogger(WeekServiceBean.class.getName());
		List<Week> weeks = null;
		if (season == null) {
			log.log(Level.SEVERE, "No Season sepecified! can't get past weeks in season.");
		}
		else {
			TypedQuery<Week> wq = em.createNamedQuery("Week.selectWeeksThroughCurrentInSeason", Week.class);
			wq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				weeks = wq.getResultList();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception caught in retrieving weeks: " + e.getMessage());
				e.printStackTrace();
				weeks = new ArrayList<Week>();
			}
		}
		
		return weeks;
	}

	@Override
	public List<Week> selectConcurrentWeeksInRangeInSeason(Season season, int beginning, int end) {
		Logger log = Logger.getLogger(WeekServiceBean.class.getName());
		List<Week> weeks = null;
		if (season == null) {
			log.log(Level.SEVERE, "No Season sepecified! can't get week range in season.");
			weeks = new ArrayList<Week>();
		}
		else {
			TypedQuery<Week> wq = em.createNamedQuery("Week.selectWeeksInRangeInSeason", Week.class);
			wq.setParameter("seasonNumber", season.getSeasonNumber());
			wq.setParameter("lowerBound", beginning);
			wq.setParameter("upperBound", end);
			try {
				weeks = wq.getResultList();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception caught in retrieving weeks: " + e.getMessage());
				e.printStackTrace();
				weeks = new ArrayList<Week>();
			}
		}
		
		return weeks;
	}
	
	@Override
	public List<Week> selectWeeksInSubseason(Subseason subseason) {
		Logger log = Logger.getLogger(WeekServiceBean.class.getName());
		List<Week> weeks = null;
		if (subseason == null) {
			log.log(Level.SEVERE, "No Subseason sepecified! can't get weeks in subseason.");
			weeks = new ArrayList<Week>();
		}
		else {
			TypedQuery<Week> wq = em.createNamedQuery("Week.selectWeeksInSubseason", Week.class);
			wq.setParameter("subseasonId", subseason.getSubseasonId());
			try {
				weeks = wq.getResultList();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception caught in retrieving weeks: " + e.getMessage());
				e.printStackTrace();
				weeks = new ArrayList<Week>();
			}
		}
		
		return weeks;
	}
}

