package com.nectp.beans.named;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;

import com.nectp.beans.ejb.ApplicationState;
import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.beans.remote.daos.WeekService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Pick.PickType;

@Named(value="makePicksContentBean")
@ViewScoped
public class MakePicksContentBean implements Serializable {
	private static final long serialVersionUID = 3805986634562818317L;

	private String regularPicksTitle;
	
	private String playerPickHeadline;
	private String playerTnoHeadline;
	
	private String playerRecord;
	private String playerTnoRecord;
	
	private boolean renderTnoPicks;
	
	private PlayerForSeason user;
	
	private Week week;
	
	@Inject
	private ApplicationState appState;
	
	@EJB
	private WeekService weekService;
	
	@EJB
	private RecordService recordService;
	
	private Logger log;
	
	public MakePicksContentBean() {
		log = Logger.getLogger(MakePicksContentBean.class.getName());
	}
	
	@PostConstruct
	public void init() {
		user = appState.getUserInstance();
		Season season = user.getSeason();
		String weekNumStr = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("week");
		Integer weekNum = null;
		try {
			weekNum = Integer.parseInt(weekNumStr);
		} catch (NumberFormatException e) {
			log.severe("Invalid week number format, can not get page info!");
			log.severe(e.getMessage());
			e.printStackTrace();
		}
		
		try {
			week = weekService.selectWeekByNumberInSeason(weekNum, season);
		} catch (NoResultException e) {
			log.severe("No week found for: " + weekNumStr + " in the season! Can not load page info");
			log.severe(e.getMessage());
			e.printStackTrace();
		}
		
		this.regularPicksTitle = "Week " + weekNumStr + " Picks";
		if (user != null) {
			playerPickHeadline = user.getNickname() + " Picks - Week " + weekNumStr;
			playerTnoHeadline = user.getNickname() + " Two and Out Picks";
			
			NEC ssType = week.getSubseason().getSubseasonType();
			RecordAggregator playerRagg = recordService.getAggregateRecordForAtfsForType(user, ssType, true);
			playerRecord = playerRagg.toString(PickType.SPREAD1);
			
			RecordAggregator playerTnoRagg = recordService.getAggregateRecordForAtfsForType(user, NEC.TWO_AND_OUT, false);
			playerTnoRecord = playerTnoRagg.toString(PickType.STRAIGHT_UP);
			
			renderTnoPicks = playerTnoRagg.getRawLosses() < season.getTnoAcceptableLosses();
		}
	}
	
	public boolean renderTnoPicks() {
		return renderTnoPicks;
	}
	
	public String getRegularPicksTitle() {
		return regularPicksTitle;
	}
	
	public String getPlayerPickHeadline() {
		return playerPickHeadline;
	}
	
	public String getPlayerRecord() {
		return playerRecord;
	}
	
	public String getPlayerTnoHeadline() {
		return playerTnoHeadline;
	}
	
	public String getPlayerTnoRecord() {
		return playerTnoRecord;
	}
}
