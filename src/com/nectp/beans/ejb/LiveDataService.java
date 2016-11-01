package com.nectp.beans.ejb;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
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

@Stateless
public class LiveDataService {

	private Logger log;
	
	private Season currentSeason;
	
	private Week currentWeek;
	
	private String queryUrl;
	
	private Element weekDomElement;
	
	private List<Element> gameElements;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private WeekFactory weekFactory;
	
	@EJB
	private TeamForSeasonService tfsService;
	
	@EJB
	private GameFactory gameFactory;
	
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
			queryUrl = "http://www.nfl.com/liveupdate/scorestrip/postseason/ss.xml";
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
			log.info("The DB current week does not match the live data, skipping live data read.");
			return false;
		}
		
		return true;
	}
	
	/** Update Games method calls on the specified query URL, and parses the game information, 
	 *  updating where necessary.
	 * 
	 * @return true if all of the games were parsed and updated without issue, false otherwise
	 */
	public boolean updateGames() {
		//	Get the DOM Element corresponding to the week information, verifying that the DB is working with the correct week
		DOMParser parser = DOMParser.newInstance(queryUrl, "gms");
		List<Element> wks = parser.generateElementList();
		weekDomElement = wks.get(0);
		boolean weekVerified = verifyWeekInfo();
		if (!weekVerified) {
			log.severe("Failed to verify the current week! Aborting live updates.");
			return false;
		}

		//	If the week was successfully verified, read the DOM elements to process the game information
		gameElements = parser.getSubElementsByTagName(weekDomElement, "g");
		if (gameElements.isEmpty()) {
			log.warning("No games found to update!");
			return false;
		}
		
		int weekNumber = currentWeek.getWeekNumber();
		
		//	Loop over the XML DOM game elements to parse
		boolean anyErrors = false;
		for (Element g : gameElements) {
			String type = g.getAttribute("gt");
			String awayScoreStr = g.getAttribute("vs");
			String awayAbbr = g.getAttribute("v");
			String homeScoreStr = g.getAttribute("hs");
			String homeAbbr = g.getAttribute("h");
			String quarter = g.getAttribute("q");
			String timeRemaining = g.getAttribute("k");
			String red_zone = g.getAttribute("rz");
			String possession = g.getAttribute("p");

			//  Skip redundant playoff entries for parsed playoff weeks
			if (skipByType(type, weekNumber, currentSeason)) {
				continue;
			}
			
			TeamForSeason homeTeam = null;
			TeamForSeason awayTeam = null;
			try {
				homeTeam = tfsService.selectTfsByAbbrSeason(homeAbbr, currentSeason);
				awayTeam = tfsService.selectTfsByAbbrSeason(awayAbbr, currentSeason);
			} catch (NoExistingEntityException e) {
				log.severe("Failed to retrieve home/away teams for: " + homeAbbr + ", " + awayAbbr);
				log.severe(e.getMessage());
				anyErrors = true;
				continue;
			}
			
			Game game = null;
			try {
				game = gameFactory.selectGameByTeamsWeek(homeTeam, awayTeam, currentWeek);
			} catch (NoExistingEntityException e) {
				log.severe("No game found for " + homeAbbr + " vs " + awayAbbr 
						+ " in week " + currentWeek.getWeekNumber());
				log.severe(e.getMessage());
				anyErrors = true;
				continue;
			}

			//	Get the game date from the live data
			Calendar parsedDate = XmlLiveReader.parseDate(g);

			//  Check whether the score has changed
			Integer homeScore = XmlLiveReader.parseInteger(homeScoreStr);
			Integer awayScore = XmlLiveReader.parseInteger(awayScoreStr);
			
			if (parsedDate == null || homeScore == null || awayScore == null) {
				anyErrors = true;
				continue;
			}
			
			GameStatus newStatus = parseGameStatus(quarter);
			String parsedTimeRemaining = getTimeRemainingString(quarter, timeRemaining, newStatus);
		
			boolean redZone = !"0".equals(red_zone);
			
			String spread1 = null, spread2 = null;
			if (game.getSpread1() != null) {
				spread1 = game.getSpread1().toPlainString();
			}
			if (game.getSpread2() != null) {
				spread2 = game.getSpread2().toPlainString();
			}
			
			Game updated = gameFactory.createGameInWeek(currentWeek, homeTeam, awayTeam, homeScore, 
					awayScore, spread1, spread2, parsedDate, 
					newStatus, game.getHomeFavoredSpread1(), game.getHomeFavoredSpread2(), 
					parsedTimeRemaining, possession, redZone, game.getStadium());
			if (updated == null) {
				anyErrors = true;
			}
		}
		return !anyErrors;
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
    
    /** Parse the game's status from the quarter string
	 * 
	 * @param quarter the quarter attribute from the game XML element, displaying the current quarter of the game
	 * @return the GameStatus enum value for the corresponding status, either PREGAME, ACTIVE, HALFTIME, OVERTIME, or FINAL
	 */
	private GameStatus parseGameStatus(String quarter) {
		if (quarter == null) return null;
		else if (quarter.equals("P")) return GameStatus.PREGAME;
		else if (quarter.equals("H")) return GameStatus.HALFTIME;
		else if (quarter.startsWith("F")) return GameStatus.FINAL;
		else return GameStatus.ACTIVE;
	}
    
    /** Based on the status of the game, and the time remaining & quarter strings, get the text for the time remaining display
	 * 
	 * @param quarter the quarter attribute representing the current quarter for the specifed game (VALUES: 'P', numerical quarter number, 'H', 'F', or 'FOT')
	 * @param timeRemaining the string representing the time remaining in the specified quarter
	 * @param status the GameStatus enum value for the current state of the game
	 * @return a String representing the current quarter & time remaining, or state of the game. Null if status not recognized or failed to parse quarter
	 */
	private String getTimeRemainingString(String quarter, String timeRemaining, GameStatus status) {
		switch(status) {
		case ACTIVE:
			String timeRemainingStr = null;
			if (quarter != null && timeRemaining !=null) {
				if (quarter.equals("1")) timeRemainingStr = timeRemaining + " 1st";
				else if (quarter.equals("2")) timeRemainingStr = timeRemaining + " 2nd";
				else if (quarter.equals("3")) timeRemainingStr = timeRemaining + " 3rd";
				else if (quarter.equals("4")) timeRemainingStr = timeRemaining + " 4th";
				else { 
					Integer otPeriod;
					try { otPeriod = Integer.parseInt(quarter); }
					catch(NumberFormatException e) {
						log.warning("Failed to parser quarter: " + e.getMessage());
						return null;
					}
					int otNum = otPeriod - 4;
					timeRemainingStr = timeRemaining + " " + otNum + "OT";
				}
			}
			return timeRemainingStr;
		case HALFTIME:
			return "Halftime";
		case FINAL:
			return "Final";
		default:
			return null;
		}
	}
}
