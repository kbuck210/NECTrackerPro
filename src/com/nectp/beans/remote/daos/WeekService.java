package com.nectp.beans.remote.daos;

import java.util.List;

import com.nectp.beans.remote.daos.DataService;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Week;

public interface WeekService extends DataService<Week> {

	public Week selectWeekByNumberInSeason(int weekNumber, Season season);
	
	public Week selectCurrentWeekInSeason(Season season);
	
	public List<Week> selectWeeksThroughCurrentWeekInSeason(Season season);
	
	public List<Week> selectConcurrentWeeksInRangeInSeason(Season season, int beginning, int end);
	
	public List<Week> listAllWeeksInSeason(Season season);
	
}
