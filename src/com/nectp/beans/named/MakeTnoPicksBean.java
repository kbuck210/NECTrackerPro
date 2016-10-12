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
import javax.persistence.NoResultException;

import com.nectp.beans.ejb.ApplicationState;
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

@Named(value="makeTnoPicksBean")
@ViewScoped
public class MakeTnoPicksBean implements Serializable, GameContainer {
	private static final long serialVersionUID = 4127693765064979951L;

	private List<GameBean> gameBeans;
	
	private Logger log;
	
	private PlayerForSeason user;
	
	private Week week;
	
	private boolean isOut;
	
	@Inject
	private ApplicationState appState;
	
	@EJB
	private WeekService weekService;
	
	@EJB
	private RecordService recordService;
	
	@EJB
	private PickFactory pickFactory;
	
	public MakeTnoPicksBean() {
		log = Logger.getLogger(MakeTnoPicksBean.class.getName());
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
			} catch (NoResultException e) {
				log.severe("No week found for: " + weekNumStr + " in the season! Can not load page info");
				log.severe(e.getMessage());
				e.printStackTrace();
			}
			
			
			if (week != null) {
				Calendar currentTime = new GregorianCalendar();
				boolean selectable = true;
				List<Pick> tnoPicks = pickFactory.selectPlayerPicksForType(user, NEC.TWO_AND_OUT);
				//	If the user already has picks for this week, determine whether or not picks can still be changed
				for (Pick p : tnoPicks) {
					Game pickedGame = p.getGame();
					if (currentTime.compareTo(pickedGame.getGameDate()) > 0) {
						selectable = false;
						break;
					}
				}
				
				RecordAggregator tnoRagg = recordService.getAggregateRecordForAtfsForType(user, NEC.TWO_AND_OUT, false);
				isOut = tnoRagg.getRawLosses() >= season.getTnoAcceptableLosses();
				
				for (Game g : week.getGames()) {
					//	Create Beans for two and out picks
					GameBean tnoBean = new GameBean();
					tnoBean.setPlayer(user);
					tnoBean.setGameDisplayType(NEC.TWO_AND_OUT);
					tnoBean.setGame(g);
					tnoBean.setSinglePick(true);
					
					TeamForSeason homeTeam = g.getHomeTeam();
					RecordAggregator homeRagg = recordService.getAggregateRecordForAtfsForType(homeTeam, NEC.SEASON, false);
					tnoBean.setHomeRecord(homeRagg.toString(PickType.STRAIGHT_UP));
					
					TeamForSeason awayTeam = g.getAwayTeam();
					RecordAggregator awayRagg = recordService.getAggregateRecordForAtfsForType(awayTeam, NEC.SEASON, false);
					tnoBean.setAwayRecord(awayRagg.toString(PickType.STRAIGHT_UP));
					
					//	Get a list of teams that were already picked prior to this week
					List<TeamForSeason> previouslyPickedTeams = new ArrayList<TeamForSeason>();
					for (Pick p : tnoPicks) {
						TeamForSeason pickedTeam = p.getPickedTeam(); 
						if (p.getApplicableRecord().getWeek().getWeekNumber() < week.getWeekNumber()) {
							previouslyPickedTeams.add(pickedTeam);
						}
					}
					
					//	Set home/away selectability based on whether picks are already locked for this player,
					//	whether the home/away teams have already been chosen, and whether or not the game has
					//	already started
					if (selectable) {
						//	Get whether the current time is before the game's time
						boolean singleSelectable = currentTime.compareTo(g.getGameDate()) < 0;
						//	Get whether the home team has not already been picked && before game time
						boolean homeSelectable = !previouslyPickedTeams.contains(g.getHomeTeam()) && singleSelectable;
						//	Get whether the away team has not already been picked && before game time
						boolean awaySelectable = !previouslyPickedTeams.contains(g.getAwayTeam()) && singleSelectable;
						
						//	Set the home/away selectability/grayed status based on the the above conditionals
						tnoBean.setHomeSelectable(homeSelectable);
						tnoBean.setAwaySelectable(awaySelectable);
						tnoBean.setHomeGrayed(!homeSelectable);
						tnoBean.setAwayGrayed(!awaySelectable);
					}
					//	If the picks have already locked, set all to gray & non-selectable
					else {
						tnoBean.setHomeSelectable(false);
						tnoBean.setAwaySelectable(false);
						tnoBean.setHomeGrayed(true);
						tnoBean.setAwayGrayed(true);
					}
					
					//	Check if the list of previously picked teams contains the home or away teams to set selectable status
					boolean homeSelectable = !previouslyPickedTeams.contains(g.getHomeTeam());
					boolean awaySelectable = !previouslyPickedTeams.contains(g.getAwayTeam());
					tnoBean.setHomeSelectable(homeSelectable);
					tnoBean.setAwaySelectable(awaySelectable);
					tnoBean.setHomeGrayed(!homeSelectable);
					tnoBean.setAwayGrayed(!awaySelectable);
					
					gameBeans.add(tnoBean);
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
	
	/** Submit action from form to lock picks for Two and Out
	 * 
	 * @param e
	 */
	public void submit(ActionEvent e) {
		//	First check whether the player is out of the TNO - if so, do nothing
		if (isOut) {
			return;
		}
		//	Loop over the tno game beans, finding the new Two and Out pick & creating it
		List<Pick> tnoPicks = pickFactory.selectPlayerPicksForType(user, NEC.TWO_AND_OUT);
		List<TeamForSeason> previouslyPickedTeams = new ArrayList<TeamForSeason>();
		for (Pick p : tnoPicks) {
			TeamForSeason pickedTeam = p.getPickedTeam(); 
			if (p.getApplicableRecord().getWeek().getWeekNumber() < week.getWeekNumber()) {
				previouslyPickedTeams.add(pickedTeam);
			}
		}
		List<TeamForSeason> tnoPicksToMake = new ArrayList<TeamForSeason>();
		for (GameBean tnoBean : gameBeans) {
			TeamForSeason homeTeam = tnoBean.getGame().getHomeTeam();
			TeamForSeason awayTeam = tnoBean.getGame().getAwayTeam();
			//	If a new pick found for the home team, create TNO pick
			if ("selectable".equals(tnoBean.getHomeSelectable()) && 
				"selected".equals(tnoBean.getHomeSelected()) && 
				!previouslyPickedTeams.contains(homeTeam)) 
			{
				tnoPicksToMake.add(homeTeam);
			}
			//	If a new pick found for the away team, create TNO pick
			else if ("selectable".equals(tnoBean.getAwaySelectable()) &&
					 "selected".equals(tnoBean.getAwaySelected()) && 
					 !previouslyPickedTeams.contains(awayTeam)) 
			{
				tnoPicksToMake.add(awayTeam);
			}
		}
		
		String growlMessage = "Message";
		String growlDetail = "Message Detail";
		Severity severity = FacesMessage.SEVERITY_INFO;
		int tnoCount = tnoPicksToMake.size();
		if (tnoCount == 1) {
			Pick p = pickFactory.createPlayerPickInWeek(user, tnoPicksToMake.get(0), week, NEC.TWO_AND_OUT, PickType.STRAIGHT_UP);
			if (p != null) {
				growlMessage = "Success!";
				growlDetail = "Week " + week.getWeekNumber() + " Two and Out Pick: " + p.getPickedTeam().getTeamCity();
				severity = FacesMessage.SEVERITY_INFO;
			}
			else {
				growlMessage = "An error occurred!";
				growlDetail = "There was a problem submitting Two and Out, please submit manually!";
				severity = FacesMessage.SEVERITY_WARN;
			}
		}
		else if (tnoCount > 1) {
			growlMessage = "Invalid Selection:";
			growlDetail = "Too many picks selected for Two And Out! - Please only make 1 selection";
			severity = FacesMessage.SEVERITY_ERROR;
		}
		else if (tnoCount < 1) {
			growlMessage = "Invalid Selection:";
			growlDetail = "No Two and Out selection made! - Please select a Two and Out team!";
			severity = FacesMessage.SEVERITY_ERROR;
		}
		
		FacesMessage message = new FacesMessage(severity, growlMessage,  growlDetail);
        FacesContext.getCurrentInstance().addMessage(null, message);
	}
}
