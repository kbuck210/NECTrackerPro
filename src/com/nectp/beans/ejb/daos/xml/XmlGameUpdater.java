package com.nectp.beans.ejb.daos.xml;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.remote.daos.GameFactory;
import com.nectp.beans.remote.daos.StadiumService;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.jpa.entities.Game.GameStatus;
import com.nectp.jpa.entities.Season;
import com.nectp.webtools.DOMParser;

/** Game XML update helper, given the factory bean and list of XML elements, update/create the games
 * 
 * @author Kevin C. Buckley
 * @since  1.0
 */
public class XmlGameUpdater {

	private static final Logger log = Logger.getLogger(XmlGameUpdater.class.getName());
	
	/** Update Games given a DOMParser and a list of XML elements
	 * 
	 * @param parser the DOMParser instance
	 * @param games a list of XML elements with qualified name 'game'
	 * @param gameFactory a GameFactory DAO
	 * @param stadiumService a StadiumService DAO
	 * @param tfsService a TeamForSeasonService DAO
	 * @param week the Week entity representing the week these games belong to
	 * @param season the Season entity representing the season these games belong to
	 */
	public static void updateGames(DOMParser parser, List<Element> games, GameFactory gameFactory, 
			StadiumService stadiumService, TeamForSeasonService tfsService, Week week, Season season) {
		for (Element g : games){
			String homeTeam = parser.getTextSubElementByTagName(g, "homeTeam");
			String homeScoreStr = parser.getTextSubElementByTagName(g, "homeScore");
			String homeFavored1 = parser.getTextSubElementByTagName(g, "homeFavored1");
			String homeFavored2 = parser.getTextSubElementByTagName(g, "homeFavored2");
			String spread1 = parser.getTextSubElementByTagName(g, "spread1");
			String spread2 = parser.getTextSubElementByTagName(g, "spread2");
			String awayTeam = parser.getTextSubElementByTagName(g, "awayTeam");
			String awayScoreStr = parser.getTextSubElementByTagName(g, "awayScore");
			String gameDate = parser.getTextSubElementByTagName(g, "date");
			String kickoff = parser.getTextSubElementByTagName(g, "time");
			String status = parser.getTextSubElementByTagName(g, "status");
			String stadiumName = parser.getTextSubElementByTagName(g, "stadium");
			
			//	Get the home/away teams
			TeamForSeason home = tfsService.selectTfsByAbbrSeason(homeTeam, season);
			if (home == null) {
				log.severe("Failed to retrieve home team for: " + homeTeam + " can not create game!");
				continue;
			}
			TeamForSeason away = tfsService.selectTfsByAbbrSeason(awayTeam, season);
			if (away == null) {
				log.severe("Failed to retrieve away team for: " + awayTeam + " can not create game!");
				continue;
			}
			
			Integer homeScore = null;
			try {
				homeScore = Integer.parseInt(homeScoreStr);
			} catch (NumberFormatException e) {
				log.warning("Invalid format for home score: will not be set - default to zero");
				homeScore = 0;
				
			}
			Integer awayScore = null;
			try {
				awayScore = Integer.parseInt(awayScoreStr);
			} catch (NumberFormatException e) {
				log.warning("Invalid format for away score: will not be set - default to zero");
				awayScore = 0;
			}
			
			Boolean isHomeFavored1 = homeFavored1 != null ? homeFavored1.toUpperCase().equals("Y") : null;
			Boolean isHomeFavored2 = homeFavored2 != null ? homeFavored2.toUpperCase().equals("Y") : null;
			
			GregorianCalendar gameCal = null;
			if (gameDate != null && kickoff != null) {
				String[] dateParts = gameDate.split("\\.");
				String[] timeParts = kickoff.split(":");
				int month = GregorianCalendar.JANUARY;
				int day = 1, year = 1900;
				int hour = 0, minute = 0;
				boolean pm;
				if (dateParts.length == 3) {
					try {
						month = Integer.parseInt(dateParts[0]) - 1;	//	Month is zero-based
						day = Integer.parseInt(dateParts[1]);
						year = Integer.parseInt(dateParts[2]);
					} catch (NumberFormatException e) {
						log.severe("Invalid game date format: " + gameDate + " can not create game!");
						continue;
					}
				}
				if (timeParts.length == 3) {
					pm = timeParts[2].toUpperCase().equals("PM");
					try {
						minute = Integer.parseInt(timeParts[1]);
						hour = Integer.parseInt(timeParts[0]);
					} catch (NumberFormatException e) {
						log.severe("Invalid game time format: " + kickoff + " can not create game!");
						continue;
					}
					if (pm) hour += 12;	//	set game date time to be HOUR_OF_DAY format
				}
				
				gameCal = new GregorianCalendar(year, month, day, hour, minute);
			}
			
			
			
			Stadium stadium = null;
			try {
				stadium = stadiumService.selectStadiumByName(stadiumName);
			} catch (NoExistingEntityException e) {
				log.warning("Stadium not found!");
			}
			
			GameStatus gameStatus = GameStatus.getGameStatusForString(status);
			String timeRemaining = gameStatus != null ? gameStatus.name() : null;
			
			gameFactory.createGameInWeek(week, home, away, homeScore, awayScore, spread1, spread2, gameCal, gameStatus, isHomeFavored1, isHomeFavored2, timeRemaining, null, false, stadium);
		}
	}
}
