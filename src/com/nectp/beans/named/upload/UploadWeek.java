package com.nectp.beans.named.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.w3c.dom.Element;

import com.nectp.beans.ejb.daos.xml.XmlWeekUpdater;
import com.nectp.beans.remote.daos.GameFactory;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.StadiumService;
import com.nectp.beans.remote.daos.SubseasonService;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.beans.remote.daos.WeekFactory;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.Week.WeekStatus;
import com.nectp.webtools.DOMParser;

/** Upload Week is a standalone implementation of the FileUpload interface to specifically 
 *  create/update Week & Game entities based on the selected XML file
 * 
 *  USAGE: the provided XML file should have a root element with 'nec', 'number', 'current', 'ss' and 'status' 
 *  	   attributes specifying the season number, week number, whether is current week, and subseason for
 *  	   the Week being updated.  Within the document should be elements with the qualified node name 'game' 
 *  	   which designate a Game entity.  Within the 'game' element should be the following sub-elements, 
 *  	   defining the entity attributes:
 *  	   - 'homeTeam' : the TFS teamAbbr attribute representing the home team for this game
 *  	   - 'homeScore' : the integer number of points that the home team has scored
 *  	   - 'homeFavored1' : either Y or N denoting whether or not the home team is favored vs spread 1
 *  	   - 'homeFavored2' : (Optional) if there is a spread 2, denotes whether the home team is favored vs spread2
 *  	   - 'spread1' : the normal spread for this game (a.k.a. Friday's spread
 *  	   - 'spread2' : (Optional) if a second spread is used for the early games, insert here
 *  	   - 'awayScore' : the integer number of points that the away team has scored
 *    	   - 'awayTeam' : the TFS teamAbbr attribute representing the away team for this game
 *  	   - 'date' : the month, day and year of this game, separated by '.'
 *  	   - 'time' : the hour, minute and AM/PM designation for the kickoff time, separated by ':'
 *  	   - 'status' : the string name of the GameStatus enum value for this game's status
 *  	   - 'stadium' : the name of the Stadium entity in which this game is being played
 * 
 * @author Kevin C. Buckley
 * @since  1.0
 */
@Named(value="uploadWeek")
@ViewScoped
public class UploadWeek extends FileUploadImpl {
	private static final long serialVersionUID = -2603772511079306013L;

	private Logger log;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private SubseasonService subseasonService;
	
	@EJB
	private WeekFactory weekFactory;
	
	@EJB
	private GameFactory gameFactory;
	
	@EJB
	private StadiumService stadiumService;
	
	@EJB
	private TeamForSeasonService tfsService;
	
	public UploadWeek() {
		log = Logger.getLogger(UploadWeek.class.getName());
	}
	
	@Override
	public void upload(FileUploadEvent event) {
		files.add(event.getFile());
	}
	
	@Override
	public void submit() {
		for (UploadedFile file : files) {
			if (file != null) {
				try {
					InputStream iStream = file.getInputstream();
					parseWeek(iStream);
				} catch (IOException e) {
					log.severe("Exception retrieving input stream from uploaded file, can not update week: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
	
	/** From the 'week' XML root, get the 'game' elements and update Week & Games for this season
	 *  
	 * @param iStream the InputStream read from the uploaded file containing the XML document
	 */
	private void parseWeek(InputStream iStream) {
		DOMParser parser = DOMParser.newInstance(iStream);
		Element docRoot = parser.getRootElement();
		Integer seasonNum = parseSeason(docRoot);
		//	TODO: fix status
		WeekStatus status = parseWeekStatus(docRoot);
		Season season = null;
		if (seasonNum != null) {
			season = seasonService.selectById(seasonNum);
			Subseason subseason = parseSubseason(docRoot, season);
			if (subseason != null) {
				parser.setQualifiedNodeName("week");
				List<Element> weeks = parser.generateElementList();
				XmlWeekUpdater.updateWeeks(parser, weeks, weekFactory, gameFactory, stadiumService, tfsService, subseasonService, season);
			}
		}
		else {
			log.severe("No season specified in XML root 'nec' attribute, can not create/update teams.");
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
	
	/** From the root XML element, get the subseason type name, & find the subseason in the specified season
	 * 
	 * @param root the root XML element in this document
	 * @param season the season specified by the season number in the root 'nec' attribute 
	 * @return the Subseason entity representing the specified subseason in the specified season
	 */
	private Subseason parseSubseason(Element root, Season season) {
		String ss = root.getAttribute("ss");
		NEC subseasonType = NEC.getNECForString(ss);
		Subseason subseason = null;
		try {
			subseason = subseasonService.selectSubseasonInSeason(subseasonType, season);
		} catch (NoResultException e) {
			log.severe("No subseason found for: " + ss + " in NEC " + season.getSeasonNumber());
			log.severe(e.getMessage());
		}
		
		return subseason;
	}
	
	/** From the root XML element, get the WeekStatus enum value for the status of this week
	 * 
	 * @param root the root XML element in this document
	 * @return the WeekStatus enum value for the specified status string
	 */
	private WeekStatus parseWeekStatus(Element root) {
		WeekStatus status;
		String statusStr = root.getAttribute("status");
		status = WeekStatus.getWeekStatusForString(statusStr);
		return status != null ? status : WeekStatus.WAITING;
	}
}
