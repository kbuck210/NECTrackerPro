package com.nectp.beans.remote.daos;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.TeamForSeason;

/** TeamStatisticService extends the RecordService interface to provide specific common-use queries for aggregating records concerning TeamForSeason records
 * 
 * @author Kevin C. Buckley
 * @since  1.0
 */
public interface TeamStatisticService extends StatisticService<TeamForSeason> {
	
	/** Aggregates the 5 most recent games played against the specified opponent, using the specified subseason
	 * 
	 * @param tfs the TeamForSeason for which to find the aggregate record
	 * @param opponent the opponent TeamForSeason to use as a comparison
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all recent games between opponents for the specified criteria
	 */
	public RecordAggregator getRecentRecordAgainstOpponent(TeamForSeason tfs, TeamForSeason opponent, NEC subseasonType, boolean againstSpread);
}

