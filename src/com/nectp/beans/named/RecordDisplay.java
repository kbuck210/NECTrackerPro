package com.nectp.beans.named;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.jpa.entities.AbstractTeamForSeason;

public class RecordDisplay {

	private String wltStr;
	
	private String pctStr;
	
	private String rankStr = "N/a";
	
	private RecordAggregator ragg;
	
	private int wins;
	private int losses;
	private int ties;
	
	private double winPct;
	
	private DecimalFormat format;
	
	public RecordDisplay() {
		wltStr = "(-/-/-)";
		pctStr = "%N/a";
		format = new DecimalFormat(".##");
	}
	
	public RecordDisplay(RecordAggregator ragg, boolean againstSpread) {
		this.ragg = ragg;
		setWLT(againstSpread);
	}
	
	public void setRecordAggregator(RecordAggregator ragg) {
		this.ragg = ragg;
	}
	
	public void setWLT(boolean againstSpread) {
		if (ragg != null && !againstSpread) {
			wins = ragg.getRawWins();
			losses = ragg.getRawLosses();
			ties = ragg.getRawTies();
		}
		else if (ragg != null) {
			wins = ragg.getWinsATS1();
			losses = ragg.getLossATS1();
			ties = ragg.getTiesATS1();
		}
		
		//	If there are no records available, set the strings to "N/A"
		if ((wins + losses + ties) == 0) {
			wltStr = "N/a";
			pctStr = "N/a";
		}
		//	If there are records available, calculate the stats
		else {
			winPct = (double) wins / (double) (wins + losses + ties);
			
			wltStr = "(" + wins + "-" + losses;
			if (ties > 0) {
				wltStr += "-" + ties;
			}
			wltStr += ")";
			
			pctStr = format.format(winPct);
		}
	}

	public void setRank(TreeMap<RecordAggregator, List<AbstractTeamForSeason>> rankMap) {
		int rank = 1;
		List<AbstractTeamForSeason> leaders = new ArrayList<AbstractTeamForSeason>();
		for (RecordAggregator ragg : rankMap.keySet()) {
			if (this.ragg.equals(ragg)) {
				leaders = rankMap.get(ragg);
				break;
			}
			rank += 1;
		}
		if (leaders.isEmpty()) {
			rankStr = "N/a";
		}
		else {
			if (leaders.size() > 1) {
				rankStr = "T(" + leaders.size() + ")-";
			}
			if (rank == 1) {
				rankStr += "1st";
			}
			else if (rank == 2) {
				rankStr += "2nd";
			}
			else if (rank == 3) {
				rankStr += "3rd";
			}
			else {
				rankStr += rank + "th";
			}
		}
	}
	
	public String getWltString() {
		return wltStr;
	}
	
	public String getPctString() {
		return pctStr;
	}
	
	public String getRankString() {
		return rankStr;
	}
	
}
