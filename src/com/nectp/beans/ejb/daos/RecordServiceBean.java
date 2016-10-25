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
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.Pick.PickType;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Game.GameStatus;

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
	public Record selectWeekRecordForAtfs(Week week, AbstractTeamForSeason atfs, NEC recordType) throws NoExistingEntityException {
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
				throw new NoExistingEntityException(e);
			} catch (Exception e) {
				log.severe("Exception caught retrieving record: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return record;
	}

	@Override
	public RecordAggregator getAggregateRecordForAtfsForType(AbstractTeamForSeason atfs, NEC recordType, boolean againstSpread) {
		RecordAggregator agg = new RecordAggregator(atfs, againstSpread);
		if (atfs == null || recordType == null) {
			log.severe("ATFS/RecordType not specified, can not retrieve aggregate record.");
		}
		else {
			//	Get the current week in the season
			Season season = atfs.getSeason();
			
			//	Create a list to store all of the weeks corresponding to the specified record (using default ranges)
			List<Week> weeksForRecord = getWeekRangeList(season, recordType, null, null);
			
			for (Week week : weeksForRecord) {
				Record record = null;
				try {
					NEC searchType = recordType;
					if (recordType == NEC.SEASON) {
						searchType = week.getSubseason().getSubseasonType();
					}
					record = selectWeekRecordForAtfs(week, atfs, searchType);
					agg.addRecord(record);
				} catch (NoExistingEntityException e) {
					log.warning("No Record found for week " + week.getWeekNumber() + " for " + atfs.getNickname());
				}
			}
		}
		
		return agg;
	}
	
	@Override
	public RecordAggregator getRecordForConcurrentWeeksForAtfs(AbstractTeamForSeason atfs, Integer startWeek, Integer endWeek, NEC recordType, boolean againstSpread) {
		RecordAggregator agg = new RecordAggregator(atfs, againstSpread);
		if (atfs == null || recordType == null) {
			log.severe("ATFS/RecordType not specified, can not retrieve aggregate record.");
		}
		else {
			//	Get the current week in the season
			Season season = atfs.getSeason();
			
			//	Create a list to store all of the weeks corresponding to the specified record
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
				} catch (NoExistingEntityException e) {
					log.warning("No Record found for week " + w.getWeekNumber() + " for " + atfs.getNickname());
				}
			}
		}
		
		return agg;
	}
	
	@Override
	public RecordAggregator getOverallRecordThroughWeekForAtfs(AbstractTeamForSeason atfs, Week week, NEC recordType, boolean againstSpread) {
		RecordAggregator agg = new RecordAggregator(atfs, againstSpread);
		if (atfs == null || recordType == null) {
			log.severe("ATFS/RecordType not specified, can not retrieve aggregate record.");
		}
		else {
			//	Get the current week in the season
			Season season = atfs.getSeason();
			
			//	Create a list to store all of the weeks corresponding to the specified record - if in playoffs, and season record type, get only regular season weeks
			int endWeek = week.getWeekNumber();
			if (recordType == NEC.SEASON) {
				NEC subseasonType = week.getSubseason().getSubseasonType();
				if (subseasonType == NEC.PLAYOFFS || subseasonType == NEC.SUPER_BOWL) {
					endWeek = season.getPlayoffStartWeek() - 1;
				}
			}
			List<Week> weeksForRecord = weekService.selectConcurrentWeeksInRangeInSeason(season, 1, endWeek);
			
			for (Week w : weeksForRecord) {
				Record record = null;
				try {
					NEC searchType = recordType;
					if (recordType == NEC.SEASON) {
						searchType = w.getSubseason().getSubseasonType();
					}
					record = selectWeekRecordForAtfs(w, atfs, searchType);
					agg.addRecord(record);
				} catch (NoExistingEntityException e) {
					log.warning("No Record found for week " + w.getWeekNumber() + " for " + atfs.getNickname());
				}
			}
		}
		
		return agg;
	}
	
	private TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getAtfsRankedScoresByType(List<AbstractTeamForSeason> atfsList, Comparator<RecordAggregator> comparator, NEC recordType, Season season, boolean againstSpread) {
		if (comparator == null) {
			comparator = Collections.reverseOrder();
		}
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> rankMap = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(comparator);
		for (AbstractTeamForSeason afts : atfsList) {
			RecordAggregator agg = getAggregateRecordForAtfsForType(afts, recordType, againstSpread);
			if (rankMap.containsKey(agg)) {
				rankMap.get(agg).add(afts);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(afts);
				rankMap.put(agg, rankList);
			}
		}
		
		return rankMap;
	}
	
	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getTeamRankedScoresForType(NEC recordType, Season season, boolean againstSpread) {
		List<AbstractTeamForSeason> teams = new ArrayList<AbstractTeamForSeason>(season.getTeams());
		return getAtfsRankedScoresByType(teams, null, recordType, season, againstSpread);
	}

	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getPlayerRankedScoresForType(NEC recordType, Season season, boolean againstSpread) {
		//	Get the list of players from the season
		List<AbstractTeamForSeason> players = new ArrayList<AbstractTeamForSeason>(season.getPlayers());

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

		return getAtfsRankedScoresByType(players, rankComparator, recordType, season, againstSpread);
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
				} catch (NoExistingEntityException e) {
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
			} catch (NoExistingEntityException e) {
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
	public void updateRecordForPlayerPick(Pick p) {
		Logger log = Logger.getLogger(PickServiceBean.class.getName());
		if (p == null) {
			log.warning("No pick specified! can not update player records.");
		}
		else {
			Game game = p.getGame();
			if (game == null || game.getGameStatus() != GameStatus.FINAL) {
				log.severe("Game either not defined or not complete. Skipping update.");
				return;
			}
			Record applicableRecord = p.getApplicableRecord();
			TeamForSeason pickedTeam = p.getPickedTeam();
			
			boolean recordUpdated = false;
			
			//	If the pick is independent of the spread, add a raw win/loss based on the pick
			TeamForSeason winner;
			if (p.getPickType().equals(PickType.STRAIGHT_UP)) {
				winner = game.getWinner();
				if (winner == null) {
					applicableRecord.addTie();
					recordUpdated = true;
				}
				else if (pickedTeam.equals(winner)) {
					applicableRecord.addWin();
					recordUpdated = true;
				}
				else {
					applicableRecord.addLoss();
					recordUpdated = true;
				}
			}
			//	If the pick is dependent on spread2, add a win/loss ATS based on the pick
			else if (p.getPickType().equals(PickType.SPREAD2)) {
				winner = game.getWinnerATS2();
				if (winner == null) {
					applicableRecord.addTie();
					recordUpdated = true;
				}
				else if (pickedTeam.equals(winner)) {
					applicableRecord.addWinATS2();
					recordUpdated = true;
				}
				else {
					applicableRecord.addLossATS2();
					recordUpdated = true;
				}
			}
			//	If the pick is dependent on spread1, add a win/loss ATS based on the pick	
			else {
				winner = game.getWinnerATS1();
				if (winner == null) {
					applicableRecord.addTie();
					recordUpdated = true;
				}
				else if (pickedTeam.equals(winner)) {
					applicableRecord.addWinATS1();
					recordUpdated = true;
				}
				else {
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
