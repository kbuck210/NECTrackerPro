package com.nectp.beans.named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.ApplicationState;
import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;

@Named(value="leaderBean")
@ViewScoped
public class LeaderBean implements Serializable {
	private static final long serialVersionUID = -7238092389506995239L;
	
	//	Required Attributes:
	//	TeamURl
	//	PositionImg
	//	Avatar
	//	Nickname
	//	Record
	//	PrizeCount
	//	PrizeCategories
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
	
	private String teamUrl;
	private String avatar;
	private String nickname;
	private String record;
	private String positionImg;
	private ArrayList<PrizeBean> prizeCategories;
	private NEC displayedCategory;
	
	private PlayerForSeason leader;
	
	private Season currentSeason;
	
	private TreeMap<RecordAggregator, List<PlayerForSeason>> rankMap;
	
	@Inject
	private ApplicationState appState;
	
	@EJB
	private RecordService recordService;
	
	public LeaderBean() {
	}
	
	@PostConstruct
	public void init() {
		currentSeason = appState.getCurrentSeason();
		displayedCategory = currentSeason.getCurrentWeek().getSubseason().getSubseasonType();
		rankMap = recordService.getPlayerForSeasonRankedScoresForType(displayedCategory, currentSeason);
	}
	
	public void setPlayer(PlayerForSeason player) {
		this.leader = player;
		this.teamUrl = "/faces/players/" + player.getNickname();
		this.avatar = player.getPlayer().getAvatarUrl();
		this.nickname = player.getNickname();
		
		int posCount = 1;
		for (Map.Entry<RecordAggregator, List<PlayerForSeason>> leaderEntry : rankMap.entrySet()) {
			if (leaderEntry.getValue().contains(player)) {
				RecordAggregator ragg = leaderEntry.getKey();
				record = "(" + ragg.getRawWins() + "-" + ragg.getRawLosses();
				if (ragg.getRawTies() > 0) {
					record += "-" + ragg.getRawTies();
				}
				record += ")";
				setPositionImg(posCount);
				break;
			}
			posCount += 1;
		}
	}
	
	public PlayerForSeason getLeader() {
		return leader;
	}
	
	private void setPositionImg(int position) {
		if (position > 0 && position <= 15) {
			this.positionImg = rankImgMap.get(position);
		}
	}
	
	public String getPositionImg() {
		return positionImg;
	}
	
	public String getTeamUrl() {
		return teamUrl;
	}
	
	public String getAvatar() {
		return avatar;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public String getRecord() {
		return record;
	}
	
	public ArrayList<PrizeBean> getPrizeCategories() {
		return prizeCategories;
	}
	
	public int getPrizeCount() {
		return prizeCategories.size();
	}
	
	public void setDisplayedCategory(NEC category) {
		this.displayedCategory = category;
	}
	
	public void setCurrentSeason(Season season) {
		this.currentSeason = season;
	}
}
