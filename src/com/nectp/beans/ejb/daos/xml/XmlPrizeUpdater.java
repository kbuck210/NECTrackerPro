package com.nectp.beans.ejb.daos.xml;

import java.util.List;
import java.util.logging.Logger;

import javax.persistence.NoResultException;

import org.w3c.dom.Element;

import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.PlayerService;
import com.nectp.beans.remote.daos.PrizeFactory;
import com.nectp.beans.remote.daos.PrizeForSeasonFactory;
import com.nectp.beans.remote.daos.SubseasonService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
import com.nectp.webtools.DOMParser;

/** Prize/PZFS XML update helper, given the factory beans and list of XML elements, update/create the prizes/PZFS's
 * 
 * @author Kevin C. Buckley
 * @since  1.0
 */
public class XmlPrizeUpdater {

	private static final Logger log = Logger.getLogger(XmlPrizeUpdater.class.getName());
	
	public static void updatePrizes(DOMParser parser, PrizeFactory prizeFactory, PrizeForSeasonFactory pzfsFactory, 
			SubseasonService subseasonService, PlayerService playerService, 
			PlayerForSeasonService pfsService, List<Element> prizes, Season season) {
		for (Element pz : prizes) {
			String prizeName = parser.getTextSubElementByTagName(pz, "name");
			String amount = parser.getTextSubElementByTagName(pz, "amount");
			String winnerName = parser.getTextSubElementByTagName(pz, "winner");
			
			NEC prizeType = NEC.getNECForString(prizeName);
			if (prizeType == null) {
				log.severe("Invalid prize name: " + prizeName + " could not create/update Prize!");
				continue;
			}
			
			prizeFactory.createPrize(prizeType);
			
			//	If also creating/updating the prizeForSeason, process updates
			if (pzfsFactory != null) {
				Subseason subseason = null;
				if (prizeType == NEC.FIRST_HALF || prizeType == NEC.SECOND_HALF || 
						prizeType == NEC.PLAYOFFS || prizeType == NEC.SUPER_BOWL) {
					try {
						subseason = subseasonService.selectSubseasonInSeason(prizeType, season);
					} catch (NoResultException e) {
						log.severe("Subseason for: " + prizeName + " not found! can not create prize.");
						continue;
					}
				}
				
				Integer prizeAmount = null;
				try {
					prizeAmount = Integer.parseInt(amount);
				} catch (NumberFormatException e) {
					log.severe("Invalid prize amount format: " + e.getMessage());
					log.severe("skipping prize for season.");
					e.printStackTrace();
					continue;
				}
				
				PlayerForSeason winner = null;
				if (winnerName != null) {
					try {
						Player player = playerService.selectPlayerByName(winnerName);
						winner = pfsService.selectPlayerInSeason(player, season);
					} catch (NoResultException e) {
						log.warning("Winner name not found! will not assign winner!");
					}
				}
				
				pzfsFactory.createPrizeInSeason(prizeType, season, subseason, prizeAmount, winner);
			}
		}
	}
}
