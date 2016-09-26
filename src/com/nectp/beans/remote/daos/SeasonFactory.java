package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Season;

public interface SeasonFactory extends SeasonService {
	
	public Season generateSeason(Integer seasonNumber,
								 String seasonYear, 
								 boolean current, 
								 Integer winValue, 
								 Integer lossValue, 
								 Integer tieValue, 
								 Integer secondHalfStartWeek, 
								 Integer playoffStartWeek, 
								 Integer superbowlWeek,
								 Integer minPicks,
								 Integer maxPicks,
								 Integer tnoLosses);
	
	public Season generateSeasonWithDefaultValues(Integer seasonNumber, String seasonYear, boolean current);
}
