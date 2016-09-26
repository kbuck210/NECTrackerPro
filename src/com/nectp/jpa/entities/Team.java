package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.LinkedList;
import java.util.List;


/**
 * The persistent class for the team database table.
 * 
 */
@Entity
@Table(name="team")
@DiscriminatorValue("T")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Team.findAll", query="SELECT t FROM Team t"),
	@NamedQuery(name="Team.selectTeamByName", 
				query="SELECT t FROM Team t "
					+ "WHERE t.teamName = :teamName"),
	@NamedQuery(name="Team.selectTeamByAbbr", 
				query="SELECT t FROM Team t "
					+ "WHERE t.teamAbbr = :teamAbbr"),
	@NamedQuery(name="Team.selectTeamByCity",
				query="SELECT t FROM Team t WHERE t.teamCity = :teamCity"),
	@NamedQuery(name="Team.selectTeamByNameCity", 
				query="SELECT DISTINCT t FROM Team t "
					+ "WHERE t.teamName = :teamName "
					+ "AND t.teamCity = :teamCity")
})
public class Team extends AbstractTeam implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Basic(optional=false)
	private String teamAbbr;

	@Basic(optional=false)
	private String teamCity;

	//bi-directional many-to-one association to TeamForSeason
	@OneToMany(mappedBy="team")
	private List<TeamForSeason> teamInstances;

	public Team() {
		teamInstances = new LinkedList<TeamForSeason>();
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

	public List<TeamForSeason> getTeamInstances() {
		return this.teamInstances;
	}

	public void setTeamInstances(List<TeamForSeason> teamInstances) {
		this.teamInstances = teamInstances;
	}

	public TeamForSeason addTeamInstance(TeamForSeason team) {
		getTeamInstances().add(team);
		team.setTeam(this);

		return team;
	}

	public TeamForSeason removeTeamforseason(TeamForSeason team) {
		getTeamInstances().remove(team);
		team.setTeam(null);

		return team;
	}

	@Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.teamCity != null ? this.teamCity.hashCode() : 0);
        hash = 11 * hash + (this.teamAbbr != null ? this.teamAbbr.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Team)) {
            return false;
        }
        Team other = (Team) object;
        if ((this.getAbstractTeamId() == null && other.getAbstractTeamId() != null) || (this.getAbstractTeamId() != null && !this.getAbstractTeamId().equals(other.getAbstractTeamId()))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entities.Team[ teamId=" + getAbstractTeamId() + " ]";
    }
}