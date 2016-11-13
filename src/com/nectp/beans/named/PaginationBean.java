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
		int lastWeek = currentSeason.getSuperbowlWeek();
		gotoEndEnabled = displayWeekNum < lastWeek;
		gotoNextEnabled = displayWeekNum < lastWeek;
		gotoPlusOneEnabled = displayWeekNum < lastWeek;
		gotoPlusTwoEnabled = displayWeekNum < (lastWeek - 1);
		gotoPlusThreeEnabled = displayWeekNum < (lastWeek - 2);
		
		//	Set the link values
		if (displayWeekNum == currentWeek.getWeekNumber()) {
			currentWeekText = "Current Week:";
		}
		else if (displayWeekNum >= currentSeason.getPlayoffStartWeek()) {
			switch(displayWeekNum) {
			case 18:
				currentWeekText = " Wild Card ";
				break;
			case 19:
				currentWeekText = " Divisionals ";
				break;
			case 20:
				currentWeekText = " Conf Champ ";
				break;
			case 22:
				currentWeekText = " Superbowl ";
				break;
			default:
				currentWeekText = "   Week " + displayWeekNum + ":";
				break;
			}
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
		//	Check whether the minus-one pagination option is enabled
		if (gotoMinOneEnabled) {
			weekMinOne = getPageWeekNum(displayWeekNum - 1);
			if (weekMinOne.isEmpty()) {
				minOneDisabled = "disabled";
				weekMinOne = "  ";
			}
			else {
				minOneDisabled = "";
			}
		}
		else {
			minOneDisabled = "disabled";
			weekMinOne = "  ";
		}
		//	Check whether the minus-two pagination option is enabled
		if (gotoMinTwoEnabled) {
			weekMinTwo = getPageWeekNum(displayWeekNum - 2);
			if (weekMinTwo.isEmpty()) {
				minTwoDisabled = "disabled";
				weekMinTwo = "  ";
			}
			else {
				minTwoDisabled = "";
			}
		}
		else {
			minTwoDisabled = "disabled";
			weekMinTwo = "  ";
		}
		//	Check whether the minus-three pagination option is enabled
		if (gotoMinThreeEnabled) {
			weekMinThree = getPageWeekNum(displayWeekNum - 3);
			if (weekMinThree.isEmpty()) {
				minThreeDisabled = "disabled";
				weekMinThree = "  ";
			}
			else {
				minThreeDisabled = "";
			}
		}
		else {
			minThreeDisabled = "disabled";
			weekMinThree = "  ";
		}
		//	Check whether the plus one pagination option is enabled
		if (gotoPlusOneEnabled) {
			weekPlusOne = getPageWeekNum(displayWeekNum + 1);
			if (weekPlusOne.isEmpty()) {
				plusOneDisabled = "disabled";
				weekPlusOne = "  ";
			}
			else {
				plusOneDisabled = "";
			}
		}
		else {
			plusOneDisabled = "disabled";
			weekPlusOne = "  ";
		}
		//	Check whether the plus two pagination is enabled
		if (gotoPlusTwoEnabled) {
			weekPlusTwo = getPageWeekNum(displayWeekNum + 2);
			if (weekPlusTwo.isEmpty()) {
				plusTwoDisabled = "disabled";
				weekPlusTwo = "  ";
			}
			else {
				plusTwoDisabled = "";
			}
		}
		else {
			plusTwoDisabled = "disabled";
			weekPlusTwo = "  ";
		}
		//	Check whether the plus-three pagination option is enabled
		if (gotoPlusThreeEnabled) {
			weekPlusThree = getPageWeekNum(displayWeekNum + 3);
			if (weekPlusThree.isEmpty()) {
				plusThreeDisabled = "disabled";
				weekPlusThree = "  ";
			}
			else {
				plusThreeDisabled = "";
			}
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
	
	private String getPageWeekNum(int weekNum) {
		String pageNum;
		switch(weekNum) {
		case 18:
			pageNum = " WC ";
			break;
		case 19:
			pageNum = " Div ";
			break;
		case 20:
			pageNum = " Conf ";
			break;
		case 21:
			pageNum = "";
			break;
		case 22:
			pageNum = " SB ";
			break;
		default:
			pageNum = new Integer(weekNum).toString();
			break;
		}
		
		return pageNum;
	}
	
}
