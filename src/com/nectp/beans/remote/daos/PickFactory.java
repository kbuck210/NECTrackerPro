package com.nectp.beans.remote.daos;

import java.util.List;

import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.Pick.PickType;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

public interface PickFactory extends PickService {

	public Pick createPlayerPickInWeek(PlayerForSeason player, TeamForSeason pickedTeam, Week week, NEC pickFor, PickType pickType);
	
	public Pick createPlayerPickForRecord(Record record, Game game, TeamForSeason pickedTeam, PickType pickType);

	public List<Pick> removePicksForReplacement(Record r);
}

