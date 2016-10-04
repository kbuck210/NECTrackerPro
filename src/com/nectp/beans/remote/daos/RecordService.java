package com.nectp.beans.remote.daos;

import java.util.List;
import java.util.TreeMap;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

public interface RecordService extends DataService<Record> {

	public Record selectWeekRecordForAtfs(Week week, AbstractTeamForSeason atfs, NEC recordType);
	
	public RecordAggregator getAggregateRecordForAtfsForType(AbstractTeamForSeason atfs, NEC recordType, boolean againstSpread);
	
	public RecordAggregator getOverallRecordThroughWeekForAtfs(AbstractTeamForSeason atfs, Week week, NEC recordType, boolean againstSpread);
	
	public RecordAggregator getRecordForConcurrentWeeksForAtfs(AbstractTeamForSeason atfs, Integer startWeek, Integer endWeek, NEC recordType, boolean againstSpread);
	
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getPlayerRankedScoresForType(NEC recordType, Season season, boolean againstSpread);
	
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getTeamRankedScoresForType(NEC recordType, Season season, boolean againstSpread);
	
	public void updateRecordForPlayerPick(Pick p, TeamForSeason winningTeam, TeamForSeason losingTeam);
}

