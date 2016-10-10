package com.nectp.beans.ejb.daos.xml;

import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import com.nectp.beans.remote.daos.EmailFactory;
import com.nectp.beans.remote.daos.PlayerFactory;
import com.nectp.beans.remote.daos.PlayerForSeasonFactory;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.Season;
import com.nectp.webtools.DOMParser;

/** Player/PFS XML update helper, given the factory beans and list of XML elements, update/create the players/PFS's
 * 
 * @author Kevin C. Buckley
 * @since  1.0
 */
public class XmlPlayerUpdater {

	private static final Logger log = Logger.getLogger(XmlPlayerUpdater.class.getName());
	
	public static void updatePlayers(DOMParser parser, PlayerFactory playerFactory, 
			PlayerForSeasonFactory pfsFactory, EmailFactory emailFactory, List<Element> players, Season season) {
		for (Element pl : players) {
			//	Get the Player info from the player elements
			String name = parser.getTextSubElementByTagName(pl, "name");
			String since = parser.getTextSubElementByTagName(pl, "sinceYear");
			String avatar = parser.getTextSubElementByTagName(pl, "avatar");
			List<String> emails = parser.getTextSubElementsByTagName(pl, "email");
			String updates = parser.getTextSubElementByTagName(pl, "recieveUpdates");
			if (name == null || name.trim().isEmpty()) {
				log.severe("Failed to read player name! skipping player");
				continue;
			}
			else if (since == null || since.trim().isEmpty()) {
				log.severe("Failed to read since year! skipping player");
				continue;
			}
			else if (avatar == null || avatar.trim().isEmpty()) {
				log.warning("No avatar defined - using default image.");
				avatar = "img/avatars/default.png";
			}
			
			Integer sinceYear = null; 
			try {
				sinceYear = Integer.parseInt(since);
			} catch (NumberFormatException e) {
				log.severe("Invalid since year format: " + e.getMessage());
				log.severe("skipping player.");
				e.printStackTrace();
				continue;
			}
			boolean receieveUpdates = updates != null ? updates.trim().toUpperCase().equals("Y") : false;
			
			//	Create or update the specified player based on the supplied information
			Player player = playerFactory.createPlayer(name, sinceYear, avatar);
			
			//	Using the list of emails, get/create Email entities
			for (String address : emails) {
				boolean primary = emails.indexOf(address) == 0;
				emailFactory.createEmailForPlayer(player, address, primary, receieveUpdates);
			}
			
			//	Check whether or not updating the PlayerForSeason as well
			if (pfsFactory != null) {
				//	Get the PFS info
				String nickname = parser.getTextSubElementByTagName(pl, "nickname");
				String commishStr = parser.getTextSubElementByTagName(pl, "commish");
				String excelName = parser.getTextSubElementByTagName(pl, "excelName");
				String excelCol = parser.getTextSubElementByTagName(pl, "excelCol");
				Integer col = null;
				try {
					col = Integer.parseInt(excelCol);
				} catch (NumberFormatException e) {
					log.warning("Invalid excel column number: " + e.getMessage());
					
				}
				boolean commish = commishStr != null ? commishStr.trim().toUpperCase().equals("Y") : false;
				
				//	Create or update the specified PFS based on the supplied information
				pfsFactory.createPlayerForSeason(player, season, nickname, excelName, col, commish);
			}
		}
	}
}
