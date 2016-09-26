package com.nectp.beans.ejb.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.PrizeForSeasonService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Season;

@Stateless
public class PrizeForSeasonServiceBean extends DataServiceBean<PrizeForSeason> implements PrizeForSeasonService {
	private static final long serialVersionUID = 1334982580042392979L;

	@Override
	public PrizeForSeason selectPrizeForSeason(NEC prizeType, Season season) {
		Logger log = Logger.getLogger(PrizeForSeasonServiceBean.class.getName());
		PrizeForSeason pfs = null;
		if (prizeType == null || season == null) {
			log.severe("Type or season not specified, can not select PrizeForSeason.");
		}
		else {
			TypedQuery<PrizeForSeason> pq = em.createNamedQuery("PrizeForSeason.selectPrizeForSeason", PrizeForSeason.class);
			pq.setParameter("prizeType", prizeType.ordinal());
			pq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				pfs = pq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple prizes for: " + prizeType.name() + " found for season " + season.getSeasonNumber());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No prizes found for: " + prizeType.name() + " in season " + season.getSeasonNumber());
				log.warning(e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.severe("Exception caught retrieving prize for season: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return pfs;
	}

	@Override
	public List<PrizeForSeason> selectAllPrizesInSeason(Season season) {
		Logger log = Logger.getLogger(PrizeForSeasonServiceBean.class.getName());
		List<PrizeForSeason> prizes;
		TypedQuery<PrizeForSeason> pq = em.createNamedQuery("PrizeForSeason.selectAllPrizesInSeason", PrizeForSeason.class);
		pq.setParameter("seasonNumber", season.getSeasonNumber());
		try {
			prizes = pq.getResultList();
		} catch (Exception e) {
			log.severe("Exception caught retrieving list of prizes for season: " + e.getMessage());
			e.printStackTrace();
			prizes = new ArrayList<PrizeForSeason>();
		}
		return prizes;
	}

}
