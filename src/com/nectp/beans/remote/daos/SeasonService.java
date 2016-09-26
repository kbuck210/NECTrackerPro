package com.nectp.beans.remote.daos;

import java.util.List;

import com.nectp.beans.remote.daos.DataService;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Week;

public interface SeasonService extends DataService<Season> {

	public Season selectCurrentSeason();
	
	public Season selectSeasonByYear(String year);
	
	public List<Season> listAll();
	
	public boolean updateCurrentWeek(Week week);
	
}
