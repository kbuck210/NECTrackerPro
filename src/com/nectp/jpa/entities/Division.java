package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.LinkedList;
import java.util.List;


/**
 * The persistent class for the division database table.
 * 
 */
@Entity
@Table(name="division")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Division.findAll", query="SELECT d FROM Division d"),
	@NamedQuery(name="Division.selectByRegionConference", 
				query="SELECT DISTINCT d FROM Division d "
					+ "INNER JOIN FETCH d.conference c "
					+ "WHERE d.region = :region "
					+ "AND c.conferenceId = :conferenceId")
})
public class Division implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Region {
		NORTH,
		SOUTH,
		EAST,
		WEST;

		@Override
		public String toString() {
			switch(this) {
			case NORTH:
				return "NORTH";
			case SOUTH:
				return "SOUTH";
			case EAST:
				return "EAST";
			case WEST:
				return "WEST";
			default:
				return null;
			}
		}
		
		public static Region getRegionForString(String region) {
			Region reg = null;
			if (region != null) {
				for (Region r : Region.values()) {
					if (r.name().toUpperCase().equals(region.toUpperCase())) {
						reg = r;
						break;
					}
				}
			}
			return reg;
		}
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	private Integer divisionId;

	@Basic(optional=false)
	private Integer region;

	//bi-directional many-to-one association to Conference
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="conference_conferenceId", nullable=false)
    private Conference conference;

	//bi-directional many-to-one association to TeamForSeason
	@OneToMany(mappedBy="division")
	private List<TeamForSeason> teamHistory;

	public Division() {
		teamHistory = new LinkedList<TeamForSeason>();
	}

	public Integer getDivisionId() {
		return this.divisionId;
	}

	public void setDivisionId(Integer divisionId) {
		this.divisionId = divisionId;
	}

	public Region getRegion() {
		if (region != null) {
			return Region.values()[region];
		}
		else return null;
	}

	public void setRegion(Region region) {
		if (region != null) {
			this.region = region.ordinal();
		}
	}

	public Conference getConference() {
		return this.conference;
	}

	public void setConference(Conference conference) {
		this.conference = conference;
	}

	public List<TeamForSeason> getTeamHistory() {
		return this.teamHistory;
	}

	public void setTeamHistory(List<TeamForSeason> teamforseasons) {
		this.teamHistory = teamforseasons;
	}

	public TeamForSeason addTeamHistory(TeamForSeason teamforseason) {
		getTeamHistory().add(teamforseason);
		teamforseason.setDivision(this);

		return teamforseason;
	}

	public TeamForSeason removeTeamHistory(TeamForSeason teamforseason) {
		getTeamHistory().remove(teamforseason);
		teamforseason.setDivision(null);

		return teamforseason;
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (divisionId != null ? divisionId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Division)) {
            return false;
        }
        Division other = (Division) object;
        if ((this.divisionId == null && other.divisionId != null) || (this.divisionId != null && !this.divisionId.equals(other.divisionId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.model.Division[ divisionId=" + divisionId + " ]";
    }
}