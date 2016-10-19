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

import com.nectp.beans.ejb.daos.xml.XmlPrizeUpdater;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.PlayerService;
import com.nectp.beans.remote.daos.PrizeFactory;
import com.nectp.beans.remote.daos.PrizeForSeasonFactory;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.SubseasonService;
import com.nectp.jpa.entities.Season;
import com.nectp.webtools.DOMParser;

/** Upload Prizes is a standalone implementation of the FileUpload interface to specifically 
 *  create/update Prize & PzFS entities based on the selected XML file
 * 
 *  USAGE: the provided XML file should have a root element with an 'nec' attribute specifying the 
 *  	   season to which the PzFS objects belong.  Within the document should be elements with the
 *  	   qualified node name 'prize' which designates a Prize & PzFS entity.  Within the 'prize' element
 *  	   should be the following sub-elements, defining the entity attributes:
 *  	   - 'name' : the Prize attribute representing the name of the NEC enum prizeType
 *  	   - 'amount' : the dollar amount that the prize is worth for this PzFS
 *  	   - 'winner' : the name of the PFS that has won this PzFS if it has already been won
 * 
 * @author Kevin C. Buckley
 * @since  1.0
 */
@Named(value="uploadPrizes")
@ViewScoped
public class UploadPrizes extends FileUploadImpl {
	private static final long serialVersionUID = -4866376737049763883L;

	private Logger log;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private PrizeFactory prizeFactory;
	
	@EJB
	private PrizeForSeasonFactory pzfsFactory;
	
	@EJB
	private SubseasonService subseasonService;
	
	@EJB
	private PlayerService playerService;
	
	@EJB
	private PlayerForSeasonService pfsService;
	
	public UploadPrizes() {
		log = Logger.getLogger(UploadPrizes.class.getName());
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
					parsePrizes(iStream);
				} catch (IOException e) {
					log.severe("Exception retrieving input stream from uploaded file, can not update prizes: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
	
	/** From the 'prizes' XML root, get the 'prize' elements and update Prize & PzFS for this season
	 * 
	 * @param iStream the InputStream read from the uploaded file containing the XML document
	 */
	private void parsePrizes(InputStream iStream) {
		DOMParser parser = DOMParser.newInstance(iStream);
		Integer seasonNum = parseSeason(parser.getRootElement());
		Season season = null;
		if (seasonNum != null) {
			season = seasonService.selectById(seasonNum);
			parser.setQualifiedNodeName("prize");
			List<Element> prizes = parser.generateElementList();
			XmlPrizeUpdater.updatePrizes(parser, prizeFactory, pzfsFactory, subseasonService, playerService, pfsService, prizes, season);
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
