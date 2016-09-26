package com.nectp.beans.named;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.WeekService;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Week;

@Named(value="paginationBean")
@ViewScoped
public class PaginationBean implements Serializable {
	private static final long serialVersionUID = -1700823643009335725L;
	
	private String beginningDisabled = "";
	private String previousDisabled = "";
	private String minOneDisabled = "";
	private String minTwoDisabled = "";
	private String minThreeDisabled = "";
	private String plusOneDisabled = "";
	private String plusTwoDisabled = "";
	private String plusThreeDisabled = "";
	private String nextDisabled = "";
	private String endDisabled = "";
	
	private boolean gotoBeginningEnabled = false;
	private boolean gotoPreviousEnabled = false;
	private boolean gotoMinThreeEnabled = false;
	private boolean gotoMinTwoEnabled = false;
	private boolean gotoMinOneEnabled = false;
	private boolean gotoPlusOneEnabled = false;
	private boolean gotoPlusTwoEnabled = false;
	private boolean gotoPlusThreeEnabled = false;
	private boolean gotoNextEnabled = false;
	private boolean gotoEndEnabled = false;
	
	private String weekMinThree = "";
	private String weekMinTwo = "";
	private String weekMinOne = "";
	private String weekPlusOne = "";
	private String weekPlusTwo = "";
	private String weekPlusThree = "";
	private String currentWeekText;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private WeekService weekService;
	
	private Season currentSeason;
	
	private Week currentWeek;
	
	private Week displayWeek;
	
	private Logger log;
	
	public PaginationBean() {
	}
	
	@PostConstruct
	public void init() {
		log = Logger.getLogger(PaginationBean.class.getName());
		currentSeason = seasonService.selectCurrentSeason();
		if (currentSeason == null) {
			log.severe("Failed to retrieve current season! Can not display pagination.");
		}
		else {
			currentWeek = weekService.selectCurrentWeekInSeason(currentSeason);
			if (currentWeek == null) {
				log.severe("Failed to retrieve current week! Can not display pagination.");
			}
			else {
				if (displayWeek == null) {
					displayWeek = currentWeek;
				}
				initValues();
			}
		}
	}
	
	private void initValues() {
		Integer displayWeekNum = displayWeek.getWeekNumber();
		
		//	Check whether the backwards weeks enabled:
		gotoBeginningEnabled = displayWeekNum > 1;
		gotoPreviousEnabled = displayWeekNum > 1;
		gotoMinOneEnabled = displayWeekNum > 1;
		gotoMinTwoEnabled = displayWeekNum > 2;
		gotoMinThreeEnabled = displayWeekNum > 3;
		
		//	Check whether the forward weeks are enabled
		int lastWeek = currentSeason.getPlayoffStartWeek() - 1;
		gotoEndEnabled = displayWeekNum < lastWeek;
		gotoNextEnabled = displayWeekNum < lastWeek;
		gotoPlusOneEnabled = displayWeekNum < lastWeek;
		gotoPlusTwoEnabled = displayWeekNum < (lastWeek - 1);
		gotoPlusThreeEnabled = displayWeekNum < (lastWeek - 2);
		
		//	Set the link values
		if (displayWeekNum == currentWeek.getWeekNumber()) {
			currentWeekText = "Current Week:";
		}
		else {
			currentWeekText = "   Week " + displayWeekNum + ":";
		}
		
		if (!gotoBeginningEnabled) {
			beginningDisabled = "disabled";
		}
		else {
			beginningDisabled = "";
		}
		if (!gotoPreviousEnabled) {
			previousDisabled = "disabled";
		}
		else {
			previousDisabled = "";
		}
		if (gotoMinOneEnabled) {
			weekMinOne = new Integer(displayWeekNum - 1).toString();
			minOneDisabled = "";
		}
		else {
			minOneDisabled = "disabled";
			weekMinOne = "  ";
		}
		if (gotoMinTwoEnabled) {
			weekMinTwo = new Integer(displayWeekNum - 2).toString();
			minTwoDisabled = "";
		}
		else {
			minTwoDisabled = "disabled";
			weekMinTwo = "  ";
		}
		if (gotoMinThreeEnabled) {
			weekMinThree = new Integer(displayWeekNum - 3).toString();
			minThreeDisabled = "";
		}
		else {
			minThreeDisabled = "disabled";
			weekMinThree = "  ";
		}
		
		if (gotoPlusOneEnabled) {
			weekPlusOne = new Integer(displayWeekNum + 1).toString();
			plusOneDisabled = "";
		}
		else {
			plusOneDisabled = "disabled";
			weekPlusOne = "  ";
		}
		if (gotoPlusTwoEnabled) {
			weekPlusTwo = new Integer(displayWeekNum + 2).toString();
			plusTwoDisabled = "";
		}
		else {
			plusTwoDisabled = "disabled";
			weekPlusTwo = "  ";
		}
		if (gotoPlusThreeEnabled) {
			weekPlusThree = new Integer(displayWeekNum + 3).toString();
			plusThreeDisabled = "";
		}
		else {
			plusThreeDisabled = "disabled";
			weekPlusThree = "  ";
		}
		
		if (!gotoNextEnabled) {
			nextDisabled = "disabled";
		}
		else {
			nextDisabled = "";
		}
		if (!gotoEndEnabled) {
			endDisabled = "disabled";
		}
		else {
			endDisabled = "";
		}
	}

	public Week getDisplayWeek() {
		return displayWeek;
	}

	public String getBeginningDisabled() {
		return beginningDisabled;
	}

	public String getPreviousDisabled() {
		return previousDisabled;
	}

	public String getMinOneDisabled() {
		return minOneDisabled;
	}

	public String getMinTwoDisabled() {
		return minTwoDisabled;
	}

	public String getMinThreeDisabled() {
		return minThreeDisabled;
	}

	public String getPlusOneDisabled() {
		return plusOneDisabled;
	}

	public String getPlusTwoDisabled() {
		return plusTwoDisabled;
	}

	public String getPlusThreeDisabled() {
		return plusThreeDisabled;
	}

	public String getNextDisabled() {
		return nextDisabled;
	}

	public String getEndDisabled() {
		return endDisabled;
	}

	public String getWeekMinThree() {
		return weekMinThree;
	}

	public String getWeekMinTwo() {
		return weekMinTwo;
	}

	public String getWeekMinOne() {
		return weekMinOne;
	}

	public String getWeekPlusOne() {
		return weekPlusOne;
	}

	public String getWeekPlusTwo() {
		return weekPlusTwo;
	}

	public String getWeekPlusThree() {
		return weekPlusThree;
	}

	public String getCurrentWeekText() {
		return currentWeekText;
	}
	
	public boolean isGotoBeginningEnabled() {
		return gotoBeginningEnabled;
	}

	public boolean isGotoPreviousEnabled() {
		return gotoPreviousEnabled;
	}

	public boolean isGotoMinThreeEnabled() {
		return gotoMinThreeEnabled;
	}

	public boolean isGotoMinTwoEnabled() {
		return gotoMinTwoEnabled;
	}

	public boolean isGotoMinOneEnabled() {
		return gotoMinOneEnabled;
	}

	public boolean isGotoPlusOneEnabled() {
		return gotoPlusOneEnabled;
	}

	public boolean isGotoPlusTwoEnabled() {
		return gotoPlusTwoEnabled;
	}

	public boolean isGotoPlusThreeEnabled() {
		return gotoPlusThreeEnabled;
	}

	public boolean isGotoNextEnabled() {
		return gotoNextEnabled;
	}

	public boolean isGotoEndEnabled() {
		return gotoEndEnabled;
	}

	public void gotoBeginning() {
		displayWeek = weekService.selectWeekByNumberInSeason(1, currentSeason);
		log.info("display week: " + displayWeek.getWeekNumber());
		initValues();
	}
	
	public void gotoPrevious() {
		Integer displayedWeek = displayWeek.getWeekNumber();
		displayWeek = weekService.selectWeekByNumberInSeason((displayedWeek - 1), currentSeason);
		initValues();
	}
	
	public void gotoMinThree() {
		Integer displayedWeek = displayWeek.getWeekNumber();
		displayWeek = weekService.selectWeekByNumberInSeason((displayedWeek - 3), currentSeason);
		initValues();
	}
	
	public void gotoMinTwo() {
		Integer displayedWeek = displayWeek.getWeekNumber();
		displayWeek = weekService.selectWeekByNumberInSeason((displayedWeek - 2), currentSeason);
		initValues();
	}
	
	public void gotoMinOne() {
		Integer displayedWeek = displayWeek.getWeekNumber();
		displayWeek = weekService.selectWeekByNumberInSeason((displayedWeek - 1), currentSeason);
		initValues();
	}
	
	public void gotoPlusOne() {
		Integer displayedWeek = displayWeek.getWeekNumber();
		displayWeek = weekService.selectWeekByNumberInSeason((displayedWeek + 1), currentSeason);
		initValues();
	}
	
	public void gotoPlusTwo() {
		Integer displayedWeek = displayWeek.getWeekNumber();
		displayWeek = weekService.selectWeekByNumberInSeason((displayedWeek + 2), currentSeason);
		initValues();
	}
	
	public void gotoPlusThree() {
		Integer displayedWeek = displayWeek.getWeekNumber();
		displayWeek = weekService.selectWeekByNumberInSeason((displayedWeek + 3), currentSeason);
		initValues();
	}
	
	public void gotoNext() {
		Integer displayedWeek = displayWeek.getWeekNumber();
		displayWeek = weekService.selectWeekByNumberInSeason((displayedWeek + 1), currentSeason);
		initValues();
	}
	
	public void gotoEnd() {
		Integer lastWeek = currentSeason.getPlayoffStartWeek() - 1;
		displayWeek = weekService.selectWeekByNumberInSeason(lastWeek, currentSeason);
		initValues();
	}
}
