package com.nectp.beans.remote.daos;

import java.util.List;
import java.util.TreeMap;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Week;

public interface RecordService extends DataService<Record> {

	public Record selectWeekRecordForAtfs(Week week, AbstractTeamForSeason atfs, NEC recordType);
	
	public RecordAggregator getAggregateRecordForAtfsForType(AbstractTeamForSeason atfs, NEC recordType);
	
	public RecordAggregator getOverallRecordThroughWeekForAtfs(AbstractTeamForSeason atfs, Week week, NEC recordType);
	
	public TreeMap<RecordAggregator, List<PlayerForSeason>> getPlayerForSeasonRankedScoresForType(NEC recordType, Season season);
}
