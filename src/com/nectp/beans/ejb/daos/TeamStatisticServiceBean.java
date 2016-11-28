package com.nectp.beans.ejb.daos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.TeamStatisticService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Stadium.RoofType;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;

@Stateless
public class TeamStatisticServiceBean extends RecordServiceBean implements TeamStatisticService {
	private static final long serialVersionUID = -1785646140524328572L;
	
	private Logger log;
	
	public TeamStatisticServiceBean() {
		log = Logger.getLogger(TeamStatisticServiceBean.class.getName());
	}

	/**
	 * 
	 * @param games
	 * @return
	 */
	private List<Game> getGamesSortedByWeekNumber(List<Game> games) {
		Collections.sort(games, new Comparator<Game>() {
			@Override
			public int compare(Game g1, Game g2) {
				return g1.getWeek().getWeekNumber().compareTo(g2.getWeek().getWeekNumber());
			}
		});
		return games;
	}
	
	/**
	 * 
	 * @param records
	 * @return
	 */
	private List<Record> getRecordsSortedByWeekNumber(List<Record> records) {
		Collections.sort(records, new Comparator<Record>() {
			@Override
			public int compare(Record r1, Record r2) {
				return r1.getWeek().getWeekNumber().compareTo(r2.getWeek().getWeekNumber());
			}
		});
		return records;
	}
	
	/** Finds played games who's week matches the team's records' week, and adds the record to the aggregate
	 * 
	 * @param ragg
	 * @param records
	 * @param games
	 */
	private void addRecordsToAggregate(RecordAggregator ragg, List<Record> records, List<Game> games) {
		for (Record r : records) {
			Week weekForRecord = r.getWeek();
			for (Game g : games) {
				if (g.getWeek().equals(weekForRecord)) {
					ragg.addRecord(r);
					break;
				}
			}
		}
	}
	
	@Override
	public RecordAggregator getHomeAwayRecord(TeamForSeason tfs, NEC subseasonType, boolean home, boolean againstSpread) {
		RecordAggregator homeAwayAgg = null;
		if (tfs == null || subseasonType == null) {
			log.warning("No team/type specified, can not get home/away record.");
		}
		else {
			homeAwayAgg = new RecordAggregator(tfs, againstSpread);
			
			//	Get the games & the records for the TFS, sorting both by week number
			List<Game> games;
			if (subseasonType.equals(NEC.SEASON)) {
				if (home) games = getGamesSortedByWeekNumber(tfs.getHomeGames());
				else games = getGamesSortedByWeekNumber(tfs.getAwayGames());
				log.info("Games for home/away record: " + games.size());
			}
			else {
				List<Game> subseasonGames = new ArrayList<Game>();
				List<Game> homeAwayGames;
				if (home) homeAwayGames = tfs.getHomeGames();
				else homeAwayGames = tfs.getAwayGames();
				
				for (Game g : homeAwayGames) {
					if (g.getWeek().getSubseason().getSubseasonType().equals(subseasonType)) {
						subseasonGames.add(g);
					}
				}
				log.info("Games for home/away record: " + subseasonGames.size());
				games = getGamesSortedByWeekNumber(subseasonGames);
			}
			List<Record> records = getRecordsSortedByWeekNumber(tfs.getRecords());
			log.info("Home/Away records: " + records.size());
			//	For any home games whose week number matches a record's week number, add the record to the aggregate
			addRecordsToAggregate(homeAwayAgg, records, games);
		}
		
		return homeAwayAgg;
	}
	
	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getHomeAwayRank(TeamForSeason tfs, NEC subseasonType, boolean home, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> homeAwayRanks = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = tfs.getSeason();
		for (TeamForSeason team : season.getTeams()) {
			RecordAggregator homeAwayAgg = getHomeAwayRecord(team, subseasonType, home, againstSpread);
			if (homeAwayRanks.containsKey(homeAwayAgg)) {
				homeAwayRanks.get(homeAwayAgg).add(team);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(team);
				homeAwayRanks.put(homeAwayAgg, rankList);
			}
		}
		
		return homeAwayRanks;
	}
	
	@Override
	public RecordAggregator getFavUdogEvenRecord(TeamForSeason tfs, Boolean favorite, NEC subseasonType,
			boolean againstSpread) {
		RecordAggregator favUdogEvenRagg = null;
		if (tfs == null || subseasonType == null) {
			log.warning("No team/type specified, can not get favorite/underdog/even record.");
		}
		else {
			favUdogEvenRagg = new RecordAggregator(tfs, againstSpread);
			
			//	Get the games & the records for the TFS, sorting both by week number
			List<Game> allGames = new ArrayList<Game>();
			allGames.addAll(tfs.getHomeGames());
			allGames.addAll(tfs.getAwayGames());
			if (subseasonType.equals(NEC.SEASON)) {
				allGames = getGamesSortedByWeekNumber(allGames);
			}
			else {
				List<Game> subseasonGames = new ArrayList<Game>();
				for (Game g : allGames) {
					if (g.getWeek().getSubseason().getSubseasonType().equals(subseasonType)) {
						subseasonGames.add(g);
					}
				}
				
				allGames = getGamesSortedByWeekNumber(subseasonGames);
			}
			
			List<Game> favUdogEvenGames = new ArrayList<Game>();
			for (Game g : allGames) {
				//	If looking for even-spread games, add to games where spread = zero
				if (favorite == null && BigDecimal.ZERO.equals(g.getSpread1())) {
					favUdogEvenGames.add(g);
				}
				//	If looking for games where the team is favorite or underdog
				else {
					Boolean homeFavored = g.getHomeFavoredSpread1();
					TeamForSeason tfsFavorite = null;
					//	If this is the home team and the home team is favored, set favorite
					if (homeFavored != null && homeFavored) {
						tfsFavorite = g.getHomeTeam();
					}
					//	If this is the away team and the away team is favored, set favorite
					else if (homeFavored != null && !homeFavored) {
						tfsFavorite = g.getAwayTeam();
					}
					
					//	If looking for games where tfs is the favorite, and the found favorite equals tfs, add to games
					if (favorite && tfsFavorite != null && tfsFavorite.equals(tfs)) {
						favUdogEvenGames.add(g);
					}
					//	If looking for games where tfs is underdog, and tfs is the other team in the game for the favorite, add to games
					else if (!favorite && tfsFavorite != null && tfs.equals(g.getOtherTeam(tfsFavorite))) {
						favUdogEvenGames.add(g);
					}
				}
			}
			List<Record> records = getRecordsSortedByWeekNumber(tfs.getRecords());

			//	For any home games whose week number matches a record's week number, add the record to the aggregate
			addRecordsToAggregate(favUdogEvenRagg, records, favUdogEvenGames);
		}
		
		return favUdogEvenRagg;
	}

	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getFavUdogEvenRank(TeamForSeason tfs,
			Boolean favorite, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> favUdogEvenRank = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = tfs.getSeason();
		for (TeamForSeason team : season.getTeams()) {
			RecordAggregator favUdogEvenAgg = getFavUdogEvenRecord(team, favorite, subseasonType, againstSpread);
			if (favUdogEvenRank.containsKey(favUdogEvenAgg)) {
				favUdogEvenRank.get(favUdogEvenAgg).add(team);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(team);
				favUdogEvenRank.put(favUdogEvenAgg, rankList);
			}
		}
		
		return favUdogEvenRank;
	}

	@Override
	public RecordAggregator getDivisionRecord(TeamForSeason tfs, Division division, NEC subseasonType, boolean againstSpread) {
		RecordAggregator divisionalAgg = null;
		if (tfs == null || subseasonType == null) {
			log.warning("No team/type specified, can not get divisional record.");
		}
		else {
			divisionalAgg = new RecordAggregator(tfs, againstSpread);
		
			//	Create division query
			TypedQuery<Game> dq = em.createNamedQuery("Game.selectDivisionalGamesForTFS", Game.class);
			dq.setParameter("seasonNumber", tfs.getSeason().getSeasonNumber());
			List<Game> divisionalGames;
			try {
				divisionalGames = getGamesSortedByWeekNumber(dq.getResultList());
			} catch (Exception e) {
				log.severe("Exception caught retrieving divisional games: " + e.getMessage());
				e.printStackTrace();
				divisionalGames = new ArrayList<Game>();
			}
			//	If the specified type is not the entire season, filter only the games matching the specified subseason
			if (!subseasonType.equals(NEC.SEASON)) {
				List<Game> subseasonGames = new ArrayList<Game>();
				for (Game g : divisionalGames) {
					if (g.getWeek().getSubseason().getSubseasonType().equals(subseasonType)) {
						subseasonGames.add(g);
					}
				}
				
				divisionalGames.retainAll(subseasonGames);
			}

			//	Get the records for the TFS, sorting games & records by week number
			List<Record> records = getRecordsSortedByWeekNumber(tfs.getRecords());

			//	For any divisional games who's week number matches a record's week number, add to aggregate
			addRecordsToAggregate(divisionalAgg, records, divisionalGames);
		}
		
		return divisionalAgg;
	}
	
	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getDivisionRank(TeamForSeason tfs, Division division, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> divisionRanks = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		List<TeamForSeason> seasonTeams = tfs.getSeason().getTeams();
		List<TeamForSeason> divisionTeams = division.getTeamHistory();
		//	Get only the teams in the division for this season
		divisionTeams.retainAll(seasonTeams);
		
		for (TeamForSeason team : divisionTeams) {
			RecordAggregator divAgg = getDivisionRecord(team, division, subseasonType, againstSpread);
			if (divisionRanks.containsKey(divAgg)) {
				divisionRanks.get(divAgg).add(team);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(team);
				divisionRanks.put(divAgg, rankList);
			}
		}
		
		return divisionRanks;
	}

	@Override
	public RecordAggregator getConferenceRecord(TeamForSeason tfs, Conference conference, NEC subseasonType, boolean againstSpread) {
		RecordAggregator conferenceAgg = null;
		if (tfs == null) {
			log.warning("No team specified, can not get conference record.");
		}
		else {
			conferenceAgg = new RecordAggregator(tfs, againstSpread);

			//	Create conference query
			TypedQuery<Game> dq = em.createNamedQuery("Game.selectConferenceGamesForTFS", Game.class);
			dq.setParameter("seasonNumber", tfs.getSeason().getSeasonNumber());
			List<Game> conferenceGames;
			try {
				conferenceGames = getGamesSortedByWeekNumber(dq.getResultList());
			} catch (Exception e) {
				log.severe("Exception caught retrieving conference games: " + e.getMessage());
				e.printStackTrace();
				conferenceGames = new ArrayList<Game>();
			}
			//	If the specified type is not the entire season, filter only the games matching the specified subseason
			if (!subseasonType.equals(NEC.SEASON)) {
				List<Game> subseasonGames = new ArrayList<Game>();
				for (Game g : conferenceGames) {
					if (g.getWeek().getSubseason().getSubseasonType().equals(subseasonType)) {
						subseasonGames.add(g);
					}
				}

				conferenceGames.retainAll(subseasonGames);
			}
	
			//	Get the records for the TFS, sorting games & records by week number
			List<Record> records = getRecordsSortedByWeekNumber(tfs.getRecords());
	
			//	For any conference game games who's week number matches a record's week number, add to aggregate
			addRecordsToAggregate(conferenceAgg, records, conferenceGames);
		}

		return conferenceAgg;
	}
	
	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getConferenceRank(TeamForSeason tfs, Conference conference, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> conferenceRanks = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		List<TeamForSeason> seasonTeams = tfs.getSeason().getTeams();
		List<TeamForSeason> conferenceTeams = new ArrayList<TeamForSeason>();
		for (Division d : conference.getDivisions()) {
			conferenceTeams.addAll(d.getTeamHistory());
		}
		//	Get only the teams for this conference for this season
		conferenceTeams.retainAll(seasonTeams);
		
		for (TeamForSeason team : conferenceTeams) {
			RecordAggregator confAgg = getConferenceRecord(team, conference, subseasonType, againstSpread);
			if (conferenceRanks.containsKey(confAgg)) {
				conferenceRanks.get(confAgg).add(team);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(team);
				conferenceRanks.put(confAgg, rankList);
			}
		}
		
		return conferenceRanks;
	}

	@Override
	public RecordAggregator getRecentRecordAgainstOpponent(TeamForSeason tfs, TeamForSeason opponent, NEC subseasonType, boolean againstSpread) {
		RecordAggregator recentAgg = null;
		if (tfs == null) {
			log.warning("No team specified, can not get primetime record.");
		}
		else {
			recentAgg = new RecordAggregator(tfs, againstSpread);
			
			//	Create Opponent history query
			TypedQuery<Game> gq = em.createNamedQuery("Game.selectOpponentHistory", Game.class);
			gq.setParameter("team1Id", tfs.getTeam().getAbstractTeamId());
			gq.setParameter("team2Id", opponent.getTeam().getAbstractTeamId());
			
			List<Game> opponentHistory;
			try {
				opponentHistory = gq.getResultList();
			} catch (Exception e) {
				log.severe("Exception caught retrieving opponent history: " + e.getMessage());
				e.printStackTrace();
				opponentHistory = new ArrayList<Game>();
			}
			
			if (!opponentHistory.isEmpty()) {
				Comparator<Game> historyCompare = new Comparator<Game>() {
					@Override
					public int compare(Game g1, Game g2) {
						Integer g1Season = g1.getWeek().getSubseason().getSeason().getSeasonNumber();
						Integer g2Season = g2.getWeek().getSubseason().getSeason().getSeasonNumber();
						//	Check whether the games occur during the same season, if so, sort by most recent week number
						if (g1Season.equals(g2Season)) {
							Integer g1Week = g1.getWeek().getWeekNumber();
							Integer g2Week = g1.getWeek().getWeekNumber();
							return g1Week > g2Week ? -1 : 1;
						}
						//	If in different seasons, sort by most recent season number
						else {
							return g1Season > g2Season ? -1 : 1;
						}
					}
				};
				
				//	Filter the history by the specified subseason
				if (!subseasonType.equals(NEC.SEASON)) {
					List<Game> subseasonGames = new ArrayList<Game>();
					for (Game g : opponentHistory) {
						if (g.getWeek().getSubseason().getSubseasonType().equals(subseasonType)) {
							subseasonGames.add(g);
						}
					}
					
					opponentHistory.retainAll(subseasonGames);
				}
				
				//	Sort the history list & keep only the first 5
				Collections.sort(opponentHistory, historyCompare);
				if (opponentHistory.size() >= 5) {
					opponentHistory = opponentHistory.subList(0, 5);
				}
				
				//	From the games in the history, get the matching records
				List<Record> records = new ArrayList<Record>();
				for (Game g : opponentHistory) {
					//	Create query to get the record
					TypedQuery<Record> rq = em.createNamedQuery("Record.selectRecordForGame", Record.class);
					rq.setParameter("atfsId", tfs.getAbstractTeamForSeasonId());
					rq.setParameter("gameId", g.getGameId());
					Record r = null;
					try {
						r = rq.getSingleResult();
					} catch (NoResultException e) {
						log.warning("No Record found for " + tfs.getNickname() + " for week " + g.getWeek().getWeekNumber() + " in NEC " + tfs.getSeason().getSeasonNumber());
						log.warning(e.getMessage());
					} catch (NonUniqueResultException e) {
						log.severe("Multiple Records found for " + tfs.getNickname() + " for week " + g.getWeek().getWeekNumber() + " in NEC " + tfs.getSeason().getSeasonNumber());
						log.severe(e.getMessage());
						e.printStackTrace();
					} catch (Exception e) {
						log.severe("Exception caught retrieving record for game: " + e.getMessage());
						e.printStackTrace();
					}
					
					if (r != null) records.add(r);
				}
				
				for (Record r : records) {
					recentAgg.addRecord(r);
				}
			}
		}
		
		return recentAgg;
	}

	@Override
	public RecordAggregator getPrimetimeRecord(TeamForSeason tfs, NEC recordType, boolean againstSpread) {
		RecordAggregator primetimeAgg = null;
		if (tfs == null || recordType == null) {
			log.warning("No team/type specified, can not get primetime record.");
		}
		else {
			primetimeAgg = new RecordAggregator(tfs, againstSpread);
			//	Get the list of all games for the team, sorted by week number
			List<Game> games = tfs.getHomeGames();
			games.addAll(tfs.getAwayGames());
			games = getGamesSortedByWeekNumber(games);
			
			List<Game> primetimeGames = new ArrayList<Game>();
			for (Game g : games) {
				//	Check whether the specified record type matches the subseason for this game (or if Season specified, use all)
				NEC subseasonType = g.getWeek().getSubseason().getSubseasonType();
				if (recordType == NEC.SEASON || recordType.equals(subseasonType)){
					if (g.getPrimeTime()) {
						primetimeGames.add(g);
					}
				}
			}
			
			//	Get the records for the TFS, sorting games & records by week number
			List<Record> records = getRecordsSortedByWeekNumber(tfs.getRecords());
			
			//	For any primetime game who's week number matches a record's week number, add to aggregate
			addRecordsToAggregate(primetimeAgg, records, primetimeGames);
		}

		return primetimeAgg;
	}
	
	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getPrimetimeRank(TeamForSeason tfs, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> primetimeRanks = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = tfs.getSeason();
		for (TeamForSeason team : season.getTeams()) {
			RecordAggregator primetimeAgg = getPrimetimeRecord(team, subseasonType, againstSpread);
			if (primetimeRanks.containsKey(primetimeAgg)) {
				primetimeRanks.get(primetimeAgg).add(team);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(team);
				primetimeRanks.put(primetimeAgg, rankList);
			}
		}
		
		return primetimeRanks;
	}

	@Override
	public RecordAggregator getRecordByDateTime(TeamForSeason tfs, Integer month, Integer dayOfWeek, Integer kickoffHour, NEC subseasonType, boolean againstSpread) {
		RecordAggregator gameDateAgg = null;
		if (tfs == null || (month == null && dayOfWeek == null && kickoffHour == null)) {
			log.warning("No team/date info specified, can not get date/time record.");
		}
		else {
			gameDateAgg = new RecordAggregator(tfs, againstSpread);
		
			//	Get the list of all games for the team, sorted by week number
			List<Game> games = tfs.getHomeGames();
			games.addAll(tfs.getAwayGames());
			games = getGamesSortedByWeekNumber(games);
			
			//	If a month parameter is defined, filter the resulting games by their month played
			if (month != null) {
				List<Game> monthGames = new ArrayList<Game>();
				for (Game g : games) {
					if (g.getGameDate().get(GregorianCalendar.MONTH) == month) {
						monthGames.add(g);
					}
				}
				games.retainAll(monthGames);
			}
			//	If the day of the week is defined, filter the resulting games by the day of the week
			if (dayOfWeek != null) {
				List<Game> dateGames = new ArrayList<Game>();
				for (Game g : games) {
					if (g.getGameDate().get(GregorianCalendar.DAY_OF_WEEK) == dayOfWeek) {
						dateGames.add(g);
					}
				}
				games.retainAll(dateGames);
			}
			//	If the kickoff hour is defined, filter the resulting games by the matching times
			if (kickoffHour != null) {
				List<Game> timeGames = new ArrayList<Game>();
				for (Game g : games) {
					if (g.getGameDate().get(GregorianCalendar.HOUR_OF_DAY) == kickoffHour) {
						timeGames.add(g);
					}
				}
				games.retainAll(timeGames);
			}
			
			//	Filter games by subseason if defined
			if (!subseasonType.equals(NEC.SEASON)) {
				List<Game> subseasonGames = new ArrayList<Game>();
				for (Game g : games) {
					if (g.getWeek().getSubseason().getSubseasonType().equals(subseasonType)) {
						subseasonGames.add(g);
					}
				}
				
				games.retainAll(subseasonGames);
			}
			
			//	Get the records sorted by week number
			List<Record> records = getRecordsSortedByWeekNumber(tfs.getRecords());
			
			//	Add the filtered games & records to the aggregation
			addRecordsToAggregate(gameDateAgg, records, games);
		}
		
		return gameDateAgg;
	}
	
	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getDateTimeRank(TeamForSeason tfs, Integer month, Integer dayOfWeek, Integer kickoffHour, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> dateTimeRanks = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = tfs.getSeason();
		for (TeamForSeason team : season.getTeams()) {
			RecordAggregator dateTimeAgg = getRecordByDateTime(team, month, dayOfWeek, kickoffHour, subseasonType, againstSpread);
			if (dateTimeRanks.containsKey(dateTimeAgg)) {
				dateTimeRanks.get(dateTimeAgg).add(team);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(team);
				dateTimeRanks.put(dateTimeAgg, rankList);
			}
		}
		
		return dateTimeRanks;
	}

	@Override
	public RecordAggregator getRecordForStadium(TeamForSeason tfs, Stadium stadium, RoofType roofType, NEC subseasonType, boolean againstSpread) {
		RecordAggregator stadiumAgg = null;
		if (tfs == null || (stadium == null && roofType == null)) {
			log.warning("No team/stadium info specified, can not get stadium record.");
		}
		else {
			stadiumAgg = new RecordAggregator(tfs, againstSpread);
		
			//	Get the list of all games for the team, sorted by week number
			List<Game> games = tfs.getHomeGames();
			games.addAll(tfs.getAwayGames());
			games = getGamesSortedByWeekNumber(games);
			
			if (stadium != null) {
				List<Game> stadiumGames = new ArrayList<Game>();
				for (Game g : games) {
					if (g.getStadium().equals(stadium)) {
						stadiumGames.add(g);
					}
				}
				games.retainAll(stadiumGames);
			}
			
			if (roofType != null) {
				List<Game> roofGames = new ArrayList<Game>();
				for (Game g : games) {
					if (g.getStadium().getRoofType().equals(roofType)) {
						roofGames.add(g);
					}
				}
				games.retainAll(roofGames);
			}
			
			//	Filter games by subseason type if defined
			if (!subseasonType.equals(NEC.SEASON)) {
				List<Game> subseasonGames = new ArrayList<Game>();
				for (Game g : games) {
					if (g.getWeek().getSubseason().getSubseasonType().equals(subseasonType)) {
						subseasonGames.add(g);
					}
				}
				
				games.retainAll(subseasonGames);
			}
			
			List<Record> records = getRecordsSortedByWeekNumber(tfs.getRecords());
			
			addRecordsToAggregate(stadiumAgg, records, games);
		}
		
		return stadiumAgg;
	}
	
	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getStadiumRank(TeamForSeason tfs, Stadium stadium, RoofType roofType, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> stadiumRanks = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = tfs.getSeason();
		for (TeamForSeason team : season.getTeams()) {
			RecordAggregator stadiumAgg = getRecordForStadium(team, stadium, roofType, subseasonType, againstSpread);
			if (stadiumRanks.containsKey(stadiumAgg)) {
				stadiumRanks.get(stadiumAgg).add(team);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(team);
				stadiumRanks.put(stadiumAgg, rankList);
			}
		}
		
		return stadiumRanks;
	}

	@Override
	public RecordAggregator getRecordByTimezone(TeamForSeason tfs, TimeZone timezone, NEC subseasonType, boolean againstSpread) {
		RecordAggregator timezoneAgg = null;
		if (tfs == null || timezone == null) {
			log.warning("No team/timezone specified, can not get timezone record.");
		}
		else {
			timezoneAgg = new RecordAggregator(tfs, againstSpread);
		
			//	Get the list of all games for the team, sorted by week number
			List<Game> games = tfs.getHomeGames();
			games.addAll(tfs.getAwayGames());
			games = getGamesSortedByWeekNumber(games);
			
			List<Game> timezoneGames = new ArrayList<Game>();
			for (Game g : games) {
				if (g.getStadium().getTimezone().equals(timezone)) {
					timezoneGames.add(g);
				}
			}
			games.retainAll(timezoneGames);
			
			//	Filter by subseason if defined
			if (!subseasonType.equals(NEC.SEASON)) {
				List<Game> subseasonGames = new ArrayList<Game>();
				for (Game g : games) {
					if (g.getWeek().getSubseason().getSubseasonType().equals(subseasonType)) {
						subseasonGames.add(g);
					}
				}
				
				games.retainAll(subseasonGames);
			}
			
			List<Record> records = getRecordsSortedByWeekNumber(tfs.getRecords());
			
			addRecordsToAggregate(timezoneAgg, records, timezoneGames);
		}
		
		return timezoneAgg;
	}

	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getTimezoneRank(TeamForSeason tfs, TimeZone timezone, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> timezoneRanks = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = tfs.getSeason();
		for (TeamForSeason team : season.getTeams()) {
			RecordAggregator timezoneAgg = getRecordByTimezone(team, timezone, subseasonType, againstSpread);
			if (timezoneRanks.containsKey(timezoneAgg)) {
				timezoneRanks.get(timezoneAgg).add(team);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(team);
				timezoneRanks.put(timezoneAgg, rankList);
			}
		}
		
		return timezoneRanks;
	}

	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getMnfTntRank(TeamForSeason tfs, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfTntRanks = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = tfs.getSeason();
		for (TeamForSeason team : season.getTeams()) {
			RecordAggregator mnfAgg = getRecordByDateTime(team, null, GregorianCalendar.MONDAY, null, subseasonType, againstSpread);
			RecordAggregator tntAgg = getRecordByDateTime(team, null, GregorianCalendar.THURSDAY, null, subseasonType, againstSpread);
			RecordAggregator combined = RecordAggregator.combine(mnfAgg, tntAgg);
			
			if (mnfTntRanks.containsKey(combined)) {
				mnfTntRanks.get(combined).add(team);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(team);
				mnfTntRanks.put(combined, rankList);
			}
		}
		
		return mnfTntRanks;
	}
}

