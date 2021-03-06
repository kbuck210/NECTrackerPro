package com.nectp.beans.remote.daos;

import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

public interface RecordFactory extends RecordService {

	public Record createWeekRecordForAtfs(Week week, AbstractTeamForSeason atfs, NEC recordType);
	
	public Record createWeekRecordForGame(Game game, TeamForSeason team);
	
}
