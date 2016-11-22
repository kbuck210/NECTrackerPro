package com.nectp.beans.named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.ApplicationState;
import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.GameService;
import com.nectp.beans.remote.daos.PickFactory;
import com.nectp.beans.remote.daos.RecordFactory;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.Pick.PickType;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Record;
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
	private TeamForSeasonService tfsService;
	
	@EJB
	private RecordFactory recordFactory;
	
	@EJB
	private PickFactory pickFactory;
	
	@EJB
	private GameService gameService;
	
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
			
			RecordAggregator tnoRecord = recordFactory.getAggregateRecordForAtfsForType(user, NEC.TWO_AND_OUT, false);
			log.info("Current losses: " + tnoRecord.getRawLosses() + " acceptable: " + season.getTnoAcceptableLosses());
			renderTnoPicks = tnoRecord.getRawLosses() < season.getTnoAcceptableLosses();
			
			List<Pick> previousTnoPicks = new ArrayList<Pick>();
			if (renderTnoPicks) {
				tnoPickHeadline = "Two And Out:";
				previousTnoPicks = pickFactory.selectPlayerPicksForType(user, NEC.TWO_AND_OUT);
			}
			
			//	Create the game beans
			log.info("Reinitializing GameBeans");
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
		
		//	Check whether the individual game is selectable
		boolean singleSelectable = selectable && currentTime.compareTo(g.getGameDate()) < 0;
		//	Set the selectable & grayed properties
		gameBean.setHomeSelectable(singleSelectable);
		gameBean.setAwaySelectable(singleSelectable);
		gameBean.setHomeGrayed(!singleSelectable);
		gameBean.setAwayGrayed(!singleSelectable);

		//	If checking for previously picked teams (TNO case)
		if (previousPicks != null) {
			gameBean.setRowId("tnoId_" + g.getGameId());
			
			//	Check the list of previous picks to make previous picks unselectable
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
		
		//	Get the records for the home & away teams
		RecordAggregator homeRagg = recordFactory.getAggregateRecordForAtfsForType(homeTeam, NEC.SEASON, false);
		gameBean.setHomeRecord(homeRagg.toString(PickType.STRAIGHT_UP));

		RecordAggregator awayRagg = recordFactory.getAggregateRecordForAtfsForType(awayTeam, NEC.SEASON, false);
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
		NEC ssType = week.getSubseason().getSubseasonType();
		
		//	Loop over both the list of GameBeans, and if rendered the TnoBeans
		List<TeamForSeason> selectedTeams = getSelectedTeams(gameBeans);
		
		//	Validate that the correct number of picks were submitted
		int minPicks = season.getMinPicks() == null ? gameBeans.size() : season.getMinPicks();
		int maxPicks = season.getMaxPicks() == null ? gameBeans.size() : season.getMaxPicks();
		//	Check that there were enough picks made
		if (selectedTeams.size() < minPicks) {
			FacesMessage notEnoughPicks = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
					"ERROR!", "Not enough picks selected - min " + minPicks + " picks");
			FacesContext.getCurrentInstance().addMessage(null, notEnoughPicks);
		}
		//	Check that not too many picks made
		else if (selectedTeams.size() > maxPicks) {
			FacesMessage tooManyPicks = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
					"ERROR!", "Too many picks selected - max " + maxPicks + " picks");
			FacesContext.getCurrentInstance().addMessage(null, tooManyPicks);
		}
		//	If enough picks selected, make picks & display results
		else {
			List<Pick> createdPicks = createPicks(selectedTeams, ssType);
			if (createdPicks.size() == selectedTeams.size()) {
				FacesMessage success = new FacesMessage(FacesMessage.SEVERITY_INFO, 
						"Success!", "Picked " + selectedTeams.size() + " teams");
				FacesContext.getCurrentInstance().addMessage(null, success);
			}
			else {
				FacesMessage pickCreateError = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
						"ERROR!", "Picks failed to create - please submit manually");
				FacesContext.getCurrentInstance().addMessage(null, pickCreateError);
			}
		}
		
		//	If eligible to make TNO picks, process the selected game bean
		if (renderTnoPicks) {
			List<TeamForSeason> selectedTnos = getSelectedTeams(tnoBeans);
			
			//	Validate that not more than one team was selected for TNO picks
			if (selectedTnos.size() > 1) {
				FacesMessage tooManyPicks = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!", "Only one Two and Out pick allowed!");
				FacesContext.getCurrentInstance().addMessage(null, tooManyPicks);
			}
			else if (selectedTnos.isEmpty()) {
				FacesMessage noTno = new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning!", "No Two and Out Selected!");
				FacesContext.getCurrentInstance().addMessage(null, noTno);
			}
			else {
				List<Pick> createdPick = createPicks(selectedTnos, NEC.TWO_AND_OUT);
				if (createdPick.size() == 1) {
					FacesMessage tnoSuccess = new FacesMessage(FacesMessage.SEVERITY_INFO, 
							"Success!", "Two and Out selected - " + selectedTnos.get(0).getTeamAbbr());
					FacesContext.getCurrentInstance().addMessage(null, tnoSuccess);
					log.info("Selected TNO Pick: " + selectedTnos.get(0).getTeamCity());
				}
				else {
					FacesMessage tnoFail = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							"ERROR!", "Two and Out failed, please submit manually");
					FacesContext.getCurrentInstance().addMessage(null, tnoFail);
				}
			}
		}
	}
	
	/** Given the list of selected teams and the category for the picks, create picks for the player in the week
	 * 
	 * @param selectedTeams the list of teams clicked on the make picks screen
	 * @param pickFor the category for the picks (subseason or TNO)
	 * @return a list of created (or modified) Pick entities, based on the user submission
	 */
	private List<Pick> createPicks(List<TeamForSeason> selectedTeams, NEC pickFor) {
		List<Pick> createdPicks = new ArrayList<Pick>();
		PickType pickType = null;
		for (TeamForSeason team : selectedTeams) {
			Game game = null;
			try {
				game = gameService.selectGameByTeamWeek(team, week);
			} catch (NoExistingEntityException e) {
				log.severe("Failed to find pick-game for " + team.getTeamAbbr() + " in week " + week.getWeekNumber());
				continue;
			}
			if (pickType == null) {
				if (pickFor == NEC.TWO_AND_OUT) pickType = PickType.STRAIGHT_UP;
				else pickType = getPickType(game);
			}
			Record applicableRecord = recordFactory.createWeekRecordForAtfs(week, user, pickFor);
			Pick p = pickFactory.createPlayerPickForRecord(applicableRecord, game, team, pickType);
			if (p != null) {
				createdPicks.add(p);
			}
			
			//	Check whether the game is a MNF or TNT game (assuming regular picks)
			if (pickFor != NEC.TWO_AND_OUT) {
				int dayOfWeek = game.getGameDate().get(GregorianCalendar.DAY_OF_WEEK);
				if (dayOfWeek == GregorianCalendar.MONDAY) {
					Record mnfRecord = recordFactory.createWeekRecordForAtfs(week, user, NEC.MNF);
					Pick mnfPick = pickFactory.createPlayerPickForRecord(mnfRecord, game, team, pickType);
					if (mnfPick == null) {
						FacesMessage mnfError = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!", "MNF Pick failed - please submit manually");
						FacesContext.getCurrentInstance().addMessage(null, mnfError);
					}
				}
				else if (dayOfWeek == GregorianCalendar.THURSDAY) {
					Record tntRecord = recordFactory.createWeekRecordForAtfs(week, user, NEC.TNT);
					Pick tntPick = pickFactory.createPlayerPickForRecord(tntRecord, game, team, pickType);
					if (tntPick == null) {
						FacesMessage tntError = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!", "TNT Pick failed - please submit manually");
						FacesContext.getCurrentInstance().addMessage(null, tntError);
					}
				}
			}
		}
		return createdPicks;
	}
	
	/** Given a Team, get whether the week's games have spread2, and if so, if it's before Saturday
	 * 
	 * @param g the Game to determine the pick type
	 * @return SPREAD2 if a spread2 is available and the current time is before SATURDAY, otherwise return SPREAD1
	 */
	private PickType getPickType(Game g) {
		if (g.getSpread2() != null) {
			Calendar currentTime = new GregorianCalendar();
			int dayOfWeek = currentTime.get(GregorianCalendar.DAY_OF_WEEK);
			if (dayOfWeek > GregorianCalendar.MONDAY && dayOfWeek < GregorianCalendar.FRIDAY) {
				return PickType.SPREAD2;
			}
		}
		return PickType.SPREAD1;
	}
	
	/** Based on the specified list of GameBeans (either regular or TNO) - get a list of selected TFS
	 *  
	 * @param beans either the list of regular or TNO GameBeans
	 * @return a list of TeamForSeason objects representing the selected teams
	 */
	private List<TeamForSeason> getSelectedTeams(List<GameBean> beans) {
		List<TeamForSeason> selectedTeams = new ArrayList<TeamForSeason>();
		for (GameBean gb : beans) {
			String selectedAbbr = null;
			if ("selected".equals(gb.getHomeSelected())) {
				selectedAbbr = gb.getHomeAbbr();
			}
			else if ("selected".equals(gb.getAwaySelected())) {
				selectedAbbr = gb.getAwayAbbr();
			}
			
			if (selectedAbbr != null) {
				TeamForSeason tfs = null;
				try {
					tfs = tfsService.selectTfsByAbbrSeason(selectedAbbr, season);
				} catch (NoExistingEntityException e) {
					log.warning("No team found for abbr: " + selectedAbbr);
					continue;
				}
				
				selectedTeams.add(tfs);
			}
		}
		
		return selectedTeams;
	}
	
	/** Handles the action for clicking on a Team helmet, passing the clicked teamAbbr & rowId
	 * 
	 * @param selectedTeam the Team Abbr for the clicked TFS
	 * @param rowId the row ID from the clicked helmet - specifies the type of pick
	 */
	public void selectTeam(String selectedTeam, String rowId) {
		log.info("Selected team: " + selectedTeam);
		log.info("RowId: " + rowId);
		TeamForSeason team = null;
		try {
			team = tfsService.selectTfsByAbbrSeason(selectedTeam, season);
		} catch (NoExistingEntityException e) {
			log.severe("No team found for: " + selectedTeam + " can not create pick!");
			FacesMessage selectError = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!", "Failed to find selected team - pick not saved");
			FacesContext.getCurrentInstance().addMessage(null, selectError);
			return;
		}
		
		//	Check the row ID to determine whether it's a TNO row or regular Row
		if (rowId != null && rowId.startsWith("tnoId_")) {
			processTnoPick(team);
		}
		else {
			processRegularPick(team);
		}
	}
	
	/** If the pick is a TNO pick, handle un-selecting the selectable options & selecting the picked team,
	 *  if the clicked team is already selected, un-select it without selecting anything else
	 *  
	 * @param team the TeamForSeason picked for this TNO pick
	 */
	private void processTnoPick(TeamForSeason team) {
		log.info("Picked TNO team: " + team.getTeamAbbr());
		//	Get the gameBean from the tnoBeans representing the selected team
		for (GameBean gb : tnoBeans) {
			//	If the selected team is the home team, update the game beans
			if (gb.getHomeAbbr().equals(team.getTeamAbbr())) {
				log.info("found home team - selectable: " + gb.getHomeSelectable());
				if ("selectable".equals(gb.getHomeSelectable())) {
					//	If the clicked team is the selected team already, un-select it
					if ("selected".equals(gb.getHomeSelected())) {
						gb.setHomeSelected(false);
					}
					//	Otherwise, un-select all of the selectable gamebeans & select the home team
					else {
						unselectAllSelectables(tnoBeans);
						gb.setHomeSelected(true);
					}
				}
			}
			//	If the selected team is the away team, update the game beans
			else if (gb.getAwayAbbr().equals(team.getTeamAbbr())) {
				log.info("found away team - selectable: " + gb.getAwaySelectable());
				if ("selectable".equals(gb.getAwaySelectable())) {
					//	If the clicked team is the selected team already, un-select it
					if ("selected".equals(gb.getAwaySelected())) {
						gb.setAwaySelected(false);
					}
					//	Otherwise, un-select all of the selectable gamebeans & select the away team
					else {
						unselectAllSelectables(tnoBeans);
						gb.setAwaySelected(true);
					}
				}
			}
		}
	}
	
	/** Given the specified list of GameBeans, sets the selected property to false
	 * 
	 * @param beans the specified list of GameBeans
	 */
	private void unselectAllSelectables(List<GameBean> beans) {
		for (GameBean gb : beans) {
			if ("selectable".equals(gb.getHomeSelectable()) && 
				"selected".equals(gb.getHomeSelected())) {
				gb.setHomeSelected(false);
			}
			if ("selectable".equals(gb.getAwaySelectable()) && 
				"selected".equals(gb.getAwaySelected())) {
				gb.setAwaySelected(false);
			}
		}
	}
	
	/** Handles the selection of a regular pick, un-selecting the opponent if the opponent is selected,
	 *  if the clicked team is already selected, unselect it without selecting anything else
	 * 
	 * @param team the selected TeamForSeason object
	 */
	private void processRegularPick(TeamForSeason team) {
		//	Get the gameBean representing the selected team
		for (GameBean gb : gameBeans) {
			//	If the selected team is the home team, update game bean
			if (gb.getHomeAbbr().equals(team.getTeamAbbr())) {
				if ("selectable".equals(gb.getHomeSelectable())) {
					//	If the home team was already selected, un-select it
					if ("selected".equals(gb.getHomeSelected())) {
						gb.setHomeSelected(false);
					}
					//	Otherwise select the home team, and set the away team to unselected
					else {
						gb.setHomeSelected(true);
						gb.setAwaySelected(false);
						log.info("set home selected: " + gb.getHomeSelected());
					}
				}
				else {
					FacesMessage unselectable = new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning:", team.getTeamCity() + " is ineligible");
					FacesContext.getCurrentInstance().addMessage(null, unselectable);
				}
			}
			//	If the selected team is the away team
			else if (gb.getAwayAbbr().equals(team.getTeamAbbr())) {
				log.info(gb.getAwayAbbr() + " Selectable: " + gb.getAwaySelectable());
				if ("selectable".equals(gb.getAwaySelectable())) {
					//	If the away team is already selected, un-select it
					if ("selected".equals(gb.getAwaySelected())) {
						gb.setAwaySelected(false);
					}
					//	Otherwise select the away team and unselect the home team
					else {
						gb.setAwaySelected(true);
						gb.setHomeSelected(false);
						log.info("set away selected: " + gb.getAwaySelected());	
					}
				}
				else {
					FacesMessage unselectable = new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning:", team.getTeamCity() + " is ineligible");
					FacesContext.getCurrentInstance().addMessage(null, unselectable);
				}
			}
		}
	}
}
