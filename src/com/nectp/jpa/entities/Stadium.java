package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import com.nectp.jpa.constants.Timezone;

import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;


/**
 * The persistent class for the stadium database table.
 * 
 */
@Entity
@Table(name="stadium")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Stadium.findAll", query="SELECT s FROM Stadium s"),
	@NamedQuery(name="Stadium.selectStadiumByName", 
				query="SELECT DISTINCT s FROM Stadium s "
					+ "WHERE s.stadiumName = :stadiumName")
})
public class Stadium implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum RoofType {
    	OPEN,
    	DOME,
    	RETRACTABLE;
    	
    	public static RoofType getRoofTypeForString(String type) {
    		RoofType rtype = null;
    		if (type != null) {
    			for (RoofType rt : RoofType.values()) {
    				if (rt.name().toLowerCase().equals(type.toLowerCase())) {
    					rtype = rt;
    					break;
    				}
    			}
    			
    		}
    		return rtype;
    	}
    }
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long stadiumId;

	@Basic(optional=false)
	private Long capacity;

	@Basic(optional=false)
	private boolean international;

	@Basic(optional=false)
	private Integer roofType;

	@Basic(optional=false)
	private String stadiumName;

	@Basic(optional=false)
	private Integer timezone;

	//bi-directional many-to-one association to Game
	@OneToMany(mappedBy="stadium")
	private List<Game> gamesPlayedInStadium;

	//one-directional one-to-one association to Address
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="address_addressId", nullable=false)
	private Address address;

	//bi-directional many-to-one association to TeamForSeason
	@OneToMany(mappedBy="stadium")
	private List<TeamForSeason> teamsUsingStadium;

	public Stadium() {
		gamesPlayedInStadium = new LinkedList<Game>();
		teamsUsingStadium = new LinkedList<TeamForSeason>();
	}

	public Long getStadiumId() {
		return this.stadiumId;
	}

	public void setStadiumId(Long stadiumId) {
		this.stadiumId = stadiumId;
	}

	public Long getCapacity() {
		return this.capacity;
	}

	public void setCapacity(Long capacity) {
		this.capacity = capacity;
	}

	public boolean getInternational() {
		return this.international;
	}

	public void setInternational(boolean international) {
		this.international = international;
	}

	public RoofType getRoofType() {
		if (roofType != null) {
			return RoofType.values()[roofType];
		}
		else return null;
	}

	public void setRoofType(RoofType roofType) {
		if (roofType != null) {
			this.roofType = roofType.ordinal();
		}
	}

	public String getStadiumName() {
		return this.stadiumName;
	}

	public void setStadiumName(String stadiumName) {
		this.stadiumName = stadiumName;
	}

	public TimeZone getTimezone() {
 		return TimeZone.getTimeZone(Timezone.values()[timezone].toString());
	}

	public void setTimezone(Timezone timezone) {
		if (timezone != null) {
			this.timezone = timezone.ordinal();
		}
	}

	public List<Game> getGamesPlayedInStadium() {
		return this.gamesPlayedInStadium;
	}

	public void setGamesPlayedInStadium(List<Game> gamesPlayedInStadium) {
		this.gamesPlayedInStadium = gamesPlayedInStadium;
	}

	public Game addGamePlayedInStadium(Game game) {
		getGamesPlayedInStadium().add(game);
		game.setStadium(this);

		return game;
	}

	public Game removeGamePlayedInStadium(Game game) {
		getGamesPlayedInStadium().remove(game);
		game.setStadium(null);

		return game;
	}

	public Address getAddress() {
		return this.address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public List<TeamForSeason> getTeamsUsingStadium() {
		return this.teamsUsingStadium;
	}

	public void setTeamsUsingStadium(List<TeamForSeason> teamsUsingStadium) {
		this.teamsUsingStadium = teamsUsingStadium;
	}

	public TeamForSeason addTeamUsingStadium(TeamForSeason team) {
		getTeamsUsingStadium().add(team);
		team.setStadium(this);

		return team;
	}

	public TeamForSeason removeTeamUsingStadium(TeamForSeason team) {
		getTeamsUsingStadium().remove(team);
		team.setStadium(null);

		return team;
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (stadiumId != null ? stadiumId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Stadium)) {
            return false;
        }
        Stadium other = (Stadium) object;
        if ((this.stadiumId == null && other.stadiumId != null) || (this.stadiumId != null && !this.stadiumId.equals(other.stadiumId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entities.Stadium[ stadiumId=" + stadiumId + " ]";
    }
}