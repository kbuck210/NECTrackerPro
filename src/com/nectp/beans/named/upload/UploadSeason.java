package com.nectp.beans.named.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.w3c.dom.Element;

import com.nectp.beans.ejb.daos.xml.XmlPlayerUpdater;
import com.nectp.beans.ejb.daos.xml.XmlPrizeUpdater;
import com.nectp.beans.ejb.daos.xml.XmlSubseasonUpdater;
import com.nectp.beans.ejb.daos.xml.XmlTeamUpdater;
import com.nectp.beans.ejb.daos.xml.XmlWeekUpdater;
import com.nectp.beans.named.FileUploadImpl;
import com.nectp.beans.remote.daos.ConferenceFactory;
import com.nectp.beans.remote.daos.DivisionFactory;
import com.nectp.beans.remote.daos.EmailFactory;
import com.nectp.beans.remote.daos.GameFactory;
import com.nectp.beans.remote.daos.PlayerFactory;
import com.nectp.beans.remote.daos.PlayerForSeasonFactory;
import com.nectp.beans.remote.daos.PrizeFactory;
import com.nectp.beans.remote.daos.PrizeForSeasonFactory;
import com.nectp.beans.remote.daos.SeasonFactory;
import com.nectp.beans.remote.daos.StadiumService;
import com.nectp.beans.remote.daos.SubseasonFactory;
import com.nectp.beans.remote.daos.TeamFactory;
import com.nectp.beans.remote.daos.TeamForSeasonFactory;
import com.nectp.beans.remote.daos.WeekFactory;
import com.nectp.jpa.entities.Season;
import com.nectp.webtools.DOMParser;

@Named(value="seasonUpload")
@RequestScoped
public class UploadSeason extends FileUploadImpl {
	private static final long serialVersionUID = -6846892764373561471L;

	private DOMParser parser;
	
	@Inject
	private SeasonFactory seasonFactory;
	
	@Inject
	private SubseasonFactory subseasonFactory;
	
	@Inject
	private PlayerFactory playerFactory;
	
	@Inject
	private PlayerForSeasonFactory pfsFactory;
	
	@Inject
	private EmailFactory emailFactory;
	
	@Inject
	private TeamFactory teamFactory;
	
	@Inject
	private TeamForSeasonFactory tfsFactory;
	
	@Inject
	private ConferenceFactory confFactory;
	
	@Inject
	private DivisionFactory divFactory;
	
	@Inject
	private PrizeFactory prizeFactory;
	
	@Inject
	private PrizeForSeasonFactory pzfsFactory;
	
	@Inject
	private StadiumService stadiumService;
	
	@Inject
	private WeekFactory weekFactory;
	
	@Inject
	private GameFactory gameFactory;
	
	private Season season;
	
	private Logger log;
	
	public UploadSeason() {
		log = Logger.getLogger(UploadSeason.class.getName());
	}
	
	@Override
	public void upload() {
		if (file != null) {
			//	Get the inputStream from the uploaded file
			InputStream iStream = null;
			try {
				iStream = file.getInputstream();
			} catch (IOException e) {
				log.severe("IOException getting input stream: " + e.getMessage());
				e.printStackTrace();
			}
			
			if (iStream != null) {
				//	Parse the file for Players, Teams, Prizes and Weeks
				parser = DOMParser.newInstance(iStream);
				
				Integer seasonNum = parseSeason(parser.getRootElement());
				if (seasonNum != null) {
					boolean insert = false;
					season = seasonFactory.selectById(seasonNum);
					//	If the season does not exist, create a new season
					if (season == null) {
						season = new Season();
						insert = true;
					}
					
					parser.setQualifiedNodeName("subseasons");
					List<Element> elements = parser.generateElementList();
					parseSubseasons(elements);
					
					parser.setQualifiedNodeName("players");
					elements = parser.generateElementList();
					parsePlayers(elements);
					
					parser.setQualifiedNodeName("teams");
					elements = parser.generateElementList();
					parseTeams(elements);
					
					parser.setQualifiedNodeName("prizes");
					elements = parser.generateElementList();
					parsePrizes(elements);
					
					parser.setQualifiedNodeName("weeks");
					elements = parser.generateElementList();
					parseWeeks(elements);
					
					if (insert) {
						seasonFactory.insert(season);
					}
					else {
						seasonFactory.update(season);
					}
				}
			}	
		}
	}
	
	/** Parse the 'nec' attribute from the document root element, which provides the season number
	 * 
	 * @param root the root XML element in this document
	 * @return the parsed season number as an Integer, or null on error
	 */
	private Integer parseSeason(Element root) {
		String seasonNumber = root.getAttribute("nec");
		Integer seasonNum = null;
		try {
			seasonNum = Integer.parseInt(seasonNumber);
		} catch (NumberFormatException e) {
			log.severe("Failed to read season number! Can not create/update season: " + e.getMessage());
			e.printStackTrace();
		}
		
		return seasonNum;
	}
	
	/** From the 'subseasons' XML root, get the 'subseason' elements and update Subseasons for this season
	 * 
	 * @param elements the List of Elements that represent the 'subseasons' XML root
	 */
	private void parseSubseasons(List<Element> elements) {
		//	NOTE: typically only 1 'subseasons' element - so not usually O(n^2) time
		for (Element p : elements) {
			//	Get the sub-element list for individual players
			List<Element> subseasons = parser.getSubElementsByTagName(p, "subseason");
			XmlSubseasonUpdater.updateSubseasons(subseasonFactory, subseasons, season);
		}
	}
	
	/** From the 'players' XML root, get the 'player' elements and update Players & PFS for this season
	 * 
	 * @param elements the List of Elements that represents the 'players' XML root
	 */
	private void parsePlayers(List<Element> elements) {
		//	NOTE: typically only 1 'players' element - so not usually O(n^2) time
		for (Element p : elements) {
			//	Get the sub-element list for individual players
			List<Element> players = parser.getSubElementsByTagName(p, "player");
			XmlPlayerUpdater.updatePlayers(parser, playerFactory, pfsFactory, emailFactory, players, season);
		}
	}
	
	/** From the 'teams' XML root, get the 'team' elements and update Teams & TFS for this season
	 * 
	 * @param elements the list of Elements that represents the 'teams' XML root
	 */
	private void parseTeams(List<Element> elements) {
		//	NOTE: typically only 1 'teams' element - so not usually O(n^2) time
		for (Element t : elements) {
			//	Get the sub-element list for individual teams
			List<Element> teams = parser.getSubElementsByTagName(t, "team");
			XmlTeamUpdater.updateTeams(parser, teamFactory, tfsFactory,
					confFactory, divFactory, stadiumService, teams, season);
		}
	}
	
	/** From the 'prizes' XML root, get the 'prize' elements and update Prize & PzFS for this season
	 * 
	 * @param elements the list of Elements that represent the 'prizes' XML root
	 */
	private void parsePrizes(List<Element> elements) {
		//	NOTE: typically only 1 'prizes' element - so not usually O(n^2) time
		for (Element t : elements) {
			//	Get the sub-element list for individual prizes
			List<Element> prizes = parser.getSubElementsByTagName(t, "prize");
			XmlPrizeUpdater.updatePrizes(parser, prizeFactory, pzfsFactory,
					subseasonFactory, playerFactory, pfsFactory, prizes, season);
		}
	}
	
	/** From the 'weeks' XML root, get the 'weeks' elements and update Weeks & Games for this season
	 * 
	 * @param elements the list of Elements that represent the 'weeks' XML root
	 */
	private void parseWeeks(List<Element> elements) {
		//	NOTE: typically only 1 'weeks' element - so not usually O(n^2) time
		for (Element t : elements) {
			//	Get the sub-element list for individual weeks
			List<Element> weeks = parser.getSubElementsByTagName(t, "prize");
			XmlWeekUpdater.updateWeeks(parser, weeks, weekFactory, gameFactory,
					stadiumService, tfsFactory, subseasonFactory, season);
		}
	}
}
