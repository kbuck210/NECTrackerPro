package com.nectp.beans.remote.daos;

import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;

public interface PrizeForSeasonFactory extends PrizeForSeasonService {

	public PrizeForSeason createPrizeInSeason(NEC prizeType, Season season, Subseason subseason, int amount);
	
}
