package com.nectp.beans.ejb.daos;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.SubseasonFactory;
import com.nectp.beans.remote.daos.WeekFactory;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Week.WeekStatus;

@Stateless
public class WeekFactoryBean extends WeekServiceBean implements WeekFactory {
	private static final long serialVersionUID = 3602428978199727133L;
	
	@EJB
	private SubseasonFactory subseasonFactory;
	
	@Override
	public Week createWeekInSeason(int weekNumber, Season season, WeekStatus status, boolean current) {
		Week week = null;
		//	Check whether the week already exists, if so, return the existing week
		try {
			week = selectWeekByNumberInSeason(weekNumber, season);
		} catch (NoResultException e) {
			week = new Week();
			week.setWeekNumber(weekNumber);
			week.setWeekStatus(status);
			if (current) {
				season.setCurrentWeek(week);
			}
			
			NEC subseasonType;
			
			//	Determine the subseason to add the week to
			if (weekNumber < season.getSecondHalfStartWeek()) {
				subseasonType = NEC.FIRST_HALF;
			}
			else if (weekNumber < season.getPlayoffStartWeek()) {
				subseasonType = NEC.SECOND_HALF;
			}
			else if (weekNumber < season.getSuperbowlWeek()) {
				subseasonType = NEC.PLAYOFFS;
			}
			else {
				subseasonType = NEC.SUPER_BOWL;
			}
			
			//	Either gets the existing subseason or creates a new one in this season for the specified type
			Subseason subseason = subseasonFactory.createSubseasonInSeason(subseasonType, season);
			
			week.setSubseason(subseason);
			
			boolean success = insert(week);
			if (!success) {
				week = null;
			}
		}
		
		return week;
	}
}
