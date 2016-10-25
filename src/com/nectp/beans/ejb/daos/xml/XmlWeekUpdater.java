package com.nectp.beans.ejb.daos.xml;

import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.remote.daos.GameFactory;
import com.nectp.beans.remote.daos.StadiumService;
import com.nectp.beans.remote.daos.SubseasonService;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.beans.remote.daos.WeekFactory;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Week.WeekStatus;
import com.nectp.webtools.DOMParser;

/** Week XML update helper, given the factory bean and list of XML elements, update/create the weeks
 * 
 * @author Kevin C. Buckley
 * @since  1.0
 */
public class XmlWeekUpdater {

	private static final Logger log = Logger.getLogger(XmlWeekUpdater.class.getName());
	
	/** Update Games given a DOMParser and a list of XML elements
	 * 
	 * @param parser the DOMParser instance
	 * @param weeks a list of XML elements with qualified name 'week' 
	 * @param weekFactory a WeekFactory DAO
	 * @param gameFactory a GameFactory DAO
	 * @param stadiumService a StadiumService DAO
	 * @param tfsService a TeamForSeason DAO
	 * @param subseasonService a SubseasonService DAO
	 * @param season the Season entity representing the seasons in which these weeks belong
	 */
	public static void updateWeeks(DOMParser parser, List<Element> weeks, WeekFactory weekFactory, 
			GameFactory gameFactory, StadiumService stadiumService, TeamForSeasonService tfsService, 
			SubseasonService subseasonService, Season season) {
		for (Element wk : weeks) {
			String wkNum = wk.getAttribute("number");
			String current = wk.getAttribute("current");
			String ss = wk.getAttribute("ss");
			String status = wk.getAttribute("status");
			
			Integer weekNumber = null;
			try {
				weekNumber = Integer.parseInt(wkNum);
			} catch (NumberFormatException e) {
				log.severe("Invalid week number format: " + e.getMessage());
				log.severe("skipping week");
				e.printStackTrace();
				continue;
			}
			
			Boolean isCurrent = Boolean.parseBoolean(current);
			
			NEC subseasonType = NEC.getNECForName(ss);
			Subseason subseason = null;
			try {
				subseason = subseasonService.selectSubseasonInSeason(subseasonType, season);
			} catch (NoExistingEntityException e) {
				log.severe("No subseason found for: " + ss + " can not add week!");
				continue;
			}
			
			WeekStatus wkStatus = WeekStatus.getWeekStatusForString(status);
			
			//	Get/Create the week for the corresponding week number
			Week week = weekFactory.createWeekInSeason(weekNumber, subseason, season, wkStatus, isCurrent);
			
			List<Element> games = parser.getSubElementsByTagName(wk, "game");
			XmlGameUpdater.updateGames(parser, games, gameFactory, stadiumService, tfsService, week, season);
		}
	}
	
}
