package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.PlayerForSeasonFactory;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;

@Stateless
public class PlayerForSeasonFactoryBean extends PlayerForSeasonServiceBean implements PlayerForSeasonFactory {
	private static final long serialVersionUID = -8487560194569537359L;

	@Override
	public PlayerForSeason createPlayerForSeason(Player player, Season season, String nickname, 
			String excelPrintName, Integer excelCol, boolean commish) {
		Logger log = Logger.getLogger(PlayerForSeasonFactoryBean.class.getName());
		PlayerForSeason pfs = null;
		if (player == null || season == null || nickname == null || excelCol == null) {
			log.severe("Parameters not defined, can not create PlayerForSeason.");
		}
		else {
			//	Check whether the specified player for season already exists
			try {
				pfs = selectPlayerInSeason(player, season);
				
				//	Check whether the nickname, excelPrintName, or excelColumn are updated
				boolean update = false;
				if (!pfs.getNickname().equals(nickname)) {
					pfs.setNickname(nickname);
					update = true;
				}
				if (!pfs.getExcelPrintName().equals(excelPrintName)) {
					pfs.setExcelPrintName(excelPrintName);
					update = true;
				}
				if (!pfs.getExcelColumn().equals(excelCol)) {
					pfs.setExcelColumn(excelCol);
					update = true;
				}
				if (pfs.isCommish() != commish) {
					pfs.setCommish(commish);
					update = true;
				}
				if (update) {
					update(pfs);
				}
			}
			//	If no PlayerForSeason exists, create one
			catch (NoResultException e) {
				pfs = new PlayerForSeason();
				pfs.setNickname(nickname);
				pfs.setExcelPrintName(excelPrintName);
				pfs.setExcelColumn(excelCol);
				pfs.setCommish(commish);
				
				pfs.setPlayer(player);
				player.addPlayerforseason(pfs);
				
				pfs.setSeason(season);
				season.addPlayer(pfs);
				
				boolean success = insert(pfs);
				if (!success) {
					pfs = null;
				}
			}
		}
		
		return pfs;
	}

}