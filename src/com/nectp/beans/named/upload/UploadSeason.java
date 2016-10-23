package com.nectp.beans.named.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.w3c.dom.Element;

import com.nectp.beans.ejb.daos.xml.XmlPlayerUpdater;
import com.nectp.beans.ejb.daos.xml.XmlPrizeUpdater;
import com.nectp.beans.ejb.daos.xml.XmlSubseasonUpdater;
import com.nectp.beans.ejb.daos.xml.XmlTeamUpdater;
import com.nectp.beans.ejb.daos.xml.XmlWeekUpdater;
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

@Named(value="uploadSeasons")
@ViewScoped
public class UploadSeason extends FileUploadImpl {
	private static final long serialVersionUID = -6846892764373561471L;

	private DOMParser parser;
	
	@EJB
	private SeasonFactory seasonFactory;
	
	@EJB
	private SubseasonFactory subseasonFactory;
	
	@EJB
	private PlayerFactory playerFactory;
	
	@EJB
	private PlayerForSeasonFactory pfsFactory;
	
	@EJB
	private EmailFactory emailFactory;
	
	@EJB
	private TeamFactory teamFactory;
	
	@EJB
	private TeamForSeasonFactory tfsFactory;
	
	@EJB
	private ConferenceFactory confFactory;
	
	@EJB
	private DivisionFactory divFactory;
	
	@EJB
	private PrizeFactory prizeFactory;
	
	@EJB
	private PrizeForSeasonFactory pzfsFactory;
	
	@EJB
	private StadiumService stadiumService;
	
	@EJB
	private WeekFactory weekFactory;
	
	@EJB
	private GameFactory gameFactory;
	
	private Season season;
	
	private Logger log;
	
	public UploadSeason() {
		log = Logger.getLogger(UploadSeason.class.getName());
	}
	
	@Override
	public void upload(FileUploadEvent event) {
		files.add(event.getFile());
	}
	
	@Override
	public void submit() {
		for (UploadedFile file : files) {
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
					Element seasonRoot = parser.getRootElement();
					Integer seasonNum = parseSeason(seasonRoot);
					if (seasonNum != null) {
						season = seasonFactory.selectById(seasonNum);
						//	If the season does not exist, create a new season
						if (season == null) {
							season = createSeasonFromAttributes(seasonNum, seasonRoot);
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
	
	/** Parses the other attributes on the season root element to set the new season info
	 * 
	 * @param seasonNum the integer season number of the season to create
	 * @param seasonRoot the root XML element in this document
	 */
	private Season createSeasonFromAttributes(Integer seasonNum, Element seasonRoot) {
		String seasonYear = seasonRoot.getAttribute("year");
		String currentStr = seasonRoot.getAttribute("current");
		String minPicksStr = seasonRoot.getAttribute("minPicks");
		String maxPicksStr = seasonRoot.getAttribute("maxPicks");
		String playoffStart = seasonRoot.getAttribute("playoffStart");
		String secondStart = seasonRoot.getAttribute("secondHalfStart");
		String superbowlWk = seasonRoot.getAttribute("superbowlWeek");
		String winValStr = seasonRoot.getAttribute("winValue");
		String lossValStr = seasonRoot.getAttribute("lossValue");
		String tieValStr = seasonRoot.getAttribute("tieValue");
		String tnoLosses = seasonRoot.getAttribute("tnoLosses");
		
		boolean current = Boolean.parseBoolean(currentStr);
		Integer minPicks = null;
		if (minPicksStr != null) {
			try {
				minPicks = Integer.parseInt(minPicksStr);
			} catch(NumberFormatException e) {
				log.warning("Invalid format for minimum picks! Needs to be manually set.");
				log.warning(e.getMessage());
			}
		}
		Integer maxPicks = null;
		if (maxPicksStr != null) {
			try {
				maxPicks = Integer.parseInt(maxPicksStr);
			} catch (NumberFormatException e) {
				log.warning("Invalid format for maximum picks! Needs to be manually set.");
				log.warning(e.getMessage());
			}
		}
		Integer secondHalfStart = null;
		if (secondStart != null) {
			try {
				secondHalfStart = Integer.parseInt(secondStart);
			} catch (NumberFormatException e) {
				log.warning("Invalid format for second half start week! Needs to be manually set.");
				log.warning(e.getMessage());
			}
		}
		Integer playoffStartWeek = null;
		if (playoffStart != null) {
			try {
				playoffStartWeek = Integer.parseInt(playoffStart);
			} catch (NumberFormatException e) {
				log.warning("Invalid format for playoff start week! Needs to be manually set.");
				log.warning(e.getMessage());
			}
		}
		Integer superbowlWeek = null;
		if (superbowlWk != null) {
			try {
				superbowlWeek = Integer.parseInt(superbowlWk);
			} catch (NumberFormatException e) {
				log.warning("Invalid format for superbowl week! Needs to be manually set.");
				log.warning(e.getMessage());
			}
		}
		Integer winValue = null;
		if (winValStr != null) {
			try {
				winValue = Integer.parseInt(winValStr);
			} catch (NumberFormatException e) {
				log.warning("Invalid format for win value! Needs to be manually set.");
				log.warning(e.getMessage());
			}
		}
		Integer lossValue = null;
		if (lossValStr != null) {
			try {
				lossValue = Integer.parseInt(lossValStr);
			} catch (NumberFormatException e) {
				log.warning("Invalid format for loss value! Needs to be manually set.");
				log.warning(e.getMessage());
			}
		}
		Integer tieValue = null;
		if (tieValStr != null) {
			try {
				tieValue = Integer.parseInt(tieValStr);
			} catch (NumberFormatException e) {
				log.warning("Invalid format for tie value! Needs to be manually set.");
				log.warning(e.getMessage());
			}
		}
		Integer tnoAcceptableLosses = null;
		if (tnoLosses != null) {
			try {
				tnoAcceptableLosses = Integer.parseInt(tnoLosses);
			} catch (NumberFormatException e) {
				log.warning("Invalid format for tno losses value! Needs to be manually set.");
				log.warning(e.getMessage());
			}
		}
		
		return seasonFactory.generateSeason(seasonNum, seasonYear, current, winValue, lossValue, tieValue, 
				secondHalfStart, playoffStartWeek, superbowlWeek, minPicks, maxPicks, tnoAcceptableLosses);
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
			List<Element> weeks = parser.getSubElementsByTagName(t, "week");
			XmlWeekUpdater.updateWeeks(parser, weeks, weekFactory, gameFactory,
					stadiumService, tfsFactory, subseasonFactory, season);
		}
	}
}
