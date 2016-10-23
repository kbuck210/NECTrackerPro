package com.nectp.beans.ejb.daos;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.ejb.daos.DataServiceBean;
import com.nectp.beans.remote.daos.PrizeForSeasonService;
import com.nectp.beans.remote.daos.SubseasonService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Week.WeekStatus;

@Stateless
public class SubseasonServiceBean extends DataServiceBean<Subseason> implements SubseasonService {
	private static final long serialVersionUID = -2007513002724168161L;
	
	@EJB
	private PrizeForSeasonService pzfsService;
	
	@Override
	public Subseason selectSubseasonInSeason(NEC subseasonType, Season season) throws NoResultException {
		Logger log = Logger.getLogger(SubseasonService.class.getName());
		Subseason subseason = null;
		TypedQuery<Subseason> sq = em.createNamedQuery("Subseason.selectSubseasonByTypeInSeason", Subseason.class);
		sq.setParameter("subseasonType", subseasonType.ordinal());
		sq.setParameter("seasonNumber", season.getSeasonNumber());
		try {
			subseason = sq.getSingleResult();
		} catch (NonUniqueResultException e) {
			log.log(Level.SEVERE, "Multiple subseasons found for type: " + subseasonType + " in season " + season.getSeasonNumber());
			log.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
		} catch (NoResultException e) {
			log.log(Level.WARNING, "No subseason found for type: " + subseasonType + " in season " + season.getSeasonNumber());
			log.log(Level.WARNING, e.getMessage());
			throw e;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Exception caught retrieving subseason: " + e.getMessage());
			e.printStackTrace();
		}
		
		return subseason;
	}

	@Override
	public void updateSubseasonForWeekComplete(Subseason subseason) {
		Logger log = Logger.getLogger(SubseasonServiceBean.class.getName());
		//	Check whether every week in the subseason is complete, if so, update the prizes
		boolean allComplete = true;
		for (Week w : subseason.getWeeks()) {
			if (!w.getWeekStatus().equals(WeekStatus.COMPLETED)) {
				allComplete = false;
				break;
			}
		}
		
		if (allComplete) {
			//	Get each prize in the subeason, calculate a winner, and assign
			for (PrizeForSeason pzfs : subseason.getPrizesForSubseason()) {
				pzfsService.calculateWinner(pzfs);
			}
			
			NEC subseasonType = subseason.getSubseasonType();
			Season season = subseason.getSeason();
			
			//	If this subseason is the second half, update the season prizes for MNF/TNT
			//	TODO: ensure that MNF/TNT doesn't extend into the playoffs...
			if (subseasonType.equals(NEC.SECOND_HALF)) {
				try {
					PrizeForSeason mnfTnt = pzfsService.selectPrizeForSeason(NEC.MNF_TNT, season);
					pzfsService.calculateWinner(mnfTnt);
				} catch (NoResultException e) {
					log.severe("No prize found for MNF_TNT! Must create & allocate manually.");
				}
			}
			else if (subseasonType.equals(NEC.PLAYOFFS)) {
				try {
					PrizeForSeason playoffs = pzfsService.selectPrizeForSeason(NEC.PLAYOFFS, season);
					pzfsService.calculateWinner(playoffs);
				} catch (NoResultException e) {
					log.severe("No prize found for playoffs! Must create & allocate manually.");
				}
			}
			else if (subseasonType.equals(NEC.SUPER_BOWL)) {
				try {
					PrizeForSeason superbowl = pzfsService.selectPrizeForSeason(NEC.SUPER_BOWL, season);
					pzfsService.calculateWinner(superbowl);
				} catch (NoResultException e) {
					log.severe("No prize found for superbowl! Must create & allocate manually.");
				}
				
				try {
					PrizeForSeason moneyback = pzfsService.selectPrizeForSeason(NEC.MONEY_BACK, season);
					pzfsService.calculateWinner(moneyback);
				} catch (NoResultException e) {
					log.severe("No prize found for moneyback bowl! Must create & allocate manually.");
				}
			}
		}
	}
}
