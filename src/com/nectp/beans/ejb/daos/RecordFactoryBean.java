package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.RecordFactory;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Record;
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
			} catch (NoResultException e) {
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

}
