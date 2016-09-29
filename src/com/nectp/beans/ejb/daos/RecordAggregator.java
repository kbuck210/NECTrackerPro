package com.nectp.beans.ejb.daos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;

public class RecordAggregator implements Comparable<RecordAggregator>, Serializable {
	private static final long serialVersionUID = -8336359327553779266L;
	
	private int rawWins = 0;
	private int rawLosses = 0;
	private int rawTies = 0;
	
	private int winsATS1 = 0;
	private int lossATS1 = 0;
	private int tiesATS1 = 0;
	
	private int winsATS2 = 0;
	private int lossATS2 = 0;
	private int tiesATS2 = 0;
	
	private int winScore1 = 0;
	private int lossScore1 = 0;
	private int tieScore1 = 0;
	
	private int winScore2 = 0;
	private int lossScore2 = 0;
	private int tieScore2 = 0;
	
	private int rawTotal = 0;
	private int totalScore = 0;
	
	private List<Record> records;
	
	private boolean againstSpread;
	
	private Logger log;
	
	private AbstractTeamForSeason atfs;
	
	public RecordAggregator(AbstractTeamForSeason atfs, boolean againstSpread) {
		log = Logger.getLogger(RecordAggregator.class.getName());
		records = new ArrayList<Record>();
		this.atfs = atfs;
		this.againstSpread = againstSpread;
	}
	
	public void addRecord(Record record) {
		if (record == null) {
			log.warning("Attempting to add Null record, scores not processed.");
		}
		else if (!record.getTeam().equals(atfs)) {
			log.warning("Attempting to add record for incorrect Team to aggregation, scores not processed.");
		}
		else {
			Season season = record.getTeam().getSeason();
			rawWins += record.getWins();
			rawLosses += record.getLosses();
			rawTies += record.getTies();
			
			winsATS1 += record.getWinsATS1();
			lossATS1 += record.getLossesATS1();
			tiesATS1 += record.getTiesATS1();
			
			winsATS2 += record.getWinsATS2();
			lossATS2 += record.getLossesATS2();
			tiesATS2 += record.getTiesATS2();
			
			//	If no season-scope win value defined, but win modifier applied, use win modifier for scoring
			if (season.getWinValue() == 0 && record.getWinModifier() != 0) {
				winScore1 += (winsATS1 * record.getWinModifier());
				winScore2 += (winsATS2 * record.getWinModifier());
			}
			//	If season-scope win value is not zero, use to calculate points
			else {
				int winTotal1 = (season.getWinValue() * winsATS1);
				int winTotal2 = (season.getWinValue() * winsATS2);
				if (record.getWinModifier() != 0) {
					winTotal1 *= record.getWinModifier();
					winTotal2 *= record.getWinModifier();
				}
				winScore1 += winTotal1;
				winScore2 += winTotal2;
			}
			//	If no season-scope loss value defined, but loss modifier applied, use loss modifier for scoring
			if (season.getLossValue() == 0 && record.getLossModifier() != 0) {
				lossScore1 += (lossATS1 * record.getLossModifier());
				lossScore2 += (lossATS2 * record.getLossModifier());
			}
			//	If season-scope loss value is not zero, use to calculate points
			else {
				int lossTotal1 = (season.getLossValue() * lossATS1);
				int lossTotal2 = (season.getLossValue() * lossATS2);
				if (record.getLossModifier() != 0) {
					lossTotal1 *= record.getLossModifier();
					lossTotal2 *= record.getLossModifier();
				}
				lossScore1 += lossTotal1;
				lossScore2 += lossTotal2;
			}
			//	If no season-scope tie value defined, but tie modifier applied, use tie modifier for scoring
			if (season.getTieValue() == 0 && record.getTieModifier() != 0) {
				tieScore1 += (tiesATS1 * record.getTieModifier());
				tieScore2 += (tiesATS2 * record.getTieModifier());
			}
			//	If season-scope tie value is not zero, use to calculate points
			else {
				int tieTotal1 = (season.getTieValue() * tiesATS1);
				int tieTotal2 = (season.getTieValue() * tiesATS2);
				if (record.getTieModifier() != 0) {
					tieTotal1 *= record.getTieModifier();
					tieTotal2 *= record.getTieModifier();
				}
				tieScore1 += tieTotal1;
				tieScore2 += tieTotal2;
			}
			
			rawTotal = rawWins - rawLosses;
			totalScore = (winScore1 + winScore2) + (tieScore1 + tieScore2) - (lossScore1 + lossScore2);
			records.add(record);
		}
	}
	
	public void removeRecord(Record record) {
		if (record == null) {
			log.warning("Attempting to remove Null record, scores not processed.");
		}
		else {
			Season season = record.getTeam().getSeason();
			rawWins -= record.getWins();
			rawLosses -= record.getLosses();
			rawTies -= record.getTies();
			
			winsATS1 -= record.getWinsATS1();
			lossATS1 -= record.getLossesATS1();
			tiesATS1 -= record.getTiesATS1();
			
			winsATS2 -= record.getWinsATS2();
			lossATS2 -= record.getLossesATS2();
			tiesATS2 -= record.getTiesATS2();
			
			//	If no season-scope win value defined, but win modifier applied, use win modifier for scoring
			if (season.getWinValue() == 0 && record.getWinModifier() != 0) {
				winScore1 -= (winsATS1 * record.getWinModifier());
				winScore2 -= (winsATS2 * record.getWinModifier());
			}
			//	If season-scope win value is not zero, use to calculate points
			else {
				int winTotal1 = (season.getWinValue() * winsATS1);
				int winTotal2 = (season.getWinValue() * winsATS2);
				if (record.getWinModifier() != 0) {
					winTotal1 *= record.getWinModifier();
					winTotal2 *= record.getWinModifier();
				}
				winScore1 -= winTotal1;
				winScore2 -= winTotal2;
			}
			//	If no season-scope loss value defined, but loss modifier applied, use loss modifier for scoring
			if (season.getLossValue() == 0 && record.getLossModifier() != 0) {
				lossScore1 -= (lossATS1 * record.getLossModifier());
				lossScore2 -= (lossATS2 * record.getLossModifier());
			}
			//	If season-scope loss value is not zero, use to calculate points
			else {
				int lossTotal1 = (season.getLossValue() * lossATS1);
				int lossTotal2 = (season.getLossValue() * lossATS2);
				if (record.getLossModifier() != 0) {
					lossTotal1 *= record.getLossModifier();
					lossTotal2 *= record.getLossModifier();
				}
				lossScore1 -= lossTotal1;
				lossScore2 -= lossTotal2;
			}
			//	If no season-scope tie value defined, but tie modifier applied, use tie modifier for scoring
			if (season.getTieValue() == 0 && record.getTieModifier() != 0) {
				tieScore1 -= (tiesATS1 * record.getTieModifier());
				tieScore2 -= (tiesATS2 * record.getTieModifier());
			}
			//	If season-scope tie value is not zero, use to calculate points
			else {
				int tieTotal1 = (season.getTieValue() * tiesATS1);
				int tieTotal2 = (season.getTieValue() * tiesATS2);
				if (record.getTieModifier() != 0) {
					tieTotal1 *= record.getTieModifier();
					tieTotal2 *= record.getTieModifier();
				}
				tieScore1 -= tieTotal1;
				tieScore2 -= tieTotal2;
			}
			
			rawTotal = rawWins - rawLosses;
			totalScore = (winScore1 + winScore2) + (tieScore1 + tieScore2) - (lossScore1 + lossScore2);
			records.remove(record);
		}	
	}

	/**
	 * @return the rawWins
	 */
	public int getRawWins() {
		return rawWins;
	}

	/**
	 * @param rawWins the rawWins to set
	 */
	public void setRawWins(int rawWins) {
		this.rawWins = rawWins;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getWinsATS1() {
		return winsATS1;
	}
	
	/**
	 * 
	 * @param winsATS
	 */
	public void setWinsATS1(int winsATS) {
		this.winsATS1 = winsATS;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getWinsATS2() {
		return winsATS2;
	}
	
	/**
	 * 
	 * @param winsATS
	 */
	public void setWinsATS2(int winsATS) {
		this.winsATS2 = winsATS;
	}

	/**
	 * @return the rawLosses
	 */
	public int getRawLosses() {
		return rawLosses;
	}

	/**
	 * @param rawLosses the rawLosses to set
	 */
	public void setRawLosses(int rawLosses) {
		this.rawLosses = rawLosses;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getLossATS1() {
		return lossATS1;
	}
	
	/**
	 * 
	 * @param lossATS
	 */
	public void setLossATS1(int lossATS) {
		this.lossATS1 = lossATS;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getLossATS2() {
		return lossATS2;
	}
	
	/**
	 * 
	 * @param lossATS
	 */
	public void setLossATS2(int lossATS) {
		this.lossATS2 = lossATS;
	}

	/**
	 * @return the rawTies
	 */
	public int getRawTies() {
		return rawTies;
	}

	/**
	 * @param rawTies the rawTies to set
	 */
	public void setRawTies(int rawTies) {
		this.rawTies = rawTies;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getTiesATS1() {
		return tiesATS1;
	}
	
	/**
	 * 
	 * @param tiesATS
	 */
	public void setTiesATS1(int tiesATS) {
		this.tiesATS1 = tiesATS;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getTiesATS2() {
		return tiesATS2;
	}
	
	/**
	 * 
	 * @param tiesATS
	 */
	public void setTiesATS2(int tiesATS) {
		this.tiesATS2 = tiesATS;
	}

	/**
	 * @return the winScore
	 */
	public int getWinScore1() {
		return winScore1;
	}

	/**
	 * @param winScore the winScore to set
	 */
	public void setWinScore1(int winScore) {
		this.winScore1 = winScore;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getWinScore2() {
		return winScore2;
	}
	
	/**
	 * 
	 * @param winScore
	 */
	public void setWinScore2(int winScore) {
		this.winScore2 = winScore;
	}

	/**
	 * @return the lossScore
	 */
	public int getLossScore1() {
		return lossScore1;
	}

	/**
	 * @param lossScore the lossScore to set
	 */
	public void setLossScore1(int lossScore) {
		this.lossScore1 = lossScore;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getLossScore2() {
		return lossScore2;
	}
	
	/**
	 * 
	 * @param lossScore
	 */
	public void setLossScore2(int lossScore) {
		this.lossScore2 = lossScore;
	}

	/**
	 * @return the tieScore
	 */
	public int getTieScore1() {
		return tieScore1;
	}

	/**
	 * @param tieScore the tieScore to set
	 */
	public void setTieScore1(int tieScore) {
		this.tieScore1 = tieScore;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getTieScore2() {
		return tieScore2;
	}
	
	/**
	 * 
	 * @param tieScore
	 */
	public void setTieScore2(int tieScore) {
		this.tieScore2 = tieScore;
	}
	
	public Integer getRawTotal() {
		return rawTotal;
	}
	
	/**
	 * 
	 * @param rawTotal
	 */
	public void setRawTotal(int rawTotal) {
		this.rawTotal = rawTotal;
	}

	/**
	 * @return the totalScore
	 */
	public Integer getTotalScore() {
		return totalScore;
	}

	/**
	 * @param totalScore the totalScore to set
	 */
	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}

	/**
	 * @return the records
	 */
	public List<Record> getRecords() {
		return records;
	}
	
	public boolean isSortingAgainstSpread() {
		return againstSpread;
	}
	
	public void setSortAgainstSpread(boolean againstSpread) {
		this.againstSpread = againstSpread;
	}

	/**
	 * @param records the records to set
	 */
	public void setRecords(List<Record> records) {
		this.records = records;
	}

	@Override
	public int compareTo(RecordAggregator ra2) {
		if (ra2 == null) return -1;
		else if (againstSpread) {
			return getTotalScore().compareTo(ra2.getTotalScore());
		}
		else {
			return getRawTotal().compareTo(ra2.getRawTotal());
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RecordAggregator) {
			RecordAggregator ra2 = (RecordAggregator)obj;
			//	Determine how to comare equality based on whether aggregating against spread or not
			if (againstSpread) {
				return this.getTotalScore().equals(ra2.getTotalScore());
			}
			else {
				return this.getRawTotal().equals(ra2.getRawTotal());
			}
		}
		else return false;
	}
}

