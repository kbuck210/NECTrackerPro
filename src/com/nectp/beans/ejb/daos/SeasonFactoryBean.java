package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

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
			} catch (NoResultException e) {
				season = new Season();

				season.setSeasonNumber(seasonNumber);
				season.setSeasonYear(seasonYear);
				season.setCurrentSeason(current);

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
		
		return season;
	}

	@Override
	public Season generateSeasonWithDefaultValues(Integer seasonNumber, String seasonYear, boolean current) {
		return generateSeason(seasonNumber, seasonYear, current, null, null, null, null, null, null, null, null, null);
	}
}
