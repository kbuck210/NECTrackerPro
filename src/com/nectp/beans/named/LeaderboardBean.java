package com.nectp.beans.named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.PlayerStatisticService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Week;

@Named(value="leaderboardBean")
@RequestScoped
public class LeaderboardBean implements Serializable {
	private static final long serialVersionUID = 5541117606621564194L;
	
	private List<Leader> leaders;
	
	private Season currentSeason;
	
	private NEC displayType;
	
	private String displayTitle;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private PlayerStatisticService playerStatService;
	
	@PostConstruct
	public void init() {
		currentSeason = seasonService.selectCurrentSeason();
		if (currentSeason == null) {
			//	TODO: redirect somehow
		}
		displayType = currentSeason.getCurrentWeek().getSubseason().getSubseasonType();
//		updateLeaders(displayType);
		setDisplayCurrentWeek();
	}
	
	public String getCurrentWeek() {
		if (currentSeason != null) {
			Week currentWeek = currentSeason.getCurrentWeek();
			if (currentWeek != null) {
				return "Week: " + currentWeek.getWeekNumber().toString();
			}
		}
		
		return "N/a";
	}
	
	/** Gets the list of Leaderboard line items based on player records for the specified display type
	 * 
	 * @param displayType the NEC enum object representing the type of record leaderboards to display
	 */
	public void updateLeaders(NEC displayType) {
		displayTitle = displayType.toString();
		System.out.println("Set display title: " + displayTitle);
		//	Re-initialize the leader list
		leaders = new ArrayList<Leader>();
		boolean againstSpread = displayType != NEC.TWO_AND_OUT;
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> playerRanks = playerStatService.getPlayerRankedScoresForType(displayType, currentSeason, againstSpread);
		
		//	Get the players from the rank map in order of ranking (players with tied records are in insertion order)
		for (RecordAggregator ragg : playerRanks.keySet()) {
			List<AbstractTeamForSeason> ranks = playerRanks.get(ragg);
			for (AbstractTeamForSeason atfs : ranks) {
				if (atfs instanceof PlayerForSeason) {
					PlayerForSeason player = (PlayerForSeason)atfs;
					//	Can't use ragg from map because while the scores are equal, the individual records may not...
					RecordAggregator playerRagg = playerStatService.getAggregateRecordForAtfsForType(player, displayType, againstSpread);
					Leader leader = new Leader(player, playerRagg, playerRanks);
					leaders.add(leader);
				}
			}
		}
	}
	
	public List<Leader> getLeaders() {
		return leaders;
	}
	
	public void setDisplayCurrentWeek() {
		displayTitle = "Current Week";
		//	Re-initialize the leader list
		leaders = new ArrayList<Leader>();
		Week currentWeek = currentSeason.getCurrentWeek();
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> playerRanks = playerStatService.getWeekRanks(currentWeek, true);

		//	Get the players from the rank map in order of ranking (players with tied records are in insertion order)
		for (RecordAggregator ragg : playerRanks.keySet()) {
			List<AbstractTeamForSeason> ranks = playerRanks.get(ragg);
			for (AbstractTeamForSeason atfs : ranks) {
				if (atfs instanceof PlayerForSeason) {
					PlayerForSeason player = (PlayerForSeason)atfs;
					//	Can't use ragg from map because while the scores are equal, the individual records may not...
					RecordAggregator playerRagg = playerStatService.getRecordForWeek(player, currentWeek, true);
					Leader leader = new Leader(player, playerRagg, playerRanks);
					leaders.add(leader);
				}
			}
		}
	}
	
	public void setDisplayFirstHalf() {
		System.out.println("changing to first half.");
		displayType = NEC.FIRST_HALF;
		updateLeaders(displayType);
	}
	
	public void setDisplaySecondHalf() {
		displayType = NEC.SECOND_HALF;
		updateLeaders(displayType);
	}
	
	public void setDisplayPlayoffs() {
		displayType = NEC.PLAYOFFS;
		updateLeaders(displayType);
	}
	
	public void setDisplayMnfTnt() {
		displayType = NEC.MNF_TNT;
		updateLeaders(displayType);
	}
	
	public void setDisplayTwoAndOut() {
		displayType = NEC.TWO_AND_OUT;
		updateLeaders(displayType);
	}
	
	public void setDisplayMoneyback() {
		displayType = NEC.MONEY_BACK;
		updateLeaders(displayType);
	}
	
	public String getSelectedDisplay() {
		return displayTitle;
	}
}
