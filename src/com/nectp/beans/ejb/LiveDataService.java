package com.nectp.beans.ejb;

import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.w3c.dom.Element;

import com.nectp.beans.remote.daos.GameService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.beans.remote.daos.WeekFactory;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.webtools.DOMParser;

public class LiveDataService {

	private Logger log;
	
	private Season currentSeason;
	
	private Week currentWeek;
	
	private String queryUrl;
	
	private Element weekDomElement;
	
	private LinkedList<Element> gameElements;
	
	boolean initialized;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private WeekFactory weekFactory;
	
	@EJB
	private TeamForSeasonService tfsService;
	
	@EJB
	private GameService gameService;
	
	@PostConstruct
	public void init() {
		log = Logger.getLogger(LiveDataService.class.getName());
		
		//	Get the current season from the DB, and select the current week
		currentSeason = seasonService.selectCurrentSeason();
		currentWeek = weekFactory.selectCurrentWeekInSeason(currentSeason);
		
		//	Determine which URL to use based on whether it is regular season or playoffs
		if (currentWeek.getWeekNumber() < currentSeason.getPlayoffStartWeek()) {
			queryUrl = "http://www.nfl.com/liveupdate/scorestrip/ss.xml";
		}
		else {
			//	TODO: update to playoff url
			queryUrl = "http://www.nfl.com/liveupdate/scorestrip/ss.xml";
		}
		
		//	Get the DOM Element corresponding to the week information, verifying that the DB is working with the correct week
		weekDomElement = getWeekDomElement(queryUrl);
		boolean weekVerified = verifyWeekInfo();
		if (!weekVerified) {
			log.severe("Failed to verify the current week! Aborting live updates.");
			initialized = false;
		}
		
		//	If the week was successfully verified, read the DOM elements to process the game information
		else {
			gameElements = DOMParser.getSubElementsByTagName(weekDomElement, "g");
		}
	}
	
	/** Reads the DOM specified week number, and compares it to the current week in the Season from the DB,
	 * 	if the week numbers do not match, retreive/create a week in the season corresponding to the live week, 
	 *  setting as the current week for which to read game data
	 * 
	 * @return true if the week number was read successfully, and the current week was retrieved/updated
	 */
	private boolean verifyWeekInfo() {
		String weekNumStr = weekDomElement.getAttribute("w");
		Integer weekNumber = null;
		try {
			weekNumber = Integer.parseInt(weekNumStr);
		} 
		catch (NumberFormatException e) {
			log.severe("Failed to parse week number. Can not update games!");
			log.severe(e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		//	Check whether the current week in the season is not the read week number
		if (currentWeek != null && currentWeek.getWeekNumber() != weekNumber) {
			log.info("The DB current week does not match the live data, updating the current week.");
			
			//	Change the value of the current week, creating the next week if necessary
			Week newWeek = weekFactory.createWeekInSeason(weekNumber, currentSeason);
			if (newWeek != null) {
				boolean updated = seasonService.updateCurrentWeek(newWeek);
				if (updated) {
					currentWeek = newWeek;
				}
				else {
					log.severe("Week updated but could not update database! Aborting data parsing.");
					return false;
				}
			}
		}
		
		return true;
	}
	
	private void updateGames() {
		int weekNumber = currentWeek.getWeekNumber();
		
		//	Loop over the XML DOM game elements to parse
		for (Element g : gameElements) {
			String time = g.getAttribute("t");
			String type = g.getAttribute("gt");
			String awayScoreStr = g.getAttribute("vs");
			String awayAbbr = g.getAttribute("v");
			String homeScoreStr = g.getAttribute("hs");
			String homeAbbr = g.getAttribute("h");
			String quarter = g.getAttribute("q");
			String day = g.getAttribute("d");
			String date = g.getAttribute("eid");

			//  Skip redundant playoff entries for parsed playoff weeks
			if (skipByType(type, weekNumber, currentSeason)) {
				continue;
			}

			//  Get the home/away teams by their abbreviations
			TeamForSeason homeTeam = tfsService.selectTfsByAbbr(homeAbbr, currentSeason);
			if (homeTeam == null) {
				log.severe("Failed to get home team by abbr: " + homeAbbr);
				continue;
			}

			TeamForSeason awayTeam = tfsService.selectTfsByAbbr(awayAbbr, currentSeason);
			if (awayTeam == null) {
				log.severe("Failed to get away team by abbr: " + awayAbbr);
				continue;
			}

			//  Get the game for these teams by week in season
			Game game = GameDB.selectGameByTeamsWeekInSeason(homeTeam, awayTeam, week, season);
			if (game == null) {
				Logger.getLogger(GameDayService.class.getName())
				.log(Level.SEVERE, "Failed to find game for teams in week: " + awayAbbr + " at " + homeAbbr);
				continue;
			}

			boolean gameUpdated = false;

			//	Check that the game date & time haven't changed
			GregorianCalendar gameDate = game.getGameDate();
			GregorianCalendar parsedDate = parseGameDate(day, time);
			if (parsedDate != null && gameDate != null) {
				int hour, min, dayOfWeek;
				hour = parsedDate.get(GregorianCalendar.HOUR);
				min = parsedDate.get(GregorianCalendar.MINUTE);
				dayOfWeek = parsedDate.get(GregorianCalendar.DAY_OF_WEEK);

				if (gameDate.get(GregorianCalendar.DAY_OF_WEEK) != dayOfWeek) {
					gameDate.set(GregorianCalendar.DAY_OF_WEEK, dayOfWeek);
					game.setGameDate(gameDate);
					gameUpdated = true;
				}
				if (gameDate.get(GregorianCalendar.HOUR) != hour) {
					gameDate.set(GregorianCalendar.HOUR, hour);
					game.setGameDate(gameDate);
					gameUpdated = true;
				}
				if (gameDate.get(GregorianCalendar.MINUTE) != min) {
					gameDate.set(GregorianCalendar.MINUTE, min);
					game.setGameDate(gameDate);
					gameUpdated = true;
				}
			}

			//  Check whether the score has changed
			Integer homeScore = null;
			try { homeScore = Integer.parseInt(homeScoreStr); }
			catch (NumberFormatException ex) {
				Logger.getLogger(GameDayService.class.getName())
				.log(Level.WARNING, "Failed to read home score, score left unchanged: " + ex.getMessage(), ex);
			}
			if (homeScore != null && homeScore != game.getHomeScore()) {
				game.setHomeScore(homeScore);
				gameUpdated = true;
			}

			Integer awayScore = null;
			try { awayScore = Integer.parseInt(awayScoreStr); }
			catch (NumberFormatException ex) {
				Logger.getLogger(GameDayService.class.getName())
				.log(Level.WARNING, "Failed to read away score, score left unchanged: " + ex.getMessage(), ex);
			}
			if (awayScore != null && awayScore != game.getAwayScore()) {
				game.setAwayScore(awayScore);
				gameUpdated = true; 
			}

			//	Get the time remaining in the game, or whether the game is completed
			//	TODO: evaluate quarter string in-game;

			if (gameUpdated) {
				GameDB.updateGame(game);
			}
		}

	}
	
	private Element getWeekDomElement(String queryUrl) {
		LinkedList<Element> elements = new LinkedList<Element>();
		DOMParser parser = DOMParser.newInstance(queryUrl, "gms");
        if (parser != null) {
            elements = parser.generateElementList();
        }
        
        //	Should only be 1 gms element, return as dom Root
        return elements.get(0);
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

        //  If it is a regular season game, process all game information
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
            return weekNumber != (playoffStartWeek + 4);
        }
        else return true;
    }
}
