package com.nectp.beans.named.profile;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeMap;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.named.RecordDisplay;
import com.nectp.beans.named.profile.StatsBean;
import com.nectp.beans.remote.daos.ConferenceService;
import com.nectp.beans.remote.daos.DivisionService;
import com.nectp.beans.remote.daos.StatisticService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Conference.ConferenceType;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Division.Region;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.Week;

@Named(value="playerStatsBean")
@ViewScoped
public class PlayerStatsBean extends StatsBean<PlayerForSeason> implements Serializable {
	private static final long serialVersionUID = -227434938298483558L;

	private RecordDisplay firstHalfRecord;
	private RecordDisplay firstHalfRecordAts;
	private RecordDisplay secondHalfRecord;
	private RecordDisplay secondHalfRecordAts;
	private RecordDisplay playoffsRecord;
	private RecordDisplay playoffsRecordAts;
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
	private RecordDisplay afcEastRecord;
	private RecordDisplay afcNorthRecord;
	private RecordDisplay afcSouthRecord;
	private RecordDisplay afcWestRecord;
	private RecordDisplay nfcEastRecord;
	private RecordDisplay nfcNorthRecord;
	private RecordDisplay nfcSouthRecord;
	private RecordDisplay nfcWestRecord;
	private RecordDisplay afcEastRecordAts;
	private RecordDisplay afcNorthRecordAts;
	private RecordDisplay afcSouthRecordAts;
	private RecordDisplay afcWestRecordAts;
	private RecordDisplay nfcEastRecordAts;
	private RecordDisplay nfcNorthRecordAts;
	private RecordDisplay nfcSouthRecordAts;
	private RecordDisplay nfcWestRecordAts;
	private RecordDisplay afcRecord;
	private RecordDisplay afcRecordAts;
	private RecordDisplay nfcRecord;
	private RecordDisplay nfcRecordAts;
	
	//	Rank maps used to get scores and leader scores
	private TreeMap<RecordAggregator, List<AbstractTeamForSeason>> firstHalfRanks;
	private TreeMap<RecordAggregator, List<AbstractTeamForSeason>> secondHalfRanks;
	private TreeMap<RecordAggregator, List<AbstractTeamForSeason>> playoffRanks;

	@EJB
	private StatisticService<PlayerForSeason> playerStats;
	
	@EJB
	private ConferenceService conferenceService;
	
	@EJB
	private DivisionService divisionService;
	
	public void calculateStats() {
		//	Get the scores for each of the subseasons (except superbowl)
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> firstHalfRawRank = playerStats.getPlayerRankedScoresForType(NEC.FIRST_HALF, season, false);
		firstHalfRanks = playerStats.getPlayerRankedScoresForType(NEC.FIRST_HALF, season, true);
		firstHalfRecord = getRankedRecordDisplay(firstHalfRawRank, profileEntity, false);
		firstHalfRecordAts = getRankedRecordDisplay(firstHalfRanks, profileEntity, true);
		
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> secondHalfRawRank = playerStats.getPlayerRankedScoresForType(NEC.SECOND_HALF, season, false);
		secondHalfRanks = playerStats.getPlayerRankedScoresForType(NEC.SECOND_HALF, season, true);
		secondHalfRecord = getRankedRecordDisplay(secondHalfRawRank, profileEntity, false);
		secondHalfRecordAts = getRankedRecordDisplay(secondHalfRanks, profileEntity, true);
		
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> playoffsRawRank = playerStats.getPlayerRankedScoresForType(NEC.PLAYOFFS, season, false);
		playoffRanks = playerStats.getPlayerRankedScoresForType(NEC.PLAYOFFS, season, true);
		playoffsRecord = getRankedRecordDisplay(playoffsRawRank, profileEntity, false);
		playoffsRecordAts = getRankedRecordDisplay(playoffRanks, profileEntity, true);
		
		//	Get the overall record for this season so far for the displayed player (ordering by raw scores for rank)
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> rawRecordRanks = playerStats.getPlayerRankedScoresForType(NEC.SEASON, season, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> recordAtsRanks = playerStats.getPlayerRankedScoresForType(NEC.SEASON, season, true);
		rawRecord = getRankedRecordDisplay(rawRecordRanks, profileEntity, false);
		recordAts = getRankedRecordDisplay(recordAtsRanks, profileEntity, true);

		//	Get the home record for this season for the displayed team (ordering by spread scores for rank)
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> homeRecordRanks = playerStats.getHomeAwayRank(profileEntity, NEC.SEASON, true, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> homeAtsRanks = playerStats.getHomeAwayRank(profileEntity, NEC.SEASON, true, true);
		homeRecord = getRankedRecordDisplay(homeRecordRanks, profileEntity, false);
		homeAts = getRankedRecordDisplay(homeAtsRanks, profileEntity, true);

		//	Get the road record for this season for the displayed team (ordering by spread scores for rank)
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> roadAtsRanks = playerStats.getHomeAwayRank(profileEntity, NEC.SEASON, false, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> roadRecordRanks = playerStats.getHomeAwayRank(profileEntity, NEC.SEASON, false, false);
		roadRecord = getRankedRecordDisplay(roadRecordRanks, profileEntity, false);
		roadAts = getRankedRecordDisplay(roadAtsRanks, profileEntity, true);

		//	Get the primetime record for this season for the displayed team (ordering by spread scores for rank)
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> primtimeAtsRanks = playerStats.getPrimetimeRank(profileEntity, NEC.SEASON, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> primetimeRanks = playerStats.getPrimetimeRank(profileEntity, NEC.SEASON, false);
		primetimeRecord = getRankedRecordDisplay(primetimeRanks, profileEntity, false);
		primetimeAts = getRankedRecordDisplay(primtimeAtsRanks, profileEntity, true);

		//	Get the MNF record for this season for the displayed team (ordering by spread scores for rank)
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfAtsRanks = playerStats.getDateTimeRank(profileEntity, null, GregorianCalendar.MONDAY, null, NEC.SEASON, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfRanks = playerStats.getDateTimeRank(profileEntity, null, GregorianCalendar.MONDAY, null, NEC.SEASON, false);
		mnfRecord = getRankedRecordDisplay(mnfRanks, profileEntity, false);
		mnfAts = getRankedRecordDisplay(mnfAtsRanks, profileEntity, true);

		//	Get the TNT record for this season for the displayed team (ordering by spread scores for rank)
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> tntRanks = playerStats.getDateTimeRank(profileEntity, null, GregorianCalendar.THURSDAY, null, NEC.SEASON, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> tntAtsRanks = playerStats.getDateTimeRank(profileEntity, null, GregorianCalendar.THURSDAY, null, NEC.SEASON, true);
		tntRecord = getRankedRecordDisplay(tntRanks, profileEntity, false);
		tntAts = getRankedRecordDisplay(tntAtsRanks, profileEntity, true);

		//	Get the combined mnf/tnt record for this season for the displayed team (ordering by spread scores for rank)
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfTntRanks = playerStats.getMnfTntRank(profileEntity, NEC.SEASON, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> mnfTntAtsRanks = playerStats.getMnfTntRank(profileEntity, NEC.SEASON, true);
		mnfTntCombined = getRankedRecordDisplay(mnfTntRanks, profileEntity, false);
		mnfTntAts = getRankedRecordDisplay(mnfTntAtsRanks, profileEntity, true);

		//	Get the divisonal & conference records 
		Conference afc = conferenceService.selectConferenceByType(ConferenceType.AFC);
		Conference nfc = conferenceService.selectConferenceByType(ConferenceType.NFC);
		Division afcEast = afc.getDivisionForRegion(Region.EAST);
		Division afcNorth = afc.getDivisionForRegion(Region.NORTH);
		Division afcSouth = afc.getDivisionForRegion(Region.SOUTH);
		Division afcWest = afc.getDivisionForRegion(Region.WEST);
		Division nfcEast = nfc.getDivisionForRegion(Region.EAST);
		Division nfcNorth = nfc.getDivisionForRegion(Region.NORTH);
		Division nfcSouth = nfc.getDivisionForRegion(Region.SOUTH);
		Division nfcWest = nfc.getDivisionForRegion(Region.WEST);

		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> afcEastRanks = playerStats.getDivisionRank(profileEntity, afcEast, NEC.SEASON, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> afcNorthRanks = playerStats.getDivisionRank(profileEntity, afcNorth, NEC.SEASON, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> afcSouthRanks = playerStats.getDivisionRank(profileEntity, afcSouth, NEC.SEASON, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> afcWestRanks = playerStats.getDivisionRank(profileEntity, afcWest, NEC.SEASON, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> nfcEastRanks = playerStats.getDivisionRank(profileEntity, nfcEast, NEC.SEASON, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> nfcNorthRanks = playerStats.getDivisionRank(profileEntity, nfcNorth, NEC.SEASON, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> nfcSouthRanks = playerStats.getDivisionRank(profileEntity, nfcSouth, NEC.SEASON, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> nfcWestRanks = playerStats.getDivisionRank(profileEntity, nfcWest, NEC.SEASON, false);

		afcEastRecord = getRankedRecordDisplay(afcEastRanks, profileEntity, false);
		afcNorthRecord = getRankedRecordDisplay(afcNorthRanks, profileEntity, false);
		afcSouthRecord = getRankedRecordDisplay(afcSouthRanks, profileEntity, false);
		afcWestRecord = getRankedRecordDisplay(afcWestRanks, profileEntity, false);
		nfcEastRecord = getRankedRecordDisplay(nfcEastRanks, profileEntity, false);
		nfcNorthRecord = getRankedRecordDisplay(nfcNorthRanks, profileEntity, false);
		nfcSouthRecord = getRankedRecordDisplay(nfcSouthRanks, profileEntity, false);
		nfcWestRecord = getRankedRecordDisplay(nfcWestRanks, profileEntity, false);

		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> afcEastRanksAts = playerStats.getDivisionRank(profileEntity, afcEast, NEC.SEASON, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> afcNorthRanksAts = playerStats.getDivisionRank(profileEntity, afcNorth, NEC.SEASON, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> afcSouthRanksAts = playerStats.getDivisionRank(profileEntity, afcSouth, NEC.SEASON, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> afcWestRanksAts = playerStats.getDivisionRank(profileEntity, afcWest, NEC.SEASON, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> nfcEastRanksAts = playerStats.getDivisionRank(profileEntity, nfcEast, NEC.SEASON, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> nfcNorthRanksAts = playerStats.getDivisionRank(profileEntity, nfcNorth, NEC.SEASON, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> nfcSouthRanksAts = playerStats.getDivisionRank(profileEntity, nfcSouth, NEC.SEASON, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> nfcWestRanksAts = playerStats.getDivisionRank(profileEntity, nfcWest, NEC.SEASON, true);

		afcEastRecordAts = getRankedRecordDisplay(afcEastRanksAts, profileEntity, true);
		afcNorthRecordAts = getRankedRecordDisplay(afcNorthRanksAts, profileEntity, true);
		afcSouthRecordAts = getRankedRecordDisplay(afcSouthRanksAts, profileEntity, true);
		afcWestRecordAts = getRankedRecordDisplay(afcWestRanksAts, profileEntity, true);
		nfcEastRecordAts = getRankedRecordDisplay(nfcEastRanksAts, profileEntity, true);
		nfcNorthRecordAts = getRankedRecordDisplay(nfcNorthRanksAts, profileEntity, true);
		nfcSouthRecordAts = getRankedRecordDisplay(nfcSouthRanksAts, profileEntity, true);
		nfcWestRecordAts = getRankedRecordDisplay(nfcWestRanksAts, profileEntity, true);

		//	Get the displayed teams own conference record for this season (ordering by raw scores for rank)
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> afcRanks = playerStats.getConferenceRank(profileEntity, afc, NEC.SEASON, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> afcRanksAts = playerStats.getConferenceRank(profileEntity, afc, NEC.SEASON, true);
		afcRecord = getRankedRecordDisplay(afcRanks, profileEntity, false);
		afcRecordAts = getRankedRecordDisplay(afcRanksAts, profileEntity, true);

		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> nfcRanks = playerStats.getConferenceRank(profileEntity, nfc, NEC.SEASON, false);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> nfcRanksAts = playerStats.getConferenceRank(profileEntity, nfc, NEC.SEASON, true);
		nfcRecord = getRankedRecordDisplay(nfcRanks, profileEntity, false);
		nfcRecordAts = getRankedRecordDisplay(nfcRanksAts, profileEntity, true);
	}
	
	public String getSeasonNumber() {
		if (season != null) {
			return season.getSeasonNumber().toString();
		}
		else return "N/a";
	}
	
	/** Gets the player's score for the current subseason
	 * 
	 * @return
	 */
	public String getScore() {
		Week currentWeek = season.getCurrentWeek();
		if (currentWeek == null) return "N/a";
		
		Subseason subseason = currentWeek.getSubseason();
		RecordAggregator scoreAgg = null;
		switch (subseason.getSubseasonType()) {
		case FIRST_HALF:
			scoreAgg = firstHalfRecordAts.getRecordAggregator();
			break;
		case SECOND_HALF:
			scoreAgg = secondHalfRecordAts.getRecordAggregator();
			break;
		case PLAYOFFS:
			scoreAgg = playoffsRecordAts.getRecordAggregator();
			break;
		case SUPER_BOWL:
			scoreAgg = playoffsRecordAts.getRecordAggregator();
			break;
		default:
			return "N/a";
		}
		
		return scoreAgg.scoreString(true);
	}
	
	/** Gets the player's trending score for the current subseason
	 * 
	 * @return
	 */
	public String getTrending() {
		Week currentWeek = season.getCurrentWeek();
		if (currentWeek == null) return "N/a";
		
		Subseason subseason = currentWeek.getSubseason();
		RecordAggregator trendAgg = null;
		switch (subseason.getSubseasonType()) {
		case FIRST_HALF:
			trendAgg = firstHalfRecordAts.getRecordAggregator();
			break;
		case SECOND_HALF:
			trendAgg = secondHalfRecordAts.getRecordAggregator();
			break;
		case PLAYOFFS:
			trendAgg = playoffsRecordAts.getRecordAggregator();
			break;
		case SUPER_BOWL:
			trendAgg = playoffsRecordAts.getRecordAggregator();
			break;
		default:
			return "N/a";
		}
		if (trendAgg != null) {
			//	Get the records & sort by most recent week first
			List<Record> records = trendAgg.getRecords();
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
	
	public String getLeaderScore() {
		Week currentWeek = season.getCurrentWeek();
		if (currentWeek == null) return "N/a";
		
		Subseason subseason = currentWeek.getSubseason();
		RecordAggregator leaderAgg = null;
		switch (subseason.getSubseasonType()) {
		case FIRST_HALF:
			leaderAgg = firstHalfRanks.firstKey();
			break;
		case SECOND_HALF:
			leaderAgg = secondHalfRanks.firstKey();
			break;
		case PLAYOFFS:
			leaderAgg = playoffRanks.firstKey();
			break;
		case SUPER_BOWL:
			leaderAgg = playoffRanks.firstKey();
			break;
		default:
			return "N/a";
		}
		
		return leaderAgg.scoreString(true);
	}
	
	public RecordDisplay getFirstHalfDisplay() {
		return firstHalfRecordAts;
	}
	
	public RecordDisplay getSecondHalfDisplay() {
		return secondHalfRecordAts;
	}
	
	public RecordDisplay getPlayoffsDisplay() {
		return playoffsRecordAts;
	}
	
	public RecordDisplay getSeasonDisplay() {
		return recordAts;
	}
	
	public String getFirstHalfRecord() {
		return firstHalfRecord != null ? firstHalfRecord.getWltString() : "N/a";
	}
	
	public String getFirstHalfWinPct() {
		return firstHalfRecord != null ? firstHalfRecord.getPctString() : "N/a";
	}
	
	public String getFirstHalfRank() {
		return firstHalfRecord != null ? firstHalfRecord.getRankString() : "N/a";
	}
	
	public String getFirstHalfRecordAts() {
		return firstHalfRecordAts != null ? firstHalfRecordAts.getWltString() : "N/a";
	}
	
	public String getFirstHalfWinPctAts() {
		return firstHalfRecordAts != null ? firstHalfRecordAts.getPctString() : "N/a";
	}
	
	public String getFirstHalfRankAts() {
		return firstHalfRecordAts != null ? firstHalfRecordAts.getRankString() : "N/a";
	}
	
	public String getSecondHalfRecord() {
		return secondHalfRecord != null ? secondHalfRecord.getWltString() : "N/a";
	}
	
	public String getSecondHalfWinPct() {
		return secondHalfRecord != null ? secondHalfRecord.getPctString() : "N/a";
	}
	
	public String getSecondHalfRank() {
		return secondHalfRecord != null ? secondHalfRecord.getRankString() : "N/a";
	}
	
	public String getSecondHalfRecordAts() {
		return secondHalfRecordAts != null ? secondHalfRecordAts.getWltString() : "N/a";
	}
	
	public String getSecondHalfWinPctAts() {
		return secondHalfRecordAts != null ? secondHalfRecordAts.getPctString() : "N/a";
	}
	
	public String getSecondHalfRankAts() {
		return secondHalfRecordAts != null ? secondHalfRecordAts.getRankString() : "N/a";
	}
	
	public String getPlayoffsRecord() {
		return playoffsRecord != null ? playoffsRecord.getWltString() : "N/a";
	}
	
	public String getPlayoffsWinPct() {
		return playoffsRecord != null ? playoffsRecord.getPctString() : "N/a";
	}
	
	public String getPlayoffsRank() {
		return playoffsRecord != null ? playoffsRecord.getRankString() : "N/a";
	}
	
	public String getPlayoffsRecordAts() {
		return playoffsRecordAts != null ? playoffsRecordAts.getWltString() : "N/a";
	}
	
	public String getPlayoffsWinPctAts() {
		return playoffsRecordAts != null ? playoffsRecordAts.getPctString() : "N/a";
	}
	
	public String getPlayoffsRankAts() {
		return playoffsRecordAts != null ? playoffsRecordAts.getRankString() : "N/a";
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
	
	public String getAfcEastRecord() {
		return afcEastRecord != null ? afcEastRecord.getWltString() : "N/a";
	}
	
	public String getAfcEastWinPct() {
		return afcEastRecord != null ? afcEastRecord.getPctString() : "N/a";
	}
	
	public String getAfcEastRank() {
		return afcEastRecord != null ? afcEastRecord.getRankString() : "N/a";
	}
	
	public String getAfcEastRecordAts() {
		return afcEastRecordAts != null ? afcEastRecordAts.getWltString() : " N/a";
	}
	
	public String getAfcEastWinPctAts() {
		return afcEastRecordAts != null ? afcEastRecordAts.getPctString() : "N/a";
	}
	
	public String getAfcEastRankAts() {
		return afcEastRecordAts != null ? afcEastRecordAts.getRankString() : "N/a";
	}
	
	public String getAfcNorthRecord() {
		return afcNorthRecord != null ? afcNorthRecord.getWltString() : "N/a";
	}
	
	public String getAfcNorthWinPct() {
		return afcNorthRecord != null ? afcNorthRecord.getPctString() : "N/a";
	}
	
	public String getAfcNorthRank() {
		return afcNorthRecord != null ? afcNorthRecord.getRankString() : "N/a";
	}
	
	public String getAfcNorthRecordAts() {
		return afcNorthRecordAts != null ? afcNorthRecordAts.getWltString() : "N/a";
	}
	
	public String getAfcNorthWinPctAts() {
		return afcNorthRecordAts != null ? afcNorthRecordAts.getPctString() : "N/a";
	}
	
	public String getAfcNorthRankAts() {
		return afcNorthRecordAts != null ? afcNorthRecordAts.getRankString() : "N/a";
	}
	
	public String getAfcSouthRecord() {
		return afcSouthRecord != null ? afcSouthRecord.getWltString() : "N/a";
	}
	
	public String getAfcSouthWinPct() {
		return afcSouthRecord != null ? afcSouthRecord.getPctString() : "N/a";
	}
	
	public String getAfcSouthRank() {
		return afcSouthRecord != null ? afcSouthRecord.getRankString() : "N/a";
	}
	
	public String getAfcSouthRecordAts() {
		return afcSouthRecordAts != null ? afcSouthRecordAts.getWltString() : "N/a";
	}
	
	public String getAfcSouthWinPctAts() {
		return afcSouthRecordAts != null ? afcSouthRecordAts.getPctString() : "N/a";
	}
	
	public String getAfcSouthRankAts() {
		return afcSouthRecordAts != null ? afcSouthRecordAts.getRankString() : "N/a";
	}
	
	public String getAfcWestRecord() {
		return afcWestRecord != null ? afcWestRecord.getWltString() : "N/a";
	}
	
	public String getAfcWestWinPct() {
		return afcWestRecord != null ? afcWestRecord.getPctString() : "N/a";
	}
	
	public String getAfcWestRank() {
		return afcWestRecord != null ? afcWestRecord.getRankString() : "N/a";
	}
	
	public String getAfcWestRecordAts() {
		return afcWestRecordAts != null ? afcWestRecordAts.getWltString() : "N/a";
	}
	
	public String getAfcWestWinPctAts() {
		return afcWestRecordAts != null ? afcWestRecordAts.getPctString() : "N/a";
	}
	
	public String getAfcWestRankAts() {
		return afcWestRecordAts != null ? afcWestRecordAts.getRankString() : "N/a";
	}
	
	public String getNfcEastRecord() {
		return nfcEastRecord != null ? nfcEastRecord.getWltString() : "N/a";
	}
	
	public String getNfcEastWinPct() {
		return nfcEastRecord != null ? nfcEastRecord.getPctString() : "N/a";
	}
	
	public String getNfcEastRank() {
		return nfcEastRecord != null ? nfcEastRecord.getRankString() : "N/a";
	}
	
	public String getNfcEastRecordAts() {
		return nfcEastRecordAts != null ? nfcEastRecordAts.getWltString() : " N/a";
	}
	
	public String getNfcEastWinPctAts() {
		return nfcEastRecordAts != null ? nfcEastRecordAts.getPctString() : "N/a";
	}
	
	public String getNfcEastRankAts() {
		return nfcEastRecordAts != null ? nfcEastRecordAts.getRankString() : "N/a";
	}
	
	public String getNfcNorthRecord() {
		return nfcNorthRecord != null ? nfcNorthRecord.getWltString() : "N/a";
	}
	
	public String getNfcNorthWinPct() {
		return nfcNorthRecord != null ? nfcNorthRecord.getPctString() : "N/a";
	}
	
	public String getNfcNorthRank() {
		return nfcNorthRecord != null ? nfcNorthRecord.getRankString() : "N/a";
	}
	
	public String getNfcNorthRecordAts() {
		return nfcNorthRecordAts != null ? nfcNorthRecordAts.getWltString() : "N/a";
	}
	
	public String getNfcNorthWinPctAts() {
		return nfcNorthRecordAts != null ? nfcNorthRecordAts.getPctString() : "N/a";
	}
	
	public String getNfcNorthRankAts() {
		return nfcNorthRecordAts != null ? nfcNorthRecordAts.getRankString() : "N/a";
	}
	
	public String getNfcSouthRecord() {
		return nfcSouthRecord != null ? nfcSouthRecord.getWltString() : "N/a";
	}
	
	public String getNfcSouthWinPct() {
		return nfcSouthRecord != null ? nfcSouthRecord.getPctString() : "N/a";
	}
	
	public String getNfcSouthRank() {
		return nfcSouthRecord != null ? nfcSouthRecord.getRankString() : "N/a";
	}
	
	public String getNfcSouthRecordAts() {
		return nfcSouthRecordAts != null ? nfcSouthRecordAts.getWltString() : "N/a";
	}
	
	public String getNfcSouthWinPctAts() {
		return nfcSouthRecordAts != null ? nfcSouthRecordAts.getPctString() : "N/a";
	}
	
	public String getNfcSouthRankAts() {
		return nfcSouthRecordAts != null ? nfcSouthRecordAts.getRankString() : "N/a";
	}
	
	public String getNfcWestRecord() {
		return nfcWestRecord != null ? nfcWestRecord.getWltString() : "N/a";
	}
	
	public String getNfcWestWinPct() {
		return nfcWestRecord != null ? nfcWestRecord.getPctString() : "N/a";
	}
	
	public String getNfcWestRank() {
		return nfcWestRecord != null ? nfcWestRecord.getRankString() : "N/a";
	}
	
	public String getNfcWestRecordAts() {
		return nfcWestRecordAts != null ? nfcWestRecordAts.getWltString() : "N/a";
	}
	
	public String getNfcWestWinPctAts() {
		return nfcWestRecordAts != null ? nfcWestRecordAts.getPctString() : "N/a";
	}
	
	public String getNfcWestRankAts() {
		return nfcWestRecordAts != null ? nfcWestRecordAts.getRankString() : "N/a";
	}
	
	public String getAfcRecord() {
		return afcRecord != null ? afcRecord.getWltString() : "N/a";
	}
	
	public String getAfcWinPct() {
		return afcRecord != null ? afcRecord.getPctString() : "N/a";
	}
	
	public String getAfcRank() {
		return afcRecord != null ? afcRecord.getRankString() : "N/a";
	}
	
	public String getNfcRecord() {
		return nfcRecord != null ? nfcRecord.getWltString() : "N/a";
	}
	
	public String getNfcWinPct() {
		return nfcRecord != null ? nfcRecord.getPctString() : "N/a";
	}
	
	public String getNfcRank() {
		return nfcRecord != null ? nfcRecord.getRankString() : "N/a";
	}
	
	public String getAfcRecordAts() {
		return afcRecordAts != null ? afcRecordAts.getWltString() : "N/a";
	}
	
	public String getAfcWinPctAts() {
		return afcRecordAts != null ? afcRecordAts.getPctString() : "N/a";
	}
	
	public String getAfcRankAts() {
		return afcRecordAts != null ? afcRecordAts.getRankString() : "N/a";
	}
	
	public String getNfcRecordAts() {
		return nfcRecordAts != null ? nfcRecordAts.getWltString() : "N/a";
	}
	
	public String getNfcWinPctAts() {
		return nfcRecordAts != null ? nfcRecordAts.getPctString() : "N/a";
	}
	
	public String getNfcRankAts() {
		return nfcRecordAts != null ? nfcRecordAts.getRankString() : "N/a";
	}
}