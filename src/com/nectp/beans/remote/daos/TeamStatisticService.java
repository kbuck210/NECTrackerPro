package com.nectp.beans.remote.daos;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.constants.Timezone;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Stadium.RoofType;
import com.nectp.jpa.entities.TeamForSeason;

public interface TeamStatisticService extends RecordService {
	
	public RecordAggregator getHomeAwayRecord(TeamForSeason tfs, boolean home, boolean againstSpread);
	
	public RecordAggregator getDivisionRecord(TeamForSeason tfs, Division division, boolean againstSpread);
	
	public RecordAggregator getConferenceRecord(TeamForSeason tfs, Conference conference, boolean againstSpread);
	
	public RecordAggregator getRecentRecordAgainstOpponent(TeamForSeason tfs, TeamForSeason opponent, boolean againstSpread);
	
	public RecordAggregator getPrimetimeRecord(TeamForSeason tfs, NEC recordType, boolean againstSpread);
	
	public RecordAggregator getRecordByDateTime(TeamForSeason tfs, Integer month, Integer dayOfWeek, Integer kickoffHour, boolean againstSpread);
	
	public RecordAggregator getRecordForStadium(TeamForSeason tfs, Stadium stadium, RoofType roofType, boolean againstSpread);
	
	public RecordAggregator getRecordByTimezone(TeamForSeason tfs, Timezone timezone, boolean againstSpread);
	
}
