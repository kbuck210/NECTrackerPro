package com.nectp.beans.ejb.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.PrizeForSeasonService;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Season;

@Stateless
public class PrizeForSeasonServiceBean extends DataServiceBean<PrizeForSeason> implements PrizeForSeasonService {
	private static final long serialVersionUID = 1334982580042392979L;

	@EJB
	private RecordService recordService;
	
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

	@Override
	public void calculateWinner(PrizeForSeason prizeForSeason) {
		Logger log = Logger.getLogger(PrizeForSeasonServiceBean.class.getName());
		NEC prizeType = prizeForSeason.getPrize().getPrizeType();
		Season season = prizeForSeason.getSeason();
		
		boolean againstSpread = (!prizeType.equals(NEC.TWO_AND_OUT) && !prizeType.equals(NEC.ONE_AND_OUT));
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> rankMap = recordService.getPlayerRankedScoresForType(prizeType, season, againstSpread);
		
		Entry<RecordAggregator, List<AbstractTeamForSeason>> winningEntry = rankMap.pollFirstEntry();
		List<AbstractTeamForSeason> winnerList = winningEntry.getValue();
		
		//	Check if there is only 1 winner, set the winner fields
		if (winnerList.size() == 1) {
			AbstractTeamForSeason atfs = winnerList.get(0);
			if (atfs instanceof PlayerForSeason) {
				PlayerForSeason winner = (PlayerForSeason)atfs;
				prizeForSeason.setWinner(winner);
				winner.addPrizeforseason(prizeForSeason);
				
				update(prizeForSeason);
			}
		}
		//	If there are more than 1 winner, must allocate prize manually
		else if (winnerList.size() != 0) {
			log.warning("Multiple winners found, must allocate prize manually.");
		}
		//	If there are no winners found, must allocate prize manually
		else {
			log.warning("No eligible winners found, must allocate prize manually.");
		}
	}
}
