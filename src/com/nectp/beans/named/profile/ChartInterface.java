package com.nectp.beans.named.profile;

import java.util.List;
import java.util.TreeMap;

import org.primefaces.model.chart.BarChartSeries;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;

public interface ChartInterface<T> {

	public abstract void setChartTitle();
	
	/** Defines the method signature for creating a LineChartSeries for this chart implementation
	 * 
	 * @param records a List of Record entities for this profile entity, corresponding to the defined scope of the model
	 * @param title the Label title for this series
	 * @param againstSpread if true, uses the 'against the spread' scores in the data series, the raw scoring if false
	 * @param cumulative if true, calculates the win % as an accumulating dataset, if false, calculates on a week-to-week basis
	 * @return a new LineChartSeries, initialized with the data as defined by the method parameters
	 */
	public abstract LineChartSeries createLineChartSeries(List<Record> records, String title, boolean againstSpread, boolean cumulative);
	
	/** Defines the method signature for creating a BarChartSeries for this chart implementation
	 * 
	 * @param seasonRaggMap an ordered TreeMap maintaining the pairing of aggregate records to each season
	 * @param title the Label title for this series
	 * @param againstSpread if true, uses the 'against the spread' scores in the data series, the raw scoring if false
	 * @return a new BarChartSeries, initialized with the data as defined by the method parameters
	 */
	public abstract BarChartSeries createBarChartSeries(TreeMap<Season, RecordAggregator> seasonRaggMap, String title, boolean againstSpread);
	
	/** Gets the runtime profile entity associated with this instance of the chart
	 * 
	 * @return the JPA entity for this chart
	 */
	public T getProfileEntity();
	
	/** Gets the LineChartModel for the scope of NEC.SEASON (entire season so far)
	 * 
	 * @return the created LineChartModel, initialized with season data
	 */
	public LineChartModel getSeasonChartModel();
	
	/** Gets the LineChartModel for the scope of NEC.FIRST_HALF (the first half of the current season)
	 * 
	 * @return the created LineChartModel, initialized with the first half data
	 */
	public LineChartModel getFirstHalfModel();
	
	/** Gets the LineChartModel for the scope of NEC.SECOND_HALF (the second half of the current season)
	 * 
	 * @return the created LineChartModel, initialized with the second half data
	 */
	public LineChartModel getSecondHalfModel();
	
	/** Gets the LineChartModel for the scope of NEC.PLAYOFFS (the playoffs of the current season)
	 * 
	 * @return the created LineChartModel, initialized with the playoffs data
	 */
	public LineChartModel getPlayoffModel();
	
	/** Gets the LineChartModel for the all-time history for this ATFS
	 * 
	 * @return the created LineChartModel, initialized with the entire record history of this ATFS
	 */
	public LineChartModel getAllTimeModel();
}
