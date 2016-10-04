package com.nectp.beans.remote.daos;

import java.util.TimeZone;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Stadium.RoofType;
import com.nectp.jpa.entities.TeamForSeason;

/** TeamStatisticService extends the RecordService interface to provide specific common-use queries for aggregating records concerning TeamForSeason records
 * 
 * @author Kevin C. Buckley
 * @since  1.0
 */
public interface TeamStatisticService extends RecordService {
	
	/** Finds the aggregate Home or Away record for the specified team for the specified subseason
	 * 
	 * @param tfs the TeamForSeason for which to find the aggregate record
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param home specifies whether to return the home or road records, true for home, false for away
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all home or away games for the specified criteria
	 */
	public RecordAggregator getHomeAwayRecord(TeamForSeason tfs, NEC subseasonType, boolean home, boolean againstSpread);
	
	/** Finds the aggregate Divisional record for the specified team for the specified subeseason
	 * 
	 * @param tfs the TeamForSeason for which to find the aggregate record
	 * @param division the Division object representing the division for which to compare this team's record against
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all divisonal games for the specified criteria
	 */
	public RecordAggregator getDivisionRecord(TeamForSeason tfs, Division division, NEC subseasonType, boolean againstSpread);
	
	/** Finds the aggregate Conference record for the specified team for the specified subeseason
	 * 
	 * @param tfs the TeamForSeason for which to find the aggregate record
	 * @param conference the Conference object representing the conference for which to compare this team's record against
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all conference games for the specified criteria
	 */
	public RecordAggregator getConferenceRecord(TeamForSeason tfs, Conference conference, NEC subseasonType, boolean againstSpread);
	
	/** Aggregates the 5 most recent games played against the specified opponent, using the specified subseason
	 * 
	 * @param tfs the TeamForSeason for which to find the aggregate record
	 * @param opponent the opponent TeamForSeason to use as a comparison
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all recent games between opponents for the specified criteria
	 */
	public RecordAggregator getRecentRecordAgainstOpponent(TeamForSeason tfs, TeamForSeason opponent, NEC subseasonType, boolean againstSpread);
	
	/** Finds the aggregate record for Primetime games (Non-Sunday and/or Sunday Night games) for the specified subseason
	 * 
	 * @param tfs the TeamForSeason for which to find the aggregate record
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all primetime games for the specified criteria
	 */
	public RecordAggregator getPrimetimeRecord(TeamForSeason tfs, NEC subseasonType, boolean againstSpread);
	
	/** Finds the aggregate record for games played in the specified month, on the specified day, or at the specified time for the specified subseason (at least one optional field must be specified)
	 * 
	 * @param tfs the TeamForSeason for which to find the aggregate record
	 * @param month (Optional) GregorianCalendar.MONTH int representing the month in which to filter games 
	 * @param dayOfWeek (Optional) GregorianCalendar.DAY_OF_WEEK int representing the day for which to filter games
	 * @param kickoffHour (Optional) GregorianCalendar.HOUR_OF_DAY int representing the kickoff time for which to filter games
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all games for the specified Date/Time criteria
	 */
	public RecordAggregator getRecordByDateTime(TeamForSeason tfs, Integer month, Integer dayOfWeek, Integer kickoffHour, NEC subseasonType, boolean againstSpread);
	
	/** Finds the aggregate record for games played in the specified Stadium, or with the specified Roof Type for the specified subseason (one or both must be specified)
	 * 
	 * @param tfs the TeamForSeason for which to find the aggregate record
	 * @param stadium (Optional) the Stadium object representing the stadium in which games filtered for this aggregation are played
	 * @param roofType (Optional) a RoofType filter used to select games specifically played in stadiums having the specified RoofType
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all games for the specified Stadium/RoofType criteria
	 */
	public RecordAggregator getRecordForStadium(TeamForSeason tfs, Stadium stadium, RoofType roofType, NEC subseasonType, boolean againstSpread);
	
	/** Finds the aggregate record for games played in the specified timezone for the specified subseason
	 * 
	 * @param tfs the TeamForSeason for which to find the aggregate record
	 * @param timezone a TimeZone filter used to select games played in the specified timezone
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all games for the specified Timezone criteria
	 */
	public RecordAggregator getRecordByTimezone(TeamForSeason tfs, TimeZone timezone, NEC subseasonType, boolean againstSpread);
	
}

