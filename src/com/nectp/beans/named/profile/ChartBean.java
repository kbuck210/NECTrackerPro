package com.nectp.beans.named.profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.Dependent;
import javax.inject.Named;

import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartSeries;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.LegendPlacement;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Week;

/** ChartBean is a generic abstraction of the data model required to display a Primefaces 
 *  chart for either type of AbstractTeamForSeason entities.  The abstracted implementation
 *  defines a data model for each of the following scopes: 
 *  1.	The current season so far
 *  2.	The first half of the current season
 *  3.	The second half of the current season
 *  4.	The playoffs for the current season
 *  5.	An All-Time view for every instance of this ATFS
 * 
 *	It is the responsibility of the concrete implementations to define the chart 
 *	title (type-attribute dependent), along with the implementation for the creation of the 
 *	data series (based on the differences between how Record entities are handled)
 * 
 * @author Kevin C. Buckley
 * @since  1.0
 *
 * @param <T> the Runtime type for this chart, required to be implementation of AbstractTeamForSeason
 */
@Named(value="chartBean")
@Dependent
public abstract class ChartBean<T> implements Serializable, ChartInterface<T> {
	private static final long serialVersionUID = -4821276965879685539L;
	
	protected LineChartModel seasonChartModel;
	protected LineChartModel firstHalfModel;
	protected LineChartModel secondHalfModel;
	protected LineChartModel playoffModel;
	protected LineChartModel allTimeModel;
	
	private String seasonSummary;
	private String firstHalfSummary;
	private String secondHalfSummary;
	private String playoffSummary;
	private String allTimeSummary;
	
	protected String summary;
	
	protected T profileEntity;
	
	protected Season season;
	
	protected String chartTitle;
	
	protected int activeIndex;
	
	@EJB
	protected RecordService recordService;
	
	@EJB
	protected SeasonService seasonService;
	
	private Logger log = Logger.getLogger(ChartBean.class.getName());
	
	/** PostConstruct initialization retrieves the profile entity of runtime type T from the injected Profile Bean,
	 *  then calls the methods to initialize the chart data series for each of the defined scopes
	 */
	public void setProfileEntity(T profileEntity) {
		this.profileEntity = profileEntity;
		if (profileEntity instanceof AbstractTeamForSeason) {
			season = ((AbstractTeamForSeason) profileEntity).getSeason();
		}
		
		//	Create the animated models
		seasonChartModel = createAnimatedModel(NEC.SEASON);
		firstHalfModel = createAnimatedModel(NEC.FIRST_HALF);
		secondHalfModel = createAnimatedModel(NEC.SECOND_HALF);
		playoffModel = createAnimatedModel(NEC.PLAYOFFS);
		allTimeModel = createAllTimeModel();
	}
	
	/** Defines method signature for setting the Chart's title, which must be overriden by the concrete implementation
	 */
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
	public T getProfileEntity() {
		return profileEntity;
	}
	
	/** Gets the LineChartModel for the scope of NEC.SEASON (entire season so far)
	 * 
	 * @return the created LineChartModel, initialized with season data
	 */
	public LineChartModel getSeasonChartModel() {
		return seasonChartModel;
	}
	
	/** Gets the LineChartModel for the scope of NEC.FIRST_HALF (the first half of the current season)
	 * 
	 * @return the created LineChartModel, initialized with the first half data
	 */
	public LineChartModel getFirstHalfModel() {
		return firstHalfModel;
	}
	
	/** Gets the LineChartModel for the scope of NEC.SECOND_HALF (the second half of the current season)
	 * 
	 * @return the created LineChartModel, initialized with the second half data
	 */
	public LineChartModel getSecondHalfModel() {
		return secondHalfModel;
	}
	
	/** Gets the LineChartModel for the scope of NEC.PLAYOFFS (the playoffs of the current season)
	 * 
	 * @return the created LineChartModel, initialized with the playoffs data
	 */
	public LineChartModel getPlayoffModel() {
		return playoffModel;
	}
	
	/** Gets the LineChartModel for the all-time history for this ATFS
	 * 
	 * @return the created LineChartModel, initialized with the entire record history of this ATFS
	 */
	public LineChartModel getAllTimeModel() {
		return allTimeModel;
	}
	
	/** Creates the animated LineChartModel based on the specified NEC enum display type for any AbstractTeamForSeason type
	 * 
	 * @param displayType the NEC enum corresponding to the scope of the model to create
	 * @return a LineChartModel initialized with the data for the specified scope
	 */
	protected LineChartModel createAnimatedModel(NEC displayType) {
		if (profileEntity instanceof AbstractTeamForSeason) {
			//	Call the abstracted set chart title method, suche that an appropriate title is set based on the runtime type
			setChartTitle();
			
			AbstractTeamForSeason atfs = (AbstractTeamForSeason) profileEntity;
			
			Integer rangeStart = getRangeStart(displayType);
			Integer rangeEnd = getRangeEnd(displayType);
			
			LineChartModel chartModel = createChartModel(chartTitle, "Win Percentage:", 100.0, 0.0, "Week Number:", rangeEnd+1, rangeStart-1);
			chartModel.setExtender("digitExt");
			chartModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
			chartModel.setResetAxesOnResize(false);
	
			boolean againstSpread = (displayType != NEC.TWO_AND_OUT && displayType != NEC.ONE_AND_OUT);
			RecordAggregator ragg = recordService.getRecordForConcurrentWeeksForAtfs(atfs, rangeStart, rangeEnd, displayType, againstSpread);
			
			//	Get the records from the aggregation, and sort by week number
			List<Record> records = ragg.getRecords();
			Collections.sort(records, new Comparator<Record>(){
				@Override
				public int compare(Record r1, Record r2) {
					return r1.getWeek().compareTo(r2.getWeek());
				}
			});
			
			//	Create a line series for the raw win records
			LineChartSeries rawSeries = createLineChartSeries(records, "Win Percentage", false, true);
			chartModel.addSeries(rawSeries);
			
			//	Create a line series for the wins against the spread records
			LineChartSeries atsSeries = createLineChartSeries(records, "Win % Vs Spread", true, true);	
			chartModel.addSeries(atsSeries);
			
			return chartModel;
		}
		//	If the defined runtime type is not that of a PlayerForSeason or TeamForSeason, log the error and return empty model
		else {
			log.severe("Runtime type is not an AbstractTeamForSeason as expected. Can not create Chart Model!");
			return new LineChartModel();
		}
	}
	
	/** Creates the animated LineChartModel representing the all-time record history for any AbstractTeamForSeason type
	 * 
	 * @return a LineChartModel initialized with the data from the ATFS's entire record history
	 */
	private LineChartModel createAllTimeModel() {
		if (profileEntity instanceof AbstractTeamForSeason) {
			setChartTitle();
			
			AbstractTeamForSeason atfs = (AbstractTeamForSeason) profileEntity;
			
			//	Create the chart model, specifying only the Y-Axis parameters
			LineChartModel model = createChartModel("All Time Performance:", "Win Percentage:", 100.0, 0.0, null, -1, -1);
			model.setExtender("digitExt");
			model.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
			model.setResetAxesOnResize(false);
			
			//	Get all persisted seasons in order by season number
			List<Season> allSeasons = seasonService.findAll();
			Collections.sort(allSeasons);
			
			//	Loop over each of the seasons, getting the aggregate record for the regular season & adding them to the overall list
			List<Record> seasonRecords = new ArrayList<Record>();
			TreeMap<Season, RecordAggregator> seasonRaggMap = new TreeMap<Season, RecordAggregator>();
			for (Season season : allSeasons) {
				//	Get the aggregate record for the regular season
				RecordAggregator seasonRagg = recordService.getRecordForConcurrentWeeksForAtfs(atfs, 1, 
						(season.getPlayoffStartWeek() - 1), NEC.SEASON, false);
			
				seasonRaggMap.put(season, seasonRagg);
				
				List<Record> records = seasonRagg.getRecords();
				Collections.sort(records, new Comparator<Record>(){
					@Override
					public int compare(Record r1, Record r2) {
						return r1.getWeek().compareTo(r2.getWeek());
					}
				});
				seasonRecords.addAll(records);
			}
			
			//	Create a line series for the weekly win percentage for each season
			LineChartSeries rawSeries = createLineChartSeries(seasonRecords, "Win Percentage", false, true);
			rawSeries.setXaxis(AxisType.X2);
			rawSeries.setYaxis(AxisType.Y);
			
			//	Create a line series for the weekly ATS win percentage for each season
			LineChartSeries atsSeries = createLineChartSeries(seasonRecords, "Win % vs Spread", true, true);
			atsSeries.setXaxis(AxisType.X2);
			atsSeries.setYaxis(AxisType.Y);
			
			//	Create a bar series for the season average win % for each season
			BarChartSeries rawAverage = createBarChartSeries(seasonRaggMap, "Average Win %", false);
			
			//	Create a bar series for the season average win % against the spread for each season
			BarChartSeries atsAverage = createBarChartSeries(seasonRaggMap, "Avg Win % vs Spread", true);
			
			//	Set the minimum on the X axis to the first NEC season available
			Season minSeason = allSeasons.get(0);
			model.getAxis(AxisType.X).setMin(minSeason.getSeasonNumber());
			
			//	Create category axes for the X-Axis & add to model
			CategoryAxis seasonAxis = new CategoryAxis("Season Number:");
			CategoryAxis weeksAxis = new CategoryAxis("Weeks:");
			weeksAxis.setTickAngle(90);
			model.getAxes().put(AxisType.X, seasonAxis);
			model.getAxes().put(AxisType.X2, weeksAxis);
			
			//	Add the bar & line series to the model
			model.addSeries(rawAverage);
			model.addSeries(atsAverage);
			model.addSeries(rawSeries);
			model.addSeries(atsSeries);
			
			return model;
		}
		else {
			log.severe("Runtime type is not of AbstractTeamForSeason as expected, can not create All Time model!");
			return new LineChartModel();
		}
	}
	
	/** Creates a LineChartModel with the following attributes specified
	 * 
	 * @param title the chart title
	 * @param yLabel the label for the Y-Axis (optional)
	 * @param yMax the maximum value for the Y-Axis (use -1 for default value)
	 * @param yMin the minimum value for the Y-Axis (use -1 for default value)
	 * @param xLabel the label for the X-Axis (optional)
	 * @param xMax the maximum value for the X-Axis (use -1 for default value)
	 * @param xMin the minimum value for the X-Axis (use -1 for default value)
	 * @return the initialized LineChartModel
	 */
	protected LineChartModel createChartModel(String title, String yLabel, double yMax, double yMin, String xLabel, int xMax, int xMin) {
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
		//	If the display type is the playoffs, but currently outside of the playoffs, get the end of the playoffs
		else if (displayType == NEC.PLAYOFFS) {
			rangeEnd = season.getSuperbowlWeek() - 2;
		}
		//	If the display type is either the second half or a non-subseason type, get the end of the regular season
		else {
			rangeEnd = season.getPlayoffStartWeek() - 1;
		}
		
		return rangeEnd;
	}
	
	public int getActiveIndex() {
		return activeIndex;
	}
	
	public void onTabChange(TabChangeEvent event) {
	    activeIndex = ((TabView) event.getSource()).getIndex();
	}
	
	public String getSummary() {
		return summary;
	}
	
	protected void setSummary(String summary) {
		this.summary = summary;
	}
}
