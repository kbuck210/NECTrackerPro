package com.nectp.beans.ejb.daos;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.SubseasonFactory;
import com.nectp.beans.remote.daos.WeekFactory;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Game.GameStatus;
import com.nectp.jpa.entities.Week.WeekStatus;

@Stateless
public class WeekFactoryBean extends WeekServiceBean implements WeekFactory {
	private static final long serialVersionUID = 3602428978199727133L;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private SubseasonFactory subseasonFactory;
	
	@Override
	public Week createWeekInSeason(int weekNumber, Subseason subseason, Season season, WeekStatus status, boolean current) {
		Week week = null;
		//	Check whether the week already exists, if so, return the existing week
		try {
			week = selectWeekByNumberInSeason(weekNumber, season);
			//	Check whether the week needs to be updated
			boolean update = false;
			if (!week.getSubseason().equals(subseason)) {
				week.getSubseason().removeWeek(week);
				week.setSubseason(subseason);
				subseason.addWeek(week);
				update = true;
			}
			if (!week.getWeekStatus().equals(status)) {
				week.setWeekStatus(status);
				update = true;
			}
			if (!season.getCurrentWeek().equals(week)) {
				season.setCurrentWeek(week);
				update = true;
			}
			
			if (update) {
				update(week);
			}
			
		} catch (NoResultException e) {
			week = new Week();
			week.setWeekNumber(weekNumber);
			week.setWeekStatus(status);
			if (current) {
				season.setCurrentWeek(week);
			}
			week.setSubseason(subseason);
			
			boolean success = insert(week);
			if (!success) {
				week = null;
			}
		}
		
		return week;
	}

	@Override
	public boolean updateWeekForGameComplete(Week week) {
		//	Check whether every game in the week is complete, if so, set the status accordingly
		boolean allComplete = true;
		for (Game g : week.getGames()) {
			if (!g.getGameStatus().equals(GameStatus.FINAL)) {
				allComplete = false;
				break;
			}
		}
		
		if (allComplete) {
			Subseason subseason = week.getSubseason();
			Season season = subseason.getSeason();
			//	Set the status of this week to complete
			week.setWeekStatus(WeekStatus.COMPLETED);
			
			//	Update the current week & return it's update success value
			allComplete = update(week);
			
			//	If the current week number is less than the week number of the superbowl week, create the next week if it doesn't exist,
			//	and/or set the status of next week to be the active week
			if (week.getWeekNumber() < season.getSuperbowlWeek()) {
				Week nextWeek = createWeekInSeason(week.getWeekNumber() + 1, subseason, season, WeekStatus.ACTIVE, true);
				seasonService.updateCurrentWeek(nextWeek);
			}
			
			//	Update subseason
			subseasonFactory.updateSubseasonForWeekComplete(week.getSubseason());
		}
		
		return allComplete;
	}
}

