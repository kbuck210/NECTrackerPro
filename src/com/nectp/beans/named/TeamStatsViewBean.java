package com.nectp.beans.named;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.TeamStatisticService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Named(value="teamStatsViewBean")
@RequestScoped
public class TeamStatsViewBean implements Serializable {
	private static final long serialVersionUID = 8048143957108685011L;
	
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
	private RecordDisplay stadiumRecord;
	private RecordDisplay stadiumAts;
	private RecordDisplay timezoneRecord;
	private RecordDisplay timezoneAts;
	private RecordDisplay roofTypeRecord;
	private RecordDisplay roofTypeAts;
	private RecordDisplay last5Record;
	private RecordDisplay last5Ats;
	private RecordDisplay divOppRecord;
	private RecordDisplay divOppAts;
	private RecordDisplay confOppRecord;
	private RecordDisplay confOppAts;
	
	//	The team who's stats to display, & the scope for which to get the statistics
	private TeamForSeason displayTeam = null;
	private Game nextGame = null;
	private NEC statScope = NEC.SEASON;
	
	private Season currentSeason = null;
	
	@Inject
	private TeamStatisticService teamStats;
	
	public TeamStatsViewBean() {
	}
	
	@PostConstruct
	public void init() {
		//	TODO: get team from request & load stat models
		if (displayTeam != null) {
			currentSeason = displayTeam.getSeason();
			Week currentWeek = currentSeason.getCurrentWeek();
			
			//	Get the overall record for this season so far for the displayed team (ordering by raw scores for rank)
			RecordAggregator teamOverallRecord = teamStats.getOverallRecordThroughWeekForAtfs(displayTeam, currentWeek, statScope, false);
			rawRecord = new RecordDisplay(teamOverallRecord, false);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> rawRecordRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
			rawRecord.setRank(rawRecordRanks);
			
			recordAts = new RecordDisplay(teamOverallRecord, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> recordAtsRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
			recordAts.setRank(recordAtsRanks);
			
			//	Get the home record for this season for the displayed team (ordering by spread scores for rank)
			RecordAggregator homeAgg = teamStats.getHomeAwayRecord(displayTeam, statScope, true, true);
			homeRecord = new RecordDisplay(homeAgg, false);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> homeRecordRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
			homeRecord.setRank(homeRecordRanks);
			
			homeAts = new RecordDisplay(homeAgg, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> homeAtsRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
			homeAts.setRank(homeAtsRanks);
			
			//	Get the road record for this season for the displayed team (ordering by spread scores for rank)
			RecordAggregator roadAgg = teamStats.getHomeAwayRecord(displayTeam, statScope, false, true);
			roadRecord = new RecordDisplay(roadAgg, false);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> roadRecordRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
			roadRecord.setRank(roadRecordRanks);
			
			roadAts = new RecordDisplay(roadAgg, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> roadAtsRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
			roadAts.setRank(roadAtsRanks);
			
			//	Get the primetime record for this season for the displayed team (ordering by spread scores for rank)
			RecordAggregator primetimeAgg = teamStats.getPrimetimeRecord(displayTeam, statScope, true);
			primetimeRecord = new RecordDisplay(primetimeAgg, false);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> primetimeRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
			primetimeRecord.setRank(primetimeRanks);
			
			primetimeAts = new RecordDisplay(primetimeAgg, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> primtimeAtsRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
			primetimeAts.setRank(primtimeAtsRanks);
			
			//	Get the MNF record for this season for the displayed team (ordering by spread scores for rank)
			RecordAggregator mnfAgg = teamStats.getRecordByDateTime(displayTeam, null, GregorianCalendar.MONDAY, null, statScope, true);
			mnfRecord = new RecordDisplay(mnfAgg, false);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
			mnfRecord.setRank(mnfRanks);
			
			mnfAts = new RecordDisplay(mnfAgg, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfAtsRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
			mnfAts.setRank(mnfAtsRanks);
			
			//	Get the TNT record for this season for the displayed team (ordering by spread scores for rank)
			RecordAggregator tntAgg = teamStats.getRecordByDateTime(displayTeam, null, GregorianCalendar.THURSDAY, null, statScope, true);
			tntRecord = new RecordDisplay(tntAgg, false);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> tntRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
			tntRecord.setRank(tntRanks);
			
			tntAts = new RecordDisplay(tntAgg, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> tntAtsRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
			tntAts.setRank(tntAtsRanks);
			
			//	Get the combined mnf/tnt record for this season for the displayed team (ordering by spread scores for rank)
			RecordAggregator mnfTntAgg = RecordAggregator.combine(mnfAgg, tntAgg);
			mnfTntCombined = new RecordDisplay(mnfTntAgg, false);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfTntRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
			mnfTntCombined.setRank(mnfTntRanks);
			
			mnfTntAts = new RecordDisplay(mnfTntAgg, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfTntAtsRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
			mnfTntAts.setRank(mnfTntAtsRanks);
			
			//	Get the displayed teams own divisional record for this season (ordering by raw scores for rank)
			RecordAggregator divAgg = teamStats.getDivisionRecord(displayTeam, displayTeam.getDivision(), statScope, false);
			divisionRecord = new RecordDisplay(divAgg, false);
			//	TODO: evaluate ranking strategy for division/conference filtering
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> divisionRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
			divisionRecord.setRank(divisionRanks);
			
			divisionAts = new RecordDisplay(divAgg, true);
//			TODO: evaluate ranking strategy for division/conference filtering
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> divisionAtsRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
			divisionAts.setRank(divisionAtsRanks);
			
			//	Get the displayed teams own conference record for this season (ordering by raw scores for rank)
			RecordAggregator confAgg = teamStats.getConferenceRecord(displayTeam, displayTeam.getDivision().getConference(), statScope, false);
			conferenceRecord = new RecordDisplay(confAgg, false);
//			TODO: evaluate ranking strategy for division/conference filtering
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> conferenceRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
			conferenceRecord.setRank(conferenceRanks);
			
			conferenceAts = new RecordDisplay(confAgg, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> conferenceAtsRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
			conferenceAts.setRank(conferenceAtsRanks);
			
			//	If an opponent found for the next week, get the following stats
			if (nextGame != null) {
				RecordAggregator stadiumAgg = teamStats.getRecordForStadium(displayTeam, nextGame.getStadium(), null, statScope, false);
				stadiumRecord = new RecordDisplay(stadiumAgg, false);
				TreeMap<RecordAggregator, List<AbstractTeamForSeason>> stadiumRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
				stadiumRecord.setRank(stadiumRanks);
				
				stadiumAts = new RecordDisplay(stadiumAgg, true);
				TreeMap<RecordAggregator, List<AbstractTeamForSeason>> stadiumAtsRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
				stadiumAts.setRank(stadiumAtsRanks);
				
				RecordAggregator timezoneAgg = teamStats.getRecordByTimezone(displayTeam, nextGame.getStadium().getTimezone(), statScope, false);
				timezoneRecord = new RecordDisplay(timezoneAgg, false);
				TreeMap<RecordAggregator, List<AbstractTeamForSeason>> timezoneRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
				timezoneRecord.setRank(timezoneRanks);
				
				timezoneAts = new RecordDisplay(timezoneAgg, true);
				TreeMap<RecordAggregator, List<AbstractTeamForSeason>> timezoneAtsRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
				timezoneAts.setRank(timezoneAtsRanks);
				
				RecordAggregator roofAgg = teamStats.getRecordForStadium(displayTeam, null, nextGame.getStadium().getRoofType(), statScope, false);
				roofTypeRecord = new RecordDisplay(roofAgg, false);
				TreeMap<RecordAggregator, List<AbstractTeamForSeason>> roofRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
				roofTypeRecord.setRank(roofRanks);
				
				roofTypeAts = new RecordDisplay(roofAgg, true);
				TreeMap<RecordAggregator, List<AbstractTeamForSeason>> roofRanksAts = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
				roofTypeAts.setRank(roofRanksAts);
				
				TeamForSeason opponent = nextGame.getOtherTeam(displayTeam);
				RecordAggregator last5Agg = teamStats.getRecentRecordAgainstOpponent(displayTeam, opponent, statScope, false);
				last5Record = new RecordDisplay(last5Agg, false);
				TreeMap<RecordAggregator, List<AbstractTeamForSeason>> last5Ranks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, false);
				last5Record.setRank(last5Ranks);
				
				last5Ats = new RecordDisplay(last5Agg, true);
				TreeMap<RecordAggregator, List<AbstractTeamForSeason>> last5AtsRanks = teamStats.getTeamRankedScoresForType(statScope, currentSeason, true);
			}
		}
	}
}
