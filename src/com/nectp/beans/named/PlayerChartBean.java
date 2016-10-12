package com.nectp.beans.named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.beans.remote.daos.SubseasonService;
import com.nectp.beans.remote.daos.WeekService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.Week;

@Named(value="playerChartBean")
@ViewScoped
public class PlayerChartBean implements Serializable {
	private static final long serialVersionUID = -2035195378076290794L;
	
	private LineChartModel firstHalfChartModel;
	private LineChartModel secondHalfChartModel;
	private LineChartModel playoffChartModel;
	private LineChartModel seasonChartModel;
	
	private PlayerForSeason instance;
	
	private Logger log;
	
	private Season season;
	
	private Week currentWeek;
	
	private String chartTitle;
	
	private String summary;
	
	private NEC currentSubseason;
	
	@EJB
	private PlayerForSeasonService pfsService;
	
	@EJB
	private RecordService recordService;
	
	@EJB
	private WeekService weekService;
	
	@EJB
	private SubseasonService subseasonService;
	
	public PlayerChartBean() {
		log = Logger.getLogger(PlayerChartBean.class.getName());
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
			season = instance.getSeason();
			currentWeek = season.getCurrentWeek();
			currentSubseason = currentWeek.getSubseason().getSubseasonType();
			if (currentSubseason == NEC.SUPER_BOWL) {
				currentSubseason = NEC.PLAYOFFS;
			}
			chartTitle = "NEC " + season.getSeasonNumber() + " - " + instance.getNickname();
		}
	}
	
	public LineChartModel getFirstHalfChartModel() {
		firstHalfChartModel = createAnimatedModel(NEC.FIRST_HALF);
		return firstHalfChartModel;
	}
	
	public LineChartModel getSecondHalfChartModel() {
		secondHalfChartModel = createAnimatedModel(NEC.SECOND_HALF);
		return secondHalfChartModel;
	}
	
	public LineChartModel getPlayoffChartModel() {
		playoffChartModel = createAnimatedModel(NEC.PLAYOFFS);
		return playoffChartModel;
	}
	
	public LineChartModel getSeasonChartModel() {
		seasonChartModel = createAnimatedModel(NEC.SEASON);
		return seasonChartModel;
	}
	
	public String getActiveIndex() {
		switch(currentSubseason) {
		case FIRST_HALF:
			return "firstHalfTab";
		case SECOND_HALF:
			return "secondHalfTab";
		case PLAYOFFS:
			return "playoffsTab";
		case SUPER_BOWL:
			return "playoffsTab";
		default:
			return "seasonTab";
		}
	}
	
	private LineChartSeries createPlayerSeries(PlayerForSeason player, NEC displayType, List<Week> weeks, boolean fill) {
		LineChartSeries playerSeries = new LineChartSeries();
		playerSeries.setLabel(player.getNickname());
		playerSeries.setFill(fill);
		
		for (Week w : weeks) {
			//	Only plot as many weeks as have processed in the current subseason
			if (w.getWeekNumber() > currentWeek.getWeekNumber()) {
				break;
			}
			RecordAggregator ragg = recordService.getAggregateRecordForAtfsForType(player, displayType, true);
			playerSeries.set(w.getWeekNumber(), ragg.getTotalScore());
		}
		
		return playerSeries;
	}
	
	private LineChartModel createAnimatedModel(NEC displayType) {
		LineChartModel chartModel = new LineChartModel();
		chartModel.setTitle(chartTitle);
		chartModel.setAnimate(true);
		chartModel.setLegendPosition("ne");
		chartModel.setShowDatatip(true);
	
		Axis yAxis = chartModel.getAxis(AxisType.Y);
		yAxis.setLabel(displayType.toString() + " Scores:");
		
		Axis xAxis = chartModel.getAxis(AxisType.X);
		xAxis.setLabel("Week Number:");
		
		//	Get the weeks for the specified display type
		List<Week> weeks = new ArrayList<Week>();
		if (displayType == NEC.SEASON) {
			Subseason firstHalf = subseasonService.selectSubseasonInSeason(NEC.FIRST_HALF, season);
			Subseason secondHalf = subseasonService.selectSubseasonInSeason(NEC.SECOND_HALF, season);
			Subseason playoffs = subseasonService.selectSubseasonInSeason(NEC.PLAYOFFS, season);
			weeks = weekService.selectWeeksInSubseason(firstHalf);
			weeks.addAll(weekService.selectWeeksInSubseason(secondHalf));
			weeks.addAll(weekService.selectWeeksInSubseason(playoffs));
		}
		else if (displayType != NEC.SUPER_BOWL) {
			Subseason subseason = subseasonService.selectSubseasonInSeason(NEC.PLAYOFFS, season);
			weeks = weekService.selectWeeksInSubseason(subseason);
		}
		
		//	Create player series as primary series color, then a series for each additional player
		LineChartSeries playerSeries = createPlayerSeries(instance, displayType, weeks, true);
		chartModel.addSeries(playerSeries);
		
		List<PlayerForSeason> seasonPlayers = season.getPlayers();
		seasonPlayers.remove(instance);
		for (PlayerForSeason pfs : seasonPlayers) {
			LineChartSeries otherPlayerSeries = createPlayerSeries(pfs, displayType, weeks, false);
			chartModel.addSeries(otherPlayerSeries);
		}
		
		return chartModel;
	}
	
	public void onTabChange(TabChangeEvent event) {
	    int currentIndex = ((TabView) event.getSource()).getIndex();
	    setSummary(currentIndex);
	}
	
	public void setSummary(int tabIndex) {
		switch(tabIndex) {
		case 0:
			summary = createSummary(NEC.FIRST_HALF);
			break;
		case 1:
			summary = createSummary(NEC.SECOND_HALF);
			break;
		case 2:
			summary = createSummary(NEC.PLAYOFFS);
			break;
		case 3:
			summary = createSummary(NEC.SEASON);
			break;
		default:
			summary = createSummary(NEC.SEASON);
		}
	}
	
	public String getSummary() {
		return summary;
	}
	
	private String createSummary(NEC summaryType) {
		RecordAggregator ragg = recordService.getAggregateRecordForAtfsForType(instance, summaryType, true);
		TreeMap<RecordAggregator, List<AbstractTeamForSeason>> rankMap = recordService.getPlayerRankedScoresForType(summaryType, season, true);
		RecordAggregator leaderRagg = rankMap.firstKey();
		StringBuilder summary = new StringBuilder();
		if (summaryType == currentSubseason) {
			summary.append("Through the " + summaryType.toString() + " so far, ");
			summary.append(instance.getNickname() + " has scored a total of " + ragg.scoreString(true) + " points, ");
			if (ragg.equals(leaderRagg)) {
				List<AbstractTeamForSeason> leaders = new ArrayList<AbstractTeamForSeason>(rankMap.get(leaderRagg));
				leaders.remove(instance);
				if (leaders.size() > 1) {
					summary.append("creating a " + leaders.size() + "-way tie for the lead with ");
					for (int i = 0; i < leaders.size(); ++i) {
						AbstractTeamForSeason nextTeam = leaders.get(i);
						summary.append(nextTeam.getNickname());
						if (leaders.size() > 2 && i < (leaders.size() - 2)) {
							summary.append(", ");
						}
						else if (i == leaders.size() - 2) {
							summary.append(" and ");
						}
					}
					summary.append(".");
				}
				else {
					if (leaders.size() == 1) {
						summary.append("locking into a dead-heat with ");
						AbstractTeamForSeason tiedPlayer = leaders.get(0);
						summary.append(tiedPlayer.getNickname());
						summary.append(". Together, their lead is held by a margin of ");
					}
					else {
						summary.append("which is good enough for first place! ");
						summary.append(instance.getNickname() + "'s lead is held by a margin of ");
					}
					RecordAggregator[] ranks = rankMap.keySet().toArray(new RecordAggregator[rankMap.keySet().size()]);
					if (ranks.length >= 2) {
						RecordAggregator secondPlace = ranks[1];
						List<AbstractTeamForSeason> runnersUp = rankMap.get(secondPlace);
						int margin = leaderRagg.getTotalScore() - secondPlace.getTotalScore();
						summary.append(margin + " points, over ");
						if (runnersUp.size() > 1) {
							for (int i = 0; i < runnersUp.size(); ++i) {
								AbstractTeamForSeason nextTeam = runnersUp.get(i);
								summary.append(nextTeam.getNickname());
								if (runnersUp.size() > 2 && i < (runnersUp.size() - 2)) {
									summary.append(", ");
								}
								else if (i == leaders.size() - 2) {
									summary.append(" and ");
								}
							}
						}
						else {
							summary.append(runnersUp.get(0).getNickname());
						}
						
						summary.append(".");
					}
				}
			}
			else {
				if (rankMap.containsKey(ragg)) {
					//	Nifty trick to get position of element in map, headmap creates map including elements up to ragg 
					//	(inclusive because of 'true' param), the size of the returned map gives the index of ragg in the original map
					int rank = rankMap.headMap(ragg, true).size();
					summary.append("currently in ");
					if (rank == 2) summary.append("second ");
					else if (rank == 3) summary.append("third ");
					else summary.append(rank + "th ");
					
					int margin = leaderRagg.getTotalScore() - ragg.getTotalScore();
					summary.append("place, trailing " + margin + " points behind the leader");
					List<AbstractTeamForSeason> leaders = new ArrayList<AbstractTeamForSeason>(rankMap.get(leaderRagg));
					if (leaders.size() == 1) {
						summary.append(" " + leaders.get(0).getNickname());
					}
					else {
						summary.append("s: ");
						for (int i = 0; i < leaders.size(); ++i) {
							AbstractTeamForSeason nextTeam = leaders.get(i);
							summary.append(nextTeam.getNickname());
							if (leaders.size() > 2 && i < (leaders.size() - 2)) {
								summary.append(", ");
							}
							else if (i == leaders.size() - 2) {
								summary.append(" and ");
							}
						}
					}
					summary.append(".");
				}
			}
		}
		
		return summary.toString();
	}
}
