package com.nectp.beans.named;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.GameService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.beans.remote.daos.TeamStatisticService;
import com.nectp.beans.remote.daos.WeekService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Conference.ConferenceType;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Division.Region;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Stadium.RoofType;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Week.WeekStatus;

@Named(value="teamStatsViewBean")
@ViewScoped
public class TeamStatsViewBean implements Serializable {
	private static final long serialVersionUID = 7685890247822202869L;
	
	//	Stats independent of next opponent (all initialized to blank values)
	private RecordDisplay rawRecord;
	private RecordDisplay recordAts;
	private RecordDisplay homeRecord;
	private RecordDisplay homeAts;
	private RecordDisplay roadRecord;
	private RecordDisplay roadAts;
	private RecordDisplay primetimeRecord; 
	private RecordDisplay primetimeAts;
	private RecordDisplay mnfRecord;
	private RecordDisplay mnfAts;
	private RecordDisplay tntRecord;
	private RecordDisplay tntAts;
	private RecordDisplay mnfTntCombined;
	private RecordDisplay mnfTntAts;
	private RecordDisplay divisionRecord;
	private RecordDisplay divisionAts;
	private RecordDisplay conferenceRecord;
	private RecordDisplay conferenceAts;
	
	//	Statistics based on next opponent (all initialized to blank values)
	private RecordDisplay oppRawRecord;
	private RecordDisplay oppRecordAts;
	private RecordDisplay last5Record;
	private RecordDisplay last5Ats;
	private RecordDisplay divOppRecord;
	private RecordDisplay divOppAts;
	private RecordDisplay confOppRecord;
	private RecordDisplay confOppAts;
	private RecordDisplay stadiumRecord;
	private RecordDisplay stadiumAts;
	private RecordDisplay timezoneRecord;
	private RecordDisplay timezoneAts;
	private RecordDisplay roofTypeRecord;
	private RecordDisplay roofTypeAts;
	
	//	The team who's stats to display, & the scope for which to get the statistics
	private TeamForSeason displayTeam = null;
	private Week currentWeek = null;
	private Game nextGame = null;
	private NEC statScope = NEC.SEASON;
	private Season currentSeason = null;
	
	private RecordAggregator teamOverallRecord;
	private TreeMap<RecordAggregator, List<AbstractTeamForSeason>> divisionRanks;
	
	@EJB
	private TeamStatisticService teamStats;
	
	@EJB
	private WeekService weekService;
	
	@EJB 
	private GameService gameService;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private TeamForSeasonService tfsService;
	
	@EJB
	private TeamProfileBean profile;
	
	private Logger log;
	
	public TeamStatsViewBean() {
		log = Logger.getLogger(TeamStatsViewBean.class.getName());
	}
	
	@PostConstruct
	public void init() {
		setDisplayedTeam(profile.getDisplayTeam());
//		Map<String, String> paramMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
//		String teamAbbr = paramMap.get("teamAbbr");
//		String seasonNum = paramMap.get("nec");
//		Integer seasonNumber = null;
//		try {
//			seasonNumber = Integer.parseInt(seasonNum);
//			currentSeason = seasonService.selectById(seasonNumber);
//		} catch (NumberFormatException e) {
//			log.warning("Invalid season number format, using current season instead.");
//			log.warning(e.getMessage());
//			currentSeason = seasonService.selectCurrentSeason();
//		}
//		try {
//			displayTeam = tfsService.selectTfsByAbbrSeason(teamAbbr, currentSeason);
//		} catch (NoExistingEntityException e) {
//			//	Catch
//		}
//		if (displayTeam != null) {
//			
//		}
	}
	
	public void setDisplayedTeam(TeamForSeason displayTeam) {
		this.displayTeam = displayTeam;
		if (displayTeam == null) {
			log.severe("Null display team! can not display stats.");
		}
		currentSeason = displayTeam.getSeason();
		currentWeek = currentSeason.getCurrentWeek();
		if (WeekStatus.COMPLETED.equals(currentWeek.getWeekStatus())) {
			try {
				Week nextWeek = weekService.selectWeekByNumberInSeason((currentWeek.getWeekNumber() + 1), currentSeason);
				nextGame = gameService.selectGameByTeamWeek(displayTeam, nextWeek);
			} catch (NoExistingEntityException e) {
				//	Eat the exception
			}
		}
		else {
			nextGame = gameService.selectGameByTeamWeek(displayTeam, currentWeek);
		}
		
		//	Get the overall record for this season so far for the displayed team (ordering by raw scores for rank)
		teamOverallRecord = teamStats.getOverallRecordThroughWeekForAtfs(displayTeam, currentWeek, statScope, false);
		rawRecord = new RecordDisplay(teamOverallRecord, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> rawRecordRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
		rawRecord.setRank(rawRecordRanks);
		
		recordAts = new RecordDisplay(teamOverallRecord, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> recordAtsRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
		recordAts.setRank(recordAtsRanks);
		
		//	Get the home record for this season for the displayed team (ordering by spread scores for rank)
		RecordAggregator homeAgg = teamStats.getHomeAwayRecord(displayTeam, statScope, true, true);
		homeRecord = new RecordDisplay(homeAgg, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> homeRecordRanks = teamStats.getHomeAwayRank(displayTeam, statScope, true, false);
		homeRecord.setRank(homeRecordRanks);
		
		homeAts = new RecordDisplay(homeAgg, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> homeAtsRanks = teamStats.getHomeAwayRank(displayTeam, statScope, true, true);
		homeAts.setRank(homeAtsRanks);
		
		//	Get the road record for this season for the displayed team (ordering by spread scores for rank)
		RecordAggregator roadAgg = teamStats.getHomeAwayRecord(displayTeam, statScope, false, true);
		roadRecord = new RecordDisplay(roadAgg, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> roadRecordRanks = teamStats.getHomeAwayRank(displayTeam, statScope, false, false);
		roadRecord.setRank(roadRecordRanks);
		
		roadAts = new RecordDisplay(roadAgg, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> roadAtsRanks = teamStats.getHomeAwayRank(displayTeam, statScope, false, true);
		roadAts.setRank(roadAtsRanks);
		
		//	Get the primetime record for this season for the displayed team (ordering by spread scores for rank)
		RecordAggregator primetimeAgg = teamStats.getPrimetimeRecord(displayTeam, statScope, true);
		primetimeRecord = new RecordDisplay(primetimeAgg, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> primetimeRanks = teamStats.getPrimetimeRank(displayTeam, statScope, false);
		primetimeRecord.setRank(primetimeRanks);
		
		primetimeAts = new RecordDisplay(primetimeAgg, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> primtimeAtsRanks = teamStats.getPrimetimeRank(displayTeam, statScope, true);
		primetimeAts.setRank(primtimeAtsRanks);
		
		//	Get the MNF record for this season for the displayed team (ordering by spread scores for rank)
		RecordAggregator mnfAgg = teamStats.getRecordByDateTime(displayTeam, null, GregorianCalendar.MONDAY, null, statScope, true);
		mnfRecord = new RecordDisplay(mnfAgg, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfRanks = teamStats.getDateTimeRank(displayTeam, null, GregorianCalendar.MONDAY, null, statScope, false);
		mnfRecord.setRank(mnfRanks);
		
		mnfAts = new RecordDisplay(mnfAgg, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfAtsRanks = teamStats.getDateTimeRank(displayTeam, null, GregorianCalendar.MONDAY, null, statScope, true);
		mnfAts.setRank(mnfAtsRanks);
		
		//	Get the TNT record for this season for the displayed team (ordering by spread scores for rank)
		RecordAggregator tntAgg = teamStats.getRecordByDateTime(displayTeam, null, GregorianCalendar.THURSDAY, null, statScope, true);
		tntRecord = new RecordDisplay(tntAgg, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> tntRanks = teamStats.getDateTimeRank(displayTeam, null, GregorianCalendar.THURSDAY, null, statScope, false);
		tntRecord.setRank(tntRanks);
		
		tntAts = new RecordDisplay(tntAgg, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> tntAtsRanks = teamStats.getDateTimeRank(displayTeam, null, GregorianCalendar.THURSDAY, null, statScope, true);
		tntAts.setRank(tntAtsRanks);
		
		//	Get the combined mnf/tnt record for this season for the displayed team (ordering by spread scores for rank)
		RecordAggregator mnfTntAgg = RecordAggregator.combine(mnfAgg, tntAgg);
		mnfTntCombined = new RecordDisplay(mnfTntAgg, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfTntRanks = teamStats.getMnfTntRank(displayTeam, statScope, false);
		mnfTntCombined.setRank(mnfTntRanks);
		
		mnfTntAts = new RecordDisplay(mnfTntAgg, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfTntAtsRanks = teamStats.getMnfTntRank(displayTeam, statScope, true);
		mnfTntAts.setRank(mnfTntAtsRanks);
		
		//	Get the displayed teams own divisional record for this season (ordering by raw scores for rank)
		RecordAggregator divAgg = teamStats.getDivisionRecord(displayTeam, displayTeam.getDivision(), statScope, false);
		divisionRecord = new RecordDisplay(divAgg, false);
		divisionRanks = teamStats.getDivisionRank(displayTeam, displayTeam.getDivision(), statScope, false);
		divisionRecord.setRank(divisionRanks);
		
		divisionAts = new RecordDisplay(divAgg, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> divisionAtsRanks = teamStats.getDivisionRank(displayTeam, displayTeam.getDivision(), statScope, true);
		divisionAts.setRank(divisionAtsRanks);
		
		//	Get the displayed teams own conference record for this season (ordering by raw scores for rank)
		RecordAggregator confAgg = teamStats.getConferenceRecord(displayTeam, displayTeam.getDivision().getConference(), statScope, false);
		conferenceRecord = new RecordDisplay(confAgg, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> conferenceRanks = teamStats.getConferenceRank(displayTeam, displayTeam.getDivision().getConference(), statScope, false);
		conferenceRecord.setRank(conferenceRanks);
		
		conferenceAts = new RecordDisplay(confAgg, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> conferenceAtsRanks = teamStats.getConferenceRank(displayTeam, displayTeam.getDivision().getConference(), statScope, true);
		conferenceAts.setRank(conferenceAtsRanks);
		
		//	If an opponent found for the next week, get the following stats
		if (nextGame != null) {
			//	Get the opponent from the game, and get the last 5 records against the specified opponent (no ranks, not really applicable)
			TeamForSeason opponent = nextGame.getOtherTeam(displayTeam);
			RecordAggregator oppRawAgg = teamStats.getOverallRecordThroughWeekForAtfs(opponent, currentWeek, statScope, false);
			oppRawRecord = new RecordDisplay(oppRawAgg, false);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> oppRank = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
			oppRawRecord.setRank(oppRank);
			
			oppRecordAts = new RecordDisplay(oppRawAgg, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> oppAtsRank = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
			oppRecordAts.setRank(oppAtsRank);
			
			RecordAggregator last5Agg = teamStats.getRecentRecordAgainstOpponent(displayTeam, opponent, statScope, false);
			last5Record = new RecordDisplay(last5Agg, false);
			last5Ats = new RecordDisplay(last5Agg, true);
			
			//	Get the opponent's divisional record
			Division oppDivision = opponent.getDivision();
			RecordAggregator oppDivAgg = teamStats.getDivisionRecord(opponent, opponent.getDivision(), statScope, false);
			divOppRecord = new RecordDisplay(oppDivAgg, false);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> oppDivRanks = teamStats.getDivisionRank(opponent, oppDivision, statScope, false);
			divOppRecord.setRank(oppDivRanks);
			
			divOppAts = new RecordDisplay(oppDivAgg, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> oppDivAtsRanks = teamStats.getDivisionRank(opponent, oppDivision, statScope, true);
			divOppAts.setRank(oppDivAtsRanks);
			
			//	Get the opponent's conference record
			Conference oppConf = oppDivision.getConference();
			RecordAggregator oppConfAgg = teamStats.getConferenceRecord(opponent, oppConf, statScope, false);
			confOppRecord = new RecordDisplay(oppConfAgg, false);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> oppConfRanks = teamStats.getConferenceRank(opponent, oppConf, statScope, false);
			confOppRecord.setRank(oppConfRanks);
			
			confOppAts = new RecordDisplay(oppConfAgg, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> oppConfAtsRanks = teamStats.getConferenceRank(opponent, oppConf, statScope, true);
			confOppAts.setRank(oppConfAtsRanks);
			
			//	Get the stadium from the next game & get this team's record in the stadium
			Stadium stadium = nextGame.getStadium();
			RecordAggregator stadiumAgg = teamStats.getRecordForStadium(displayTeam, stadium, null, statScope, false);
			stadiumRecord = new RecordDisplay(stadiumAgg, false);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> stadiumRanks = teamStats.getStadiumRank(displayTeam, stadium, null, statScope, false);
			stadiumRecord.setRank(stadiumRanks);
			
			stadiumAts = new RecordDisplay(stadiumAgg, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> stadiumAtsRanks = teamStats.getStadiumRank(displayTeam, stadium, null, statScope, true);
			stadiumAts.setRank(stadiumAtsRanks);
			
			//	Get the timezone from the stadium and get the team's record for the specified timezone
			TimeZone timezone = stadium.getTimezone();
			RecordAggregator timezoneAgg = teamStats.getRecordByTimezone(displayTeam, timezone, statScope, false);
			timezoneRecord = new RecordDisplay(timezoneAgg, false);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> timezoneRanks = teamStats.getTimezoneRank(displayTeam, timezone, statScope, false);
			timezoneRecord.setRank(timezoneRanks);
			
			timezoneAts = new RecordDisplay(timezoneAgg, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> timezoneAtsRanks = teamStats.getTimezoneRank(displayTeam, timezone, statScope, true);
			timezoneAts.setRank(timezoneAtsRanks);
			
			//	Get the roof type from the stadium and get the team's record for the specified roof type
			RoofType roof = stadium.getRoofType();
			RecordAggregator roofAgg = teamStats.getRecordForStadium(displayTeam, null, roof, statScope, false);
			roofTypeRecord = new RecordDisplay(roofAgg, false);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> roofRanks = teamStats.getStadiumRank(displayTeam, null, roof, statScope, false);
			roofTypeRecord.setRank(roofRanks);
			
			roofTypeAts = new RecordDisplay(roofAgg, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> roofAtsRanks = teamStats.getStadiumRank(displayTeam, null, roof, statScope, true);
			roofTypeAts.setRank(roofAtsRanks);
		}
	}
	
	public String getSeasonNumber() {
		if (currentSeason != null) {
			return currentSeason.getSeasonNumber().toString();
		}
		else return "N/a";
	}
	
	public String getConferenceName() {
		if (displayTeam != null) {
			ConferenceType conf = displayTeam.getDivision().getConference().getConferenceType();
			return conf.toString();
		}
		else return "Conference";
	}
	
	public String getDivisionName() {
		if (displayTeam != null) {
			Region region = displayTeam.getDivision().getRegion();
			return getConferenceName() + " " + region.toString();
		}
		else return "Division";
	}
	
	public String getStadiumName() {
		if (nextGame != null) {
			return nextGame.getStadium().getStadiumName();
		}
		else return "Stadium";
	}
	
	public String getRoofType() {
		if (nextGame != null) {
			return nextGame.getStadium().getRoofType().toString();
		}
		else return "Roof";
	}
	
	public String getTimezone() {
		if (nextGame != null) {
			String displayName = nextGame.getStadium().getTimezone().getDisplayName();
			int index = displayName.indexOf("/");
			if (index > -1) {
				displayName = displayName.substring(index + 1);
			}
			return displayName;
		}
		else return "Timezone";
	}
	
	public String getTrending() {
		if (teamOverallRecord != null) {
			//	Get the records & sort by most recent week first
			List<Record> records = teamOverallRecord.getRecords();
			Collections.sort(records, new Comparator<Record>() {
				@Override
				public int compare(Record r1, Record r2) {
					return -1 * (r1.getWeek().getWeekNumber().compareTo(r2.getWeek().getWeekNumber()));
				}
			});
			//	Loop over the records from most recent week to end, breaking if trend ends
			Integer trend = null;
			for (Record r : records) {
				//	Get the wins & losses for the record
				int wins = r.getWins();
				int losses = r.getLosses();
				//	If trend not initialized, initialize to either wins/losses, or zero
				if (trend == null) {
					if (wins > 0) trend = wins;
					else if (losses > 0) trend = -1 * losses;
					else trend = 0;
				}
				else {
					//	If wins is positive with positive trend, add win to trend
					if (wins > 0 && trend >= 0) {
						trend += wins;
					}
					//	If wins is positive with negative trend, found end of trend
					else if (wins > 0) {
						break;
					}
					//	If losses positive with negative trend, add negative loss to trend
					else if (losses > 0 && trend <= 0) {
						trend -= losses;
					}
					//	If losses positive with positive trend, found end of trend
					else if (losses > 0) {
						break;
					}
				}
			}
			
			if (trend != null) {
				String trendString = "";
				if (trend > 0) {
					trendString += "+";
				}
				trendString += trend.toString();
				return trendString;
			}
			else return "0";
		}
		else return "0";
	}
	
	public String getGamesBack() {
		if (divisionRanks != null) {
			int leaderRecord = -999;
			int teamRecord = 0;
			for (RecordAggregator ragg : divisionRanks.keySet()) {
				//	First time through the loop, get the first place score
				if (leaderRecord == -999) {	
					leaderRecord = ragg.getRawTotal();
				}
				//	Check the current list of teams to see if the display team is at this position, exiting loop when found
				if (divisionRanks.get(ragg).contains(displayTeam)) {
					teamRecord = ragg.getRawTotal();
					break;
				}
			}
			//	Get the number of games back, then ensure rounded to nearest 0.5
			double gamesBack = ((double) leaderRecord - (double) teamRecord) / 2;
			Double rounded = new Double(Math.round(gamesBack * 2) / 2.0);
			return rounded.toString();
		}
		else {
			return "-";
		}
	}
	
	public String getRawRecord() {
		return rawRecord != null ? rawRecord.getWltString() : "N/a";
	}
	
	public String getRawWinPct() {
		return rawRecord != null ? rawRecord.getPctString() : "N/a";
	}
	
	public String getRawRank() {
		return rawRecord != null ? rawRecord.getRankString() : "N/a";
	}
	
	public String getRecordAts() {
		return recordAts != null ? recordAts.getWltString() : "N/a";
	}
	
	public String getAtsWinPct() {
		return recordAts != null ? recordAts.getPctString() : "N/a";
	}
	
	public String getAtsRank() {
		return recordAts != null ? recordAts.getRankString() : "N/a";
	}
	
	public String getHomeRecord() {
		return homeRecord != null ? homeRecord.getWltString() : "N/a";
	}
	
	public String getHomeWinPct() {
		return homeRecord != null ? homeRecord.getPctString() : "N/a";
	}
	
	public String getHomeRank() {
		return homeRecord != null ? homeRecord.getRankString() : "N/a";
	}
	
	public String getHomeAtsRecord() {
		return homeAts != null ? homeAts.getWltString() : "N/a";
	}
	
	public String getHomeAtsWinPct() {
		return homeAts != null ? homeAts.getPctString() : "N/a";
	}
	
	public String getHomeAtsRank() {
		return homeAts != null ? homeAts.getRankString() : "N/a";
	}
	
	public String getRoadRecord() {
		return roadRecord != null ? roadRecord.getWltString() : "N/a";
	}
	
	public String getRoadWinPct() {
		return roadRecord != null ? roadRecord.getPctString() : "N/a";
	}
	
	public String getRoadRank() {
		return roadRecord != null ? roadRecord.getRankString() : "N/a";
	}
	
	public String getRoadAtsRecord() {
		return roadAts != null ? roadAts.getWltString() : "N/a";
	}
	
	public String getRoadAtsWinPct() {
		return roadAts != null ? roadAts.getPctString() : "N/a";
	}
	
	public String getRoadAtsRank() {
		return roadAts != null ? roadAts.getRankString() : "N/a";
	}
	
	public String getPrimetimeRecord() {
		return primetimeRecord != null ? primetimeRecord.getWltString() : "N/a";
	}
	
	public String getPrimetimeWinPct() {
		return primetimeRecord != null ? primetimeRecord.getPctString() : "N/a";
	}
	
	public String getPrimetimeRank() {
		return primetimeRecord != null ? primetimeRecord.getRankString() : "N/a";
	}
	
	public String getPrimetimeAtsRecord() {
		return primetimeAts != null ? primetimeAts.getWltString() : "N/a";
	}
	
	public String getPrimetimeAtsWinPct() {
		return primetimeAts != null ? primetimeAts.getPctString() : "N/a";
	}
	
	public String getPrimetimeAtsRank() {
		return primetimeAts != null ? primetimeAts.getRankString() : "N/a";
	}
	
	public String getMnfRecord() {
		return mnfRecord != null ? mnfRecord.getWltString() : "N/a";
	}
	
	public String getMnfWinPct() {
		return mnfRecord != null ? mnfRecord.getPctString() : "N/a";
	}
	
	public String getMnfRank() {
		return mnfRecord != null ? mnfRecord.getRankString() : "N/a";
	}
	
	public String getMnfAtsRecord() {
		return mnfAts != null ? mnfAts.getWltString() : "N/a";
	}
	
	public String getMnfAtsWinPct() {
		return mnfAts != null ? mnfAts.getPctString() : "N/a";
	}
	
	public String getMnfAtsRank() {
		return mnfAts != null ? mnfAts.getRankString() : "N/a";
	}
	
	public String getTntRecord() {
		return tntRecord != null ? tntRecord.getWltString() : "N/a";
	}
	
	public String getTntWinPct() {
		return tntRecord != null ? tntRecord.getPctString() : "N/a";
	}
	
	public String getTntRank() {
		return tntRecord != null ? tntRecord.getRankString() : "N/a";
	}
	
	public String getTntAtsRecord() {
		return tntAts != null ? tntAts.getWltString() : "N/a";
	}
	
	public String getTntAtsWinPct() {
		return tntAts != null ? tntAts.getPctString() : "N/a";
	}
	
	public String getTntAtsRank() {
		return tntAts != null ? tntAts.getRankString() : "N/a";
	}
	
	public String getMnfTntCombinedRecord() {
		return mnfTntCombined != null ? mnfTntCombined.getWltString() : "N/a";
	}
	
	public String getMnfTntCombinedWinPct() {
		return mnfTntCombined != null ? mnfTntCombined.getPctString() : "N/a";
	}
	
	public String getMnfTntCombinedRank() {
		return mnfTntCombined != null ? mnfTntCombined.getRankString() : "N/a";
	}
	
	public String getMnfTntAtsRecord() {
		return mnfTntAts != null ? mnfTntAts.getWltString() : "N/a";
	}
	
	public String getMnfTntAtsWinPct() {
		return mnfTntAts != null ? mnfTntAts.getPctString() : "N/a";
	}
	
	public String getMnfTntAtsRank() {
		return mnfTntAts != null ? mnfTntAts.getRankString() : "N/a";
	}
	
	public String getDivisionRecord() {
		return divisionRecord != null ? divisionRecord.getWltString() : "N/a";
	}
	
	public String getDivisionWinPct() {
		return divisionRecord != null ? divisionRecord.getPctString() : "N/a";
	}
	
	public String getDivisionRank() {
		return divisionRecord != null ? divisionRecord.getRankString() : "N/a";
	}
	
	public String getDivisionAtsRecord() {
		return divisionAts != null ? divisionAts.getWltString() : "N/a";
	}
	
	public String getDivisionAtsWinPct() {
		return divisionAts != null ? divisionAts.getPctString() : "N/a";
	}
	
	public String getDivisionAtsRank() {
		return divisionAts != null ? divisionAts.getRankString() : "N/a";
	}
	
	public String getConferenceRecord() {
		return conferenceRecord != null ? conferenceRecord.getWltString() : "N/a";
	}
	
	public String getConferenceWinPct() {
		return conferenceRecord != null ? conferenceRecord.getPctString() : "N/a";
	}
	
	public String getConferenceRank() {
		return conferenceRecord != null ? conferenceRecord.getRankString() : "N/a";
	}
	
	public String getConferenceAtsRecord() {
		return conferenceAts != null ? conferenceAts.getWltString() : "N/a";
	}
	
	public String getConferenceAtsWinPct() {
		return conferenceAts != null ? conferenceAts.getPctString() : "N/a";
	}
	
	public String getConferenceAtsRank() {
		return conferenceAts != null ? conferenceAts.getRankString() : "N/a";
	}
	
	public String getOpponentRecord() {
		return oppRawRecord != null ? oppRawRecord.getWltString() : "N/a";
	}
	
	public String getOpponentWinPct() {
		return oppRawRecord != null ? oppRawRecord.getPctString() : "N/a";
	}
	
	public String getOpponentRank() {
		return oppRawRecord != null  ? oppRawRecord.getRankString() : "N/a";
	}
	
	public String getOpponentAtsRecord() {
		return oppRecordAts != null ? oppRecordAts.getWltString() : "N/a";
	}
	
	public String getOpponentAtsWinPct() {
		return oppRecordAts != null ? oppRecordAts.getPctString() : "N/a";
	}
	
	public String getOpponentAtsRank() {
		return oppRecordAts != null ? oppRecordAts.getRankString() : "N/a";
	}
	
	public String getLast5Record() {
		return last5Record != null ? last5Record.getWltString() : "N/a";
	}
	
	public String getLast5WinPct() {
		return last5Record != null ? last5Record.getPctString() : "N/a";
	}
	
	public String getLast5AtsRecord() {
		return last5Ats != null ? last5Ats.getWltString() : "N/a";
	}
	
	public String getLast5AtsWinPct() {
		return last5Ats != null ? last5Ats.getPctString() : "N/a";
	}
	
	public String getOpponentDivisionRecord() {
		return divOppRecord != null ? divOppRecord.getWltString() : "N/a";
	}
	
	public String getOpponentDivisionWinPct() {
		return divOppRecord != null ? divOppRecord.getPctString() : "N/a";
	}
	
	public String getOpponentDivisionRank() {
		return divOppRecord != null ? divOppRecord.getRankString() : "N/a";
	}
	
	public String getOpponentDivisionAtsRecord() {
		return divOppAts != null ? divOppAts.getWltString() : "N/a";
	}
	
	public String getOpponentDivisionAtsWinPct() {
		return divOppAts != null ? divOppAts.getPctString() : "N/a";
	}
	
	public String getOpponentDivisionAtsRank() {
		return divOppAts != null ? divOppAts.getRankString() : "N/a";
	}
	
	public String getOpponentConferenceRecord() {
		return confOppRecord != null ? confOppRecord.getWltString() : "N/a";
	}
	
	public String getOpponentConferenceWinPct() {
		return confOppRecord != null ? confOppRecord.getPctString() : "N/a";
	}
	
	public String getOpponentConferenceRank() {
		return confOppRecord != null ? confOppRecord.getRankString() : "N/a";
	}
	
	public String getOpponentConferenceAtsRecord() {
		return confOppAts != null ? confOppAts.getWltString() : "N/a";
	}
	
	public String getOpponentConferenceAtsWinPct() {
		return confOppAts != null ? confOppAts.getPctString() : "N/a";
	}
	
	public String getOpponentConferenceAtsRank() {
		return confOppAts != null ? confOppAts.getRankString() : "N/a";
	}
	
	public String getStadiumRecord() {
		return stadiumRecord != null ? stadiumRecord.getWltString() : "N/a";
	}
	
	public String getStadiumWinPct() {
		return stadiumRecord != null ? stadiumRecord.getPctString() : "N/a";
	}
	
	public String getStadiumRank() {
		return stadiumRecord != null ? stadiumRecord.getRankString() : "N/a";
	}
	
	public String getStadiumAtsRecord() {
		return stadiumAts != null ? stadiumAts.getWltString() : "N/a";
	}
	
	public String getStadiumAtsWinPct() {
		return stadiumAts != null ? stadiumAts.getPctString() : "N/a";
	}
	
	public String getStadiumAtsRank() {
		return stadiumAts != null ? stadiumAts.getRankString() : "N/a";
	}
	
	public String getTimezoneRecord() {
		return timezoneRecord != null ? timezoneRecord.getWltString() : "N/a";
	}
	
	public String getTimezoneWinPct() {
		return timezoneRecord != null ? timezoneRecord.getPctString() : "N/a";
	}
	
	public String getTimezoneRank() {
		return timezoneRecord != null ? timezoneRecord.getRankString() : "N/a";
	}
	
	public String getTimezoneAtsRecord() {
		return timezoneAts != null ? timezoneAts.getWltString() : "N/a";
	}
	
	public String getTimezoneAtsWinPct() {
		return timezoneAts != null ? timezoneAts.getPctString() : "N/a";
	}
	
	public String getTimezoneAtsRank() {
		return timezoneAts != null ? timezoneAts.getRankString() : "N/a";
	}
	
	public String getRoofTypeRecord() {
		return roofTypeRecord != null ? roofTypeRecord.getWltString() : "N/a";
	}
	
	public String getRoofTypeWinPct() {
		return roofTypeRecord != null ? roofTypeRecord.getPctString() : "N/a";
	}
	
	public String getRoofTypeRank() {
		return roofTypeRecord != null ? roofTypeRecord.getRankString() : "N/a";
	}
	
	public String getRoofTypeAtsRecord() {
		return roofTypeAts != null ? roofTypeAts.getWltString() : "N/a";
	}
	
	public String getRoofTypeAtsWinPct() {
		return roofTypeAts != null ? roofTypeAts.getPctString() : "N/a";
	}
	
	public String getRoofTypeAtsRank() {
		return roofTypeAts != null ? roofTypeAts.getRankString() : "N/a";
	}
}
