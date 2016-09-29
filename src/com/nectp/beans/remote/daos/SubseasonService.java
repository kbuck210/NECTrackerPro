package com.nectp.beans.remote.daos;

import com.nectp.beans.remote.daos.DataService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;

public interface SubseasonService extends DataService<Subseason> {

	public Subseason selectSubseasonInSeason(NEC subseasonType, Season season);
	
	public void updateSubseasonForWeekComplete(Subseason subseason);
}
