package com.nectp.beans.named.profile;

import java.util.List;
import java.util.TreeMap;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import java.util.Map.Entry;

import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartSeries;
import org.primefaces.model.chart.LineChartSeries;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;

@Named(value="playerChartBean")
@ViewScoped
public class PlayerChartBean extends ChartBean<PlayerForSeason> {
	private static final long serialVersionUID = 6012386529925496359L;

	@Override
	public void setChartTitle() {
		Integer seasonNumber = profileEntity.getSeason().getSeasonNumber();
		chartTitle = "NEC " + seasonNumber.toString() + " - " + profileEntity.getNickname();
	}

	@Override
	public LineChartSeries createLineChartSeries(List<Record> records, String title, boolean againstSpread, boolean cumulative) {
		LineChartSeries series = new LineChartSeries();
		series.setLabel(title);
		series.setFill(false);
		
		//	Loop over the records adding data points for each record
		int totalPicks = 0;
		int wins = 0;
		for (int i = 0; i < records.size(); ++i) {
			Record r = records.get(i);
			//	If calculating on a week to week basis, reset the total picks & total wins for each record, otherwise accumulate scores
			if (!cumulative) {
				wins = 0;
				totalPicks = 0;
			}
			//	If not against the spread, get the raw win total
			if (!againstSpread) {
				wins += r.getWins();
			}
			//	If against the spread, get the spread1 record (
			else {
				wins += r.getWinsATS1();
			}
			//	Get the number of picks so far in the season & calculate the total win %
			totalPicks += r.getPicksInRecord().size();
			double currentWinPct = 0.0;
			if (totalPicks > 0) {
				currentWinPct = (double) wins / (double) totalPicks;
			}
			
			series.set(r.getWeek().getWeekNumber(), currentWinPct);
		}
		
		return series;
	}

	@Override
	public BarChartSeries createBarChartSeries(TreeMap<Season, RecordAggregator> seasonRaggMap, String title, boolean againstSpread) {
		BarChartSeries barSeries = new BarChartSeries();
		barSeries.setLabel(title);
		barSeries.setXaxis(AxisType.X);
		barSeries.setYaxis(AxisType.Y);
		
		for (Entry<Season, RecordAggregator> entry : seasonRaggMap.entrySet()) {
			Season season = entry.getKey();
			RecordAggregator seasonRagg = entry.getValue();
			
			int wins;
			if (againstSpread) {
				wins = seasonRagg.getWinsATS1();
			}
			else {
				wins = seasonRagg.getRawWins();
			}
			double seasonWinPct = (double) wins / (double) seasonRagg.getRecords().size();
			
			barSeries.set(season.getSeasonNumber().toString(), seasonWinPct);
		}
		
		return barSeries;
	}

}
