package com.nectp.beans.ejb.daos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.Pick.PickType;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Stateless
public class RecordServiceBean extends DataServiceBean<Record> implements RecordService {
	private static final long serialVersionUID = -9223035500861134009L;

	@EJB
	private SubseasonService subseasonService;
	
	@EJB
	private WeekService weekService;
	
	private Logger log;
	
	public RecordServiceBean() {
		log = Logger.getLogger(RecordServiceBean.class.getName());
	}
	
	@Override
	public Record selectWeekRecordForAtfs(Week week, AbstractTeamForSeason atfs, NEC recordType) {
		Record record = null;
		if (week == null || atfs == null || recordType == null) {
			log.severe("Parameters not specified, can not select Record.");
		}
		else {
			TypedQuery<Record> rq = em.createNamedQuery("Record.selectWeekRecordForAtfs", Record.class);
			rq.setParameter("weekId", week.getWeekId());
			rq.setParameter("atfsId", atfs.getAbstractTeamForSeasonId());
			//	If querying for SEASON records, get the subseason type for this specific query
			if (NEC.SEASON.equals(recordType)) {
				recordType = week.getSubseason().getSubseasonType();
			}
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
		boolean ats = (recordType != NEC.TWO_AND_OUT && recordType != NEC.ONE_AND_OUT);
		RecordAggregator agg = new RecordAggregator(atfs, ats);
		if (atfs == null || recordType == null) {
			log.severe("ATFS/RecordType not specified, can not retrieve aggregate record.");
		}
		else {
			//	Get the current week in the season
			Season season = atfs.getSeason();
			
			//	Create a list to store all of the weeks corresponding to the specified record (using default ranges)
			List<Week> weeksForRecord = getWeekRangeList(season, recordType, null, null);
			
//			Week currentWeek = null;
//			try {
//				currentWeek = weekService.selectCurrentWeekInSeason(season);
//			} catch (NoResultException e) {
//				log.severe("Could not get the current week in the season! Can not get the current record.");
//				return agg;
//			}
//			//	Create a list to store all of the weeks corresponding to the specified record
//			ArrayList<Week> weeksForRecord = new ArrayList<Week>();
//			Subseason subseason = currentWeek.getSubseason();
//			
//			//	Based on the specified record type, get the correct amount of weeks for the record
//			//	If the current subseason is the specified record type, 
//			//	get the weeks in the subseason leading up to this week
//			if (subseason.getSubseasonType().equals(recordType.ordinal())) {
//				for (Week w : subseason.getWeeks()) {
//					if (w.getWeekNumber() <= currentWeek.getWeekNumber()) {
//						weeksForRecord.add(w);
//					}
//				}
//			}
//			//	If the record type is not this subseason, but is related to a subseason, 
//			//	get all of the weeks in the specified subseason
//			else if (recordType == NEC.FIRST_HALF || recordType == NEC.SECOND_HALF || 
//					 recordType == NEC.PLAYOFFS || recordType == NEC.SUPER_BOWL) {
//				try {
//					subseason = subseasonService.selectSubseasonInSeason(recordType, season);
//				} catch (NoResultException e) {
//					log.severe("Failed to retrieve specified subseason! can not get record for weeks.");
//					return agg;
//				}
//				
//				weeksForRecord.addAll(subseason.getWeeks());
//			}
//			else {
//				//	If the record type is a season-long record (other than season) type, get all weeks up until current week
//				weeksForRecord.addAll(weekService.selectWeeksThroughCurrentWeekInSeason(season));
//			}
			
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
	public RecordAggregator getRecordForConcurrentWeeksForAtfs(AbstractTeamForSeason atfs, Integer startWeek, Integer endWeek, NEC recordType) {
		boolean ats = (recordType != NEC.TWO_AND_OUT && recordType != NEC.ONE_AND_OUT);
		RecordAggregator agg = new RecordAggregator(atfs, ats);
		if (atfs == null || recordType == null) {
			log.severe("ATFS/RecordType not specified, can not retrieve aggregate record.");
		}
		else {
			//	Get the current week in the season
			Season season = atfs.getSeason();
			
			//	Create a list to store all of the weeks corresponding to the specified record
//			List<Week> weeksForRecord = weekService.selectConcurrentWeeksInRangeInSeason(season, startWeek.getWeekNumber(), endWeek.getWeekNumber());
			List<Week> weeksForRecord = getWeekRangeList(season, recordType, startWeek, endWeek);
			
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
		boolean ats = (recordType != NEC.TWO_AND_OUT && recordType != NEC.ONE_AND_OUT);
		RecordAggregator agg = new RecordAggregator(atfs, ats);
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
		
		//	Create a comparator for the treemap: use reverse natural ordering for all but Moneyback condition (Raggs compare themselves)
		Comparator<RecordAggregator> rankComparator;
		//	If moneyback condition, rank by natural ordering for eligible players, put players having won a prize at the end;
		if (recordType.equals(NEC.MONEY_BACK)) {
			rankComparator = new Comparator<RecordAggregator>() {
				@Override
				public int compare(RecordAggregator ragg1, RecordAggregator ragg2) {
					//	Check whether either player for each ragg has won a prize
					Record ragg1Record = ragg1.getRecords().get(0);
					Pick ragg1Pick = ragg1Record.getPicksInRecord().get(0);
					PlayerForSeason player1 = ragg1Pick.getPlayer();
					boolean player1Winner = player1.getPrizesWon().size() > 0;

					Record ragg2Record = ragg2.getRecords().get(0);
					Pick ragg2Pick = ragg2Record.getPicksInRecord().get(0);
					PlayerForSeason player2 = ragg2Pick.getPlayer();
					boolean player2Winner = player2.getPrizesWon().size() > 0;

					//	If both teams have won, return the comparsion over record
					if (player1Winner && player2Winner) {
						return ragg1.compareTo(ragg2);
					}
					//	If only player1 has won, sort it after player2
					else if (player1Winner) {
						return 1;
					}
					//	If only player2 has won, sort it after player1
					else if (player2Winner) {
						return -1;
					}
					//	If neither team has won, use natural ordering
					else {
						return ragg1.compareTo(ragg2);
					}
				}
			};
		}
		//	If normal condition, order by highest total score
		else {
			rankComparator = Collections.reverseOrder();
		}
		TreeMap<RecordAggregator, List<PlayerForSeason>> rankMap = new TreeMap<RecordAggregator, List<PlayerForSeason>>(rankComparator);
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
	
	private List<Week> getWeekRangeList(Season currentSeason, NEC recordType, Integer startWeek, Integer endWeek) {
		//	If the start week is defined, set the range start to the specified week
		if (startWeek == null) {
			startWeek = getStartWeekByType(currentSeason, recordType);
		}
		if (endWeek == null) {
			endWeek = getEndWeek(currentSeason, recordType);
		}

		return weekService.selectConcurrentWeeksInRangeInSeason(currentSeason, startWeek, endWeek);
	}

	private Integer getEndWeek(Season currentSeason, NEC recordType) {
		Integer endWeek = null;

		//	If the record type corresponds to a subseason type, get the end week contingent on whether or not it is the current subseason
		if (recordType == NEC.FIRST_HALF || recordType == NEC.SECOND_HALF || recordType == NEC.PLAYOFFS || recordType == NEC.SUPER_BOWL) {
			endWeek = getEndWeekForType(currentSeason, recordType);
		}
		//	If the end week is not contingent on a subseason (season-long record) get the current week or last week in regular season
		if (endWeek == null) {
			Week currentWeek = currentSeason.getCurrentWeek();
			int currentWeekNum = currentWeek.getWeekNumber();
			//	If the current week is in the playoffs, and are selecting a season-long record, get the end week of only the regular season
			if (currentWeekNum >= currentSeason.getPlayoffStartWeek()) {
				try {
					Subseason firstHalf = subseasonService.selectSubseasonInSeason(NEC.FIRST_HALF, currentSeason);
					Subseason secondHalf = subseasonService.selectSubseasonInSeason(NEC.SECOND_HALF, currentSeason);
					endWeek = firstHalf.getWeeks().size() + secondHalf.getWeeks().size();
				} catch (NoResultException e) {
					log.warning("No results found for subseason queries, defaulting to playoff week minus one.");
					endWeek = currentSeason.getPlayoffStartWeek() - 1;
				}
			}
			//	If not past the playoff start, use the current week number as the range end
			else {
				endWeek = currentWeekNum;
			}
		}

		return endWeek;
	}

	private Integer getEndWeekForType(Season currentSeason, NEC ssType) {
		Week currentWeek = currentSeason.getCurrentWeek();
		Subseason currentSubseason = currentWeek.getSubseason();
		Integer endWeek = null;
		if (ssType.equals(currentSubseason.getSubseasonType())) {
			endWeek = currentWeek.getWeekNumber();
		}
		else {
			try {
				Subseason subseason = subseasonService.selectSubseasonInSeason(ssType, currentSeason);
				endWeek = subseason.getWeeks().size();
			} catch (NoResultException e) {
				log.warning("No subseason found for " + ssType.toString() + ", can not get the end week!");
			}
		}
		return endWeek;
	}

	private Integer getStartWeekByType(Season currentSeason, NEC recordType) {
		Integer startWeek;
		if (recordType != null) {
			switch (recordType) {
			case SECOND_HALF:
				startWeek = currentSeason.getSecondHalfStartWeek();
				break;
			case PLAYOFFS:
				startWeek = currentSeason.getPlayoffStartWeek();
				break;
			case SUPER_BOWL:
				startWeek = currentSeason.getSuperbowlWeek();
				break;
			default:
				startWeek = 1;
				break;
			}
		}
		else startWeek = 1;

		return startWeek;
	}
	
	@Override
	public void updateRecordForPlayerPick(Pick p, TeamForSeason winningTeam, TeamForSeason losingTeam) {
		Logger log = Logger.getLogger(PickServiceBean.class.getName());
		if (p == null) {
			log.warning("No pick specified! can not update player records.");
		}
		else {
			Record applicableRecord = p.getApplicableRecord();
			TeamForSeason pickedTeam = p.getPickedTeam();
			
			boolean recordUpdated = false;
			
			//	If the pick is independent of the spread, add a raw win/loss based on the pick
			if (p.getPickType().equals(PickType.STRAIGHT_UP)) {
				if (pickedTeam.equals(winningTeam)) {
					applicableRecord.addWin();
					recordUpdated = true;
				}
				else if (pickedTeam.equals(losingTeam)) {
					applicableRecord.addLoss();
					recordUpdated = true;
				}
			}
			//	If the pick is dependent on spread2, add a win/loss ATS based on the pick
			else if (p.getPickType().equals(PickType.SPREAD2)) {
				if (pickedTeam.equals(winningTeam)) {
					applicableRecord.addWinATS2();
					recordUpdated = true;
				}
				else if (pickedTeam.equals(losingTeam)) {
					applicableRecord.addLossATS2();
					recordUpdated = true;
				}
			}
			//	If the pick is dependent on spread1, add a win/loss ATS based on the pick	
			else {
				if (pickedTeam.equals(winningTeam)) {
					applicableRecord.addWinATS1();
					recordUpdated = true;
				}
				else if (pickedTeam.equals(losingTeam)) {
					applicableRecord.addLossATS1();
					recordUpdated = true;
				}
			}
			
			if (recordUpdated) {
				update(applicableRecord);
			}
		} 
	}
}
