package com.nectp.beans.named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.ApplicationState;
import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.GameContainer;
import com.nectp.beans.remote.daos.PickFactory;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.beans.remote.daos.WeekService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Pick.PickType;

@Named(value="makeRegularPicksBean")
@ViewScoped
public class MakeRegularPicksBean implements Serializable, GameContainer {
	private static final long serialVersionUID = -5093075825059791162L;
	
	//	Need:
	//	List of games for the week

	private Logger log;
	
	private PlayerForSeason user;
	
	private Week week;
	
	private NEC pickFor;
	
	private List<GameBean> gameBeans;
	
	@EJB
	private WeekService weekService;
	
	@EJB
	private RecordService recordService;
	
	@EJB
	private PickFactory pickFactory;
	
	@Inject
	private ApplicationState appState;
	
	public MakeRegularPicksBean() {
		log = Logger.getLogger(MakeRegularPicksBean.class.getName());
	}
	
	@PostConstruct
	public void init() {
		user = appState.getUserInstance();
		if (user != null) {
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
			} catch (NoExistingEntityException e) {
				log.severe("No week found for: " + weekNumStr + " in the season! Can not load page info");
				log.severe(e.getMessage());
				e.printStackTrace();
			}
			
			
			if (week != null) {
				Calendar currentTime = new GregorianCalendar();
				boolean selectable = true;
				pickFor = week.getSubseason().getSubseasonType();
				List<Pick> userPicks = pickFactory.selectPlayerPicksForWeekForType(user, week, pickFor);
				//	If the user already has picks for this week, determine whether or not picks can still be changed
				for (Pick p : userPicks) {
					Game pickedGame = p.getGame();
					if (currentTime.compareTo(pickedGame.getGameDate()) > 0) {
						selectable = false;
						break;
					}
				}
				
				for (Game g : week.getGames()) {
					//	Create Beans for regular game picks
					GameBean gameBean = new GameBean();
					gameBean.setPlayer(user);
					gameBean.setGameDisplayType(pickFor);
					gameBean.setGame(g);
					
					//	If picks can still be made, determine whether this game has already locked
					if (selectable) {
						boolean singleSelectable = currentTime.compareTo(g.getGameDate()) < 0;
						gameBean.setHomeSelectable(singleSelectable);
						gameBean.setAwaySelectable(singleSelectable);
						gameBean.setHomeGrayed(!singleSelectable);
						gameBean.setAwayGrayed(!singleSelectable);
					}
					else {
						gameBean.setHomeSelectable(false);
						gameBean.setAwaySelectable(false);
						gameBean.setHomeGrayed(true);
						gameBean.setAwayGrayed(true);
					}
					
					TeamForSeason homeTeam = g.getHomeTeam();
					RecordAggregator homeRagg = recordService.getAggregateRecordForAtfsForType(homeTeam, NEC.SEASON, false);
					gameBean.setHomeRecord(homeRagg.toString(PickType.STRAIGHT_UP));
					
					TeamForSeason awayTeam = g.getAwayTeam();
					RecordAggregator awayRagg = recordService.getAggregateRecordForAtfsForType(awayTeam, NEC.SEASON, false);
					gameBean.setAwayRecord(awayRagg.toString(PickType.STRAIGHT_UP));
					
					gameBeans.add(gameBean);
				}
			}
		}
	}
	
	@Override
	public List<GameBean> getGameBeans() {
		return gameBeans;
	}
	
	public void setGameBeans(List<GameBean> gameBeans) {
		this.gameBeans = gameBeans;
	}
	
	/** Submit action from form to lock picks
	 * 
	 * @param e
	 */
	public void submit(ActionEvent e) {
		List<TeamForSeason> pickedTeams = new ArrayList<TeamForSeason>();
		List<TeamForSeason> mnfPicks = new ArrayList<TeamForSeason>();
		List<TeamForSeason> tntPicks = new ArrayList<TeamForSeason>();

		boolean mnfExists = false;
		
		PickType pickType = PickType.SPREAD1;
		for (GameBean gameBean : gameBeans) {
			Game game = gameBean.getGame();
			String homeSelected = gameBean.getHomeSelected();
			String awaySelected = gameBean.getAwaySelected();
			TeamForSeason pickedTeam = null;
			if ("selected".equals(homeSelected) && "selected".equals(awaySelected)) {
				log.severe("Both teams in game: " + game.getGameId() + " selected! can not create pick for both teams.");
			}
			else if ("selected".equals(homeSelected)) {
				pickedTeam = game.getHomeTeam();
			}
			else if ("selected".equals(awaySelected)) {
				pickedTeam = game.getAwayTeam();
			}

			int dayOfWeek = game.getGameDate().get(GregorianCalendar.DAY_OF_WEEK);
			if (dayOfWeek == GregorianCalendar.MONDAY) {
				mnfExists = true;
			}
			
			if (pickedTeam != null) {
				pickedTeams.add(pickedTeam);
				if (dayOfWeek == GregorianCalendar.MONDAY) {
					mnfPicks.add(pickedTeam);
				}
				else if (dayOfWeek == GregorianCalendar.THURSDAY) {
					tntPicks.add(pickedTeam);
				}

				//	Check whether the picked game has a spread 2, and is on a spread2 qualifying day (tuesday-friday)
				//	If so, set the pick type to spread2 for this player's picks for the week
				if (game.getSpread2() != null && 
					dayOfWeek > GregorianCalendar.MONDAY && 
					dayOfWeek != GregorianCalendar.SATURDAY) 
				{
					pickType = PickType.SPREAD2;
				}
			}
		}
		
		//	Check whether enough/too many picks were made
		Season season = user.getSeason();
		int pickCount = pickedTeams.size();
		int mnfCount = mnfPicks.size();
		int tntCount = tntPicks.size();
		
		String growlMessage = "Message";
		Severity severity = FacesMessage.SEVERITY_INFO;
		if (pickCount < season.getMinPicks()) {
			growlMessage = "Only " + pickCount + " picks selected, " + season.getMinPicks() + " picks required!";
			severity = FacesMessage.SEVERITY_ERROR;
		}
		else if (season.getMaxPicks() != null && pickCount > season.getMaxPicks()) {
			growlMessage = pickCount + " picks selected, only " + season.getMaxPicks() + " picks allowed!";
			severity = FacesMessage.SEVERITY_ERROR;
		}
		else if (mnfExists && mnfCount == 0) {
			growlMessage = "No MNF game selected, please make a selection!";
			severity = FacesMessage.SEVERITY_ERROR;
		}
		
		if (severity != FacesMessage.SEVERITY_ERROR) {
			//	Loop over the lists of picked teams, creating picks for the week & mnf/tnt picks & tno picks
			int successfulPicks = 0;
			int successfulMnf = 0;
			int successfulTnt = 0;
			for (TeamForSeason tfs : pickedTeams) {
				Pick p = pickFactory.createPlayerPickInWeek(user, tfs, week, pickFor, pickType);
				if (p != null) successfulPicks += 1;
			}
			for (TeamForSeason tfs : mnfPicks) {
				Pick p = pickFactory.createPlayerPickInWeek(user, tfs, week, NEC.MNF, pickType);
				if (p != null) successfulMnf += 1;
			}
			for (TeamForSeason tfs : tntPicks) {
				Pick p = pickFactory.createPlayerPickInWeek(user, tfs, week, NEC.TNT, pickType);
				if (p != null) successfulTnt += 1;
			}
			
			//	Determine if all picks were submitted successfully
			if (successfulPicks == pickCount && successfulMnf == mnfCount && successfulTnt == tntCount) {
				growlMessage = "Week " + week.getWeekNumber() + " - " + successfulPicks + " picks submitted!";
				severity = FacesMessage.SEVERITY_INFO;
				FacesMessage message = new FacesMessage(severity, "Success!",  growlMessage);
		        FacesContext.getCurrentInstance().addMessage(null, message);
			}
			else {
				if (successfulPicks != pickCount) {
					int difference = pickCount - successfulPicks;
					growlMessage = "Only " + difference + " picks submitted! - Please submit picks manually";
					severity = FacesMessage.SEVERITY_WARN;
					FacesMessage message = new FacesMessage(severity, "An error occurred!",  growlMessage);
			        FacesContext.getCurrentInstance().addMessage(null, message);
				}
				if (successfulMnf != mnfCount) {
					int difference = mnfCount - successfulMnf;
					growlMessage = "Only " + difference + " MNF picks submitted! - Please submit MNF picks manually";
					severity = FacesMessage.SEVERITY_WARN;
					FacesMessage message = new FacesMessage(severity, "An error occurred!",  growlMessage);
			        FacesContext.getCurrentInstance().addMessage(null, message);
				}
				if (successfulTnt != tntCount) {
					int difference = tntCount - successfulTnt;
					growlMessage = "Only " + difference + " TNT picks submitted! - Please submit TNT picks manually";
					severity = FacesMessage.SEVERITY_WARN;
					FacesMessage message = new FacesMessage(severity, "An error occurred!",  growlMessage);
			        FacesContext.getCurrentInstance().addMessage(null, message);
				}
			}
		}
		else {
			FacesMessage message = new FacesMessage(severity, "Invalid Selection:",  growlMessage);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
}
