package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;


/**
 * The persistent class for the game database table.
 * 
 */
@Entity
@Table(name="game")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Game.findAll", query="SELECT g FROM Game g"),
	@NamedQuery(name="Game.selectGameByTeamsWeek", 
				query="SELECT DISTINCT g FROM Game g "
					+ "INNER JOIN FETCH g.homeTeam ht "
					+ "INNER JOIN FETCH g.awayTeam at "
					+ "INNER JOIN FETCH g.week w "
					+ "WHERE w.weekId = :weekId "
					+ "AND ht.abstractTeamForSeasonId = :homeTfsId "
					+ "AND at.abstractTeamForSeasonId = :awayTfsId"),
	@NamedQuery(name="Game.selectGameByTeamWeek", 
				query="SELECT DISTINCT g FROM Game g "
					+ "INNER JOIN FETCH g.homeTeam ht "
					+ "INNER JOIN FETCH g.awayTeam at "
					+ "INNER JOIN FETCH g.week w "
					+ "WHERE w.weekId = :weekId "
					+ "AND ht.abstractTeamForSeasonId = :atfsId "
					+ "OR at.abstractTeamForSeasonId = :atfsId")
})
public class Game implements Serializable, Comparable<Game> {
	private static final long serialVersionUID = 1L;

	public enum GameStatus {
		PREGAME,
		ACTIVE,
		HALFTIME,
		FINAL
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	private Long gameId;

	@Temporal(TemporalType.TIMESTAMP)
	@Basic(optional=false)
	private Calendar gameDate;

	@Basic(optional=false)
	private Integer gameStatus = GameStatus.PREGAME.ordinal();

	@Basic(optional=true)
	private Boolean homeFavoredSpread1 = null;
	
	@Basic(optional=true)
	private Boolean homeFavoredSpread2 = null;

	@Basic(optional=false)
	private Integer homeScore= 0;
	
	@Basic(optional=false)
	private Integer awayScore = 0;
	
	@Basic(optional=true)
	private String possession;

	@Basic(optional=false)
	private boolean primeTime = false;

	@Basic(optional=true)
	private boolean redZone;

	@Basic(optional=false)
	private BigDecimal spread1;

	@Basic(optional=true)
	private BigDecimal spread2;

	@Basic(optional=false)
	private String timeRemaining;

	//bi-directional many-to-one association to Stadium
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="stadium_stadiumId")
    private Stadium stadium;

	//bi-directional many-to-one association to TeamForSeason
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="homeTeam_abstractTeamForSeasonId", nullable=false)
    private TeamForSeason homeTeam;

	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="awayTeam_abstractTeamForSeasonId", nullable=false)
    private TeamForSeason awayTeam;

	//bi-directional many-to-one association to Week
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="week_weekId", nullable=false)
    private Week week;

	//bi-directional many-to-one association to Pick
	@OneToMany(mappedBy="game")
	private List<Pick> picks;

	public Game() {
		picks = new LinkedList<Pick>();
	}

	public Long getGameId() {
		return this.gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public int getAwayScore() {
		return this.awayScore;
	}

	public void setAwayScore(int awayScore) {
		this.awayScore = awayScore;
	}

	public Calendar getGameDate() {
		return this.gameDate;
	}

	public void setGameDate(Calendar gameDate) {
		this.gameDate = gameDate;
		if (gameDate != null) {
			int day = gameDate.get(GregorianCalendar.DAY_OF_WEEK);
			if (day != GregorianCalendar.SUNDAY) {
				primeTime = true;
			}
			else {
				int hour = gameDate.get(GregorianCalendar.HOUR_OF_DAY);
				primeTime = hour > 17;
			}
		}
	}

	public GameStatus getGameStatus() {
		if (gameStatus != null) {
			return GameStatus.values()[gameStatus];
		}
		else return null;
	}

	public void setGameStatus(GameStatus gameStatus) {
		if (gameStatus != null) {
			this.gameStatus = gameStatus.ordinal();
		}
	}

	public Boolean getHomeFavoredSpread1() {
		return homeFavoredSpread1;
	}
	
	public Boolean getHomeFavoredSpread2() {
		return homeFavoredSpread2;
	}

	public void setHomeFavoredSpread1(Boolean homeFavored) {
		this.homeFavoredSpread1 = homeFavored;
	}
	
	public void setHomeFavoredSpread2(Boolean homeFavored) {
		this.homeFavoredSpread2 = homeFavored;
	}

	public int getHomeScore() {
		return this.homeScore;
	}

	public void setHomeScore(int homeScore) {
		this.homeScore = homeScore;
	}

	public String getPossession() {
		return this.possession;
	}

	public void setPossession(String possession) {
		this.possession = possession;
	}

	public boolean getPrimeTime() {
		return this.primeTime;
	}

	public void setPrimeTime(boolean primeTime) {
		this.primeTime = primeTime;
	}

	public boolean getRedZone() {
		return this.redZone;
	}

	public void setRedZone(boolean redZone) {
		this.redZone = redZone;
	}

	public BigDecimal getSpread1() {
		return this.spread1;
	}

	public void setSpread1(BigDecimal spread1) {
		this.spread1 = spread1;
	}

	public BigDecimal getSpread2() {
		return this.spread2;
	}

	public void setSpread2(BigDecimal spread2) {
		this.spread2 = spread2;
	}

	public String getTimeRemaining() {
		return this.timeRemaining;
	}

	public void setTimeRemaining(String timeRemaining) {
		this.timeRemaining = timeRemaining;
	}

	public Stadium getStadium() {
		return this.stadium;
	}

	public void setStadium(Stadium stadium) {
		this.stadium = stadium;
	}

	public TeamForSeason getHomeTeam() {
		return this.homeTeam;
	}

	public void setHomeTeam(TeamForSeason homeTeam) {
		this.homeTeam = homeTeam;
	}

	public TeamForSeason getAwayTeam() {
		return this.awayTeam;
	}

	public void setAwayTeam(TeamForSeason awayTeam) {
		this.awayTeam = awayTeam;
	}

	public Week getWeek() {
		return this.week;
	}

	public void setWeek(Week week) {
		this.week = week;
	}

	public List<Pick> getPicks() {
		return this.picks;
	}

	public void setPicks(List<Pick> picks) {
		this.picks = picks;
	}

	public Pick addPick(Pick pick) {
		getPicks().add(pick);
		pick.setGame(this);

		return pick;
	}

	public Pick removePick(Pick pick) {
		getPicks().remove(pick);
		pick.setGame(null);

		return pick;
	}

	@Override
    public int compareTo(Game g) {
        return gameDate.compareTo(g.getGameDate());
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (gameId != null ? gameId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Game)) {
            return false;
        }
        Game other = (Game) object;
        if ((this.gameId == null && other.gameId != null) || this.gameId != null && !this.gameId.equals(other.gameId)) {
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.model.Game[ gameId=" + gameId + " ]";
    }
}