package com.nectp.beans.named;

import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.PrizeForSeasonService;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;

@Named(value="seasonSummaryBean")
@RequestScoped
public class SeasonSummaryBean implements Serializable {
	private static final long serialVersionUID = 5349437583699550006L;
	
	private String topScore;
	private String lowScore;
	private String bestWeek;
	private String worstWeek;
	private String avgSuccess;
	private String avgPickCount;
	private PrizeForSeason firstHalfWinner;
	private PrizeForSeason secondHalfWinner;
	private PrizeForSeason playoffsWinner;
	private PrizeForSeason superBowlWinner;
	private PrizeForSeason mnfTntWinner;
	private PrizeForSeason tnoWinner;
	private PrizeForSeason moneyBackWinner;
	
	private Season season;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private PlayerForSeasonService pfsService;
	
	@EJB
	private RecordService recordService;
	
	@EJB
	private PrizeForSeasonService pzfsService;

	private Logger log;
	
	public SeasonSummaryBean() {
		log = Logger.getLogger(SeasonSummaryBean.class.getName());
	}
	
	@PostConstruct
	public void init() {
		String seasonNum = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("nec");
		Integer necNum = null;
		if (seasonNum != null) {
			try {
				necNum = Integer.parseInt(seasonNum);
			} catch (NumberFormatException e) {
				log.warning("Invalid season number format! Displaying current season.");
				//	TODO: insert faces message
			}
		}
		
		if (necNum != null) {
			season = seasonService.selectById(necNum);
		}
		else {
			season = seasonService.selectCurrentSeason();
		}
		
		//	TODO: Double check that season is not null, if so, forward response to not found page
		if (season == null) {
			
		}
		else {
			//	Populate the stats for scores
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> playerRanks = recordService.getPlayerRankedScoresForType(NEC.SEASON, season, true);
			RecordAggregator leader = playerRanks.firstKey();
			if (leader != null) {
				List<AbstractTeamForSeason> leaders = playerRanks.get(leader);
				StringBuilder leaderBuilder = new StringBuilder();
				if (leader.getTotalScore() > 0) leaderBuilder.append("+");
				leaderBuilder.append(leader.getTotalScore());
				leaderBuilder.append(" (");
				for (int i = 0; i < leaders.size(); ++i) {
					leaderBuilder.append(leaders.get(i).getNickname());
					if (i < (leaders.size() - 1)) {
						leaderBuilder.append(", ");
					}
				}
				leaderBuilder.append(")");
				topScore = leaderBuilder.toString();
			}	
			
			RecordAggregator loser = playerRanks.lastKey();
			if (loser != null) {
				List<AbstractTeamForSeason> losers = playerRanks.get(loser);
				StringBuilder loserBuilder = new StringBuilder();
				if (loser.getTotalScore() > 0) loserBuilder.append("+");
				loserBuilder.append(leader.getTotalScore());
				loserBuilder.append(" (");
				for (int i = 0; i < losers.size(); ++i) {
					loserBuilder.append(losers.get(i).getNickname());
					if (i < (losers.size() - 1)) {
						loserBuilder.append(", ");
					}
				}
				loserBuilder.append(")");
				lowScore = loserBuilder.toString();
			}
			
			Integer best = null;
			Integer worst = null;
			double totWinPct = 0;
			double totPickAvg = 0;
			List<PlayerForSeason> players = season.getPlayers();
			for (PlayerForSeason player : players) {
				RecordAggregator ragg = recordService.getAggregateRecordForAtfsForType(player, NEC.SEASON, true);
				List<Record> records = ragg.getRecords();
				int totalPlayerPicks = 0;
				for (int j = 0; j < records.size(); j++) {
					Record r = records.get(j);
					if (j == 0) {
						best = r.getTotalScore();
						worst = r.getTotalScore();
						
						bestWeek = "";
						if (best > 0) bestWeek += "+";
						bestWeek += best.toString() + " (" + player.getNickname() + ")";
						
						worstWeek = "";
						if (worst > 0) worstWeek += "+";
						worstWeek += worst.toString() + " (" + player.getNickname() + ")";
					}
					else if (r.getTotalScore() > best) {
						best = r.getTotalScore();
						bestWeek = "";
						if (best > 0) bestWeek += "+";
						bestWeek += best.toString() + " (" + player.getNickname() + ")";
					}
					else if (r.getTotalScore() < worst) {
						worst = r.getTotalScore();
						worstWeek = "";
						if (worst > 0) worstWeek += "+";
						worstWeek += worst.toString() + " (" + player.getNickname() + ")";
					}
					totalPlayerPicks += r.getPicksInRecord().size();
				}
				
				int totalWins = ragg.getTotalWinCount();
				int totalCount = ragg.getTotalWinCount() + ragg.getTotalLossCount() + ragg.getTotalTieCount();
				totWinPct += (double) totalWins / (double) totalCount;
			
				totPickAvg += (double) totalPlayerPicks / (double) records.size();
			}
			
			avgSuccess = new Double(totWinPct / (double) players.size()).toString();
			avgPickCount = new Double(totPickAvg / (double) players.size()).toString();
			
			firstHalfWinner = null;
			secondHalfWinner = null;
			playoffsWinner = null;
			superBowlWinner = null;
			try {
				firstHalfWinner = pzfsService.selectPrizeForSeason(NEC.FIRST_HALF, season);
				secondHalfWinner = pzfsService.selectPrizeForSeason(NEC.SECOND_HALF, season);
				playoffsWinner = pzfsService.selectPrizeForSeason(NEC.PLAYOFFS, season);
				superBowlWinner = pzfsService.selectPrizeForSeason(NEC.SUPER_BOWL, season);
			} catch (NoExistingEntityException e) {
				log.warning("Failed to get prizes for subseasons.  Winners may not be set.");
			}
			
			mnfTntWinner = null;
			tnoWinner = null;
			moneyBackWinner = null;
			try {
				mnfTntWinner = pzfsService.selectPrizeForSeason(NEC.MNF_TNT, season);
			} catch (NoExistingEntityException e) {
				
			}
			try {
				tnoWinner = pzfsService.selectPrizeForSeason(NEC.TWO_AND_OUT, season);
			} catch (NoExistingEntityException e) {
				
			}
			try {
				moneyBackWinner = pzfsService.selectPrizeForSeason(NEC.MONEY_BACK, season);
			} catch (NoExistingEntityException e) {
				
			}
		}
	}
	
	public String getPlayerCount() {
		return new Integer(season.getPlayers().size()).toString();
	}
	
	public String getCommish() {
		PlayerForSeason commish = null;
		try {
			commish = pfsService.selectCommishBySeason(season);
		} catch (NoExistingEntityException e) {
			log.warning("No commish found for season " + season.getSeasonNumber());
		}
		return commish != null ? commish.getNickname() : "";
	}
	
	public String getTopScore() {
		return topScore;
	}
	
	public String getLowScore() {
		return lowScore;
	}
	
	public String getBestWeek() {
		return bestWeek;
	}
	
	public String getWorstWeek() {
		return worstWeek;
	}
	
	public String getAvgSuccess() {
		return avgSuccess;
	}
	
	public String getAvgPickCount() {
		return avgPickCount;
	}
	
	public String getFirstHalfWinner() {
		if (firstHalfWinner != null) {
			PlayerForSeason winner = firstHalfWinner.getWinner();
			return winner != null ? winner.getNickname() : null;
		}
		else return null;
	}
	
	public String getSecondHalfWinner() {
		if (secondHalfWinner != null) {
			PlayerForSeason winner = secondHalfWinner.getWinner();
			return winner != null ? winner.getNickname() : null;
		}
		else return null;
	}
	
	public String getPlayoffsWinner() {
		if (playoffsWinner != null) {
			PlayerForSeason winner = playoffsWinner.getWinner();
			return winner != null ? winner.getNickname() : null;
		}
		else return null;
	}
	
	public String getSuperBowlWinner() {
		if (superBowlWinner != null) {
			PlayerForSeason winner = superBowlWinner.getWinner();
			return winner != null ? winner.getNickname() : null;
		}
		else return null;
	}
	
	public String getMnfTntWinner() {
		if (mnfTntWinner != null) {
			PlayerForSeason winner = mnfTntWinner.getWinner();
			return winner != null ? winner.getNickname() : null;
		}
		else return null;
	}
	
	public String getTnoWinner() {
		if (tnoWinner != null) {
			PlayerForSeason winner = tnoWinner.getWinner();
			return winner != null ? winner.getNickname() : null;
		}
		else return null;
	}
	
	public String getMoneyBackWinner() {
		if (moneyBackWinner != null) {
			PlayerForSeason winner = moneyBackWinner.getWinner();
			return winner != null ? winner.getNickname() : null;
		}
		else return null;
	}
}
