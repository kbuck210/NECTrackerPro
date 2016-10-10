package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;


/**
 * The persistent class for the week database table.
 * 
 */
@Entity
@Table(name="week")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Week.findAll", query="SELECT w FROM Week w"),
	@NamedQuery(name="Week.selectCurrentWeekInSeason",
				query="SELECT DISTINCT w FROM Week w "
					+ "INNER JOIN FETCH w.subseason ss "
					+ "INNER JOIN FETCH ss.season s "
					+ "INNER JOIN FETCH s.currentWeek cw "
					+ "WHERE s.seasonNumber = :seasonNumber "
					+ "AND cw.weekNumber = w.weekNumber"),
	@NamedQuery(name="Week.selectWeekByNumberInSeason", 
				query="SELECT DISTINCT w FROM Week w "
					+ "INNER JOIN FETCH w.subseason ss "
					+ "INNER JOIN FETCH ss.season s "
					+ "WHERE w.weekNumber = :weekNumber "
					+ "AND s.seasonNumber = :seasonNumber"),
	@NamedQuery(name="Week.selectWeeksInSeason", 
				query="SELECT w FROM Week w "
					+ "INNER JOIN FETCH w.subseason ss "
					+ "INNER JOIN FETCH ss.season s "
					+ "WHERE s.seasonNumber = :seasonNumber"),
	@NamedQuery(name="Week.selectWeeksThroughCurrentInSeason",
				query="SELECT w FROM Week w "
					+ "INNER JOIN FETCH w.subseason ss "
					+ "INNER JOIN FETCH ss.season s "
					+ "INNER JOIN FETCH s.currentWeek cw "
					+ "WHERE s.seasonNumber = :seasonNumber "
					+ "AND w.weekNumber <= cw.weekNumber"),
	@NamedQuery(name="Week.selectWeeksInRangeInSeason",
				query="SELECT w FROM Week w "
					+ "WHERE w.subseason.season.seasonNumber = :seasonNumber "
					+ "AND w.weekNumber >= :lowerBound "
					+ "AND w.weekNumber <= :upperBound")
})
public class Week implements Serializable, Comparable<Week> {
	private static final long serialVersionUID = 1L;

	public enum WeekStatus {
    	WAITING,
    	ACTIVE,
    	COMPLETED;
		
		public static WeekStatus getWeekStatusForString(final String statusString) {
    		if (statusString == null || statusString.trim().isEmpty()) return null;
    		
    		for (WeekStatus status : WeekStatus.values()) {
    			if (status.name().toLowerCase().trim().equals(statusString.toLowerCase().trim())) {
    				return status;
    			}
    		}
    		return null;
    	}
    }
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	private Long weekId;

	@Basic(optional=false)
	private Integer weekNumber;

	@Basic(optional=false)
	private Integer weekStatus;

	//bi-directional many-to-one association to Game
	@OneToMany(mappedBy="week")
	private List<Game> games;

	//bi-directional many-to-one association to Record
	@OneToMany(mappedBy="week")
	private List<Record> records;

	//bi-directional many-to-one association to Subseason
	@ManyToOne
	@JoinColumn(name="subseason_subseasonId", nullable=false)
	private Subseason subseason;

	public Week() {
		games = new LinkedList<Game>();
		records = new LinkedList<Record>();
	}

	public Long getWeekId() {
		return this.weekId;
	}

	public void setWeekId(Long weekId) {
		this.weekId = weekId;
	}

	public Integer getWeekNumber() {
		return this.weekNumber;
	}

	public void setWeekNumber(int weekNumber) {
		this.weekNumber = weekNumber;
	}

	public WeekStatus getWeekStatus() {
		if (weekStatus != null) {
			return WeekStatus.values()[weekStatus];
		}
		else return null;
	}

	public void setWeekStatus(WeekStatus weekStatus) {
		if (weekStatus != null) {
			this.weekStatus = weekStatus.ordinal();
		}
	}

	public List<Game> getGames() {
		return this.games;
	}

	public void setGames(List<Game> games) {
		this.games = games;
	}

	public Game addGame(Game game) {
		getGames().add(game);
		game.setWeek(this);

		return game;
	}

	public Game removeGame(Game game) {
		getGames().remove(game);
		game.setWeek(null);

		return game;
	}
	
	/** Return a list of the games in the week that are on the specified day
	 * 
	 * @param dayOfWeek an int in the format GregorianCalendar.DAY_OF_WEEK
	 * @return a list of the games in the week that are on the specified day
	 */
	public List<Game> getGamesInWeekForDay(int dayOfWeek) {
		List<Game> dayGames = new ArrayList<Game>();
		for (Game g : games) {
			if (g.getGameDate().get(GregorianCalendar.DAY_OF_WEEK) == dayOfWeek) {
				dayGames.add(g);
			}
		}
		return dayGames;
	}
	
	/** Returns a list of the games in the week that are prior to the Sunday games
	 * 
	 * @return a list of the games in the week that are prior to the Sunday games
	 */
	public List<Game> getEarlyGames() {
		List<Game> earlyGames = new ArrayList<Game>();
		for (Game g : games) {
			if (g.getGameDate().get(GregorianCalendar.DAY_OF_WEEK) > GregorianCalendar.MONDAY) {
				earlyGames.add(g);
			}
		}
		return earlyGames;
	}

	public List<Record> getRecords() {
		return this.records;
	}

	public void setRecords(List<Record> records) {
		this.records = records;
	}

	public Record addRecord(Record record) {
		getRecords().add(record);
		record.setWeek(this);

		return record;
	}

	public Record removeRecord(Record record) {
		getRecords().remove(record);
		record.setWeek(null);

		return record;
	}

	public Subseason getSubseason() {
		return this.subseason;
	}

	public void setSubseason(Subseason subseason) {
		this.subseason = subseason;
	}
	
	@Override
	public int compareTo(Week week2) {
		return this.getWeekNumber().compareTo(week2.getWeekNumber());
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (weekId != null ? weekId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Stadium)) {
            return false;
        }
        Week other = (Week) object;
        if ((this.weekId == null && other.weekId != null) || (this.weekId != null && !this.weekId.equals(other.weekId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entities.Stadium[ stadiumId=" + weekId + " ]";
    }
}