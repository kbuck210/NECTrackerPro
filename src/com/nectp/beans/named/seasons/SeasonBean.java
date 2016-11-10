package com.nectp.beans.named.seasons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.nectp.beans.remote.daos.RecordService;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Season;

public class SeasonBean implements Serializable {
	private static final long serialVersionUID = -2806936876539170703L;

	private Season season;
	
	private List<PrizeBean> prizes;
	
	private HistoryChartBean historyChart;
	
	public void setSeason(Season season, RecordService recordService) {
		this.season = season;
		this.prizes = new ArrayList<PrizeBean>();
		for (PrizeForSeason pzfs : season.getPrizes()) {
			PrizeBean pb = new PrizeBean(pzfs, recordService);
			prizes.add(pb);
		}
		this.historyChart = new HistoryChartBean();
		historyChart.setRecordService(recordService);
		historyChart.setSeason(season);
	}

	public String getSeasonNumber() {
		return season != null ? season.getSeasonNumber().toString() : "N/a";
	}
	
	public String getSeasonYear() {
		return season != null ? season.getSeasonYear() : "N/a";
	}
	
	public HistoryChartBean getHistoryChart() {
		return historyChart;
	}
	
	public List<PrizeBean> getPrizes() {
		return prizes;
	}
}
