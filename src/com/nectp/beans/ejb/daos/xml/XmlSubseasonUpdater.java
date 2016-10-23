package com.nectp.beans.ejb.daos.xml;

import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import com.nectp.beans.remote.daos.SubseasonFactory;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Season;

/** Subseason XML update helper, given the factory bean and list of XML elements, update/create the subseasons
 * 
 * @author Kevin C. Buckley
 * @since  1.0
 */
public class XmlSubseasonUpdater {

	private static final Logger log = Logger.getLogger(XmlSubseasonUpdater.class.getName());
	
	public static void updateSubseasons(SubseasonFactory subseasonFactory, List<Element> subseasons, Season season) {
		for (Element ss : subseasons) {
			String subseasonName = ss.getAttribute("name");
			NEC ssType = NEC.getNECForName(subseasonName);
			if (ssType == null) {
				log.severe("Invalid subseason name: " + subseasonName + " Can not create subseason!");
				continue;
			}
			
			subseasonFactory.createSubseasonInSeason(ssType, season);
		}
	}
	
}
