package com.nectp.beans.ejb.daos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.TeamStatisticService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.constants.Timezone;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Stadium.RoofType;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Record;

@Stateless
public class TeamStatisticServiceBean extends RecordServiceBean implements TeamStatisticService {
	private static final long serialVersionUID = -1785646140524328572L;
	
	private Logger log;
	
	public TeamStatisticServiceBean() {
		log = Logger.getLogger(TeamStatisticServiceBean.class.getName());
	}

	/**
	 * 
	 * @param games
	 * @return
	 */
	private List<Game> getGamesSortedByWeekNumber(List<Game> games) {
		Collections.sort(games, new Comparator<Game>() {
			@Override
			public int compare(Game g1, Game g2) {
				return g1.getWeek().getWeekNumber().compareTo(g2.getWeek().getWeekNumber());
			}
		});
		return games;
	}
	
	/**
	 * 
	 * @param records
	 * @return
	 */
	private List<Record> getRecordsSortedByWeekNumber(List<Record> records) {
		Collections.sort(records, new Comparator<Record>() {
			@Override
			public int compare(Record r1, Record r2) {
				return r1.getWeek().getWeekNumber().compareTo(r2.getWeek().getWeekNumber());
			}
		});
		return records;
	}
	
	/** Finds played games who's week matches the team's records' week, and adds the record to the aggregate
	 * 
	 * @param ragg
	 * @param records
	 * @param games
	 */
	private void addRecordsToAggregate(RecordAggregator ragg, List<Record> records, List<Game> games) {
		for (Record r : records) {
			Week weekForRecord = r.getWeek();
			for (Game g : games) {
				if (g.getWeek().equals(weekForRecord)) {
					ragg.addRecord(r);
					break;
				}
			}
		}
	}
	
	@Override
	public RecordAggregator getHomeAwayRecord(TeamForSeason tfs, boolean home, boolean againstSpread) {
		RecordAggregator homeAwayAgg = null;
		if (tfs == null) {
			log.warning("No team specified, can not get home/away record.");
		}
		else {
			homeAwayAgg = new RecordAggregator(tfs, againstSpread);
		}
		//	Get the games & the records for the TFS, sorting both by week number
		List<Game> games = getGamesSortedByWeekNumber(tfs.getHomeGames());
		List<Record> records = getRecordsSortedByWeekNumber(tfs.getRecords());
		
		//	For any home games whose week number matches a record's week number, add the record to the aggregate
		addRecordsToAggregate(homeAwayAgg, records, games);
		
		return homeAwayAgg;
	}

	@Override
	public RecordAggregator getDivisionRecord(TeamForSeason tfs, Division division, boolean againstSpread) {
		RecordAggregator divisionalAgg = null;
		if (tfs == null) {
			log.warning("No team specified, can not get divisional record.");
		}
		else {
			divisionalAgg = new RecordAggregator(tfs, againstSpread);
		}
		
		//	Create division query
		TypedQuery<Game> dq = em.createNamedQuery("Game.selectDivisionalGamesForTFS", Game.class);
		dq.setParameter("seasonNumber", tfs.getSeason().getSeasonNumber());
		List<Game> divisionalGames;
		try {
			divisionalGames = getGamesSortedByWeekNumber(dq.getResultList());
		} catch (Exception e) {
			log.severe("Exception caught retrieving divisional games: " + e.getMessage());
			e.printStackTrace();
			divisionalGames = new ArrayList<Game>();
		}
		
		//	Get the records for the TFS, sorting games & records by week number
		List<Record> records = getRecordsSortedByWeekNumber(tfs.getRecords());
		
		//	For any divisional games who's week number matches a record's week number, add to aggregate
		addRecordsToAggregate(divisionalAgg, records, divisionalGames);
		
		return divisionalAgg;
	}

	@Override
	public RecordAggregator getConferenceRecord(TeamForSeason tfs, Conference conference, boolean againstSpread) {
		RecordAggregator conferenceAgg = null;
		if (tfs == null) {
			log.warning("No team specified, can not get conference record.");
		}
		else {
			conferenceAgg = new RecordAggregator(tfs, againstSpread);
		}
		
		//	Create conference query
		TypedQuery<Game> dq = em.createNamedQuery("Game.selectConferenceGamesForTFS", Game.class);
		dq.setParameter("seasonNumber", tfs.getSeason().getSeasonNumber());
		List<Game> conferenceGames;
		try {
			conferenceGames = getGamesSortedByWeekNumber(dq.getResultList());
		} catch (Exception e) {
			log.severe("Exception caught retrieving conference games: " + e.getMessage());
			e.printStackTrace();
			conferenceGames = new ArrayList<Game>();
		}

		//	Get the records for the TFS, sorting games & records by week number
		List<Record> records = getRecordsSortedByWeekNumber(tfs.getRecords());

		//	For any conference game games who's week number matches a record's week number, add to aggregate
		addRecordsToAggregate(conferenceAgg, records, conferenceGames);

		return conferenceAgg;
	}

	@Override
	public RecordAggregator getRecentRecordAgainstOpponent(TeamForSeason tfs, TeamForSeason opponent, boolean againstSpread) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecordAggregator getPrimetimeRecord(TeamForSeason tfs, NEC recordType, boolean againstSpread) {
		RecordAggregator primetimeAgg = null;
		if (tfs == null) {
			log.warning("No team specified, can not get conference record.");
		}
		else {
			primetimeAgg = new RecordAggregator(tfs, againstSpread);
		}
		
		//	Create primetime query
		TypedQuery<Game> dq = em.createNamedQuery("Game.selectPrimetimeGamesForTFS", Game.class);
		dq.setParameter("seasonNumber", tfs.getSeason().getSeasonNumber());
		List<Game> primetimeGames;
		try {
			primetimeGames = getGamesSortedByWeekNumber(dq.getResultList());
		} catch (Exception e) {
			log.severe("Exception caught retrieving primetime games: " + e.getMessage());
			e.printStackTrace();
			primetimeGames = new ArrayList<Game>();
		}
		
		//	Get the records for the TFS, sorting games & records by week number
		List<Record> records = getRecordsSortedByWeekNumber(tfs.getRecords());
		
		//	For any primetime game who's week number matches a record's week number, add to aggregate
		addRecordsToAggregate(primetimeAgg, records, primetimeGames);

		return primetimeAgg;
	}

	@Override
	public RecordAggregator getRecordByDateTime(TeamForSeason tfs, Integer month, Integer dayOfWeek,
			Integer kickoffHour, boolean againstSpread) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecordAggregator getRecordForStadium(TeamForSeason tfs, Stadium stadium, RoofType roofType, boolean againstSpread) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecordAggregator getRecordByTimezone(TeamForSeason tfs, Timezone timezone, boolean againstSpread) {
		// TODO Auto-generated method stub
		return null;
	}

}
