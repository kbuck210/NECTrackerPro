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
	
	private int winScore = 0;
	private int lossScore = 0;
	private int tieScore = 0;
	
	private int totalScore = 0;
	
	private List<Record> records;
	
	private Logger log;
	
	private AbstractTeamForSeason atfs;
	
	public RecordAggregator(AbstractTeamForSeason atfs) {
		log = Logger.getLogger(RecordAggregator.class.getName());
		records = new ArrayList<Record>();
		this.atfs = atfs;
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
			
			//	Ensure the season-scope point multiplier is not zero if the win modifier is not zero
			if (season.getWinValue() == 0 && record.getWinModifier() != 0) {
				winScore += (record.getWins() * record.getWinModifier());
			}
			else {
				int winTotal = (season.getWinValue() * record.getWins());
				if (record.getWinModifier() != 0) winTotal *= record.getWinModifier();
				winScore += winTotal;
			}
			
			if (season.getLossValue() == 0 && record.getLossModifier() != 0) {
				lossScore += (record.getLosses() * record.getLossModifier());
			}
			else {
				int lossTotal = (season.getLossValue() * record.getLosses());
				if (record.getLossModifier() != 0) lossTotal *= record.getLossModifier();
				lossScore += lossTotal;
			}
			
			if (season.getTieValue() == 0 && record.getTieModifier() != 0) {
				tieScore += (record.getTies() * record.getTieModifier());
			}
			else {
				int tieTotal = (season.getTieValue() * record.getTies());
				if (record.getTieModifier() != 0) tieTotal *= record.getTieModifier();
				tieScore += tieTotal;
			}
			
			totalScore = winScore + tieScore - lossScore;
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
			
			//	Ensure the season-scope point multiplier is not zero if the win modifier is not zero
			if (season.getWinValue() == 0 && record.getWinModifier() != 0) {
				winScore -= (record.getWins() * record.getWinModifier());
			}
			else {
				int winTotal = (season.getWinValue() * record.getWins());
				if (record.getWinModifier() != 0) winTotal *= record.getWinModifier();
				winScore -= winTotal;
			}
			
			if (season.getLossValue() == 0 && record.getLossModifier() != 0) {
				lossScore -= (record.getLosses() * record.getLossModifier());
			}
			else {
				int lossTotal = (season.getLossValue() * record.getLosses());
				if (record.getLossModifier() != 0) lossTotal *= record.getLossModifier();
				lossScore -= lossTotal;
			}
			
			if (season.getTieValue() == 0 && record.getTieModifier() != 0) {
				tieScore -= (record.getTies() * record.getTieModifier());
			}
			else {
				int tieTotal = (season.getTieValue() * record.getTies());
				if (record.getTieModifier() != 0) tieTotal *= record.getTieModifier();
				tieScore -= tieTotal;
			}
			
			totalScore = winScore + tieScore - lossScore;
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
	 * @return the winScore
	 */
	public int getWinScore() {
		return winScore;
	}

	/**
	 * @param winScore the winScore to set
	 */
	public void setWinScore(int winScore) {
		this.winScore = winScore;
	}

	/**
	 * @return the lossScore
	 */
	public int getLossScore() {
		return lossScore;
	}

	/**
	 * @param lossScore the lossScore to set
	 */
	public void setLossScore(int lossScore) {
		this.lossScore = lossScore;
	}

	/**
	 * @return the tieScore
	 */
	public int getTieScore() {
		return tieScore;
	}

	/**
	 * @param tieScore the tieScore to set
	 */
	public void setTieScore(int tieScore) {
		this.tieScore = tieScore;
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

	/**
	 * @param records the records to set
	 */
	public void setRecords(List<Record> records) {
		this.records = records;
	}

	@Override
	public int compareTo(RecordAggregator ra2) {
		if (ra2 == null) return -1;
		else return getTotalScore().compareTo(ra2.getTotalScore());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RecordAggregator) {
			RecordAggregator ra2 = (RecordAggregator)obj;
			return this.getTotalScore().equals(ra2.getTotalScore());
		}
		else return false;
	}
	
}
