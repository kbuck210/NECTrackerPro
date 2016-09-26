package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Week.WeekStatus;

public interface WeekFactory extends WeekService {

	public Week createWeekInSeason(int weekNumber, Season season, WeekStatus status, boolean current);
	
}
