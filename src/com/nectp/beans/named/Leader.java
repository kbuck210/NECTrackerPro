package com.nectp.beans.named;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Prize;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Pick.PickType;

public class Leader {

	private static Map<Integer, String> rankImgMap = new HashMap<Integer, String>();
	static {
		rankImgMap.put(1, "img/first.png");
		rankImgMap.put(2, "img/second.png");
		rankImgMap.put(3, "img/third.png");
		rankImgMap.put(4, "img/fourth.png");
		rankImgMap.put(5, "img/fifth.png");
		rankImgMap.put(6, "img/sixth.png");
		rankImgMap.put(7, "img/seventh.png");
		rankImgMap.put(8, "img/eighth.png");
		rankImgMap.put(9, "img/ninth.png");
		rankImgMap.put(10, "img/tenth.png");
		rankImgMap.put(11, "img/eleventh.png");
		rankImgMap.put(12, "img/twelveth.png");
		rankImgMap.put(13, "img/thirteenth.png");
		rankImgMap.put(14, "img/fourteenth.png");
		rankImgMap.put(15, "img/fifteenth.png");
	}
	
	private PlayerForSeason pfs;
	
	private RecordAggregator ragg;
	
	private TreeMap<RecordAggregator, List<AbstractTeamForSeason>> rankMap;
	
	public Leader(PlayerForSeason pfs, RecordAggregator ragg, TreeMap<RecordAggregator, List<AbstractTeamForSeason>> rankMap) {
		this.pfs = pfs;
		this.ragg = ragg;
		this.rankMap = rankMap;
	}
	
	public String getPfsId() {
		return pfs != null ? pfs.getAbstractTeamForSeasonId().toString() : "";
	}
	
	public String getNickname() {
		return pfs != null ? pfs.getNickname() : "N/a";
	}
	
	public String getScore() {
		if (ragg != null) {
			List<Record> records = ragg.getRecords();
			if (!records.isEmpty()) {
				NEC recordType = records.get(0).getRecordType();
				boolean againstSpread = recordType != NEC.TWO_AND_OUT && recordType != NEC.ONE_AND_OUT;
				return ragg.scoreString(againstSpread);
			}
		}
		
		return "N/a";
	}
	
	public String getRecord() {
		if (ragg != null) {
			List<Record> records = ragg.getRecords();
			if (!records.isEmpty()) {
				NEC recordType = records.get(0).getRecordType();
				if (recordType == NEC.TWO_AND_OUT || recordType == NEC.ONE_AND_OUT) {
					return ragg.toString(PickType.STRAIGHT_UP);
				}
				else return ragg.toString(PickType.SPREAD1);
			}
		}
		return "N/a";
	}
	
	public String getPosition() {
		int rank = 1;
		for (RecordAggregator ragg : rankMap.keySet()) {
			if (this.ragg.equals(ragg)) {
				break;
			}
			rank += 1;
		}
		if (rank <= 15) {
			return rankImgMap.get(rank);
		}
		else return "";
	}
	
	public String getAvatar() {
		return pfs != null ? pfs.getPlayer().getAvatarUrl() : "img/avatars/generic-avatar.png";
	}
	
	public String getFirstHalfIcon() {
		if (pfs != null) {
			Season season = pfs.getSeason();
			List<PrizeForSeason> prizes = season.getPrizes();
			for (PrizeForSeason prize : prizes) {
				Prize p = prize.getPrize();
				if (p.getPrizeType() == NEC.FIRST_HALF && pfs.equals(prize.getWinner())) {
					return "img/icons/firstHalfWinner-small.png";
				}
			}
		}
		return "img/icons/firstHalfNonWin-small-light.png";
	}
	
	public String getSecondHalfIcon() {
		if (pfs != null) {
			Season season = pfs.getSeason();
			List<PrizeForSeason> prizes = season.getPrizes();
			for (PrizeForSeason prize : prizes) {
				Prize p = prize.getPrize();
				if (p.getPrizeType() == NEC.SECOND_HALF && pfs.equals(prize.getWinner())) {
					return "img/icons/secondHalfWinner-small.png";
				}
			}
		}
		return "img/icons/secondHalfNonWin-small-light.png";
	}
	
	public String getPlayoffIcon() {
		if (pfs != null) {
			Season season = pfs.getSeason();
			List<PrizeForSeason> prizes = season.getPrizes();
			for (PrizeForSeason prize : prizes) {
				Prize p = prize.getPrize();
				if (p.getPrizeType() == NEC.PLAYOFFS && pfs.equals(prize.getWinner())) {
					return "img/icons/playoffWinner2-small.png";
				}
			}
		}
		return "img/icons/playoffNonWin-small-light.png";
	}
	
	public String getMnfTntIcon() {
		if (pfs != null) {
			Season season = pfs.getSeason();
			List<PrizeForSeason> prizes = season.getPrizes();
			for (PrizeForSeason prize : prizes) {
				Prize p = prize.getPrize();
				if (p.getPrizeType() == NEC.MNF_TNT && pfs.equals(prize.getWinner())) {
					return "img/icons/mnfTntWinner2-small.png";
				}
			}
		}
		return "img/icons/mnfTntNonWin-small-light.png";
	}
	
	public String getTnoIcon() {
		if (pfs != null) {
			Season season = pfs.getSeason();
			List<PrizeForSeason> prizes = season.getPrizes();
			for (PrizeForSeason prize : prizes) {
				Prize p = prize.getPrize();
				if (p.getPrizeType() == NEC.TWO_AND_OUT && pfs.equals(prize.getWinner())) {
					return "img/icons/twoAndOutWinner2-small.png";
				}
			}
		}
		return "img/icons/twoAndOutNonWin-small-light.png";
	}
	
	public String getMoneybackIcon() {
		if (pfs != null) {
			Season season = pfs.getSeason();
			List<PrizeForSeason> prizes = season.getPrizes();
			for (PrizeForSeason prize : prizes) {
				Prize p = prize.getPrize();
				if (p.getPrizeType() == NEC.MONEY_BACK && pfs.equals(prize.getWinner())) {
					return "img/icons/moneyBackWinner-small.png";
				}
			}
		}
		return "img/icons/moneyBackNonWin-small-light.png";
	}
	
	public String getSuperbowlIcon() {
		if (pfs != null) {
			Season season = pfs.getSeason();
			List<PrizeForSeason> prizes = season.getPrizes();
			for (PrizeForSeason prize : prizes) {
				Prize p = prize.getPrize();
				if (p.getPrizeType() == NEC.SUPER_BOWL && pfs.equals(prize.getWinner())) {
					return "img/icons/superbowlWinner-small.png";
				}
			}
		}
		return "img/icons/superbowlNonWin-small-light.png";
	}
}
