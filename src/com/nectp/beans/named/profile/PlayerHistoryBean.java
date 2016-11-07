package com.nectp.beans.named.profile;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.PrizeForSeasonService;
import com.nectp.beans.remote.daos.StatisticService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Pick.PickType;

@Named(value="playerHistoryBean")
@ViewScoped
public class PlayerHistoryBean implements Serializable {
	private static final long serialVersionUID = -7052822560713856993L;

	private List<PlayerForSeason> instances;
	
	@EJB
	private StatisticService<PlayerForSeason> pfsStatService;
	
	@EJB
	private PrizeForSeasonService pzfsService;
	
	protected void setProfileEntity(PlayerForSeason profileEntity) {
		Player player = profileEntity.getPlayer();
		instances = player.getPlayerInstances();
		//	Sort the instances by descending order of season number
		Collections.sort(instances, new Comparator<PlayerForSeason>(){
			@Override
			public int compare(PlayerForSeason pfs1, PlayerForSeason pfs2) {
				return (pfs2.getSeason().getSeasonNumber().compareTo(pfs1.getSeason().getSeasonNumber()));
			}
		});
	}
	
	public List<PlayerForSeason> getInstances() {
		return instances;
	}
	
	public String getSeasonNumber(PlayerForSeason instance) {
		return instance != null ? instance.getSeason().getSeasonNumber().toString() : "N/a";
	}
	
	public String getRecord(PlayerForSeason instance) {
		RecordAggregator ragg = pfsStatService.getAggregateRecordForAtfsForType(instance, NEC.SEASON, true);
		if (ragg != null) {
			return ragg.toString(PickType.SPREAD1);
		}
		else return "(N/a)";
	}
	
	public String getFirstHalfIcon(PlayerForSeason instance) {
		if (instance != null) {
			Season season = instance.getSeason();
			PrizeForSeason firstHalf = null;
			try {
				firstHalf = pzfsService.selectPrizeForSeason(NEC.FIRST_HALF, season);
			} catch (NoExistingEntityException e) {
				//	Eat exception
			}
			
			if (firstHalf != null && instance.equals(firstHalf.getWinner())) {
				return "img/icons/firstHalfWinner-small.png";
			}
		}
		
		return "img/icons/firstHalfNonWin-small-light.png";
	}
	
	public String getSecondHalfIcon(PlayerForSeason instance) {
		if (instance != null) {
			Season season = instance.getSeason();
			PrizeForSeason secondHalf = null;
			try {
				secondHalf = pzfsService.selectPrizeForSeason(NEC.SECOND_HALF, season);
			} catch (NoExistingEntityException e) {
				//	Eat exception
			}
			
			if (secondHalf != null && instance.equals(secondHalf.getWinner())) {
				return "img/icons/secondHalfWinner-small.png";
			}
		}
		
		return "img/icons/secondHalfNonWin-small-light.png";
	}
	
	public String getPlayoffIcon(PlayerForSeason instance) {
		if (instance != null) {
			Season season = instance.getSeason();
			PrizeForSeason playoffs = null;
			try {
				playoffs = pzfsService.selectPrizeForSeason(NEC.PLAYOFFS, season);
			} catch (NoExistingEntityException e) {
				//	Eat exception
			}
			
			if (playoffs != null && instance.equals(playoffs.getWinner())) {
				return "img/icons/playoffWinner2-small.png";
			}
		}
		
		return "img/icons/playoffNonWin-small-light.png";
	}
	
	public String getMnfTntIcon(PlayerForSeason instance) {
		if (instance != null) {
			Season season = instance.getSeason();
			PrizeForSeason mnfTnt = null;
			try {
				mnfTnt = pzfsService.selectPrizeForSeason(NEC.MNF_TNT, season);
			} catch (NoExistingEntityException e) {
				//	Eat exception
			}
			
			if (mnfTnt != null && instance.equals(mnfTnt.getWinner())) {
				return "img/icons/mnfTntWinner2-small.png";
			}
		}
		
		return "img/icons/mnfTntNonWin-small-light.png";
	}
	
	public String getTnoIcon(PlayerForSeason instance) {
		if (instance != null) {
			Season season = instance.getSeason();
			PrizeForSeason tno = null;
			try {
				tno = pzfsService.selectPrizeForSeason(NEC.TWO_AND_OUT, season);
			} catch (NoExistingEntityException e) {
				//	Eat exception
			}
			
			if (tno != null && instance.equals(tno.getWinner())) {
				return "img/icons/twoAndOutWinner2-small.png";
			}
		}
		
		return "img/icons/twoAndOutNonWin-small-light.png";
	}
	
	public String getMoneybackIcon(PlayerForSeason instance) {
		if (instance != null) {
			Season season = instance.getSeason();
			PrizeForSeason moneyback = null;
			try {
				moneyback = pzfsService.selectPrizeForSeason(NEC.MONEY_BACK, season);
			} catch (NoExistingEntityException e) {
				//	Eat exception
			}
			
			if (moneyback != null && instance.equals(moneyback.getWinner())) {
				return "img/icons/moneyBackWinner-small.png";
			}
		}
		
		return "img/icons/moneyBackNonWin-small-light.png";
	}
	
	public String getSuperbowlIcon(PlayerForSeason instance) {
		if (instance != null) {
			Season season = instance.getSeason();
			PrizeForSeason superbowl = null;
			try {
				superbowl = pzfsService.selectPrizeForSeason(NEC.SUPER_BOWL, season);
			} catch (NoExistingEntityException e) {
				//	Eat exception
			}
			
			if (superbowl != null && instance.equals(superbowl.getWinner())) {
				return "img/icons/superbowlWinner-small.png";
			}
		}
		
		return "img/icons/superbowlNonWin-small-light.png";
	}
}
