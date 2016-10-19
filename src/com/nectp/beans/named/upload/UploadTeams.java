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

import com.nectp.beans.ejb.daos.xml.XmlTeamUpdater;
import com.nectp.beans.remote.daos.ConferenceFactory;
import com.nectp.beans.remote.daos.DivisionFactory;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.StadiumService;
import com.nectp.beans.remote.daos.TeamFactory;
import com.nectp.beans.remote.daos.TeamForSeasonFactory;
import com.nectp.jpa.entities.Season;
import com.nectp.webtools.DOMParser;

/** Upload Teams is a standalone implementation of the FileUpload interface to specifically 
 *  create/update Team & TFS entities based on the selected XML file
 *  
 *  USAGE: the provided XML file should have a root element with an 'nec' attribute specifying the 
 *  	   season to which the TFS objects belong.  Within the document should be elements with the
 *  	   qualified node name 'team' which designates a Team & TFS entity.  Within the 'team' element
 *  	   should be the following sub-elements, defining the entity attributes:
 *  	   - 'franchiseId' : the Team attribute representing the franchise for which the TFS belongs
 *  	   - 'abbr' : the two or three letter abbreviation corresponding to this TFS
 *  	   - 'city' : the name of the City corresponding to this TFS
 *  	   - 'nickname' : the colloquial name used for this TFS
 *  	   - 'conference' : either AFC or NFC, dependent on the conference to which this TFS belongs
 *  	   - 'division' : either NORTH, SOUTH, EAST, or WEST, dependent on the region to which this TFS belongs
 *  	   - 'stadium' : the name of the Stadium entity representing the home stadium for this TFS
 *  	   - 'homeImg' : the URL path to use of the helmet image for when this TFS is a home team
 *  	   - 'awayImg' : the URL path to use of the helmet image for when this TFS is an away team
 *  	   - 'excelName' : the display name to use for printing this TFS to Excel reports
 * 
 * @author Kevin C. Buckley
 * @since  1.0
 */
@Named(value="uploadTeams")
@ViewScoped
public class UploadTeams extends FileUploadImpl {
	private static final long serialVersionUID = -6034118780233588609L;
	
	private Logger log;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private TeamFactory teamFactory;
	
	@EJB
	private TeamForSeasonFactory tfsFactory;
	
	@EJB
	private ConferenceFactory confFactory;
	
	@EJB
	private DivisionFactory divFactory;
	
	@EJB
	private StadiumService stadiumService;
	
	public UploadTeams() {
		log = Logger.getLogger(UploadTeams.class.getName());
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
					parseTeams(iStream);
				} catch (IOException e) {
					log.severe("Exception retrieving input stream from uploaded file, can not update teams: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
	
	/** From the 'teams' XML root, get the 'team' elements and update Teams & TFS for this season
	 * 
	 * @param iStream the InputStream read from the uploaded file containing the XML document
	 */
	private void parseTeams(InputStream iStream) {
		DOMParser parser = DOMParser.newInstance(iStream);
		Integer seasonNum = parseSeason(parser.getRootElement());
		Season season = null;
		if (seasonNum != null) {
			season = seasonService.selectById(seasonNum);
			parser.setQualifiedNodeName("team");
			List<Element> teams = parser.generateElementList();
			XmlTeamUpdater.updateTeams(parser, teamFactory, tfsFactory, confFactory, divFactory, stadiumService, teams, season);
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
}
