package com.nectp.beans.named.seasons;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LegendPlacement;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Week;

public class HistoryChartBean implements Serializable {
	private static final long serialVersionUID = -7062610606680807581L;
	
	private Season season;
	private RecordService recordService;
	
	private LineChartModel firstHalfModel;
	private LineChartModel secondHalfModel;
	private LineChartModel playoffModel;
	private LineChartModel seasonChartModel;
	
	private Logger log = Logger.getLogger(HistoryChartBean.class.getName());
	
	public void setSeason(Season season) {
		this.season = season;
		
		this.firstHalfModel = createAnimatedModel(NEC.FIRST_HALF);
		this.secondHalfModel = createAnimatedModel(NEC.SECOND_HALF);
		this.playoffModel = createAnimatedModel(NEC.PLAYOFFS);
		this.seasonChartModel = createAnimatedModel(NEC.SEASON);
	}
	
	public void setRecordService(RecordService recordService) {
		this.recordService = recordService;
	}
	
	public LineChartModel getFirstHalfModel() {
		if (firstHalfModel == null) {
			log.info("Null first half");
		}
		else {
			log.info("First half model: " + firstHalfModel.getSeries().size() + " series");
		}
		return firstHalfModel;
	}
	
	public LineChartModel getSecondHalfModel() {
		return secondHalfModel;
	}
	
	public LineChartModel getPlayoffModel() {
		return playoffModel;
	}
	
	public LineChartModel getSeasonChartModel() {
		return seasonChartModel;
	}
	
	/** Creates the animated LineChartModel based on the specified NEC enum display type for any AbstractTeamForSeason type
	 * 
	 * @param displayType the NEC enum corresponding to the scope of the model to create
	 * @return a LineChartModel initialized with the data for the specified scope
	 */
	private LineChartModel createAnimatedModel(NEC displayType) {
		Integer rangeStart = getRangeStart(displayType);
		Integer rangeEnd = getRangeEnd(displayType);
		String chartTitle = "NEC " + season.getSeasonNumber() + " - " + displayType.toString();
		log.info("Chart title: " + chartTitle);
		
		//	Call the abstracted set chart title method, such that an appropriate title is set based on the runtime type
		LineChartModel chartModel = createChartModel(chartTitle, "Points:", -1, -1, "Week Number:", rangeEnd, rangeStart);
		chartModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
		chartModel.setLegendCols(2);
		chartModel.setExtender("digitExt");
		chartModel.setResetAxesOnResize(false);
		log.info("created chart model for weeks: " + rangeStart.toString() + " to " + rangeEnd.toString());
		
		boolean againstSpread = (displayType != NEC.TWO_AND_OUT && displayType != NEC.ONE_AND_OUT);
		
		for (PlayerForSeason player : season.getPlayers()) {
			RecordAggregator ragg = recordService.getRecordForConcurrentWeeksForAtfs(player, rangeStart, rangeEnd, displayType, againstSpread);
			if (ragg != null) {
				//	Get the records from the aggregation, and sort by week number
				List<Record> records = ragg.getRecords();
				Collections.sort(records, new Comparator<Record>(){
					@Override
					public int compare(Record r1, Record r2) {
						return r1.getWeek().compareTo(r2.getWeek());
					}
				});

				//	Create a line series for the wins against the spread records
				String nickname = player.getNickname().replace("'", "\\'");
				log.info(player.getNickname() + " changed to: " + nickname);
				LineChartSeries atsSeries = createLineChartSeries(player, records, nickname, true);	
				chartModel.addSeries(atsSeries);
			}
			else {
				log.info("No RAGG found for " + player.getNickname() + " for " + displayType.toString());
			}
		}

		return chartModel;
	}

	public LineChartSeries createLineChartSeries(PlayerForSeason player, List<Record> records, String title, boolean againstSpread) {
		LineChartSeries series = new LineChartSeries();
		series.setLabel(title);
		series.setFill(false);
		
		//	Loop over the records adding data points for each record
		RecordAggregator tempRagg = new RecordAggregator(player, againstSpread);
		for (int i = 0; i < records.size(); ++i) {
			Record r = records.get(i);
			//	Only add to the chart where picks were made
			int picksForWeek = r.getPicksInRecord().size();
			if (picksForWeek == 0) {
				continue;
			}
			
			tempRagg.addRecord(r);
			
			int value;
			if (againstSpread) {
				value = tempRagg.getTotalScore();
			}
			else {
				value = tempRagg.getRawWins();
			}
			series.set(r.getWeek().getWeekNumber(), value);
		}
		
		return series;
	}
	
	protected LineChartModel createChartModel(String title, String yLabel, int yMax, int yMin, String xLabel, int xMax, int xMin) {
		LineChartModel chartModel = new LineChartModel();
		chartModel.setTitle(title);
		chartModel.setAnimate(true);
		chartModel.setLegendPosition("ne");
		chartModel.setShowDatatip(true);
	
		Axis yAxis = chartModel.getAxis(AxisType.Y);
		if (yLabel != null) {
			yAxis.setLabel(yLabel);
		}
		if (yMax > -1) {
			yAxis.setMax(yMax);
		}
		if (yMin > -1) {
			yAxis.setMin(yMin);
		}
		
		Axis xAxis = chartModel.getAxis(AxisType.X);
		if (xLabel != null) {
			xAxis.setLabel(xLabel);
		}
		if (xMax > -1) {
			xAxis.setMax(xMax);
		}
		if (xMin > -1) {
			xAxis.setMin(xMin);
		}
		
		return chartModel;
	}
	
	/** Based on the specified display type, get the week number for the beginning of the week range
	 * 
	 * @param displayType the NEC enum value denoting the range of weeks to query
	 * @return the Integer representing the week number of the first week in the range
	 */
	protected Integer getRangeStart(NEC displayType) {
		//	Create the range start (default value of 1)
		Integer rangeStart;
		if (displayType == NEC.SECOND_HALF) {
			rangeStart = season.getSecondHalfStartWeek();
		}
		else if (displayType == NEC.PLAYOFFS) {
			rangeStart = season.getPlayoffStartWeek();
		}
		else {
			rangeStart = 1;
		}
		
		return rangeStart;
	}
	
	/** Based on the specified display type, get the week number for the end of the week range
	 * 
	 * @param displayType the NEC enum value denoting the range of weeks to query
	 * @return the Integer representing the week number of the last week in the range
	 */
	protected Integer getRangeEnd(NEC displayType) {
		Week currentWeek = season.getCurrentWeek();
		NEC subseasonType = currentWeek.getSubseason().getSubseasonType();
		
		Integer rangeEnd;
		//	If the display type is within the current subseason, week range ends at current week
		if (subseasonType == displayType) {
			rangeEnd = currentWeek.getWeekNumber();
		}
		//	If the display type is the first half, but currently outside first half, get the end of the first half
		else if (displayType == NEC.FIRST_HALF) {
			rangeEnd = season.getSecondHalfStartWeek() - 1;
		}
		//	If the display type is the second half, get the end of the second half
		else if (displayType == NEC.SECOND_HALF) {
			rangeEnd = season.getPlayoffStartWeek() - 1;
		}
		//	If the display type is either the playoffs or a non-subseason type, get the end of the playoffs
		else {
			rangeEnd = season.getSuperbowlWeek() - 2;
		}
		
		return rangeEnd;
	}

}
