package com.nectp.beans.remote.daos;

import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Stadium.RoofType;

public interface StatisticService<T> extends RecordService {

	/** Finds the aggregate Home or Away record for the specified team for the specified subseason
	 * 
	 * @param tfs the AbstractTeamForSeason for which to find the aggregate record
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param home specifies whether to return the home or road records, true for home, false for away
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all home or away games for the specified criteria
	 */
	public RecordAggregator getHomeAwayRecord(T tfs, NEC subseasonType, boolean home, boolean againstSpread);
	
	/** Compiles a map of Teams in ranked order of Home/Away records
	 * 
	 * @param tfs the AbstractTeamForSeason for which to use as a basis for the ranking
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param home specifies whether to return the home or road records, true for home, false for away
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an ordered map, where each entry maps a list of AbstractTeamForSeason objects that have the same record
	 */
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getHomeAwayRank(T tfs, NEC subseasonType, boolean home, boolean againstSpread);
	
	/** Finds the aggregate Divisional record for the specified team for the specified subeseason
	 * 
	 * @param tfs the AbstractTeamForSeason for which to find the aggregate record
	 * @param division the Division object representing the division for which to compare this team's record against
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all divisonal games for the specified criteria
	 */
	public RecordAggregator getDivisionRecord(T tfs, Division division, NEC subseasonType, boolean againstSpread);
	
	/** Compiles a map of Teams in ranked order of Divisional records
	 * 
	 * @param tfs the AbstractTeamForSeason for which to use as a basis for the ranking 
	 * @param division the Division object representing the division for which to compare this team's record against
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an ordered map, where each entry maps a list of AbstractTeamForSeason objects that have the same record
	 */
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getDivisionRank(T tfs, Division division, NEC subseasonType, boolean againstSpread);
	
	/** Finds the aggregate Conference record for the specified team for the specified subeseason
	 * 
	 * @param tfs the AbstractTeamForSeason for which to find the aggregate record
	 * @param conference the Conference object representing the conference for which to compare this team's record against
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all conference games for the specified criteria
	 */
	public RecordAggregator getConferenceRecord(T tfs, Conference conference, NEC subseasonType, boolean againstSpread);
	
	/** Compiles a map of Teams in ranked order of Conference records
	 * 
	 * @param tfs the AbstractTeamForSeason for which to use as a basis for the ranking 
	 * @param conference the Conference object representing the conference for which to compare this team's record against
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an ordered map, where each entry maps a list of AbstractTeamForSeason objects that have the same record
	 */
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getConferenceRank(T tfs, Conference conference, NEC subseasonType, boolean againstSpread);
	
	/** Finds the aggregate record for Primetime games (Non-Sunday and/or Sunday Night games) for the specified subseason
	 * 
	 * @param tfs the AbstractTeamForSeason for which to find the aggregate record
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all primetime games for the specified criteria
	 */
	public RecordAggregator getPrimetimeRecord(T tfs, NEC subseasonType, boolean againstSpread);
	
	/** Compiles a map of Teams in ranked order of Primetime records
	 * 
	 * @param tfs the AbstractTeamForSeason for which to use as a basis for the ranking 
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an ordered map, where each entry maps a list of AbstractTeamForSeason objects that have the same record
	 */
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getPrimetimeRank(T tfs, NEC subseasonType, boolean againstSpread);
	
	/** Finds the aggregate record for games played in the specified month, on the specified day, or at the specified time for the specified subseason (at least one optional field must be specified)
	 * 
	 * @param tfs the AbstractTeamForSeason for which to find the aggregate record
	 * @param month (Optional) GregorianCalendar.MONTH int representing the month in which to filter games 
	 * @param dayOfWeek (Optional) GregorianCalendar.DAY_OF_WEEK int representing the day for which to filter games
	 * @param kickoffHour (Optional) GregorianCalendar.HOUR_OF_DAY int representing the kickoff time for which to filter games
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all games for the specified Date/Time criteria
	 */
	public RecordAggregator getRecordByDateTime(T tfs, Integer month, Integer dayOfWeek, Integer kickoffHour, NEC subseasonType, boolean againstSpread);
	
	/** Compiles a map of Teams in ranked order of Date/Time records
	 * 
	 * @param tfs the AbstractTeamForSeason for which to use as a basis for the ranking 
	 * @param month (Optional) GregorianCalendar.MONTH int representing the month in which to filter games 
	 * @param dayOfWeek (Optional) GregorianCalendar.DAY_OF_WEEK int representing the day for which to filter games
	 * @param kickoffHour (Optional) GregorianCalendar.HOUR_OF_DAY int representing the kickoff time for which to filter games
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an ordered map, where each entry maps a list of AbstractTeamForSeason objects that have the same record
	 */
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getDateTimeRank(T tfs, Integer month, Integer dayOfWeek, Integer kickoffHour, NEC subseasonType, boolean againstSpread);
	
	/** Finds the aggregate record for games played in the specified Stadium, or with the specified Roof Type for the specified subseason (one or both must be specified)
	 * 
	 * @param tfs the AbstractTeamForSeason for which to find the aggregate record
	 * @param stadium (Optional) the Stadium object representing the stadium in which games filtered for this aggregation are played
	 * @param roofType (Optional) a RoofType filter used to select games specifically played in stadiums having the specified RoofType
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all games for the specified Stadium/RoofType criteria
	 */
	public RecordAggregator getRecordForStadium(T tfs, Stadium stadium, RoofType roofType, NEC subseasonType, boolean againstSpread);
	
	/** Compiles a map of Teams in ranked order of Stadium records 
	 * 
	 * @param tfs the AbstractTeamForSeason for which to use as a basis for the ranking 
	 * @param stadium (Optional) the Stadium object representing the stadium in which games filtered for this aggregation are played
	 * @param roofType (Optional) a RoofType filter used to select games specifically played in stadiums having the specified RoofType
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an ordered map, where each entry maps a list of AbstractTeamForSeason objects that have the same record
	 */
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getStadiumRank(T tfs, Stadium stadium, RoofType roofType, NEC subseasonType, boolean againstSpread);
	
	/** Finds the aggregate record for games played in the specified timezone for the specified subseason
	 * 
	 * @param tfs the AbstractTeamForSeason for which to find the aggregate record
	 * @param timezone a TimeZone filter used to select games played in the specified timezone
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an aggregation of records combining all games for the specified Timezone criteria
	 */
	public RecordAggregator getRecordByTimezone(T tfs, TimeZone timezone, NEC subseasonType, boolean againstSpread);
	
	/** Compiles a map of Teams in ranked order of Timezone records 
	 * 
	 * @param tfs the AbstractTeamForSeason for which to use as a basis for the ranking 
	 * @param timezone a TimeZone filter used to select games played in the specified timezone
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an ordered map, where each entry maps a list of AbstractTeamForSeason objects that have the same record
	 */
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getTimezoneRank(T tfs, TimeZone timezone, NEC subseasonType, boolean againstSpread);
	
	/** Compiles a map of Teams in ranked order of their combined MNF/TNT records
	 * 
	 * @param tfs the AbstractTeamForSeason for which to use as a basis for the ranking 
	 * @param subseasonType a NEC filter used to select specific subeasons for the aggregation, use NEC.SEASON to select all games
	 * @param againstSpread boolean used for sorting/equality purposes only, in determining rank ordering
	 * @return an ordered map, where each entry maps a list of AbstractTeamForSeason objects that have the same record
	 */
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getMnfTntRank(T tfs, NEC subseasonType, boolean againstSpread);
}
