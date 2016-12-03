package com.nectp.beans.named.seasons;

import java.io.Serializable;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Pick.PickType;

public class PrizeBean implements Serializable {
	private static final long serialVersionUID = 3752130390554870223L;
	
	private NEC prizeType;
	private boolean againstSpread;
	private PrizeForSeason prize;
	private PlayerForSeason winner;
	private RecordAggregator ragg;
	
	public PrizeBean(PrizeForSeason prize, RecordService recordService) {
		this.prize = prize;
		if (prize != null) {
			this.winner = prize.getWinner();
			this.prizeType = prize.getPrize().getPrizeType();
			this.againstSpread = prizeType != NEC.TWO_AND_OUT && prizeType != NEC.ONE_AND_OUT;
			if (winner != null) {
				if (prizeType == NEC.MNF_TNT) {
					RecordAggregator mnfRagg = recordService.getAggregateRecordForAtfsForType(winner, NEC.MNF, true);
					RecordAggregator tntRagg = recordService.getAggregateRecordForAtfsForType(winner, NEC.TNT, true);
					ragg = RecordAggregator.combine(mnfRagg, tntRagg);
				}
				else if (prizeType == NEC.MONEY_BACK) {
					ragg = recordService.getAggregateRecordForAtfsForType(winner, NEC.SEASON, true);
				}
				else {
					ragg = recordService.getAggregateRecordForAtfsForType(winner, prizeType, againstSpread);
				}	
			}
		}
	}
	
	public String getName() {
		return prize != null ? prize.getPrize().getPrizeType().toString() : "N/a";
	}
	
	public String getWinner() {
		return winner != null ? winner.getNickname() : "-";
	}
	
	public String getRecord() {
		if (againstSpread) {
			return ragg != null ? ragg.toString(PickType.SPREAD1) : "-";
		}
		else {
			return ragg != null ? ragg.toString(PickType.STRAIGHT_UP) : "-";
		}
	}
	
	public String getScore() {
		return ragg != null ? ragg.scoreString(againstSpread) : "N/a";
	}
	
	public String getPayout() {
		return prize != null ? ("$" + prize.getPrizeAmount()) : "-";
	}
}

