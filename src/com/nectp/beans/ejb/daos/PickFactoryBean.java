package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.GameService;
import com.nectp.beans.remote.daos.PickFactory;
import com.nectp.beans.remote.daos.PickService;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.Pick.PickType;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Stateless
public class PickFactoryBean extends PickServiceBean implements PickFactory {
	private static final long serialVersionUID = 843892000815116319L;

	@EJB
	private GameService gameService;
	
	@EJB
	private PickService pickService;
	
	@EJB
	private RecordService recordService;
	
	@Override
	public Pick createPlayerPickInWeek(PlayerForSeason player, TeamForSeason pickedTeam, Week week, NEC pickFor) {
		Logger log = Logger.getLogger(PickFactoryBean.class.getName());
		Pick pick = null;
		if (player == null || pickedTeam == null || week == null) {
			log.severe("Parameters not set, can not create pick for player!");
		}
		else {
			//	Get the game for this pick
			Game gameForPick = null;
			try {
				gameForPick = gameService.selectGameByTeamWeek(pickedTeam, week);
			} catch (NoResultException e) {
				log.severe("No game found for " + pickedTeam.getTeam().getTeamAbbr() 
						+ " in week " + week.getWeekNumber() + ". Can not create Pick!");
				return null;
			}
			
			//	First check to determine whether the pick already exists, creating it if not
			try {
				pick = pickService.selectPlayerPickForGame(player, gameForPick);
			} catch (NoResultException e) {
				pick = new Pick();
				
				pick.setPlayer(player);
				player.addPick(pick);
				
				Record applicableRecord = null;
				try {
					applicableRecord = recordService.selectWeekRecordForAtfs(week, player, pickFor);
					pick.setApplicableRecord(applicableRecord);
					applicableRecord.addPickInRecord(pick);
				} catch (NoResultException ex) {
					log.warning("Applicable record not set for " + player.getNickname() + "'s pick of " 
						+ pickedTeam.getNickname() + " for " + pickFor.name() + " in week " + week.getWeekNumber());
				}
				
				pick.setPickedTeam(pickedTeam);
				pickedTeam.addPickForTeam(pick);
				
				PickType pickType;
				switch (pickFor) {
				case TWO_AND_OUT:
					pickType = PickType.STRAIGHT_UP;
					break;
				case ONE_AND_OUT:
					pickType = PickType.STRAIGHT_UP;
					break;
				case TNT:
					if (gameForPick.getSpread2() != null) {
						pickType = PickType.SPREAD2;
					}
					else {
						pickType = PickType.SPREAD1;
					}
					break;
				default:
					pickType = PickType.SPREAD1;
					break;
				}
				
				pick.setPickType(pickType);
				
				boolean success = insert(pick);
				if (!success) {
					pick = null;
				}
			}
		}
		
		return pick;
	}

}
