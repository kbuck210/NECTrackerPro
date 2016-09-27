package com.nectp.beans.named;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.NoResultException;

import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.GameService;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.beans.remote.daos.WeekService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Named(value="teamChartBean")
@RequestScoped
public class TeamChartBean implements Serializable {
	private static final long serialVersionUID = -3539268203660128108L;

	private LineChartModel seasonChartModel;
	private LineChartModel firstHalfModel;
	private LineChartModel secondHalfModel;
	private LineChartModel playoffModel;
	private LineChartModel superbowlModel;
	
	private String teamAbbr;
	
	private String chartTitle;
	
	private TeamForSeason displayedTeam;
	
	private Season currentSeason;
	
	@EJB
	private TeamForSeasonService tfsService;
	
	@EJB
	private RecordService recordService;
	
	@EJB
	private WeekService weekService;
	
	@EJB
	private GameService gameService;
	
	private Logger log;
	
	//	TODO: delete this & replace with parameter
	@EJB
	private SeasonService seasonService;
	
	public TeamChartBean() {
		log = Logger.getLogger(TeamChartBean.class.getName());
	}
	
	@PostConstruct
	public void init() {
		teamAbbr = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("teamAbbr");
		currentSeason = seasonService.selectCurrentSeason();	//	TODO: delete
		try {
			displayedTeam = tfsService.selectTfsByAbbr(teamAbbr, currentSeason);
		} catch (NoResultException e) {
			//	Catch
		}
		if (displayedTeam != null) {
			currentSeason = displayedTeam.getSeason();
			this.chartTitle = "NEC " + currentSeason.getSeasonNumber() + " - " + displayedTeam.getTeam().getTeamCity();
			seasonChartModel = createAnimatedModel(NEC.SEASON);
		}
	}
	
	public LineChartModel getSeasonChartModel() {
		seasonChartModel = createAnimatedModel(NEC.SEASON);
		return seasonChartModel;
	}
	
	public LineChartModel getFirstHalfModel() {
		firstHalfModel = createAnimatedModel(NEC.FIRST_HALF);
		return firstHalfModel;
	}
	
	public LineChartModel getSecondHalfModel() {
		secondHalfModel = createAnimatedModel(NEC.SECOND_HALF);
		return secondHalfModel;
	}
	
	public LineChartModel getPlayoffModel() {
		playoffModel = createAnimatedModel(NEC.PLAYOFFS);
		return playoffModel;
	}
	
	public LineChartModel getSuperbowlModel() {
		superbowlModel = createAnimatedModel(NEC.SUPER_BOWL);
		return superbowlModel;
	}
	
	private LineChartModel createAnimatedModel(NEC displayType) {
		LineChartModel chartModel = new LineChartModel();
		chartModel.setTitle(chartTitle);
		chartModel.setAnimate(true);
		chartModel.setLegendPosition("ne");
		chartModel.setShowDatatip(true);
	
		Axis yAxis = chartModel.getAxis(AxisType.Y);
		yAxis.setMax(1.0);
		yAxis.setMin(0.0);
		yAxis.setLabel("Win Percentage:");
		
		Axis xAxis = chartModel.getAxis(AxisType.X);
		xAxis.setLabel("Week Number:");
		
		LineChartSeries straightUpSeries = new LineChartSeries();
		straightUpSeries.setLabel("No Spread");
		straightUpSeries.setFill(false);
		
		Week rangeStart;
		Week rangeEnd;
		
		switch(displayType) {
		case FIRST_HALF:
			rangeStart = weekService.selectWeekByNumberInSeason(1, currentSeason);
			//	If currently in the first half, set the end of the range to the current week, otherwise to the end of the first half
			if (currentSeason.getCurrentWeek().getSubseason().getSubseasonType().equals(NEC.FIRST_HALF)) {
				rangeEnd = weekService.selectWeekByNumberInSeason(currentSeason.getCurrentWeek().getWeekNumber(), currentSeason);
			}
			else {
				rangeEnd = weekService.selectWeekByNumberInSeason((currentSeason.getSecondHalfStartWeek() - 1), currentSeason);
			}
			break;
		case SECOND_HALF:
			rangeStart = weekService.selectWeekByNumberInSeason(currentSeason.getSecondHalfStartWeek(), currentSeason);
			//	If currently in the second half, set the end of the range to the current week, otherwise to the end of the second half
			if (currentSeason.getCurrentWeek().getSubseason().getSubseasonType().equals(NEC.SECOND_HALF)) {
				rangeEnd = weekService.selectWeekByNumberInSeason(currentSeason.getCurrentWeek().getWeekNumber(), currentSeason);
			}
			else {
				rangeEnd = weekService.selectWeekByNumberInSeason((currentSeason.getPlayoffStartWeek() - 1), currentSeason);
			}
			break;
		case PLAYOFFS:
			rangeStart = weekService.selectWeekByNumberInSeason(currentSeason.getPlayoffStartWeek(), currentSeason);
			//	If currently in the playoffs, set the end of the range to the current week, otherwise to the end of the playoffs
			if (currentSeason.getCurrentWeek().getSubseason().getSubseasonType().equals(NEC.PLAYOFFS)) {
				rangeEnd = weekService.selectWeekByNumberInSeason(currentSeason.getCurrentWeek().getWeekNumber(), currentSeason);
			}
			else {
				rangeEnd = weekService.selectWeekByNumberInSeason((currentSeason.getSuperbowlWeek() - 2), currentSeason);
			}
			break;
		case SUPER_BOWL:
			rangeStart = weekService.selectWeekByNumberInSeason(currentSeason.getSuperbowlWeek(), currentSeason);
			rangeEnd = weekService.selectWeekByNumberInSeason(currentSeason.getSuperbowlWeek(), currentSeason);
			break;
		default:
			rangeStart = weekService.selectWeekByNumberInSeason(1, currentSeason);
			rangeEnd = currentSeason.getCurrentWeek();
			break;
		}
		
		List<Week> weeks = weekService.selectConcurrentWeeksInRangeInSeason(currentSeason, rangeStart.getWeekNumber(), rangeEnd.getWeekNumber());
		
		for (Week w : weeks) {
			RecordAggregator ragg = recordService.getRecordForConcurrentWeeksForAtfs(displayedTeam, rangeStart, w, displayType);
			//	Get the total score, then convert to a percentage of total games played
			int wins = ragg.getRawWins();
			int totalRecords = ragg.getRecords().size();
			double percentage;
			if (totalRecords != 0) {
				percentage = (double) wins / (double) totalRecords;
			}
			else {
				percentage = 0.5;
			}
			
			System.out.println("Raw Week " + w.getWeekNumber() + " - " + percentage);
			straightUpSeries.set(w.getWeekNumber(), percentage);
		}
		
		chartModel.addSeries(straightUpSeries);
		
		
		LineChartSeries spread1Series = new LineChartSeries();
		spread1Series.setLabel("Vs. Spread");
		spread1Series.setFill(false);
		
		int winAts = 0;
		int totalGames = 0;
		for (Week w : weeks) {
			Game game = null; 
			try {
				game = gameService.selectGameByTeamWeek(displayedTeam, w);
			} catch (NoResultException e) {
				log.warning("No game found for week " + w.getWeekNumber() + " for " + displayedTeam.getTeam().getTeamAbbr() + " - skipping spread score");
			}
			if (game != null) {
				boolean homeTeam = game.getHomeTeam().equals(displayedTeam);
				Boolean homeCovering = game.homeTeamCoveringSpread1();
				//	If the home team & covering, increment wins
				if (homeTeam && homeCovering != null && homeCovering) {
					winAts += 1;
				}
				//	If the away team & covering, increment wins
				else if (!homeTeam && homeCovering != null && !homeCovering) {
					winAts += 1;
				}
				totalGames += 1;
			}
			
			double percentage;
			if (totalGames != 0) {
				percentage = (double) winAts / (double) totalGames;
			}
			else {
				percentage = 0.5;
			}
			
			System.out.println("Spread Week " + w.getWeekNumber() + " - " + percentage);
			spread1Series.set(w.getWeekNumber(), percentage);
		}
		
		chartModel.addSeries(spread1Series);
		
		return chartModel;
	}
	
	public void setDisplayedTeam(TeamForSeason displayedTeam) {
		this.displayedTeam = displayedTeam;
	}
	
	public TeamForSeason getDisplayedTeam() {
		return displayedTeam;
	}
}
