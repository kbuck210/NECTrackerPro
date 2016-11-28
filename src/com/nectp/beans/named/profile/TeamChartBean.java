package com.nectp.beans.named.profile;

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartSeries;
import org.primefaces.model.chart.LineChartSeries;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;

/** Team Chart Bean extends the abstract ChartBean generic type for a TeamForSeason implementation.
 *  The concrete implementation defines how to calculate the scores based on the TeamForSeason records,
 *  each of which contains exactly one game per week.  This enables omitting consideration of the Pick
 *  entities for each Record (none should exist for TFS's) in calculation of the win percentages.
 *  
 * @author Kevin C. Buckley
 * @since  1.0
 */
@Named(value="teamChartBean")
@ViewScoped
public class TeamChartBean extends ChartBean<TeamForSeason> {
	private static final long serialVersionUID = 1310168801290447210L;
	
	/** Sets the chart title based on the City the TeamForSeason represents
	 * 
	 */
	@Override
	public void setChartTitle() {
		Integer seasonNumber = profileEntity.getSeason().getSeasonNumber();
		chartTitle = "NEC " + seasonNumber.toString() + " - " + profileEntity.getTeamCity();
	}
	
	/** Creates a LineChartSeries for the specified list of Records, in order by week number.
	 *  **NOTE: TFS entities have only ONE GAME PER RECORD - Do not need to consider Pick entities for value calculation**
	 * 
	 * @param records a List of Record entities for this TeamForSeason, corresponding to the defined scope of the model
	 * @param title the Label title for this series
	 * @param againstSpread if true, uses the 'against the spread' scores in the data series, the raw scoring if false
	 * @param cumulative not used in this implementation because teams have 1 game per record (i.e. per-week percentage would always be 1 or 0)
	 * @return a new LineChartSeries, initialized with the data as defined by the method parameters
	 */
	@Override
	public LineChartSeries createLineChartSeries(List<Record> records, String title, boolean againstSpread, boolean cumulative) { 
		LineChartSeries series = new LineChartSeries();
		series.setLabel(title);
		series.setFill(false);
		
		//	Loop over the records adding data points for each record
		int wins = 0;
		for (int i = 1; i <= records.size(); ++i) {
			Record r = records.get(i-1);
			//	If not against the spread, get the raw win total
			if (!againstSpread) {
				wins += r.getWins();
			}
			//	If against the spread, get the spread1 record (
			else {
				wins += r.getWinsATS1();
			}
			double currentWinPct = ((double) wins / (double) i) * 100;
			
			series.set(r.getWeek().getWeekNumber(), currentWinPct);
		}
		
		return series;
	}
	
	/** Creates a Bar Chart Series for each season as an accumulated total average win percentage
	 *  **NOTE: TFS entities have only ONE GAME PER RECORD - Do not need to consider Pick entities for value calculation**
	 * 
	 * @param seasonRaggMap an ordered TreeMap maintaining the pairing of aggregate records to each season
	 * @param title the Label title for this series
	 * @param againstSpread if true, uses the 'against the spread' scores in the data series, the raw scoring if false
	 * @return a new BarChartSeries, initialized with the data as defined by the method parameters
	 */
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
