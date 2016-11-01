package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.LinkedList;
import java.util.List;


/**
 * The persistent class for the season database table.
 * 
 */
@Entity
@Table(name="season")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Season.findAll", query="SELECT s FROM Season s ORDER BY s.seasonNumber DESC"),
	@NamedQuery(name="Season.selectCurrentSeason", 
				query="SELECT DISTINCT s FROM Season s "
					+ "WHERE s.currentSeason = true"),
	@NamedQuery(name="Season.selectByYear", 
				query="SELECT DISTINCT s FROM Season s "
					+ "WHERE s.seasonYear = :year")
})
public class Season implements Serializable, Comparable<Season> {
	private static final long serialVersionUID = 1L;

	@Id
	@Basic(optional = false)
	private Integer seasonNumber;

	@Basic(optional = false)
	private boolean currentSeason;
	
	@Basic(optional = false)
	private int playoffStartWeek = 18;

	@Basic(optional = false)
	private String seasonYear;

	@Basic(optional = false)
	private int secondHalfStartWeek = 10;

	@Basic(optional = false)
	private int superbowlWeek = 22;

	@Basic(optional = false)
	private int winValue = 1;
	
	@Basic(optional = false)
	private int lossValue = 1;
	
	@Basic(optional = false)
	private int tieValue = 0;
	
	@Basic(optional = true)
	private Integer minPicks = null;
	
	@Basic(optional = true)
	private Integer maxPicks = null;
	
	@Basic(optional = false)
	private Integer tnoAcceptableLosses = 2;

	//bi-directional many-to-one association to PlayerForSeason
	@OneToMany(mappedBy="season")
	private List<PlayerForSeason> players;

	//bi-directional many-to-one association to PrizeForSeason
	@OneToMany(mappedBy="season")
	private List<PrizeForSeason> prizes;

	//bi-directional many-to-one association to Subseason
	@OneToMany(mappedBy="season")
	private List<Subseason> subseasons;

	//bi-directional many-to-one association to TeamForSeason
	@OneToMany(mappedBy="season")
	private List<TeamForSeason> teams;
	
	//one-directional one-to-one association to Address
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="week_weekId")
	private Week currentWeek;

	public Season() {
		players = new LinkedList<PlayerForSeason>();
		prizes = new LinkedList<PrizeForSeason>();
		subseasons = new LinkedList<Subseason>();
		teams = new LinkedList<TeamForSeason>();
	}

	public Integer getSeasonNumber() {
		return this.seasonNumber;
	}

	public void setSeasonNumber(Integer seasonNumber) {
		this.seasonNumber = seasonNumber;
	}

	public boolean getCurrentSeason() {
		return this.currentSeason;
	}

	public void setCurrentSeason(boolean currentSeason) {
		this.currentSeason = currentSeason;
	}

	public int getLossValue() {
		return this.lossValue;
	}

	public void setLossValue(int lossValue) {
		this.lossValue = lossValue;
	}

	public int getPlayoffStartWeek() {
		return this.playoffStartWeek;
	}

	public void setPlayoffStartWeek(int playoffStartWeek) {
		this.playoffStartWeek = playoffStartWeek;
	}

	public String getSeasonYear() {
		return this.seasonYear;
	}

	public void setSeasonYear(String seasonYear) {
		this.seasonYear = seasonYear;
	}

	public int getSecondHalfStartWeek() {
		return this.secondHalfStartWeek;
	}

	public void setSecondHalfStartWeek(int secondHalfStartWeek) {
		this.secondHalfStartWeek = secondHalfStartWeek;
	}

	public int getSuperbowlWeek() {
		return this.superbowlWeek;
	}

	public void setSuperbowlWeek(int superbowlWeek) {
		this.superbowlWeek = superbowlWeek;
	}

	public int getTieValue() {
		return this.tieValue;
	}

	public void setTieValue(int tieValue) {
		this.tieValue = tieValue;
	}

	public int getWinValue() {
		return this.winValue;
	}

	public void setWinValue(int winValue) {
		this.winValue = winValue;
	}
	
	public Integer getMinPicks() {
		return minPicks;
	}
	
	public void setMinPicks(Integer minPicks) {
		this.minPicks = minPicks;
	}
	
	public Integer getMaxPicks() {
		return maxPicks;
	}
	
	public void setMaxPicks(Integer maxPicks) {
		this.maxPicks = maxPicks;
	}

	public Integer getTnoAcceptableLosses() {
		return tnoAcceptableLosses;
	}

	public void setTnoAcceptableLosses(Integer tnoAcceptableLosses) {
		this.tnoAcceptableLosses = tnoAcceptableLosses;
	}

	public List<PlayerForSeason> getPlayers() {
		return this.players;
	}

	public void setPlayers(List<PlayerForSeason> players) {
		this.players = players;
	}

	public PlayerForSeason addPlayer(PlayerForSeason player) {
		getPlayers().add(player);
		player.setSeason(this);

		return player;
	}

	public PlayerForSeason removePlayer(PlayerForSeason player) {
		getPlayers().remove(player);
		player.setSeason(null);

		return player;
	}

	public List<PrizeForSeason> getPrizes() {
		return this.prizes;
	}

	public void setPrizes(List<PrizeForSeason> prizes) {
		this.prizes = prizes;
	}

	public PrizeForSeason addPrize(PrizeForSeason prize) {
		getPrizes().add(prize);
		prize.setSeason(this);

		return prize;
	}

	public PrizeForSeason removePrize(PrizeForSeason prize) {
		getPrizes().remove(prize);
		prize.setSeason(null);

		return prize;
	}

	public List<Subseason> getSubseasons() {
		return this.subseasons;
	}

	public void setSubseasons(List<Subseason> subseasons) {
		this.subseasons = subseasons;
	}

	public Subseason addSubseason(Subseason subseason) {
		getSubseasons().add(subseason);
		subseason.setSeason(this);

		return subseason;
	}

	public Subseason removeSubseason(Subseason subseason) {
		getSubseasons().remove(subseason);
		subseason.setSeason(null);

		return subseason;
	}

	public List<TeamForSeason> getTeams() {
		return this.teams;
	}

	public void setTeams(List<TeamForSeason> teams) {
		this.teams = teams;
	}

	public TeamForSeason addTeam(TeamForSeason team) {
		getTeams().add(team);
		team.setSeason(this);

		return team;
	}

	public TeamForSeason removeTeam(TeamForSeason team) {
		getTeams().remove(team);
		team.setSeason(null);

		return team;
	}
	
	public Week getCurrentWeek() {
		return currentWeek;
	}
	
	public void setCurrentWeek(Week currentWeek) {
		this.currentWeek = currentWeek;
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (seasonNumber != null ? seasonNumber.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Season)) {
            return false;
        }
        Season other = (Season) object;
        if ((this.seasonNumber == null && other.seasonNumber != null) || (this.seasonNumber != null && !this.seasonNumber.equals(other.seasonNumber))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.entities.jpa.Season[ seasonNumber=" + seasonNumber + " ]";
    }

	@Override
	public int compareTo(Season otherSeason) {
		return seasonNumber.compareTo(otherSeason.seasonNumber);
	}
}
