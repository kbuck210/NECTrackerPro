package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;

import com.nectp.beans.remote.daos.RecordFactory;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Game.GameStatus;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Stateless
public class RecordFactoryBean extends RecordServiceBean implements RecordFactory {
	private static final long serialVersionUID = -6940509414489794137L;

	@Override
	public Record createWeekRecordForAtfs(Week week, AbstractTeamForSeason atfs, NEC recordType) {
		Logger log = Logger.getLogger(RecordFactoryBean.class.getName());
		Record record = null;
		if (week == null || atfs == null || recordType == null) {
			log.severe("Parameters not defined, can not create record!");
		}
		else {
			//	Check whether the record already exists, if not, create it
			try {
				record = selectWeekRecordForAtfs(week, atfs, recordType);
			} catch (NoExistingEntityException e) {
				record = new Record();
				record.setRecordType(recordType);
				
				record.setTeam(atfs);
				atfs.addRecord(record);
				
				record.setWeek(week);
				week.addRecord(record);
				
				boolean success = insert(record);
				if (!success) {
					record = null;
				}
 			}
		}
		
		return record;
	}

	@Override
	public Record createWeekRecordForGame(Game game, TeamForSeason team, NEC recordType) {
		Logger log = Logger.getLogger(RecordFactoryBean.class.getName());
		Record record = null;
		if (game == null || team == null || recordType == null) {
			log.severe("Parameters not defined, can not create record!");
		}
		else {
			Week week = game.getWeek();
			//	Check whether the record already exists, if not, create it
			try {
				record = selectWeekRecordForAtfs(week, team, recordType);
				
				//	Determine whether the record needs to be updated
				boolean update = false;
				GameStatus gameStatus = game.getGameStatus();
				if (gameStatus == GameStatus.FINAL) {
					//	Set the raw scores for the game
					if (game.getWinner() == null && record.getTies() != 1) {
						record.setTies(1);
						record.setWins(0);
						record.setLosses(0);
						update = true;
					}
					else if (game.getWinner() != null && game.getWinner().equals(team) && record.getWins() != 1) {
						record.setWins(1);
						record.setLosses(0);
						record.setTies(0);
						update = true;
					}
					else if (record.getLosses() != 1){
						record.setLosses(1);
						record.setWins(0);
						record.setTies(0);
						update = true;
					}
					//	Set the ATS1 scores for the game
					if (game.getWinnerATS1() == null && record.getTiesATS1() != 1) {
						record.setTiesATS1(1);
						record.setWinsATS1(0);
						record.setLossesATS1(0);
						update = true;
					}
					else if (game.getWinnerATS1() != null && game.getWinnerATS1().equals(team) && record.getWinsATS1() != 1) {
						record.setWins(1);
						record.setLosses(0);
						record.setTies(0);
						update = true;
					}
					else if (record.getLossesATS1() != 1) {
						record.setLossesATS1(1);
						record.setWinsATS1(0);
						record.setTiesATS1(0);
						update = true;
					}
					//	Set the ATS2 scores for the game (if any exist)
					if (game.getSpread2() != null) {
						if (game.getWinnerATS2() == null && record.getTiesATS2() != 1) {
							record.setTiesATS2(1);
							record.setWinsATS2(0);
							record.setLossesATS2(0);
							update = true;
						}
						else if (game.getWinnerATS2() != null && game.getWinnerATS2().equals(team) && record.getWinsATS2() != 1) {
							record.setWinsATS2(1);
							record.setLossesATS2(0);
							record.setTiesATS2(0);
							update = true;
						}
						else if (record.getLossesATS2() != 1) {
							record.setLossesATS2(1);
							record.setWinsATS2(0);
							record.setTiesATS2(0);
							update = true;
						}
					}
				}
				
				if (update) {
					update(record);
				}
				
			} catch (NoExistingEntityException e) {
				record = new Record();
				record.setRecordType(recordType);
				
				record.setTeam(team);
				team.addRecord(record);
				
				record.setWeek(week);
				week.addRecord(record);
				
				GameStatus gameStatus = game.getGameStatus();
				if (gameStatus != null && gameStatus == GameStatus.FINAL) {
					//	Add W/L/T if the game is final
					if (game.getWinner() == null) record.addTie();
					else if (game.getWinner().equals(team)) record.addWin();
					else record.addLoss();
					
					if (game.getWinnerATS1() == null) record.addTieATS1();
					else if (game.getWinnerATS1().equals(team)) record.addWinATS1();
					else record.addLossATS1();
					
					if (game.getSpread2() != null) {
						if (game.getWinnerATS2() == null) record.addTieATS2();
						else if (game.getWinnerATS2().equals(team)) record.addWinATS2();
						else record.addLossATS2();
					}
				}
				
				boolean success = insert(record);
				if (!success) {
					record = null;
				}
			}
		}
		
		return record;
	}

}
