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
	public Record createWeekRecordForGame(Game game, TeamForSeason team) {
		Logger log = Logger.getLogger(RecordFactoryBean.class.getName());
		Record record = null;
		if (game == null || team == null) {
			log.severe("Parameters not defined, can not create record!");
		}
		else {
			Week week = game.getWeek();
			//	Check whether the record already exists, if not, create it
			try {
				record = selectWeekRecordForAtfs(week, team, week.getSubseason().getSubseasonType());
				
				//	Determine whether the record needs to be updated
				boolean update = false;
				GameStatus gameStatus = game.getGameStatus();
				if (gameStatus == GameStatus.FINAL) {
					//	TODO: look into whether ATS scores also need to be added
					if (game.getWinner() == null) {
						if (record.getTies() == 0) {
							record.addTie();
							record.setWins(0);
							record.setLosses(0);
							update = true;
						}
					}
					else if (game.getWinner().equals(team)) {
						if (record.getWins() == 0) {
							record.addWin();
							record.setLosses(0);
							record.setTies(0);
							update = true;
						}
					}
					else {
						if (record.getLosses() == 0) {
							record.addLoss();
							record.setWins(0);
							record.setTies(0);
							update = true;
						}
					}
				}
				
				if (update) {
					update(record);
				}
				
			} catch (NoExistingEntityException e) {
				record = new Record();
				record.setRecordType(week.getSubseason().getSubseasonType());
				
				record.setTeam(team);
				team.addRecord(record);
				
				record.setWeek(week);
				week.addRecord(record);
				
				GameStatus gameStatus = game.getGameStatus();
				if (gameStatus != null && gameStatus == GameStatus.FINAL) {
					if (game.getWinner() == null) {
						record.addTie();
					}
					else if (game.getWinner().equals(team)) {
						record.addWin();
					}
					else {
						record.addLoss();
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
