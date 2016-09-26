package com.nectp.beans.remote.daos;

import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

public interface PickFactory extends PickService {

	public Pick createPlayerPickInWeek(PlayerForSeason player, TeamForSeason pickedTeam, Week week, NEC pickFor);
	
}
