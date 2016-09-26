package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.PrizeForSeasonFactory;
import com.nectp.beans.remote.daos.PrizeService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Prize;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;

@Stateless
public class PrizeForSeasonFactoryBean extends PrizeForSeasonServiceBean implements PrizeForSeasonFactory {
	private static final long serialVersionUID = 3094248171879524734L;

	@EJB
	private PrizeService prizeService;
	
	@Override
	public PrizeForSeason createPrizeInSeason(NEC prizeType, Season season, Subseason subseason, int amount) {
		Logger log = Logger.getLogger(PrizeForSeasonFactoryBean.class.getName());
		PrizeForSeason pfs = null;
		if (prizeType == null || season == null) {
			log.severe("Type or season not specified, can not create prize for season!");
		}
		else {
			//	Check whether the prize for the season already exists
			try {
				pfs = selectPrizeForSeason(prizeType, season);
			}
			//	If no PFS found, create one
			catch (NoResultException e) {
				Prize prize = prizeService.selectPrizeByType(prizeType);
				if (prize == null) {
					log.severe("Error retrieving prize, can not create PrizeForSeason.");
				}
				else {
					pfs = new PrizeForSeason();
					pfs.setPrizeAmount(amount);
					
					pfs.setPrize(prize);
					prize.addPrizeHistory(pfs);
					
					pfs.setSeason(season);
					season.addPrize(pfs);
					
					if (subseason != null) {
						pfs.setSubseason(subseason);
						subseason.addPrizeForSubseason(pfs);
					}
					
					boolean success = insert(pfs);
					if (!success) {
						pfs = null;
					}
				}
			}
		}
		
		return pfs;
	}

}
