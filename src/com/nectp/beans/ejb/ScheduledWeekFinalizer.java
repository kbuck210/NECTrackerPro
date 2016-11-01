package com.nectp.beans.ejb;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

import org.w3c.dom.Element;

import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.ejb.daos.xml.XmlLiveReader;
import com.nectp.beans.remote.daos.GameFactory;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.beans.remote.daos.WeekFactory;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Game.GameStatus;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.webtools.DOMParser;

/** Scheduled Event to Finalize a Week of scoring, scheduled to occur on in-season Tuesdays at 5am
 *  On successful completion of finalizing the week, calls the report builder & sends the league the update email
 *  
 * @author Kevin C. Buckley
 * @since  1.0
 */
@Stateless
public class ScheduledWeekFinalizer {
	
	//	Default NFL.com URLs:
	//	regular season - http://www.nfl.com/liveupdate/scorestrip/ss.xml
	//	playoffs	   - http://www.nfl.com/liveupdate/scorestrip/postseason/ss.xml
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private TeamForSeasonService tfsService;
	
	@EJB
	private WeekFactory weekFactory;
	
	@EJB
	private GameFactory gameFactory;
	
	private Season currentSeason;
	
	private Week currentWeek;
	
	private Logger log;
	
	public ScheduledWeekFinalizer() {
		log = Logger.getLogger(ScheduledWeekFinalizer.class.getName());
	}
	
	/** Schedule an event to occur every Tuesday at 5am to get the final scores 
	 * and close out the week, run only during the schedule dates for the current season
	 */
	@Schedule(dayOfWeek="Tue", hour="5")
	public void finalizeWeek() {
		//	Get the current season and the dates of it's first and last games
		currentSeason = seasonService.selectCurrentSeason();
		if (currentSeason != null) {
			currentWeek = currentSeason.getCurrentWeek();
			
			//	First check whether the current date is within the same "football week" 
			//	as the current week's date (i.e. wont run in the offseason, or non-football weeks)
			boolean parse = false;
			if (currentWeek != null) {
				List<Game> games = currentWeek.getGames();
				Collections.sort(games);
				Calendar now = new GregorianCalendar();
				
				Game firstGame = games.get(0);
				Calendar gameDate = firstGame.getGameDate();
				int curYear = now.get(GregorianCalendar.YEAR);
				int gameYear = gameDate.get(GregorianCalendar.YEAR);
				int curYearWeek = now.get(GregorianCalendar.WEEK_OF_YEAR);
				int gameYearWeek = gameDate.get(GregorianCalendar.WEEK_OF_YEAR);
				//	If the current Tuesday is in the same week as the first game of this week, parse the scores
				if (curYearWeek == gameYearWeek && curYear == gameYear) {
					parse = true;
				}
				//	If the first game is not in the same week, need to account for thurs-sat games for the week previous
				else if (curYear == gameYear) {
					int gameDay = gameDate.get(GregorianCalendar.DAY_OF_WEEK);
					if (gameYearWeek == (curYearWeek - 1) && gameDay >= GregorianCalendar.THURSDAY) {
						parse = true;
					}
				}
			}
			
			if (parse) {
				String url;
				//	Get the current week number, and ping the appropriate url
				if (currentWeek.getWeekNumber() < currentSeason.getPlayoffStartWeek()) {
					url = "http://www.nfl.com/liveupdate/scorestrip/ss.xml";
				}
				else url = "http://www.nfl.com/liveupdate/scorestrip/postseason/ss.xml";
			
				boolean success = parseUrl(url);
				if (success) {
					//	TODO: generate report & send email
				}
			}
		}
	}
	
	/** Parses either the regular season or playoffs url to get the requisite information for closing out a week, 
	 *  then if no errors encountered, updates the DB with the parsed week data
	 * 
	 * @param url either the regular season or playoff URL, based on the current week of the season
	 * @return true if all of the information was parsed & updated successfully, false otherwise
	 */
	private boolean parseUrl(String url) {
		boolean success = true;
		DOMParser parser = DOMParser.newInstance(url, "gms");
		List<Element> weeks = parser.generateElementList();
		for (Element wk : weeks) {
			String weekNum = wk.getAttribute("w");
			String year = wk.getAttribute("y");
			
			Integer weekNumber = null;
			try {
				weekNumber = Integer.parseInt(weekNum);
			} catch (NumberFormatException e) {
				// TODO: log error
				success = false;
			}
			
			Week parsedWeek = null;
			Season parsedSeason = null;
			try {
				parsedSeason = seasonService.selectSeasonByYear(year);
				parsedWeek = weekFactory.selectWeekByNumberInSeason(weekNumber, parsedSeason);
			} catch (NoExistingEntityException e) {
				//	TODO: log error
				success = false;
			}
			
			//	Check that the parsed week is the current week in the season
			if (parsedWeek != null && parsedWeek.equals(currentWeek)) {
				boolean anyErrors = false;
				//	Loop through the game elements in the DOM
				for (Element g : parser.getSubElementsByTagName(wk, "g")) {
					String type = g.getAttribute("gt");
					//	If parsing the playoff games & reading games for previous weeks, skip
					if (skipByType(type, weekNumber, parsedSeason)) {
						continue;
					}
					
					//	Check that the quarter is "F" for final
					String quarter = g.getAttribute("q");
					if (!"F".equals(quarter)) {
						log.warning("Not all games completed! Can not close out week!");
						//	TODO: send email error report
						anyErrors = true;
					}
					else {
						String awayScoreStr = g.getAttribute("vs");
						String awayAbbr = g.getAttribute("v");
						String homeScoreStr = g.getAttribute("hs");
						String homeAbbr = g.getAttribute("h");
						
						Calendar gameDate = XmlLiveReader.parseDate(g);
						Integer homeScore = XmlLiveReader.parseInteger(homeScoreStr);
						Integer awayScore = XmlLiveReader.parseInteger(awayScoreStr);
						
						if (gameDate == null || homeScore == null || awayScore == null) {
							anyErrors = true;
							continue;
						}
						
						TeamForSeason homeTeam = null;
						TeamForSeason awayTeam = null;
						try {
							homeTeam = tfsService.selectTfsByAbbrSeason(homeAbbr, parsedSeason);
							awayTeam = tfsService.selectTfsByAbbrSeason(awayAbbr, parsedSeason);
						} catch (NoExistingEntityException e) {
							log.severe("Failed to parse teams for: " + homeAbbr + " and " + awayAbbr);
							anyErrors = true;
							continue;
						}
						
						Game game = null;
						try {
							game = gameFactory.selectGameByTeamsWeek(homeTeam, awayTeam, parsedWeek);
						} catch (NoExistingEntityException e) {
							log.severe("Failed to retreive game in week " + parsedWeek.getWeekNumber() + 
									" for " + homeTeam.getName() + " vs " + awayTeam.getName());
							anyErrors = true;
							continue;
						}
						
						String spread1 = null, spread2 = null;
						if (game.getSpread1() != null) {
							spread1 = game.getSpread1().toPlainString();
						}
						if (game.getSpread2() != null) {
							spread2 = game.getSpread2().toPlainString();
						}
						
						//	Call the create game method to update any attributes for the game that have changed
						gameFactory.createGameInWeek(parsedWeek, homeTeam, awayTeam, homeScore, awayScore, 
								spread1, spread2, gameDate, GameStatus.FINAL, 
								game.getHomeFavoredSpread1(), game.getHomeFavoredSpread2(), 
								"FINAL", null, false, game.getStadium());
					}
				}
				
				//	If there were no errors on the updates, finalize the week
				if (!anyErrors) {
					boolean allComplete = weekFactory.updateWeekForGameComplete(parsedWeek);
					if (!allComplete) {
						//	TODO: email error that week was not finalized
					}
				}
				else success = false;
			}
			else success = false;
		}
		
		return success;
	}
	
	/** The live updates for Playoffs include all playoff weeks, skip processing rounds not associated with this week
	 * 
	 * @param type the DOM string attribute reflecting which week of the playoffs this game represents
	 * @param weekNumber the current week number in the season
	 * @param season the current NEC season
	 * @return true to skip game if this game element is not part of this week, false otherwise
	 */
	private boolean skipByType(String type, Integer weekNumber, Season season) {
		Integer playoffStartWeek = season.getPlayoffStartWeek();
		Integer superbowlWeek = season.getSuperbowlWeek();
		
		//	If it is a regular season game, process all game information
		if ("REG".equals(type)) return false;
		else if ("WC".equals(type)) {
			return weekNumber != playoffStartWeek;
		}
		else if ("DIV".equals(type)) {
			return weekNumber != (playoffStartWeek + 1);
		}
		else if ("CON".equals(type)) {
			return weekNumber != (playoffStartWeek + 2);
		}
		else if ("PRO".equals(type)) {
			return true;
		}
		else if ("SB".equals(type)) {
			return weekNumber != superbowlWeek;
		}
		else return true;
	}
}
