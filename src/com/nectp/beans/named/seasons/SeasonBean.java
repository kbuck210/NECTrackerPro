package com.nectp.beans.named.seasons;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.nectp.beans.remote.daos.RecordService;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Week.WeekStatus;
import com.nectp.poi.ExcelSummaryWriter;

public class SeasonBean implements Serializable {
	private static final long serialVersionUID = -2806936876539170703L;

	private Season season;
	
	private List<PrizeBean> prizes;
	
	private HistoryChartBean historyChart;
	
	private String exportWeek;
	private List<SelectItem> weeks;
	
	private ExcelSummaryWriter excelExporter;
	
	public SeasonBean(ExcelSummaryWriter exporter) {
		this.excelExporter = exporter;
	}
	
	private Logger log = Logger.getLogger(SeasonBean.class.getName());
	
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
		
		this.weeks = new ArrayList<SelectItem>();
		//	Create the dropdown selection mapping for week data exports
		SelectItemGroup firstHalf = new SelectItemGroup("First Half:");
		SelectItemGroup secondHalf = new SelectItemGroup("Second Half:");
		SelectItemGroup playoffs = new SelectItemGroup("Playoffs:");
		SelectItemGroup superbowl = new SelectItemGroup("Superbowl:");
		for (Subseason ss : season.getSubseasons()) {
			List<Week> ssWeeks = ss.getWeeks();
			List<Week> completedWeeks = new ArrayList<Week>();
			for (Week w : ssWeeks) {
				if (w.getWeekStatus() == WeekStatus.COMPLETED) {
					completedWeeks.add(w);
				}
			}
			Collections.sort(completedWeeks);
			SelectItem[] selectItems = new SelectItem[completedWeeks.size()];
			switch(ss.getSubseasonType()) {
			case FIRST_HALF:
				for (int i = 0; i < completedWeeks.size(); ++i) {
					Week week = ssWeeks.get(i);
					String weekNum = week.getWeekNumber().toString();
					String label = "Week " + weekNum;
					selectItems[i] = new SelectItem(weekNum, label);
				}
				firstHalf.setSelectItems(selectItems);
				break;
			case SECOND_HALF:
				for (int i = 0; i < completedWeeks.size(); ++i) {
					Week week = ssWeeks.get(i);
					String weekNum = week.getWeekNumber().toString();
					String label = "Week " + weekNum;
					selectItems[i] = new SelectItem(weekNum, label);
				}
				secondHalf.setSelectItems(selectItems);
				break;
			case PLAYOFFS:
				//	Get the playoff weeks in order
				for (int i = 0; i < completedWeeks.size(); ++i) {
					String label;
					if (i == 0) {
						label = "Wildcard";
					}
					else if (i == 1) {
						label = "Divisional";
					}
					else if (i == 2) {
						label = "Conf Champ";
					}
					else break;
					
					SelectItem playoffItem = new SelectItem(completedWeeks.get(i).getWeekNumber().toString(), label);
					selectItems[i] = playoffItem;
				}
				break;
			case SUPER_BOWL:
				if (completedWeeks.size() == 1) {
					SelectItem sb = new SelectItem(completedWeeks.get(0).getWeekNumber().toString(), "Superbowl");
					selectItems[0] = sb;
					superbowl.setSelectItems(selectItems);
				}
				break;
			default:
				break;
			}
		}
		if (firstHalf.getSelectItems() != null && firstHalf.getSelectItems().length > 0) weeks.add(firstHalf);
		if (secondHalf.getSelectItems() != null && secondHalf.getSelectItems().length > 0) weeks.add(secondHalf);
		if (playoffs.getSelectItems() != null && playoffs.getSelectItems().length > 0) weeks.add(playoffs);
		if (superbowl.getSelectItems() != null && superbowl.getSelectItems().length > 0) weeks.add(superbowl);
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
	
	public void setExportWeek(String exportWeek) {
		this.exportWeek = exportWeek;
	}
	
	public String getExportWeek() {
		return exportWeek;
	}
	
	public List<SelectItem> getWeeks() {
		return weeks;
	}
	
	public StreamedContent getExcelDownload() {
		StreamedContent excelFile = null;
		
		String path = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("upload.excel");
//		String path = "/NECTrackerResources/excel/NEC" + season.getSeasonNumber() + File.separator;
		String filename = "NEC " + season.getSeasonNumber() + " - Week " + exportWeek + " Totals.xls";
		Path filePath = Paths.get(path + filename);
		File xlsFile = filePath.toFile();
		
		//	If the excel file for the specified week does not exist, first create it
		boolean exists = xlsFile.exists();
		if (!exists) {
			log.info("Excel file doesn't exist - creating: " + xlsFile.getAbsolutePath());
			excelExporter.setWeek(season.getSeasonNumber().toString(), exportWeek);
			exists = excelExporter.writeTotals();
		} else {
			log.info(xlsFile.getAbsolutePath() + " exists, downloading");
		}
		
		if (exists) {
			InputStream stream;
			try {
				stream = new FileInputStream(xlsFile);
				excelFile = new DefaultStreamedContent(stream, "application/xls", xlsFile.getName());
			} catch (IOException e) {
				log.severe("Exception handling: " + xlsFile.getAbsolutePath() + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		if (excelFile == null) log.warning("Streamed content null...");
		
		return excelFile;
	}
	
	public void downloadPdf() {
		//	NOT implemented yet
	}
}
