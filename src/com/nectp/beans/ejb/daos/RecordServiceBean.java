package com.nectp.beans.ejb.daos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.RecordService;
import com.nectp.beans.remote.daos.SubseasonService;
import com.nectp.beans.remote.daos.WeekService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.Week;

@Stateless
public class RecordServiceBean extends DataServiceBean<Record> implements RecordService {
	private static final long serialVersionUID = -9223035500861134009L;

	@EJB
	private SubseasonService subseasonService;
	
	@EJB
	private WeekService weekService;
	
	@Override
	public Record selectWeekRecordForAtfs(Week week, AbstractTeamForSeason atfs, NEC recordType) {
		Logger log = Logger.getLogger(RecordServiceBean.class.getName());
		Record record = null;
		if (week == null || atfs == null || recordType == null) {
			log.severe("Parameters not specified, can not select Record.");
		}
		else {
			TypedQuery<Record> rq = em.createNamedQuery("Record.selectWeekRecordForAtfs", Record.class);
			rq.setParameter("weekId", week.getWeekId());
			rq.setParameter("atfsId", atfs.getAbstractTeamForSeasonId());
			rq.setParameter("recordType", recordType.ordinal());
			try {
				record = rq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple records found for " + atfs.getNickname() + " in week " 
						+ week.getWeekNumber() + " for " + recordType.name());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No records found for " + atfs.getNickname() + " in week " 
						+ week.getWeekNumber() + " for " + recordType.name());
				log.warning(e.getMessage());
				throw new NoResultException();
			} catch (Exception e) {
				log.severe("Exception caught retrieving record: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return record;
	}

	@Override
	public RecordAggregator getAggregateRecordForAtfsForType(AbstractTeamForSeason atfs, NEC recordType) {
		Logger log = Logger.getLogger(RecordServiceBean.class.getName());
		RecordAggregator agg = new RecordAggregator(atfs);
		if (atfs == null || recordType == null) {
			log.severe("ATFS/RecordType not specified, can not retrieve aggregate record.");
		}
		else {
			//	Get the current week in the season
			Season season = atfs.getSeason();
			Week currentWeek = null;
			try {
				currentWeek = weekService.selectCurrentWeekInSeason(season);
			} catch (NoResultException e) {
				log.severe("Could not get the current week in the season! Can not get the current record.");
				return agg;
			}
			//	Create a list to store all of the weeks corresponding to the specified record
			ArrayList<Week> weeksForRecord = new ArrayList<Week>();
			Subseason subseason = currentWeek.getSubseason();
			
			//	Based on the specified record type, get the correct amount of weeks for the record
			//	If the current subseason is the specified record type, 
			//	get the weeks in the subseason leading up to this week
			if (subseason.getSubseasonType().equals(recordType.ordinal())) {
				for (Week w : subseason.getWeeks()) {
					if (w.getWeekNumber() <= currentWeek.getWeekNumber()) {
						weeksForRecord.add(w);
					}
				}
			}
			//	If the record type is not this subseason, but is related to a subseason, 
			//	get all of the weeks in the specified subseason
			else if (recordType == NEC.FIRST_HALF || recordType == NEC.SECOND_HALF || 
					 recordType == NEC.PLAYOFFS || recordType == NEC.SUPER_BOWL) {
				try {
					subseason = subseasonService.selectSubseasonInSeason(recordType, season);
				} catch (NoResultException e) {
					log.severe("Failed to retrieve specified subseason! can not get record for weeks.");
					return agg;
				}
				
				weeksForRecord.addAll(subseason.getWeeks());
			}
			else {
				//	If the record type is a season-long record (other than season) type, get all weeks up until current week
				weeksForRecord.addAll(weekService.selectWeeksThroughCurrentWeekInSeason(season));
			}
			
			for (Week week : weeksForRecord) {
				Record record = null;
				try {
					NEC searchType = recordType;
					if (recordType == NEC.SEASON) {
						searchType = week.getSubseason().getSubseasonType();
					}
					record = selectWeekRecordForAtfs(week, atfs, searchType);
					agg.addRecord(record);
				} catch (NoResultException e) {
					log.warning("No Record found for week " + week.getWeekNumber() + " for " + atfs.getNickname());
				}
			}
		}
		
		return agg;
	}
	
	@Override
	public RecordAggregator getRecordForConcurrentWeeksForAtfs(AbstractTeamForSeason atfs, Week startWeek, Week endWeek,
			NEC recordType) {
		Logger log = Logger.getLogger(RecordServiceBean.class.getName());
		RecordAggregator agg = new RecordAggregator(atfs);
		if (atfs == null || recordType == null) {
			log.severe("ATFS/RecordType not specified, can not retrieve aggregate record.");
		}
		else {
			//	Get the current week in the season
			Season season = atfs.getSeason();
			
			//	Create a list to store all of the weeks corresponding to the specified record
			List<Week> weeksForRecord = weekService.selectConcurrentWeeksInRangeInSeason(season, startWeek.getWeekNumber(), endWeek.getWeekNumber());
			
			for (Week w : weeksForRecord) {
				Record record = null;
				try {
					NEC searchType = recordType;
					if (recordType == NEC.SEASON) {
						searchType = w.getSubseason().getSubseasonType();
					}
					record = selectWeekRecordForAtfs(w, atfs, searchType);
					agg.addRecord(record);
				} catch (NoResultException e) {
					log.warning("No Record found for week " + w.getWeekNumber() + " for " + atfs.getNickname());
				}
			}
		}
		
		return agg;
	}
	
	@Override
	public RecordAggregator getOverallRecordThroughWeekForAtfs(AbstractTeamForSeason atfs, Week week, NEC recordType) {
		Logger log = Logger.getLogger(RecordServiceBean.class.getName());
		RecordAggregator agg = new RecordAggregator(atfs);
		if (atfs == null || recordType == null) {
			log.severe("ATFS/RecordType not specified, can not retrieve aggregate record.");
		}
		else {
			//	Get the current week in the season
			Season season = atfs.getSeason();
			
			//	Create a list to store all of the weeks corresponding to the specified record
			List<Week> weeksForRecord = weekService.selectConcurrentWeeksInRangeInSeason(season, 1, week.getWeekNumber());
			
			for (Week w : weeksForRecord) {
				Record record = null;
				try {
					NEC searchType = recordType;
					if (recordType == NEC.SEASON) {
						searchType = w.getSubseason().getSubseasonType();
					}
					record = selectWeekRecordForAtfs(w, atfs, searchType);
					agg.addRecord(record);
				} catch (NoResultException e) {
					log.warning("No Record found for week " + w.getWeekNumber() + " for " + atfs.getNickname());
				}
			}
		}
		
		return agg;
	}

	@Override
	public TreeMap<RecordAggregator, List<PlayerForSeason>> getPlayerForSeasonRankedScoresForType(NEC recordType, Season season) {
		//	Get the list of players from the season
		List<PlayerForSeason> players = season.getPlayers();
		TreeMap<RecordAggregator, List<PlayerForSeason>> rankMap = new TreeMap<RecordAggregator, List<PlayerForSeason>>(Collections.reverseOrder());
		for (PlayerForSeason player : players) {
			RecordAggregator agg = getAggregateRecordForAtfsForType(player, recordType);
			if (rankMap.containsKey(agg)) {
				rankMap.get(agg).add(player);
			}
			else {
				List<PlayerForSeason> rankList = new ArrayList<PlayerForSeason>();
				rankList.add(player);
				rankMap.put(agg, rankList);
			}
		}
		
		return rankMap;
	}
	
}
