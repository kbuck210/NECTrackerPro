package com.nectp.beans.ejb.daos;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.GameService;
import com.nectp.beans.remote.daos.PickFactory;
import com.nectp.beans.remote.daos.RecordFactory;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
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
	private RecordFactory recordFactory;
	
	private Logger log = Logger.getLogger(PickFactoryBean.class.getName());
	
	@Override
	public Pick createPlayerPickInWeek(PlayerForSeason player, TeamForSeason pickedTeam, Week week, NEC pickFor, PickType pickType) {
		Pick pick = null;
		if (player == null || pickedTeam == null || week == null) {
			log.severe("Parameters not set, can not create pick for player!");
		}
		else {
			//	Get the game for this pick
			Game gameForPick = gameService.selectGameByTeamWeek(pickedTeam, week);
			if (gameForPick == null) {
				log.severe("No game found for " + pickedTeam.getTeamAbbr() 
						+ " in week " + week.getWeekNumber() + ". Can not create Pick!");
				return null;
			}
			
			//	First check to determine whether the pick already exists, creating it if not
			try {
				pick = selectPlayerPickForGameForType(player, gameForPick, pickFor);
			} catch (NoResultException e) {
				if (!checkPickEligibility(player, pickedTeam, pickFor)) {
					log.severe("The selected pick is ineligible, can not create pick of "
							+ pickedTeam.getTeamCity() + " for " + player.getNickname());
					return null;
				}
				
				pick = new Pick();
				
				pick.setPlayer(player);
				player.addPick(pick);
				
				pick.setGame(gameForPick);
				gameForPick.addPick(pick);
				
				Record applicableRecord = null;
				try {
					//	If the record doesn't already exist, creates one
					applicableRecord = recordFactory.createWeekRecordForAtfs(week, pickedTeam, pickFor);
					pick.setApplicableRecord(applicableRecord);
					applicableRecord.addPickInRecord(pick);
				} catch (NoResultException ex) {
					log.warning("Applicable record not set for " + player.getNickname() + "'s pick of " 
						+ pickedTeam.getNickname() + " for " + pickFor.name() + " in week " + week.getWeekNumber());
				}
				
				pick.setPickedTeam(pickedTeam);
				pickedTeam.addPickForTeam(pick);
				
				//	If the specified pick type is spread2, but no spread2 exists, use spread1
				if (pickType == PickType.SPREAD2 && gameForPick.getSpread2() == null) {
					pickType = PickType.SPREAD1;
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
	
	@Override
	public Pick createPlayerPickForRecord(Record record, Game game, TeamForSeason pickedTeam, PickType pickType) {
		Pick pick = null;
		if (record == null) {
			log.severe("Record not set, can not create pick for player!");
		}
		else {
			AbstractTeamForSeason team = record.getTeam();
			PlayerForSeason player = null;
			NEC pickFor = record.getRecordType();
			if (team instanceof PlayerForSeason) {
				player = (PlayerForSeason) team;
			}
			else {
				log.severe("The specified record is not for a PlayerForSeason entity, can not create a pick!");
				return null;
			}
			try {
				pick = selectPlayerPickForGameForType(player, game, pickFor);
			} catch (NoResultException e) {
				if (!checkPickEligibility(player, pickedTeam, pickFor)) {
					log.severe("The selected pick is ineligible, can not create pick of "
							+ pickedTeam.getTeamCity() + " for " + player.getNickname());
					return null;
				}
				
				pick = new Pick();
				
				pick.setPlayer(player);
				player.addPick(pick);
				
				pick.setPickedTeam(pickedTeam);
				pickedTeam.addPickForTeam(pick);
				
				pick.setGame(game);
				game.addPick(pick);
				
				pick.setApplicableRecord(record);
				record.addPickInRecord(pick);
				
				//	If the specified pick type is spread2, but no spread2 exists, use spread1
				if (pickType == PickType.SPREAD2 && game.getSpread2() == null) {
					pickType = PickType.SPREAD1;
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
	
	/** If the pick is for TWO_AND_OUT or ONE_AND_OUT, check whether the max losses has been reached 
	 * and/or whether the selected team has already been picked.
	 * 
	 * @param player the PlayerForSeason making the pick
	 * @param pickedTeam the TeamForSeason that was selected
	 * @param pickFor the NEC enum value represnting the type of pick
	 * @return true if the pick is a generic pick, or if team not picked and max losses not exceeded, false otherwise
	 */
	private boolean checkPickEligibility(PlayerForSeason player, TeamForSeason pickedTeam, NEC pickFor) {
		//	Before creating the record/pick, check whether type is two/one & out, & whether 
		//	team has already been taken, or the max number of losses has been accrued
		int maxLosses = 0;
		if (pickFor == NEC.TWO_AND_OUT) {
			maxLosses = 2;
		}
		else if (pickFor == NEC.ONE_AND_OUT) {
			maxLosses = 1;
		}
		if (maxLosses != 0) {
			RecordAggregator ragg = recordFactory.getAggregateRecordForAtfsForType(pickedTeam, pickFor, false);
			//	Check the number of losses
			if (ragg.getRawLosses() >= maxLosses) {
				log.warning(player.getNickname() + " has already exceeded the number of "
						+ "acceptable losses! Can not create pick!");
				return false;
			}
			//	Check the records for team already picked
			List<Record> records = ragg.getRecords();
			for (Record record : records) {
				if (record.getTeam() instanceof PlayerForSeason) {
					PlayerForSeason pl = (PlayerForSeason)record.getTeam();
					List<Pick> picks = selectPlayerPicksForType(pl, pickFor);
					for (Pick p : picks) {
						if (p.getPickedTeam().equals(pickedTeam)) {
							log.warning(player.getNickname() + " has already picked "
									+ pickedTeam.getTeamCity() + "! Can not create pick!");
							return false;
						}
					}
				}
			}
		}
		
		//	If losses does not exceed maximum or selected team not already picked, current pick is eligible
		return true;
	}

}


