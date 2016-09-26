package com.nectp.beans.named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.ApplicationState;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.PrizeForSeasonService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Week;

@Named(value="leaderListBean")
@RequestScoped
public class LeaderListBean implements Serializable {
	private static final long serialVersionUID = 7295312035752090279L;
	
	private List<LeaderBean> leaderList;
	
	private List<PrizeBean> prizeCategories;
	
	private Season currentSeason;
	
	private String headerTitle;
	
	@EJB
	private PrizeForSeasonService pzfsService;
	
	@EJB
	private PlayerForSeasonService pfsService;
	
	@Inject
	private ApplicationState appState;
	
	private NEC displayedCategory;
	
	public LeaderListBean() {
	}
	
	@PostConstruct
	public void init() {
		currentSeason = appState.getCurrentSeason();
		Week currentWeek = currentSeason.getCurrentWeek();
		displayedCategory = currentWeek.getSubseason().getSubseasonType();
		headerTitle = "Leaderboard:";
		updateList();
	}
	
	public void updateList() {
		prizeCategories = new ArrayList<PrizeBean>();
		leaderList = new ArrayList<LeaderBean>();
		List<PrizeForSeason> pzfs = currentSeason.getPrizes();
		for (PrizeForSeason pz : pzfs) {
			PrizeBean pb = new PrizeBean();
			pb.setPrize(pz);
			pb.setWinner(pz.getWinner());
			prizeCategories.add(pb);
		}
		
		List<PlayerForSeason> players = currentSeason.getPlayers();
		for (PlayerForSeason pfs : players) {
			LeaderBean lb = new LeaderBean();
			lb.setCurrentSeason(currentSeason);
			lb.setDisplayedCategory(displayedCategory);
			lb.setPlayer(pfs);
		}
	}
	
	public String getHeaderTitle() { 
		return headerTitle;
	}
	
	public List<PrizeBean> getPrizeCategories() {
		return prizeCategories;
	}
	
	public List<LeaderBean> getLeaderList() {
		return leaderList;
	}
	 
	public void setDisplayedCategory(NEC displayedCategory) {
		this.displayedCategory = displayedCategory;
	}
	
	public void changeCategory(String category) {
		NEC newCategory = NEC.getNECForString(category);
		setDisplayedCategory(newCategory);
	}
}
