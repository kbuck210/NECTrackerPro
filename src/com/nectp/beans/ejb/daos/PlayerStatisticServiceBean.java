package com.nectp.beans.ejb.daos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.nectp.beans.remote.daos.PickService;
import com.nectp.beans.remote.daos.PlayerStatisticService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.Pick.PickType;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Stadium.RoofType;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Stateless
public class PlayerStatisticServiceBean extends RecordServiceBean implements PlayerStatisticService {
	private static final long serialVersionUID = -2372141169706701464L;

	@EJB
	private PickService pickService;
	
	@Override
	public RecordAggregator getRecordForWeek(PlayerForSeason pfs, Week week, boolean againstSpread) {
		NEC recordType = week.getSubseason().getSubseasonType();
		RecordAggregator weekRagg = new RecordAggregator(pfs, againstSpread);
		try {
			Record record = selectWeekRecordForAtfs(week, pfs, recordType);
			weekRagg.addRecord(record);
		} catch (NoExistingEntityException e) {
			//	Eat exception
		}
		
		return weekRagg;
	}
	
	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getWeekRanks(Week week, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> weekRank = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = week.getSubseason().getSeason();
		for (PlayerForSeason player : season.getPlayers()) {
			RecordAggregator homeAwayAgg = getRecordForWeek(player, week, againstSpread);
			if (weekRank.containsKey(homeAwayAgg)) {
				weekRank.get(homeAwayAgg).add(player);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(player);
				weekRank.put(homeAwayAgg, rankList);
			}
		}
		
		return weekRank;
	}
	
	@Override
	public RecordAggregator getHomeAwayRecord(PlayerForSeason pfs, NEC subseasonType, boolean home, boolean againstSpread) {
		List<Pick> homeAwayPicks;
		if (subseasonType == NEC.SEASON) {
			homeAwayPicks = new ArrayList<Pick>();
			homeAwayPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.FIRST_HALF));
			homeAwayPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SECOND_HALF));
			homeAwayPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.PLAYOFFS));
			homeAwayPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SUPER_BOWL));
		}
		else {
			homeAwayPicks = pickService.selectPlayerPicksForType(pfs, subseasonType);
		}
		
		//	Have to create a local record because managed records store both home/away picks
		Record homeAwayRecord = new Record();
		homeAwayRecord.setRecordType(subseasonType);
		homeAwayRecord.setTeam(pfs);
		
		for (Pick p : homeAwayPicks) {
			Game g = p.getGame();
			TeamForSeason pickedTeam = p.getPickedTeam();

			if ((home && pickedTeam.equals(g.getHomeTeam())) || 
				(!home && pickedTeam.equals(g.getAwayTeam()))) {
				updateRecordForGame(homeAwayRecord, g, pickedTeam);
				homeAwayRecord.addPickInRecord(p);
			}
		}
		
		RecordAggregator homeAwayRagg = new RecordAggregator(pfs, againstSpread);
		homeAwayRagg.addRecord(homeAwayRecord);
		return homeAwayRagg;
	}

	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getHomeAwayRank(PlayerForSeason pfs, 
			NEC subseasonType, boolean home, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> homeAwayRanks = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());

		Season season = pfs.getSeason();
		for (PlayerForSeason player : season.getPlayers()) {
			RecordAggregator homeAwayAgg = getHomeAwayRecord(player, subseasonType, home, againstSpread);
			if (homeAwayRanks.containsKey(homeAwayAgg)) {
				homeAwayRanks.get(homeAwayAgg).add(player);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(player);
				homeAwayRanks.put(homeAwayAgg, rankList);
			}
		}

		return homeAwayRanks;
	}
	
	@Override
	public RecordAggregator getFavUdogEvenRecord(PlayerForSeason pfs, Boolean favorite, NEC subseasonType,
			boolean againstSpread) {
		List<Pick> favUdogEvenPicks;
		if (subseasonType == NEC.SEASON) {
			favUdogEvenPicks = new ArrayList<Pick>();
			favUdogEvenPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.FIRST_HALF));
			favUdogEvenPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SECOND_HALF));
			favUdogEvenPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.PLAYOFFS));
			favUdogEvenPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SUPER_BOWL));
		}
		else {
			favUdogEvenPicks = pickService.selectPlayerPicksForType(pfs, subseasonType);
		}
		
		//	Have to create a local record because managed records store all fav/udog/even picks
		Record favUdogEvenRecord = new Record();
		favUdogEvenRecord.setRecordType(subseasonType);
		favUdogEvenRecord.setTeam(pfs);
		
		for (Pick p : favUdogEvenPicks) {
			Game g = p.getGame();
			TeamForSeason pickedTeam = p.getPickedTeam();
			PickType pType = p.getPickType();
			
			boolean updateRecord = false;
			//	Determine whether looking for favorite/underdog/even based on parameter
			//	If favorite == null, looking for even spreads
			if (favorite == null) {
				//	If the pick type is spread 1 or 2 && the specified spread is zero (i.e. even), update the record
				updateRecord = (pType.equals(PickType.SPREAD1) && BigDecimal.ZERO.equals(g.getSpread1())) ||
							   (pType.equals(PickType.SPREAD2) && BigDecimal.ZERO.equals(g.getSpread2()));
			}
			//	If looking for games where the favorite or underdog was picked
			else {
				TeamForSeason tfsFavorite = null;
				if (pType.equals(PickType.SPREAD1)) {
					if (g.getHomeFavoredSpread1() != null && g.getHomeFavoredSpread1()) {
						tfsFavorite = g.getHomeTeam();
					}
					else if (g.getHomeFavoredSpread1() != null) {
						tfsFavorite = g.getAwayTeam();
					}
				}
				else if (pType.equals(PickType.SPREAD2)) {
					if (g.getHomeFavoredSpread2() != null && g.getHomeFavoredSpread2()) {
						tfsFavorite = g.getHomeTeam();
					}
					else if (g.getHomeFavoredSpread2() != null) {
						tfsFavorite= g.getAwayTeam();
					}
				}
				
				//	Update the record if the looking for fav/underdog, and the pickedTeam is equal to either the fav or udog
				updateRecord = (favorite && tfsFavorite != null && tfsFavorite.equals(pickedTeam)) ||
							   (!favorite && tfsFavorite != null && pickedTeam.equals(g.getOtherTeam(tfsFavorite)));
			}
			
			if (updateRecord) {
				updateRecordForGame(favUdogEvenRecord, g, pickedTeam);
				favUdogEvenRecord.addPickInRecord(p);
			}
		}
		
		RecordAggregator favUdogEvenRagg = new RecordAggregator(pfs, againstSpread);
		favUdogEvenRagg.addRecord(favUdogEvenRecord);
		return favUdogEvenRagg;
	}

	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getFavUdogEvenRank(PlayerForSeason pfs,
			Boolean favorite, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> favUdogEvenRanks = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = pfs.getSeason();
		for (PlayerForSeason player : season.getPlayers()) {
			RecordAggregator divisionRagg = getFavUdogEvenRecord(player, favorite, subseasonType, againstSpread);
			if (favUdogEvenRanks.containsKey(divisionRagg)) {
				favUdogEvenRanks.get(divisionRagg).add(player);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(player);
				favUdogEvenRanks.put(divisionRagg, rankList);
			}
		}

		return favUdogEvenRanks;
	}

	@Override
	public RecordAggregator getDivisionRecord(PlayerForSeason pfs, Division division, NEC subseasonType,
			boolean againstSpread) {
		List<Pick> playerPicks;
		if (subseasonType == NEC.SEASON) {
			playerPicks = new ArrayList<Pick>();
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.FIRST_HALF));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SECOND_HALF));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.PLAYOFFS));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SUPER_BOWL));
		}
		else {
			playerPicks = pickService.selectPlayerPicksForType(pfs, subseasonType);
		}
		
		//	From all of the player picks, get the divisional record
		Record divisionRecord = new Record();
		divisionRecord.setRecordType(subseasonType);
		divisionRecord.setTeam(pfs);
		
		for (Pick p : playerPicks) {
			Game g = p.getGame();
			TeamForSeason pickedTeam = p.getPickedTeam();
			//	Check that the picked team is in the specified division, if not skip
			if (pickedTeam.getDivision().equals(division)) {
				updateRecordForGame(divisionRecord, g, pickedTeam);
				divisionRecord.addPickInRecord(p);
			}
		}
		
		RecordAggregator divisionRagg = new RecordAggregator(pfs, againstSpread);
		divisionRagg.addRecord(divisionRecord);
		return divisionRagg;
	}

	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getDivisionRank(PlayerForSeason pfs,
			Division division, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> divisionRanks = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = pfs.getSeason();
		for (PlayerForSeason player : season.getPlayers()) {
			RecordAggregator divisionRagg = getDivisionRecord(player, division, subseasonType, againstSpread);
			if (divisionRanks.containsKey(divisionRagg)) {
				divisionRanks.get(divisionRagg).add(player);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(player);
				divisionRanks.put(divisionRagg, rankList);
			}
		}

		return divisionRanks;
	}

	@Override
	public RecordAggregator getConferenceRecord(PlayerForSeason pfs, Conference conference, NEC subseasonType,
			boolean againstSpread) {
		List<Pick> playerPicks;
		if (subseasonType == NEC.SEASON) {
			playerPicks = new ArrayList<Pick>();
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.FIRST_HALF));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SECOND_HALF));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.PLAYOFFS));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SUPER_BOWL));
		}
		else {
			playerPicks = pickService.selectPlayerPicksForType(pfs, subseasonType);
		}
		//	From all of the player picks, get the divisional record
		Record conferenceRecord = new Record();
		conferenceRecord.setRecordType(subseasonType);
		conferenceRecord.setTeam(pfs);
		
		for (Pick p : playerPicks) {
			Game g = p.getGame();
			TeamForSeason pickedTeam = p.getPickedTeam();
			if (pickedTeam.getDivision().getConference().equals(conference)) {
				updateRecordForGame(conferenceRecord, g, pickedTeam);
				conferenceRecord.addPickInRecord(p);
			}
		}

		RecordAggregator conferenceRagg = new RecordAggregator(pfs, againstSpread);
		conferenceRagg.addRecord(conferenceRecord);
		return conferenceRagg;
	}

	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getConferenceRank(PlayerForSeason pfs,
			Conference conference, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> confRanks = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = pfs.getSeason();
		for (PlayerForSeason player : season.getPlayers()) {
			RecordAggregator confRagg = getConferenceRecord(player, conference, subseasonType, againstSpread);
			if (confRanks.containsKey(confRagg)) {
				confRanks.get(confRagg).add(player);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(player);
				confRanks.put(confRagg, rankList);
			}
		}
		
		return confRanks;
	}

	@Override
	public RecordAggregator getPrimetimeRecord(PlayerForSeason pfs, NEC subseasonType, boolean againstSpread) {
		List<Pick> playerPicks;
		if (subseasonType == NEC.SEASON) {
			playerPicks = new ArrayList<Pick>();
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.FIRST_HALF));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SECOND_HALF));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.PLAYOFFS));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SUPER_BOWL));
		}
		else {
			playerPicks = pickService.selectPlayerPicksForType(pfs, subseasonType);
		}
		
		//	From all of the player picks, get the divisional record
		Record primetimeRecord = new Record();
		primetimeRecord.setRecordType(subseasonType);
		primetimeRecord.setTeam(pfs);
		
		for (Pick p : playerPicks) {
			Game g = p.getGame();
			if (g.getPrimeTime()) {
				TeamForSeason pickedTeam = p.getPickedTeam();
				updateRecordForGame(primetimeRecord, g, pickedTeam);
				primetimeRecord.addPickInRecord(p);
			}
		}

		RecordAggregator primetimeRagg = new RecordAggregator(pfs, againstSpread);
		primetimeRagg.addRecord(primetimeRecord);
		return primetimeRagg;
	}

	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getPrimetimeRank(PlayerForSeason pfs,
			NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> primetimeRank = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = pfs.getSeason();
		for (PlayerForSeason player : season.getPlayers()) {
			RecordAggregator primetimeRagg = getPrimetimeRecord(player, subseasonType, againstSpread);
			if (primetimeRank.containsKey(primetimeRagg)) {
				primetimeRank.get(primetimeRagg).add(player);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(player);
				primetimeRank.put(primetimeRagg, rankList);
			}
		}
		
		return primetimeRank;
	}

	@Override
	public RecordAggregator getRecordByDateTime(PlayerForSeason pfs, Integer month, Integer dayOfWeek,
			Integer kickoffHour, NEC subseasonType, boolean againstSpread) {
		List<Pick> playerPicks;
		if (subseasonType == NEC.SEASON) {
			playerPicks = new ArrayList<Pick>();
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.FIRST_HALF));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SECOND_HALF));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.PLAYOFFS));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SUPER_BOWL));
		}
		else {
			playerPicks = pickService.selectPlayerPicksForType(pfs, subseasonType);
		}
		//	From all of the player picks, get the divisional record
		Record dateTimeRecord = new Record();
		dateTimeRecord.setRecordType(subseasonType);
		dateTimeRecord.setTeam(pfs);
		
		Logger log = Logger.getLogger(PlayerStatisticServiceBean.class.getName());
		for (Pick p : playerPicks) {
			Game g = p.getGame();
			Calendar gameDate = g.getGameDate();
			if (month != null && !month.equals(gameDate.get(GregorianCalendar.MONTH))) {
				log.info("month doesn't match");
				continue;
			}
			else if (dayOfWeek != null && !dayOfWeek.equals(gameDate.get(GregorianCalendar.DAY_OF_WEEK))) {
				log.info("Day of week: " + dayOfWeek + " does not match: " + gameDate.get(GregorianCalendar.DAY_OF_WEEK));
				continue;
			}
			else if (kickoffHour != null && !kickoffHour.equals(gameDate.get(GregorianCalendar.HOUR_OF_DAY))) {
				log.info("kickoffHour doesn't match");
				continue;
			}
			TeamForSeason pickedTeam = p.getPickedTeam();
			updateRecordForGame(dateTimeRecord, g, pickedTeam);
			dateTimeRecord.addPickInRecord(p);
		}
		
		RecordAggregator dateTimeRagg = new RecordAggregator(pfs, againstSpread);
		dateTimeRagg.addRecord(dateTimeRecord);
		return dateTimeRagg;
	}

	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getDateTimeRank(PlayerForSeason pfs,
			Integer month, Integer dayOfWeek, Integer kickoffHour, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> dateTimeRank = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = pfs.getSeason();
		for (PlayerForSeason player : season.getPlayers()) {
			RecordAggregator dateTimeRagg = getRecordByDateTime(player, month, dayOfWeek, kickoffHour, subseasonType, againstSpread);
			if (dateTimeRank.containsKey(dateTimeRagg)) {
				dateTimeRank.get(dateTimeRagg).add(player);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(player);
				dateTimeRank.put(dateTimeRagg, rankList);
			}
		}
		
		return dateTimeRank;
	}

	@Override
	public RecordAggregator getRecordForStadium(PlayerForSeason pfs, Stadium stadium, RoofType roofType,
			NEC subseasonType, boolean againstSpread) {
		List<Pick> playerPicks;
		if (subseasonType == NEC.SEASON) {
			playerPicks = new ArrayList<Pick>();
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.FIRST_HALF));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SECOND_HALF));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.PLAYOFFS));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SUPER_BOWL));
		}
		else {
			playerPicks = pickService.selectPlayerPicksForType(pfs, subseasonType);
		}
		
		//	From all of the player picks, get the divisional record
		Record stadiumRecord = new Record();
		stadiumRecord.setRecordType(subseasonType);
		stadiumRecord.setTeam(pfs);
		
		for (Pick p : playerPicks) {
			Game g = p.getGame(); 
			TeamForSeason pickedTeam = p.getPickedTeam();
			if (g.getStadium().equals(stadium)) {
				updateRecordForGame(stadiumRecord, g, pickedTeam);
				stadiumRecord.addPickInRecord(p);
			}
			else if (g.getStadium().getRoofType().equals(roofType)) {
				updateRecordForGame(stadiumRecord, g, pickedTeam);
				stadiumRecord.addPickInRecord(p);
			}
		}
		
		RecordAggregator stadiumRagg = new RecordAggregator(pfs, againstSpread);
		stadiumRagg.addRecord(stadiumRecord);
		return stadiumRagg;
	}

	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getStadiumRank(PlayerForSeason pfs,
			Stadium stadium, RoofType roofType, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> stadiumRank = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = pfs.getSeason();
		for (PlayerForSeason player : season.getPlayers()) {
			RecordAggregator stadiumRagg = getRecordForStadium(player, stadium, roofType, subseasonType, againstSpread);
			if (stadiumRank.containsKey(stadiumRagg)) {
				stadiumRank.get(stadiumRagg).add(player);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(player);
				stadiumRank.put(stadiumRagg, rankList);
			}
		}
		
		return stadiumRank;
	}

	@Override
	public RecordAggregator getRecordByTimezone(PlayerForSeason pfs, TimeZone timezone, NEC subseasonType,
			boolean againstSpread) {
		List<Pick> playerPicks;
		if (subseasonType == NEC.SEASON) {
			playerPicks = new ArrayList<Pick>();
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.FIRST_HALF));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SECOND_HALF));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.PLAYOFFS));
			playerPicks.addAll(pickService.selectPlayerPicksForType(pfs, NEC.SUPER_BOWL));
		}
		else {
			playerPicks = pickService.selectPlayerPicksForType(pfs, subseasonType);
		}
		
		//	From all of the player picks, get the divisional record
		Record timezoneRecord = new Record();
		timezoneRecord.setRecordType(subseasonType);
		timezoneRecord.setTeam(pfs);
		
		for (Pick p : playerPicks) {
			Game g = p.getGame();
			if (g.getStadium().getTimezone().equals(timezone)) {
				TeamForSeason pickedTeam = p.getPickedTeam();
				updateRecordForGame(timezoneRecord, g, pickedTeam);
				timezoneRecord.addPickInRecord(p);
			}
		}
		
		RecordAggregator timezoneRagg = new RecordAggregator(pfs, againstSpread);
		timezoneRagg.addRecord(timezoneRecord);
		return timezoneRagg;
	}

	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getTimezoneRank(PlayerForSeason pfs,
			TimeZone timezone, NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> timezoneRank = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = pfs.getSeason();
		for (PlayerForSeason player : season.getPlayers()) {
			RecordAggregator timezoneRagg = getRecordByTimezone(player, timezone, subseasonType, againstSpread);
			if (timezoneRank.containsKey(timezoneRagg)) {
				timezoneRank.get(timezoneRagg).add(player);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(player);
				timezoneRank.put(timezoneRagg, rankList);
			}
		}
		
		return timezoneRank;
	}

	@Override
	public TreeMap<RecordAggregator, List<AbstractTeamForSeason>> getMnfTntRank(PlayerForSeason pfs,
			NEC subseasonType, boolean againstSpread) {
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfTntRank = new TreeMap<RecordAggregator, List<AbstractTeamForSeason>>(Collections.reverseOrder());
		
		Season season = pfs.getSeason();
		for (PlayerForSeason player : season.getPlayers()) {
			List<Pick> mnfPicks = pickService.selectPlayerPicksForType(player, NEC.MNF);
			List<Pick> tntPicks = pickService.selectPlayerPicksForType(player, NEC.TNT);
			
			mnfPicks.addAll(tntPicks);
			Record mnfTntRecord = new Record();
			mnfTntRecord.setRecordType(NEC.MNF_TNT);
			mnfTntRecord.setTeam(player);
			
			for (Pick p : mnfPicks) {
				Game g = p.getGame();
				//	add to record if subseason type is entire season, or the subseason for this game matches attribute
				if (subseasonType == NEC.SEASON || 
				   (g.getWeek().getSubseason().getSubseasonType().equals(subseasonType))) {
					TeamForSeason pickedTeam = p.getPickedTeam();
					updateRecordForGame(mnfTntRecord, g, pickedTeam);
					mnfTntRecord.addPickInRecord(p);
				}
			}
			
			RecordAggregator mnfTntRagg = new RecordAggregator(player, againstSpread);
			mnfTntRagg.addRecord(mnfTntRecord);
			
			if (mnfTntRank.containsKey(mnfTntRagg)) {
				mnfTntRank.get(mnfTntRagg).add(player);
			}
			else {
				List<AbstractTeamForSeason> rankList = new ArrayList<AbstractTeamForSeason>();
				rankList.add(player);
				mnfTntRank.put(mnfTntRagg, rankList);
			}
		}
	
		return mnfTntRank;
	}
	
	private void updateRecordForGame(Record r, Game g, TeamForSeason pickedTeam) {
		TeamForSeason winner = g.getWinner();
		TeamForSeason winnerAts1 = g.getWinnerATS1();
		TeamForSeason winnerAts2 = g.getWinnerATS2();
		boolean hasSpread2 = g.getSpread2() != null;

		if (winner == null) r.addTie();
		else if (winner.equals(pickedTeam)) r.addWin();
		else r.addLoss();

		if (winnerAts1 == null) r.addTieATS1();
		else if (winnerAts1.equals(pickedTeam)) r.addWinATS1();
		else r.addLossATS1();

		if (hasSpread2) {
			if (winnerAts2 == null) r.addTieATS2();
			else if (winnerAts2.equals(pickedTeam)) r.addWinATS2();
			else r.addLossATS2();
		}
	}
}
