package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;

import com.nectp.beans.remote.daos.SeasonFactory;
import com.nectp.jpa.entities.Season;

@Stateless
public class SeasonFactoryBean extends SeasonServiceBean implements SeasonFactory {
	private static final long serialVersionUID = -1361385478973248339L;
	
	@Override
	public Season generateSeason(Integer seasonNumber, String seasonYear, boolean current, Integer winValue, Integer lossValue, Integer tieValue,
			Integer secondHalfStartWeek, Integer playoffStartWeek, Integer superbowlWeek, Integer minPicks, Integer maxPicks, Integer tnoLosses) {
		
		Logger log = Logger.getLogger(SeasonFactoryBean.class.getName());
		
		if (seasonNumber == null || seasonYear == null) {
			log.severe("Season number or year not specified, can not create/retrieve season.");
			return null;
		}
		
		//	Select the season by the specified year, if it already exists, return existing, otherwise create new season
		Season season = selectById(seasonNumber);
		if (season == null) {
			//	Double check that a season with the specified year also has not been created already, if so, invalid condition
			try {
				season = selectSeasonByYear(seasonYear);
				boolean success = updateSeason(season, seasonYear, current, winValue, lossValue, tieValue, 
						secondHalfStartWeek, playoffStartWeek, superbowlWeek, minPicks, maxPicks, tnoLosses);
				if (!success) {
					season = null;
				}
			} catch (NoExistingEntityException e) {
				season = new Season();

				season.setSeasonNumber(seasonNumber);
				season.setSeasonYear(seasonYear);
				season.setCurrentSeason(current);
				
				//	If creating the current season, make the current current season not current
				if (current) {
					Season currentCurrent = selectCurrentSeason();
					if (currentCurrent != null) {
						currentCurrent.setCurrentSeason(false);
						update(currentCurrent);
					}
				}

				//	For conditional values, if null, leave for Entity to use default values, otherwise use specified
				if (winValue != null)  season.setWinValue(winValue);
				if (lossValue != null) season.setLossValue(lossValue);
				if (tieValue != null)  season.setTieValue(tieValue);

				if (secondHalfStartWeek != null) season.setSecondHalfStartWeek(secondHalfStartWeek);
				if (playoffStartWeek != null)    season.setPlayoffStartWeek(playoffStartWeek);
				if (superbowlWeek != null)		 season.setSuperbowlWeek(superbowlWeek);

				if (minPicks != null) season.setMinPicks(minPicks);
				if (maxPicks != null) season.setMaxPicks(maxPicks);
				if (tnoLosses != null) season.setTnoAcceptableLosses(tnoLosses);

				//	Insert the season into the database
				boolean success = insert(season);
				if (!success) {
					season = null;
				}
			}
		}
		else {
			boolean success = updateSeason(season, seasonYear, current, winValue, lossValue, tieValue, 
					secondHalfStartWeek, playoffStartWeek, superbowlWeek, minPicks, maxPicks, tnoLosses);
			if (!success) {
				season = null;
			}
		}	
		
		return season;
	}
	
	/** If a season was found with an ID/Year search, udpate any of it's attribute values if required
	 * 
	 * @param season
	 * @param seasonYear
	 * @param current
	 * @param winValue
	 * @param lossValue
	 * @param tieValue
	 * @param secondHalfStartWeek
	 * @param playoffStartWeek
	 * @param superbowlWeek
	 * @param minPicks
	 * @param maxPicks
	 * @param tnoLosses
	 * @return
	 */
	private boolean updateSeason(Season season, String seasonYear, boolean current, Integer winValue, Integer lossValue, Integer tieValue,
			Integer secondHalfStartWeek, Integer playoffStartWeek, Integer superbowlWeek, Integer minPicks, Integer maxPicks, Integer tnoLosses){
		boolean update = false;
		if (seasonYear != null && !season.getSeasonYear().equals(seasonYear)) {
			season.setSeasonYear(seasonYear);
			update = true;
		}
		if (season.getCurrentSeason() != current) {
			season.setCurrentSeason(current);
			
			//	If updating the current season, make the current current season not current
			if (current) {
				Season currentCurrent = selectCurrentSeason();
				if (currentCurrent != null) {
					currentCurrent.setCurrentSeason(false);
					update(currentCurrent);
				}
			}
			
			update = true;
		}
		if (winValue != null && season.getWinValue() != winValue) {
			season.setWinValue(winValue);
			update = true;
		}
		if (lossValue != null && season.getLossValue() != lossValue) {
			season.setLossValue(lossValue);
			update = true;
		}
		if (tieValue != null && season.getTieValue() != tieValue) {
			season.setTieValue(tieValue);
			update = true;
		}
		if (secondHalfStartWeek != null && season.getSecondHalfStartWeek() != secondHalfStartWeek) {
			season.setSecondHalfStartWeek(secondHalfStartWeek);
			update = true;
		}
		if (playoffStartWeek != null && season.getPlayoffStartWeek() != playoffStartWeek) {
			season.setPlayoffStartWeek(playoffStartWeek);
			update = true;
		}
		if (superbowlWeek != null && season.getSuperbowlWeek() != superbowlWeek) {
			season.setSuperbowlWeek(superbowlWeek);
			update = true;
		}
		if (season.getMinPicks() != null && !season.getMinPicks().equals(minPicks)) {
			season.setMinPicks(minPicks);
			update = true;
		}
		else if (season.getMinPicks() == null && minPicks != null) {
			season.setMinPicks(minPicks);
			update = true;
		}
		if (season.getMaxPicks() != null && !season.getMaxPicks().equals(maxPicks)) {
			season.setMaxPicks(maxPicks);
			update = true;
		}
		else if (season.getMaxPicks() == null && maxPicks != null) {
			season.setMaxPicks(maxPicks);
			update = true;
		}
		if (season.getTnoAcceptableLosses() == null && tnoLosses != null) {
			season.setTnoAcceptableLosses(tnoLosses);
			update = true;
		}
		else if (season.getTnoAcceptableLosses() != null && !season.getTnoAcceptableLosses().equals(tnoLosses)) {
			season.setTnoAcceptableLosses(tnoLosses);
			update = true;
		}
		
		boolean success = false;
		if (update) {
			success = update(season);
		}
		
		return success;
	}

	@Override
	public Season generateSeasonWithDefaultValues(Integer seasonNumber, String seasonYear, boolean current) {
		return generateSeason(seasonNumber, seasonYear, current, null, null, null, null, null, null, null, null, null);
	}
}
