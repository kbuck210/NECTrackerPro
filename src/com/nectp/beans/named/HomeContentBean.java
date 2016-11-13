package com.nectp.beans.named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.ApplicationState;
import com.nectp.beans.ejb.LiveDataService;
import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.PickService;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.PlayerService;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.WeekService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.Pick.PickType;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Week.WeekStatus;

@Named(value="homeContentBean")
@RequestScoped
public class HomeContentBean implements Serializable {
	private static final long serialVersionUID = 8380054229801489568L;

	@EJB
	private SeasonService seasonService;
	
	@EJB
	private WeekService weekService;
	
	@EJB
	private PlayerService playerService;
	
	@EJB
	private PlayerForSeasonService pfsService;
	
	@EJB
	private PickService pickService;
	
	@EJB
	private RecordService recordService;
	
	@EJB
	private LiveDataService liveDataService;
	
	@Inject
	private ApplicationState appState;
	
	private Player user;
	
	private PlayerForSeason userInstance;
	
	private List<Pick> userPicks;
	
	private Season currentSeason;
	
	private Week displayWeek;
	
	private boolean renderPlayerPicks = false;
	
	private String playerPickHeadline;
	
	private String playerRecord;
	
	private boolean renderOtherGames = true;
	
	private String otherGamesHeadline;
	
	private String displayedWeekHeadline;
	
	private List<GameBean> playerPickBeans;
	
	private List<GameBean> otherGameBeans;
	
	public HomeContentBean() {
	}
	
	@PostConstruct
	public void init() {
		currentSeason = seasonService.selectCurrentSeason();
		displayWeek = weekService.selectCurrentWeekInSeason(currentSeason);

		user = appState.getUser();
		
		updateDisplayedWeek(displayWeek);
		
		if (displayWeek.getWeekStatus() == WeekStatus.ACTIVE) {
			List<Game> gamesInWeek = displayWeek.getGames();
			Collections.sort(gamesInWeek);
			Calendar firstGameDate = gamesInWeek.get(0).getGameDate();
			Calendar now = new GregorianCalendar();
			if (now.compareTo(firstGameDate) > 1) {
				liveDataService.updateGames();
			}
		}
		
//		if (user != null) {
//			userInstance = pfsService.selectPlayerInSeason(user, currentSeason);
//			userPicks = pickService.selectPlayerPicksForWeekForType(userInstance, displayWeek, displayWeek.getSubseason().getSubseasonType());
//			//	If the user has any picks for the week, make sure the picks area is displayed
//			if (!userPicks.isEmpty()) {
//				renderPlayerPicks = true;
//				//	If the user has selected all games, do not render the 'other games' section
//				if (userPicks.size() == displayWeek.getGames().size()) {
//					renderOtherGames = false;
//				}
//				else renderOtherGames = true;
//			}
//			else renderPlayerPicks = false;
//			
//			setPlayerPickHeadline();
//			setPlayerRecord();
//		}
//		setOtherGamesHeadline();
//		setDisplayedWeekHeadline();
	}
	
	public void updateDisplayedWeek(Week newDisplayedWeek) {
		if (newDisplayedWeek != null) {
			displayWeek = newDisplayedWeek;
		}
		
		playerPickBeans = new ArrayList<GameBean>();
		otherGameBeans = new ArrayList<GameBean>();
		
		if (user != null) {
			userInstance = pfsService.selectPlayerInSeason(user, currentSeason);
			NEC pickType = displayWeek.getSubseason().getSubseasonType();
			userPicks = pickService.selectPlayerPicksForWeekForType(userInstance, displayWeek, pickType);
			//	If the user has any picks for the week, make sure the picks area is displayed
			if (!userPicks.isEmpty()) {
				renderPlayerPicks = true;
				//	If the user has selected all games, do not render the 'other games' section
				if (userPicks.size() == displayWeek.getGames().size()) {
					renderOtherGames = false;
				}
				else renderOtherGames = true;
				//	Create game beans for player picks
				List<Game> pickedGames = new ArrayList<Game>();
				for (Pick p : userPicks) {
					GameBean bean = new GameBean();
					Game game = p.getGame();
					bean.setPlayer(userInstance);
					bean.setGameDisplayType(NEC.SEASON);
					bean.setGame(game);
					bean.setHomeSelectable(false);
					bean.setAwaySelectable(false);
					TeamForSeason homeTeam = game.getHomeTeam();
					RecordAggregator homeRagg = recordService.getOverallRecordThroughWeekForAtfs(homeTeam, displayWeek, NEC.SEASON, false);
					bean.setHomeRecord(homeRagg.toString(PickType.STRAIGHT_UP));

					TeamForSeason awayTeam = game.getAwayTeam();
					RecordAggregator awayRagg = recordService.getOverallRecordThroughWeekForAtfs(awayTeam, displayWeek, NEC.SEASON, false);
					bean.setAwayRecord(awayRagg.toString(PickType.STRAIGHT_UP));

					playerPickBeans.add(bean);
					pickedGames.add(game);
				}
				List<Game> otherGames = new ArrayList<Game>(displayWeek.getGames());
				otherGames.removeAll(pickedGames);
				//	Create game beans for other games
				for (Game g : otherGames) {
					GameBean bean = new GameBean();
					bean.setGameDisplayType(NEC.SEASON);
					bean.setGame(g);
					bean.setHomeSelectable(false);
					bean.setAwaySelectable(false);
					TeamForSeason homeTeam = g.getHomeTeam();
					RecordAggregator homeRagg = recordService.getOverallRecordThroughWeekForAtfs(homeTeam, displayWeek, NEC.SEASON, false);
					bean.setHomeRecord(homeRagg.toString(PickType.STRAIGHT_UP));
					
					TeamForSeason awayTeam = g.getAwayTeam();
					RecordAggregator awayRagg = recordService.getOverallRecordThroughWeekForAtfs(awayTeam, displayWeek, NEC.SEASON, false);
					bean.setAwayRecord(awayRagg.toString(PickType.STRAIGHT_UP));
					
					otherGameBeans.add(bean);
				}
			}
			else {
				renderPlayerPicks = false;
				//	Create game beans for other games
				for (Game g : displayWeek.getGames()) {
					GameBean bean = new GameBean();
					bean.setGameDisplayType(NEC.SEASON);
					bean.setGame(g);
					bean.setHomeSelectable(false);
					bean.setAwaySelectable(false);
					TeamForSeason homeTeam = g.getHomeTeam();
					RecordAggregator homeRagg = recordService.getOverallRecordThroughWeekForAtfs(homeTeam, displayWeek, NEC.SEASON, false);
					bean.setHomeRecord(homeRagg.toString(PickType.STRAIGHT_UP));
					
					TeamForSeason awayTeam = g.getAwayTeam();
					RecordAggregator awayRagg = recordService.getOverallRecordThroughWeekForAtfs(awayTeam, displayWeek, NEC.SEASON, false);
					bean.setAwayRecord(awayRagg.toString(PickType.STRAIGHT_UP));
					
					otherGameBeans.add(bean);
				}
			}
			
			setPlayerPickHeadline();
			setPlayerRecord();
		}
		else {
			//	Create game beans for other games
			for (Game g : displayWeek.getGames()) {
				GameBean bean = new GameBean();
				bean.setGameDisplayType(NEC.SEASON);
				bean.setGame(g);
				bean.setHomeSelectable(false);
				bean.setAwaySelectable(false);
				TeamForSeason homeTeam = g.getHomeTeam();
				RecordAggregator homeRagg = recordService.getOverallRecordThroughWeekForAtfs(homeTeam, displayWeek, NEC.SEASON, false);
				bean.setHomeRecord(homeRagg.toString(PickType.STRAIGHT_UP));

				TeamForSeason awayTeam = g.getAwayTeam();
				RecordAggregator awayRagg = recordService.getOverallRecordThroughWeekForAtfs(awayTeam, displayWeek, NEC.SEASON, false);
				bean.setAwayRecord(awayRagg.toString(PickType.STRAIGHT_UP));

				otherGameBeans.add(bean);
			}
		}
		setOtherGamesHeadline();
		setDisplayedWeekHeadline();
	}
	
	public boolean isRenderPlayerPicks() {
		return renderPlayerPicks;
	}
	
	public boolean isRenderOtherGames() {
		return renderOtherGames;
	}
	
	public String getPlayerPickHeadline() {
		return playerPickHeadline;
	}
	
	private void setPlayerPickHeadline() {
		if (userInstance != null && currentSeason != null && displayWeek != null) {
			playerPickHeadline = "NEC " + currentSeason.getSeasonNumber()
				+ " - " + userInstance.getNickname() + "'s Picks";
		}
	}
	
	public String getPlayerRecord() {
		return playerRecord;
	}
	
	private void setPlayerRecord() {
		if (userInstance != null && currentSeason != null && displayWeek != null) {
			NEC subseasonType = displayWeek.getSubseason().getSubseasonType();
			RecordAggregator ragg = recordService.getAggregateRecordForAtfsForType(userInstance, subseasonType, true);
			playerRecord = "(" + ragg.getRawWins() + "-" + ragg.getRawLosses();
			if (ragg.getRawTies() > 0) {
				playerRecord += "-" + ragg.getRawTies();
			}
			playerRecord += ")";
		}
	}
	
	public String getOtherGamesHeadline() {
		return otherGamesHeadline;
	}
	
	private void setOtherGamesHeadline() {
		if (currentSeason != null && displayWeek != null) {
			otherGamesHeadline = "NEC " + currentSeason.getSeasonNumber() + " - ";
			if (user != null) {
				otherGamesHeadline += "Other ";
			}
			otherGamesHeadline += "Week " + displayWeek.getWeekNumber() + " Games ";
		}
	}
	
	public String getDisplayedWeekHeadline() {
		return displayedWeekHeadline;
	}
	
	private void setDisplayedWeekHeadline() {
		if (displayWeek != null) {
			List<Game> games =	displayWeek.getGames();
			if (!games.isEmpty()) {
				Collections.sort(games);
				Calendar gameDate = games.get(0).getGameDate();
				int month = gameDate.get(GregorianCalendar.MONTH);
				if (month == 0) {
					month = 12;
				}
				
				displayedWeekHeadline = month 
						+ "/" + gameDate.get(GregorianCalendar.DAY_OF_MONTH) 
						+ "/" + gameDate.get(GregorianCalendar.YEAR);
			}
		}
	}
	
	public List<GameBean> getPlayerPickBeans() {
		return playerPickBeans;
	}
	
	public List<GameBean> getOtherGameBeans() {
		return otherGameBeans;
	}
}
