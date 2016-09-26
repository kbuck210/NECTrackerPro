package com.nectp.beans.remote.daos;

import java.util.List;

import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Season;

public interface PrizeForSeasonService extends DataService<PrizeForSeason> {

	public PrizeForSeason selectPrizeForSeason(NEC prizeType, Season season);
	
	public List<PrizeForSeason> selectAllPrizesInSeason(Season season);
	
}
