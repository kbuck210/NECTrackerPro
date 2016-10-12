package com.nectp.beans.named;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Week;

@Named(value="playerProfileBean")
@ViewScoped
public class PlayerProfileBean implements Serializable {
	private static final long serialVersionUID = 1810019242255191136L;

	private Player player;
	
	private PlayerForSeason instance;
	
	private RecordAggregator currentRecord;
	
	private RecordAggregator leaderRecord;
	
	private int numLeaders;
	
	private NEC subType;
	
	@EJB
	private PlayerForSeasonService pfsService;
	
	@EJB
	private RecordService recordService;
	
	public PlayerProfileBean() {
	}
	
	@PostConstruct
	public void init() {
		String pfsId = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("pfsId");
		Integer instanceId = null;
		try {
			instanceId = Integer.parseInt(pfsId);
		} catch (NumberFormatException e) {
			//	TODO: load page error on growl
		}
		
		instance = pfsService.selectById(instanceId);
		if (instance != null) {
			player = instance.getPlayer();
			Season season = instance.getSeason();
			Week currentWeek = season.getCurrentWeek();
			subType = currentWeek.getSubseason().getSubseasonType();
			currentRecord = recordService.getAggregateRecordForAtfsForType(instance, subType, true);
			TreeMap<RecordAggregator, List<AbstractTeamForSeason>> rankMap = recordService.getPlayerRankedScoresForType(subType, season, true);
			leaderRecord = rankMap.firstKey();
			numLeaders = rankMap.get(leaderRecord).size();
		}
		else {
			// TODO: load page error on growl
		}
	}
	
	public String getSeasonNumber() {
		return instance != null ? instance.getSeason().getSeasonNumber().toString() : "N/a";
	}
	
	public String getPlayerName() {
		return player != null ? player.getName() : "N/a";
	}
	
	public String getSinceYear() {
		return player != null ? player.getSinceYear().toString() : "N/a";
	}
	
	public String getAvatarUrl() {
		return player != null ? player.getAvatarUrl() : "img/avatars/default.png";
	}
	
	public String getNickname() {
		return instance != null ? instance.getNickname() : "N/a";
	}
	
	public String getScore() {
		return currentRecord.scoreString(true);
	}
	
	public String getLeaderScore() {
		return leaderRecord.scoreString(true);
	}
	
	public String getTrending() {
		if (currentRecord != null) {
			//	Get the records & sort by most recent week first
			List<Record> records = currentRecord.getRecords();
			Collections.sort(records, new Comparator<Record>() {
				@Override
				public int compare(Record r1, Record r2) {
					return -1 * (r1.getWeek().getWeekNumber().compareTo(r2.getWeek().getWeekNumber()));
				}
			});
			//	Loop over the records from most recent week to end, breaking if trend ends
			Integer trend = null;
			for (Record r : records) {
				//	Get the wins & losses for the record
				int wins = r.getWins();
				int losses = r.getLosses();
				//	If trend not initialized, initialize to either wins/losses, or zero
				if (trend == null) {
					if (wins > 0) trend = wins;
					else if (losses > 0) trend = -1 * losses;
					else trend = 0;
				}
				else {
					//	If wins is positive with positive trend, add win to trend
					if (wins > 0 && trend >= 0) {
						trend += wins;
					}
					//	If wins is positive with negative trend, found end of trend
					else if (wins > 0) {
						break;
					}
					//	If losses positive with negative trend, add negative loss to trend
					else if (losses > 0 && trend <= 0) {
						trend -= losses;
					}
					//	If losses positive with positive trend, found end of trend
					else if (losses > 0) {
						break;
					}
				}
			}
			
			if (trend != null) {
				String trendString = "";
				if (trend > 0) {
					trendString += "+";
				}
				trendString += trend.toString();
				return trendString;
			}
			else return "0";
		}
		else return "0";
	}
}
