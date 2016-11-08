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
import com.nectp.beans.remote.daos.RecordService;
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
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private RecordService recordService;
	
	@PostConstruct
	public void init() {
		currentSeason = seasonService.selectCurrentSeason();
		if (currentSeason == null) {
			//	TODO: redirect somehow
		}
		displayType = currentSeason.getCurrentWeek().getSubseason().getSubseasonType();
		updateLeaders(displayType);
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
		//	Re-initialize the leader list
		leaders = new ArrayList<Leader>();
		boolean againstSpread = displayType != NEC.TWO_AND_OUT;
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> playerRanks = recordService.getPlayerRankedScoresForType(displayType, currentSeason, againstSpread);
		
		//	Get the players from the rank map in order of ranking (players with tied records are in insertion order)
		for (RecordAggregator ragg : playerRanks.keySet()) {
			List<AbstractTeamForSeason> ranks = playerRanks.get(ragg);
			for (AbstractTeamForSeason atfs : ranks) {
				if (atfs instanceof PlayerForSeason) {
					PlayerForSeason player = (PlayerForSeason)atfs;
					//	Can't use ragg from map because while the scores are equal, the individual records may not...
					RecordAggregator playerRagg = recordService.getAggregateRecordForAtfsForType(player, displayType, againstSpread);
					Leader leader = new Leader(player, playerRagg, playerRanks);
					leaders.add(leader);
				}
			}
		}
	}
	
	public List<Leader> getLeaders() {
		return leaders;
	}
}
