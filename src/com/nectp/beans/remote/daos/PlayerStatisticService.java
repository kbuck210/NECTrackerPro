package com.nectp.beans.remote.daos;

import java.util.List;
import java.util.TreeMap;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Week;

public interface PlayerStatisticService extends StatisticService<PlayerForSeason> {

	public RecordAggregator getRecordForWeek(PlayerForSeason pfs, Week week, boolean againstSpread);
	
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getWeekRanks(Week week, boolean againstSpread);
	
}
