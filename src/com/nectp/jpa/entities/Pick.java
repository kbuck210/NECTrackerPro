package com.nectp.jpa.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * The persistent class for the pick database table.
 * 
 */
@Entity
@Table(name="pick")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Pick.selectPlayerPickForGame",
				query="SELECT DISTINCT p FROM Pick p "
					+ "INNER JOIN FETCH p.player pl "
					+ "INNER JOIN FETCH p.game g "
					+ "WHERE pl.abstractTeamForSeasonId = :playerId "
					+ "AND g.gameId = :gameId"),
	@NamedQuery(name="Pick.selectPlayerPicksForWeek",
				query="SELECT p FROM Pick p "
					+ "INNER JOIN FETCH p.player pl "
					+ "INNER JOIN FETCH p.game g "
					+ "INNER JOIN FETCH g.week w "
					+ "WHERE pl.abstractTeamForSeasonId = :playerId "
					+ "AND w.weekId = :weekId"),
	@NamedQuery(name="Pick.selectPicksForGameByType",
				query="SELECT p FROM Pick p "
					+ "INNER JOIN FETCH p.game g "
					+ "INNER JOIN FETCH p.applicableRecord r "
					+ "WHERE g.gameId = :gameId "
					+ "AND r.recordType = :recordType")
})
@NamedQuery(name="Pick.findAll", query="SELECT p FROM Pick p")
public class Pick implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum PickType {
        SPREAD1,
        SPREAD2,
        STRAIGHT_UP
    }
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	private Long pickId;

	@Basic(optional=false)
	private Integer pickType;
	
	@Basic(optional=false)
	private BigDecimal spread;
	
	@Basic(optional=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar submittedTime;

	@Basic(optional=false)
	private Boolean pickLocked = false;
	
	//bi-directional many-to-one association to Game
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="game_gameId", nullable=false)
    private Game game;

	//bi-directional many-to-one association to PlayerForSeason
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="player_abstractTeamForSeasonId", nullable=false)
    private PlayerForSeason player;

	//bi-directional many-to-one association to TeamForSeason
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="pickedTeam_abstractTeamForSeasonId", nullable=false)
    private TeamForSeason pickedTeam;
	
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="applicableRecord_recordId", nullable=false)
    private Record applicableRecord;

	public Pick() {
	}

	public Long getPickId() {
		return this.pickId;
	}

	public void setPickId(Long pickId) {
		this.pickId = pickId;
	}

	public PickType getPickType() {
		if (pickType != null) {
			return PickType.values()[pickType];
		}
		else return null;
	}

	public void setPickType(PickType pickType) {
		if (pickType != null) {
			this.pickType = pickType.ordinal();
			if (game != null) {
				if (PickType.SPREAD1.equals(pickType)) {
					spread = game.getSpread1();
				}
				else if (PickType.SPREAD2.equals(pickType)) {
					spread = game.getSpread2();
				}
				else {
					spread = BigDecimal.ZERO;
				}
			}
		}
	}

	public Calendar getSubmittedTime() {
		return submittedTime;
	}

	public void setSubmittedTime(Calendar submittedTime) {
		this.submittedTime = submittedTime;
	}

	public Boolean getPickLocked() {
		return pickLocked;
	}

	public void setPickLocked(Boolean pickLocked) {
		this.pickLocked = pickLocked;
	}

	public Game getGame() {
		return this.game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public PlayerForSeason getPlayer() {
		return this.player;
	}

	public void setPlayer(PlayerForSeason playerforseason) {
		this.player = playerforseason;
	}

	public TeamForSeason getPickedTeam() {
		return this.pickedTeam;
	}

	public void setPickedTeam(TeamForSeason teamforseason) {
		this.pickedTeam = teamforseason;
	}
	
    public Record getApplicableRecord() {
    	return applicableRecord;
    }
    
    public void setApplicableRecord(Record record) {
    	this.applicableRecord = record;
    }

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (pickId != null ? pickId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Pick)) {
            return false;
        }
        Pick other = (Pick) object;
        if ((this.pickId == null && other.pickId != null) || (this.pickId != null && !this.pickId.equals(other.pickId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entities.Pick[ pickId=" + pickId + " ]";
    }
}