package com.nectp.beans.named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.primefaces.model.chart.BarChartSeries;
import org.primefaces.model.chart.CategoryAxis;
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
import com.nectp.jpa.entities.Pick.PickType;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
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
	private LineChartModel allTimeModel;
	
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
			displayedTeam = tfsService.selectTfsByAbbrSeason(teamAbbr, currentSeason);
		} catch (NoResultException e) {
			//	Catch
		}
		if (displayedTeam != null) {
			currentSeason = displayedTeam.getSeason();
			this.chartTitle = "NEC " + currentSeason.getSeasonNumber() + " - " + displayedTeam.getTeamCity();
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
	
	public LineChartModel getAllTimeModel() {
		allTimeModel = generateAllTimeModel();
		return allTimeModel;
	}
	
	private LineChartModel generateDefaultModel() {
		LineChartModel chartModel = new LineChartModel();
		chartModel.setTitle(chartTitle);
		chartModel.setAnimate(true);
		chartModel.setLegendPosition("ne");
		chartModel.setShowDatatip(true);
	
		Axis yAxis = chartModel.getAxis(AxisType.Y);
		yAxis.setMax(1.0);
		yAxis.setMin(0.0);
		yAxis.setLabel("Win Percentage:");
		
		return chartModel;
	}
	
	private LineChartSeries getStraightUpSeries() {
		LineChartSeries straightUpSeries = new LineChartSeries();
		straightUpSeries.setLabel("No Spread");
		straightUpSeries.setFill(false);
		
		return straightUpSeries;
	}
	
	private LineChartSeries getVsSpreadSeries() {
		LineChartSeries spread1Series = new LineChartSeries();
		spread1Series.setLabel("Vs. Spread");
		spread1Series.setFill(false);
		
		return spread1Series;
	}
	
	private double calculatePct(RecordAggregator ragg, PickType spreadType) {
		if (ragg == null) {
			log.warning("Record aggregator not defined! returning 0.0");
			return 0.0;
		}
		int wins;
		if (spreadType == PickType.STRAIGHT_UP) {
			wins = ragg.getRawWins();
		}
		else if (spreadType == PickType.SPREAD2) {
			wins = ragg.getWinsATS2();
		}
		else {
			wins = ragg.getWinsATS1();
		}
		int totalRecords = ragg.getRecords().size();
		double percentage;
		if (totalRecords != 0) {
			percentage = (double) wins / (double) totalRecords;
		}
		else {
			percentage = 0.5;
		}
		
		return percentage;
	}
	
	public LineChartModel generateAllTimeModel() {
		LineChartModel model = generateDefaultModel();
		model.setTitle("All Time Performance:");
		
		CategoryAxis seasonAxis = new CategoryAxis("Season Number:");
		CategoryAxis weeksAxis = new CategoryAxis("Weeks:");
		weeksAxis.setTickAngle(90);
		
		BarChartSeries barRawSeries = new BarChartSeries();
		barRawSeries.setLabel("Season - No Spread");
		barRawSeries.setXaxis(AxisType.X);
		barRawSeries.setYaxis(AxisType.Y);
		
		BarChartSeries barSpread1Series = new BarChartSeries();
		barSpread1Series.setLabel("Season - vs Spread");
		barRawSeries.setXaxis(AxisType.X);
		barRawSeries.setYaxis(AxisType.Y);
		
		LineChartSeries straightUpSeries = getStraightUpSeries();
		straightUpSeries.setLabel("Weeks - No Spread");
		straightUpSeries.setXaxis(AxisType.X2);
		straightUpSeries.setYaxis(AxisType.Y);
		
		LineChartSeries spread1Series = getVsSpreadSeries();
		spread1Series.setLabel("Weeks - Vs Spread");
		spread1Series.setXaxis(AxisType.X2);
		spread1Series.setYaxis(AxisType.Y);
		
		//	Get all seasons, and reverse order (1 to n from n to 1)
		List<Season> allSeasons = seasonService.findAll();
		Collections.reverse(allSeasons);
		
		Season minSeason = allSeasons.get(0);
		model.getAxis(AxisType.X).setMin(minSeason.getSeasonNumber());
		
		for (Season season : allSeasons) {
			Integer rangeStart = 1;
			Integer rangeEnd;
			
			//	If the current season, handle only weeks that have already passed
			if (season.getCurrentSeason()) {
				rangeEnd = season.getCurrentWeek().getWeekNumber();
			}
			//	If a past season, handle all weeks in range
			else {
				
				rangeEnd = 0;
				for (Subseason ss : season.getSubseasons()) {
					rangeEnd += ss.getWeeks().size();
				}
			}
			
			List<Week> weeks = weekService.selectConcurrentWeeksInRangeInSeason(season, rangeStart, rangeEnd);
			RecordAggregator ragg = null;
			for (Week w : weeks) {
				ragg = recordService.getRecordForConcurrentWeeksForAtfs(displayedTeam, rangeStart, w.getWeekNumber(), NEC.SEASON, true);
				//	Get the total score, then convert to a percentage of total games played
				double rawPct = calculatePct(ragg, PickType.STRAIGHT_UP);
				double ats1Pct = calculatePct(ragg, PickType.SPREAD1);
				String weekVal = season.getSeasonNumber() + "." + w.getWeekNumber();
				straightUpSeries.set(weekVal, rawPct);
				spread1Series.set(weekVal, ats1Pct);
			}
			
			String seasonval = season.getSeasonNumber().toString();
			//	Uses the final overall win pct for the bars
			double rawTotal = calculatePct(ragg, PickType.STRAIGHT_UP);
			barRawSeries.set(seasonval, rawTotal);
			
			double atsTotal = calculatePct(ragg, PickType.SPREAD1);
			barSpread1Series.set(seasonval, atsTotal);	
		}
		
		model.getAxes().put(AxisType.X, seasonAxis);
		model.getAxes().put(AxisType.X2, weeksAxis);
		
		model.addSeries(barRawSeries);
		model.addSeries(barSpread1Series);
		model.addSeries(straightUpSeries);
		model.addSeries(spread1Series);
		
		return model;
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
		
		Integer rangeStart;
		Integer rangeEnd;
		
		Week currentWeek = currentSeason.getCurrentWeek();
		int currentWeekNum = currentWeek.getWeekNumber();
		NEC subseasonType = currentWeek.getSubseason().getSubseasonType();
		
		switch(displayType) {
		case SECOND_HALF:
			rangeStart = currentSeason.getSecondHalfStartWeek();
			//	If currently in the second half, set the end of the range to the current week, otherwise to the end of the second half
			if (subseasonType.equals(NEC.SECOND_HALF)) {
				rangeEnd = currentWeekNum;
			}
			else {
				rangeEnd = currentSeason.getPlayoffStartWeek() - 1;
			}
			break;
		case PLAYOFFS:
			rangeStart = currentSeason.getPlayoffStartWeek();
			//	If currently in the playoffs, set the end of the range to the current week, otherwise to the end of the playoffs
			if (subseasonType.equals(NEC.PLAYOFFS)) {
				rangeEnd = currentWeekNum;
			}
			else {
				rangeEnd = currentSeason.getSuperbowlWeek() - 2;
			}
			break;
		case SUPER_BOWL:
			rangeStart = currentSeason.getSuperbowlWeek();
			rangeEnd = currentSeason.getSuperbowlWeek();
			break;
		default:
			rangeStart = 1;
			if (currentWeekNum <= currentSeason.getPlayoffStartWeek()) {
				rangeEnd = currentWeekNum;
			}
			else {
				rangeEnd = currentSeason.getPlayoffStartWeek() - 1;
			}
		}
		
		List<Week> weeks = weekService.selectConcurrentWeeksInRangeInSeason(currentSeason, rangeStart, rangeEnd);
		
		boolean againstSpread = (displayType != NEC.TWO_AND_OUT && displayType != NEC.ONE_AND_OUT);
		for (Week w : weeks) {
			RecordAggregator ragg = recordService.getRecordForConcurrentWeeksForAtfs(displayedTeam, rangeStart, w.getWeekNumber(), displayType, againstSpread);
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
			
			straightUpSeries.set(w.getWeekNumber(), percentage);
		}
		
		chartModel.addSeries(straightUpSeries);
		
		
		LineChartSeries spread1Series = new LineChartSeries();
		spread1Series.setLabel("Vs. Spread");
		spread1Series.setFill(false);
		
		List<Game> gamesWithSpread2 = new ArrayList<Game>();
		
		int winAts = 0;
		int totalGames = 0;
		for (Week w : weeks) {
			Game game = null; 
			try {
				game = gameService.selectGameByTeamWeek(displayedTeam, w);
			} catch (NoResultException e) {
				log.warning("No game found for week " + w.getWeekNumber() + " for " + displayedTeam.getTeamAbbr() + " - skipping spread score");
			}
			if (game != null) {
				//	Add the game to the list of games with spread2, to create a series for them if they exist for this team
				if (game.getSpread2() != null && 
						(game.getHomeTeam().equals(displayedTeam) ||
						 game.getAwayTeam().equals(displayedTeam))) {
					gamesWithSpread2.add(game);
				}
				if (displayedTeam.equals(game.getWinnerATS1())) {
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
			
			spread1Series.set(w.getWeekNumber(), percentage);
		}
		
		chartModel.addSeries(spread1Series);
		
		//	Check whether there were any spread 2 games, if so, create a series for them as well
		winAts = 0;
		if (!gamesWithSpread2.isEmpty()) {
			Collections.sort(gamesWithSpread2, new Comparator<Game>() {
				@Override
				public int compare(Game g1, Game g2) {
					return g1.getWeek().getWeekNumber().compareTo(g2.getWeek().getWeekNumber());
				}
			});
			
			LineChartSeries spread2Series = new LineChartSeries();
			spread2Series.setLabel("Vs. Early Spreads");
			spread2Series.setFill(false);
			for (Game g : gamesWithSpread2) {
				if (displayedTeam.equals(g.getWinnerATS2())) {
					winAts += 1;
				}
				
				totalGames = gamesWithSpread2.size();
			
				double percentage;
				if (totalGames != 0) {
					percentage = (double) winAts / (double) totalGames;
				}
				else {
					percentage = 0.5;
				}
				
				spread2Series.set(g.getWeek().getWeekNumber(), percentage);
			}
			
			chartModel.addSeries(spread2Series);
		}
		
		return chartModel;
	}
	
	public void setDisplayedTeam(TeamForSeason displayedTeam) {
		this.displayedTeam = displayedTeam;
	}
	
	public TeamForSeason getDisplayedTeam() {
		return displayedTeam;
	}
}

