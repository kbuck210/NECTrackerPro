package com.nectp.beans.ejb.daos.xml;

import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.remote.daos.ConferenceFactory;
import com.nectp.beans.remote.daos.DivisionFactory;
import com.nectp.beans.remote.daos.StadiumService;
import com.nectp.beans.remote.daos.TeamFactory;
import com.nectp.beans.remote.daos.TeamForSeasonFactory;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Team;
import com.nectp.webtools.DOMParser;
import com.nectp.jpa.entities.Conference.ConferenceType;
import com.nectp.jpa.entities.Division.Region;
import com.nectp.jpa.entities.Season;

/** Team/TFS XML update helper, given the factory beans and list of XML elements, update/create the team/TFS's
 * 
 * @author Kevin C. Buckley
 * @since  1.0
 */
public class XmlTeamUpdater {

	private static final Logger log = Logger.getLogger(XmlTeamUpdater.class.getName());
	
	public static void updateTeams(DOMParser parser, TeamFactory teamFactory, TeamForSeasonFactory tfsFactory, 
			ConferenceFactory confFactory, DivisionFactory divFactory, 
			StadiumService stadiumService, List<Element> teams, Season season) {
		for (Element tm : teams) { 
			//	Get the Team franchise ID to select the correct team
			String franchise = parser.getTextSubElementByTagName(tm, "franchiseId");
			
			Integer franchiseId = null;
			try {
				franchiseId = Integer.parseInt(franchise);
			} catch (NumberFormatException e) {
				log.severe("Invalid FranchiseId number format: " + e.getMessage());
				log.severe("skipping team.");
				e.printStackTrace();
				continue;
			}
			//	From the specified franchise ID, select/create the corresponding team from the DB
			Team team = teamFactory.createTeam(franchiseId);
			
			//	Get the TFS attributes
			String abbr = parser.getTextSubElementByTagName(tm, "abbr");
			String name = parser.getTextSubElementByTagName(tm, "name");
			String city = parser.getTextSubElementByTagName(tm, "city");
			String nickname = parser.getTextSubElementByTagName(tm, "nickname");
			String conf = parser.getTextSubElementByTagName(tm, "conference");
			String div = parser.getTextSubElementByTagName(tm, "division");
			String stadiumName = parser.getTextSubElementByTagName(tm, "stadium");
			String homeHelmet = parser.getTextSubElementByTagName(tm, "homeImg");
			String awayHelmet = parser.getTextSubElementByTagName(tm, "awayImg");
			String excelName = parser.getTextSubElementByTagName(tm, "excelName");
			
			ConferenceType confType = ConferenceType.getConferenceTypeForString(conf);
			Conference conference = confFactory.createConference(confType);
			
			Region region = Region.getRegionForString(div);
			Division division = divFactory.createDivision(region, conference);
			
			Stadium stadium = null;
			try {
				stadium = stadiumService.selectStadiumByName(stadiumName);
			} catch (NoExistingEntityException e) {
				log.warning("Stadium not found!");
			}
			
			//	From the specified TFS attributes, create/update the TFS
			tfsFactory.createTeamForSeason(team, season, abbr, name, city, division, stadium, nickname, excelName, homeHelmet, awayHelmet);
		}
	}
}
