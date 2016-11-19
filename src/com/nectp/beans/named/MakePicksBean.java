package com.nectp.beans.named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.ApplicationState;
import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.PickFactory;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.Pick.PickType;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Named(value="makePicksBean")
@ViewScoped
public class MakePicksBean implements Serializable {
	private static final long serialVersionUID = 3882278829489499064L;

	private boolean showPickForm = false;
	private boolean renderTnoPicks = true;
	
	private String regularPicksTitle;
	private String makePicksHeadline;
	private String tnoPickHeadline;
	private String weekHeadline;
	
	private List<GameBean> gameBeans;
	private List<GameBean> tnoBeans;
	
	private PlayerForSeason user;
	private Season season;
	private Week week;
	
	@EJB
	private RecordService recordService;
	
	@EJB
	private PickFactory pickFactory;
	
	@Inject
	private ApplicationState appState;
	
	private Logger log;
	
	public MakePicksBean() {
		log = Logger.getLogger(MakePicksBean.class.getName());
	}
	
	@PostConstruct
	public void init() {
		season = appState.getCurrentSeason();
		if (season == null) {
			FacesMessage seasonError = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!", "No current season defined - can't create picks");
			FacesContext.getCurrentInstance().addMessage(null, seasonError);
			return;
		}
		
		user = appState.getUserInstance();
		if (user == null) {
			FacesMessage userError = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!", "Not logged in - can not select picks.");
			FacesContext.getCurrentInstance().addMessage(null, userError);
			return;
		}
		else showPickForm = true;
		
		week = season.getCurrentWeek();
		
		//	Check that the current week is defined
		if (week == null) {
			FacesMessage weekError = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!", "No current week defined - can't create picks");
			FacesContext.getCurrentInstance().addMessage(null, weekError);
		}
		else {
			//	Create the title strings
			int weekNumber = week.getWeekNumber();
			
			//	Get the date of the first game in the week
			int month=-1, day=-1, year=-1;
			List<Game> games =	week.getGames();
			if (!games.isEmpty()) {
				Collections.sort(games);
				Calendar gameDate = games.get(0).getGameDate();
				month = gameDate.get(GregorianCalendar.MONTH);
				if (month == 0) {
					month = 12;
				}
				day = gameDate.get(GregorianCalendar.DAY_OF_MONTH);
				year = gameDate.get(GregorianCalendar.YEAR);
			}
			
			//	Create the title strings for the main tab title & the game container titles
			createTitleStrings(weekNumber, month, day, year);
			
			RecordAggregator tnoRecord = recordService.getAggregateRecordForAtfsForType(user, NEC.TWO_AND_OUT, false);
			log.info("Current losses: " + tnoRecord.getRawLosses() + " acceptable: " + season.getTnoAcceptableLosses());
			renderTnoPicks = tnoRecord.getRawLosses() < season.getTnoAcceptableLosses();
			
			List<Pick> previousTnoPicks = new ArrayList<Pick>();
			if (renderTnoPicks) {
				tnoPickHeadline = "Two And Out:";
				previousTnoPicks = pickFactory.selectPlayerPicksForType(user, NEC.TWO_AND_OUT);
			}
			
			//	Create the game beans
			gameBeans = new ArrayList<GameBean>();
			tnoBeans = new ArrayList<GameBean>();
			
			//	Fill the pick beans
			Calendar currentTime = new GregorianCalendar();
			boolean selectable = true;
			NEC pickFor = week.getSubseason().getSubseasonType();
			List<Pick> playerPicksForWeek = pickFactory.selectPlayerPicksForWeekForType(user, week, pickFor);
			if (!playerPicksForWeek.isEmpty()) {
				selectable = !playerPicksForWeek.get(0).getPickLocked();
			}
			
			//	Check whether any of the games in the week have a spread2
			boolean hasSpread2 = false;
			for (Game g : week.getGames()) {
				if (g.getSpread2() != null) {
					hasSpread2 = true;
					break;
				}
			}
			PickType pickType = PickType.SPREAD1;
			if (hasSpread2 && currentTime.get(GregorianCalendar.DAY_OF_WEEK) < GregorianCalendar.SATURDAY) {
				pickType = PickType.SPREAD2;
			}
			
			for (Game g : week.getGames()) {
				GameBean pickBean = createGameBean(g, pickFor, selectable, currentTime, pickType, null);
				gameBeans.add(pickBean);
				
				if (renderTnoPicks) {
					GameBean tnoBean = createGameBean(g, NEC.TWO_AND_OUT, selectable, currentTime, PickType.STRAIGHT_UP, previousTnoPicks);
					tnoBeans.add(tnoBean);
				}
			}
		}
	}
	
	/** Creates a Game Bean POJO for displaying a selectable matchup
	 * 
	 * @param g the Game represented by this display bean
	 * @param type the type of Pick this display is for
	 * @param selectable true if the game is still selectable, false otherwise
	 * @param pickType PickType representing the spread type to display
	 * @param currentTime the current time the page is rendered
	 * @param previousPicks null for regular picks case, for TNO list contains previous TNO picks
	 * @return a created GameBean instance representing a displayable game
	 */
	private GameBean createGameBean(Game g, NEC type, boolean selectable, Calendar currentTime, PickType pickType, List<Pick> previousPicks) {
		//	Create Beans for regular game picks
		GameBean gameBean = new GameBean();
		gameBean.setPlayer(user);
		gameBean.setSpreadType(pickType);
		gameBean.setGame(g);
		
		//	If the pick type is straight up (i.e. two and out), set to pick single team
		if (pickType == PickType.STRAIGHT_UP) {
			gameBean.setSinglePick(true);
		}
		
		TeamForSeason homeTeam = g.getHomeTeam();
		TeamForSeason awayTeam = g.getAwayTeam();

		//	If picks can still be made, determine whether this game has already locked, or if the team has already been picked
		if (selectable && previousPicks == null) {
			boolean singleSelectable = currentTime.compareTo(g.getGameDate()) < 0;
			gameBean.setHomeSelectable(singleSelectable);
			gameBean.setAwaySelectable(singleSelectable);
			gameBean.setHomeGrayed(!singleSelectable);
			gameBean.setAwayGrayed(!singleSelectable);
		}
		//	If the game is physically selectable, but checking for previously picked teams (TNO case)
		else if (selectable) {
			gameBean.setRowId("tnoId_" + g.getGameId());
			for (Pick p : previousPicks) {
				TeamForSeason winner = p.getGame().getWinner();
				TeamForSeason pickedTeam = p.getPickedTeam();
				//	If the home team was already picked, set unselectable & greyed, set pick image to whether or not the team won it's pick
				if (pickedTeam.equals(homeTeam)) {
					gameBean.setHomeSelectable(false);
					gameBean.setHomeSelected(true);
					gameBean.setHomeGrayed(true);
					gameBean.setHomePickImage(winner == null || winner.equals(pickedTeam));
				}
				//	If the away team was already picked, set unselectable & greyed, set pick image to whether or not the team won it's pick
				else if (pickedTeam.equals(awayTeam)) {
					gameBean.setAwaySelectable(false);
					gameBean.setAwaySelected(true);
					gameBean.setAwayGrayed(true);
					gameBean.setAwayPickImage(winner == null || winner.equals(pickedTeam));
				}
			}
		}
		//	If the game is not selectable, set unselectable & greyed
		else { 
			gameBean.setHomeSelectable(false);
			gameBean.setAwaySelectable(false);
			gameBean.setHomeGrayed(true);
			gameBean.setAwayGrayed(true);
		}
		
		//	Get the records for the home & away teams
		RecordAggregator homeRagg = recordService.getAggregateRecordForAtfsForType(homeTeam, NEC.SEASON, false);
		gameBean.setHomeRecord(homeRagg.toString(PickType.STRAIGHT_UP));

		RecordAggregator awayRagg = recordService.getAggregateRecordForAtfsForType(awayTeam, NEC.SEASON, false);
		gameBean.setAwayRecord(awayRagg.toString(PickType.STRAIGHT_UP));
		
		return gameBean;
	}
	
	/** Sets the component titles based on the current week & game times
	 * 
	 * @param weekNumber the current week number
	 * @param month the month of the first game of the week
	 * @param day the day in the month of the first game of the week
	 * @param year the year in which the first game of the week is played
	 */
	private void createTitleStrings(int weekNumber, int month, int day, int year) {
		if (day == -1) return;
		
		if (weekNumber < season.getPlayoffStartWeek()) {
			regularPicksTitle = "Week " + weekNumber + ":";
			NEC subseasonType = week.getSubseason().getSubseasonType();
			makePicksHeadline = "Select Picks - " + subseasonType.toString() + ": ";
			weekHeadline = "Week " + weekNumber + " (" + month + "/" + day + "/" + year + ")";
		}
		else if (weekNumber < season.getSuperbowlWeek()) {
			switch(weekNumber) {
			case 18:
				regularPicksTitle = "Wild Card:";
				makePicksHeadline = "Select Picks - Playoffs: ";
				weekHeadline = "Wild Card Weekend (" + month + "/" + day + "/" + year + ")";
				break;
			case 19:
				regularPicksTitle = "Divisional:";
				makePicksHeadline = "Select Picks - Playoffs: ";
				weekHeadline = "Divisional Round (" + month + "/" + day + "/" + year + ")";
				break;
			case 20:
				regularPicksTitle = "Conf Champ:";
				makePicksHeadline = "Select Picks - Playoffs: ";
				weekHeadline = "Conference Championship (" + month + "/" + day + "/" + year + ")";
				break;
			default:
				regularPicksTitle = "Week " + weekNumber + ":";
				NEC subseasonType = week.getSubseason().getSubseasonType();
				makePicksHeadline = "Select Picks - " + subseasonType.toString() + ": ";
				weekHeadline = "Week " + weekNumber + " (" + month + "/" + day + "/" + year + ")";
			}
		}
		else {
			regularPicksTitle = "Superbowl:";
			makePicksHeadline = "Select Picks - Superbowl: ";
			weekHeadline = "NEC" + season.getSeasonNumber() + " Championship - " + season.getSeasonYear();
		}
	}
	
	/** Returns whether or not the pick form should be displayed
	 * 
	 * @return false if not all of the required information to show the display is provided, true otherwise
	 */
	public boolean getShowPickForm() {
		return showPickForm;
	}
	
	/** Returns whether or not the tab for selecting Two and Out picks should be displayed
	 * 
	 * @return false if the user is already out, true otherwise
	 */
	public boolean getRenderTnoPicks() {
		log.info("Rendering tno picks: " + renderTnoPicks);
		return renderTnoPicks;
	}
	
	/** Returns the string to display for the regular weekly picks tab
	 * 
	 * @return the title for the weekly picks tab
	 */
	public String getRegularPicksTitle() {
		 return regularPicksTitle;
	}
	
	/** Returns the string to display for the regular weekly picks component headline
	 * 
	 * @return the string for the weekly picks component headline, including the subseason type
	 */
	public String getMakePicksHeadline() {
		return makePicksHeadline;
	}
	
	/** Returns the string to display for the two and out component headline
	 * 
	 * @return the string for the two and out component headline
	 */
	public String getTnoPickHeadline() {
		return tnoPickHeadline;
	}
	
	/** Returns the string to display for the component subheadline
	 * 
	 * @return the string to display for the component subheadline, including the week number and date
	 */
	public String getWeekHeadline() {
		return weekHeadline;
	}
	
	/** Gets the list of GameBean objects defined for the regular weekly picks
	 * 
	 * @return the list of GameBean POJOs used to display each of the possible weekly picks
	 */
	public List<GameBean> getGameBeans() {
		return gameBeans;
	}
	
	/** Gets the list of GameBean objects defined for the Two and Out picks
	 * 
	 * @return the list of GameBean POJOs used to display each of the possible two and out picks
	 */
	public List<GameBean> getTnoBeans() {
		return tnoBeans;
	}
	
	/** Submit ActionListener
	 * 
	 * @param event
	 */
	public void submit(ActionEvent event) {
		UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
		
		NEC ssType = week.getSubseason().getSubseasonType();
		
		//	Get the player pick form if rendered
		if (showPickForm) {
			//	Loop over each of the games in the week, getting the UIComponent representing the game row (and TNO row where applicable)
			for (Game g : week.getGames()) {
				long gameId = g.getGameId();
				UIComponent gameRow = view.findComponent("gameId_" + gameId);
				if (gameRow == null) {
					FacesMessage gameError = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!", "Game id: " + gameId + " not found! can not create picks.");
					FacesContext.getCurrentInstance().addMessage(null, gameError);
					break;
				}
			
				//	Send the UIComponent & game to create picks for the selected Teams;
				makePicksForRow(gameRow, g, ssType);
				
				UIComponent tnoRow = view.findComponent("tnoId_" + gameId);
				//	Check that the TNO row was found (if being rendered)
				if (tnoRow == null && renderTnoPicks) {
					FacesMessage tnoError = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!", "TNO id: " + gameId + " not found! can not create TNO pick.");
					FacesContext.getCurrentInstance().addMessage(null, tnoError);
					break;
				}
				//	If TNO row was found, send it to create picks
				else if (renderTnoPicks) {
					makePicksForRow(tnoRow, g, NEC.TWO_AND_OUT);
				}
				
			}
			
		}
	}
	
	private GameBean getGameBeanForTeam(List<GameBean> beans, TeamForSeason team, boolean home) {
		for (GameBean gb : beans) {
			if (home && gb.getHomeCity().equals(team.getTeamCity())) {
				return gb;
			}
			else if (!home && gb.getAwayCity().equals(team.getTeamCity())) {
				return gb;
			}
		}
		return null;
	}
	
	private Pick makePicksForRow(UIComponent gameRow, Game g, NEC picksFor) {
		//	If the row(s) were successfully found, create the picks
		Map<String, Object> gameRowAtts = gameRow.getAttributes();
		String homeSelected = (String) gameRowAtts.get("homeSelected");
		String awaySelected = (String) gameRowAtts.get("awaySelected");
		
		//	Check if the home team (and only the home team) was selected
		if (homeSelected != null && "selected".equals(homeSelected) 
				&& (awaySelected == null || "".equals(awaySelected))) {
			TeamForSeason homeTeam = g.getHomeTeam();
			GameBean gb = getGameBeanForTeam(gameBeans, homeTeam, true);
			PickType pickType = PickType.SPREAD1;
			if (gb != null) {
				pickType = gb.getSpreadType();
			}
			else {
				log.severe("Could not match the selection to a game bean! can not create pick.");
				FacesMessage gameBeanError = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!", "Could not match selection to game, can't create pick!");
				FacesContext.getCurrentInstance().addMessage(null, gameBeanError);
				return null;
			}
			log.info(user.getNickname() + " picked " + homeTeam.getTeamCity() + " in week " + week.getWeekNumber());
//			Pick pick = pickFactory.createPlayerPickInWeek(user, homeTeam, week, picksFor, pickType);
//			if (pick == null) {
//				log.severe("Failure to create pick for " + user.getNickname() + " for " + homeTeam.getNickname() + " in week " + week.getWeekNumber());
//				FacesMessage pickCreateFail = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!", "Failed to create pick in week!");
//				FacesContext.getCurrentInstance().addMessage(null, pickCreateFail);
//				break;
//			}
		}
		//	Otherwise check if the away team (and only the away team) was selected
		else if (awaySelected != null && "selected".equals(awaySelected) 
				&& (homeSelected == null || "".equals(homeSelected))) {
			TeamForSeason awayTeam = g.getAwayTeam();
			GameBean gb = getGameBeanForTeam(gameBeans, awayTeam, false);
			PickType pickType = PickType.SPREAD1;
			if (gb != null) {
				pickType = gb.getSpreadType();
			}
			else {
				log.severe("Could not match the selection to a game bean! can not create pick.");
				FacesMessage gameBeanError = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!", "Could not match selection to game, can't create pick!");
				FacesContext.getCurrentInstance().addMessage(null, gameBeanError);
				return null;
			}
			log.info(user.getNickname() + " picked " + awayTeam.getTeamCity() + " in week " + week.getWeekNumber());
//			Pick pick = pickFactory.createPlayerPickInWeek(user, awayTeam, week, picksFor, pickType);
//			if (pick == null) {
//				log.severe("Failure to create pick for " + user.getNickname() + " for " + awayTeam.getNickname() + " in week " + week.getWeekNumber());
//				FacesMessage pickCreateFail = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!", "Failed to create pick in week!");
//				FacesContext.getCurrentInstance().addMessage(null, pickCreateFail);
//				break;
//			}
		}
		
//		return pick;
		return null;
	}
}
