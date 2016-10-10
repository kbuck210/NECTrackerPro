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
import com.nectp.beans.named.FileUploadImpl;
import com.nectp.beans.remote.daos.EmailFactory;
import com.nectp.beans.remote.daos.PlayerFactory;
import com.nectp.beans.remote.daos.PlayerForSeasonFactory;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.entities.Season;
import com.nectp.webtools.DOMParser;

/** Upload Players is a standalone implementation of the FileUpload interface to specifically 
 *  create/update Player & PFS entities based on the selected XML file
 * 
 *  USAGE: the provided XML file should have a root element with an 'nec' attribute specifying the 
 *  	   season to which the PFS objects belong.  Within the document should be elements with the
 *  	   qualified node name 'player' which designates a Player & PFS entity.  Within the 'player' element
 *  	   should be the following sub-elements, defining the entity attributes:
 *  	   - 'name' : the Player attribute representing the name of the player
 *  	   - 'nickname' : the colloquial name used for this PFS
 *  	   - 'sinceYear' : the year in which this Player first started playing in the NEC
 *  	   - 'commish' : either Y or N denoting whether the PFS is the current commish
 *  	   - 'avatar' : the URL path to use of the avatar image for this PFS
 *  	   - 'stadium' : the name of the Stadium entity representing the home stadium for this TFS
 *  	   - 'excelCol' : the 1-based numerical index of the column in which this PFS is written
 *  	   - 'excelName' : the display name to use for printing this TFS to Excel reports
 * 
 * @author Kevin C. Buckley
 * @since  1.0
 */
@Named(value="uploadPlayers")
@RequestScoped
public class UploadPlayers extends FileUploadImpl {
	private static final long serialVersionUID = 1464890360533394179L;

	private Logger log;
	
	@Inject
	private SeasonService seasonService;
	
	@Inject
	private PlayerFactory playerFactory;
	
	@Inject
	private PlayerForSeasonFactory pfsFactory;
	
	@Inject
	private EmailFactory emailFactory;
	
	public UploadPlayers() {
		log = Logger.getLogger(UploadPlayers.class.getName());
	}
	
	@Override
	public void upload() {
		if (file != null) {
			try {
				InputStream iStream = file.getInputstream();
				parsePlayers(iStream);
			} catch (IOException e) {
				log.severe("Exception retrieving input stream from uploaded file, can not update players: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/** From the 'players' XML root, get the 'player' elements and update Players & PFS for this season
	 * 
	 * @param iStream the InputStream read from the uploaded file containing the XML document
	 */
	private void parsePlayers(InputStream iStream) {
		DOMParser parser = DOMParser.newInstance(iStream);
		Integer seasonNum = parseSeason(parser.getRootElement());
		Season season = null;
		if (seasonNum != null) {
			season = seasonService.selectById(seasonNum);
			parser.setQualifiedNodeName("player");
			List<Element> players = parser.generateElementList();
			XmlPlayerUpdater.updatePlayers(parser, playerFactory, pfsFactory, emailFactory, players, season);
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
