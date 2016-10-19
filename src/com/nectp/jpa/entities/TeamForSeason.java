package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.LinkedList;
import java.util.List;


/**
 * The persistent class for the teamforseason database table.
 * 
 */
@Entity
@Table(name="teamforseason")
@DiscriminatorValue("TFS")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="TeamForSeason.findAll", query="SELECT t FROM TeamForSeason t"),
	@NamedQuery(name="TeamForSeason.selectTfsByTeamSeason", 
				query="SELECT DISTINCT tfs FROM TeamForSeason tfs "
					+ "INNER JOIN FETCH tfs.season s "
					+ "INNER JOIN FETCH tfs.team t "
					+ "WHERE t.franchiseId = :franchiseId "
					+ "AND s.seasonNumber = :seasonNumber"),
	@NamedQuery(name="TeamForSeason.selectTfsByAbbrSeason",
				query="SELECT DISTINCT tfs FROM TeamForSeason tfs "
					+ "INNER JOIN FETCH tfs.season s "
					+ "WHERE tfs.teamAbbr = :teamAbbr "
					+ "AND s.seasonNumber = :seasonNumber"),
	@NamedQuery(name="TeamForSeason.selectTfsByCitySeason",
				query="SELECT DISTINCT tfs FROM TeamForSeason tfs "
					+ "INNER JOIN FETCH tfs.season s "
					+ "WHERE UPPER(tfs.teamCity) = :teamCity "
					+ "AND s.seasonNumber = :seasonNumber")
})
public class TeamForSeason extends AbstractTeamForSeason implements Serializable {
	private static final long serialVersionUID = 1L;

	@Basic(optional=false)
	private String name;
	
	@Basic(optional=false)
	private String teamAbbr;

	@Basic(optional=false)
	private String teamCity;
	
	@Basic(optional=false)
	private String homeHelmetUrl = "img/homeHelmet.png";
	
	@Basic(optional=false)
	private String awayHelmetUrl = "img/awayHelmet.png";
	
	//bi-directional many-to-one association to Game
	@OneToMany(mappedBy="homeTeam")
	private List<Game> homeGames;

	//bi-directional many-to-one association to Game
	@OneToMany(mappedBy="awayTeam")
	private List<Game> awayGames;

	//bi-directional many-to-one association to Pick
	@OneToMany(mappedBy="pickedTeam")
	private List<Pick> picksForTeam;

	//bi-directional many-to-one association to Division
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="division_divisionId", nullable=false)
	private Division division;

	//bi-directional many-to-one association to Stadium
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="stadium_stadiumId")
	private Stadium stadium;

	//bi-directional many-to-one association to Team
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="team_teamId", nullable=false)
	private Team team;

	public TeamForSeason() {
		homeGames = new LinkedList<Game>();
		awayGames = new LinkedList<Game>();
		picksForTeam = new LinkedList<Pick>();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTeamAbbr() {
		return this.teamAbbr;
	}

	public void setTeamAbbr(String teamAbbr) {
		this.teamAbbr = teamAbbr;
	}

	public String getTeamCity() {
		return this.teamCity;
	}

	public void setTeamCity(String teamCity) {
		this.teamCity = teamCity;
	}
	
	public String getHomeHelmetUrl() {
		return homeHelmetUrl;
	}
	
	public void setHomeHelmetUrl(String url) {
		this.homeHelmetUrl = url;
	}
	
	public String getAwayHelmetUrl() {
		return awayHelmetUrl;
	}
	
	public void setAwayHelmetUrl(String url) {
		this.awayHelmetUrl = url;
	}

	public List<Game> getHomeGames() {
		return this.homeGames;
	}

	public void setHomeGames(List<Game> homeGames) {
		this.homeGames = homeGames;
	}

	public Game addHomeGame(Game homeGame) {
		getHomeGames().add(homeGame);
		homeGame.setHomeTeam(this);

		return homeGame;
	}

	public Game removeHomeGame(Game homeGame) {
		getHomeGames().remove(homeGame);
		homeGame.setHomeTeam(null);

		return homeGame;
	}

	public List<Game> getAwayGames() {
		return this.awayGames;
	}

	public void setAwayGames(List<Game> awayGames) {
		this.awayGames = awayGames;
	}

	public Game addAwayGame(Game awayGame) {
		getAwayGames().add(awayGame);
		awayGame.setAwayTeam(this);

		return awayGame;
	}

	public Game removeAwayGame(Game awayGame) {
		getAwayGames().remove(awayGame);
		awayGame.setAwayTeam(null);

		return awayGame;
	}

	public List<Pick> getPicksForTeam() {
		return this.picksForTeam;
	}

	public void setPicksForTeam(List<Pick> picksForTeam) {
		this.picksForTeam = picksForTeam;
	}

	public Pick addPickForTeam(Pick pick) {
		getPicksForTeam().add(pick);
		pick.setPickedTeam(this);

		return pick;
	}

	public Pick removePickForTeam(Pick pick) {
		getPicksForTeam().remove(pick);
		pick.setPickedTeam(null);

		return pick;
	}

	public Division getDivision() {
		return this.division;
	}

	public void setDivision(Division division) {
		this.division = division;
	}

	public Stadium getStadium() {
		return this.stadium;
	}

	public void setStadium(Stadium stadium) {
		this.stadium = stadium;
	}

	public Team getTeam() {
		return this.team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	@Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.team != null ? this.team.hashCode() : 0);
        hash = 59 * hash + (this.getSeason() != null ? this.getSeason().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TeamForSeason)) {
            return false;
        }
        TeamForSeason other = (TeamForSeason) object;
        if ((this.getAbstractTeamForSeasonId() == null && other.getAbstractTeamForSeasonId() != null) || (this.getAbstractTeamForSeasonId() != null && !this.getAbstractTeamForSeasonId().equals(other.getAbstractTeamForSeasonId()))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entities.TeamForSeason[ abstractTeamForSeasonId=" + getAbstractTeamForSeasonId() + " ]";
    }
}
