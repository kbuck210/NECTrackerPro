package com.nectp.jpa.entities;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import com.nectp.jpa.constants.NEC;


/**
 * The persistent class for the record database table.
 * 
 */
@Entity
@Table(name="record")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Record.findAll", query="SELECT r FROM Record r"),
	@NamedQuery(name="Record.selectWeekRecordForAtfs",
				query="SELECT DISTINCT r FROM Record r "
					+ "WHERE r.week.weekId = :weekId "
					+ "AND r.team.abstractTeamForSeasonId = :atfsId "
					+ "AND r.recordType = :recordType")
})
public class Record implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	private Long recordId;

	@Basic(optional=false)
	private Integer recordType;

	@Basic(optional=false)
	private int wins = 0;
	
	@Basic(optional=false)
	private int winModifier = 0;
	
	@Basic(optional=false)
	private int losses = 0;
	
	@Basic(optional=false)
	private int lossModifier = 0;
	
	@Basic(optional=false)
	private int ties = 0;
	
	@Basic(optional=false)
	private int tieModifier = 0;

	//bi-directional many-to-one association to AbstractTeamForSeason
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="abstractteamforseason_abstractTeamForSeasonId", nullable=false)
	private AbstractTeamForSeason team;

	//bi-directional many-to-one association to Week
	@ManyToOne
	@JoinColumn(name="weekId")
	private Week week;
	
	@OneToMany(mappedBy="applicableRecord")
	private List<Pick> picksInRecord = new LinkedList<Pick>();

	public Record() {
	}

	public Long getRecordId() {
		return this.recordId;
	}

	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	public int getLosses() {
		return this.losses;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}
	
	public void addLoss() {
		this.losses += 1;
	}
	
	public int getLossModifier() {
		return lossModifier;
	}
	
	public void setLossModifier(int lossModifier) {
		this.lossModifier = lossModifier;
	}

	public NEC getRecordType() {
		return NEC.values()[this.recordType];
	}

	public void setRecordType(NEC recordType) {
		this.recordType = recordType.ordinal();
	}

	public int getTies() {
		return this.ties;
	}

	public void setTies(int ties) {
		this.ties = ties;
	}
	
	public void addTie() {
		this.ties += 1;
	}
	
	public int getTieModifier() {
		return tieModifier;
	}
	
	public void setTieModifier(int tieModifier) {
		this.tieModifier = tieModifier;
	}
 
	public int getWins() {
		return this.wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}
	
	public void addWin() {
		this.wins += 1;
	}
	
	public int getWinModifier() {
		return winModifier;
	}
	
	public void setWinModifier(int winModifier) {
		this.winModifier = winModifier;
	}

	public AbstractTeamForSeason getTeam() {
		return this.team;
	}

	public void setTeam(AbstractTeamForSeason team) {
		this.team = team;
	}

	public Week getWeek() {
		return this.week;
	}

	public void setWeek(Week week) {
		this.week = week;
	}
	
	public List<Pick> getPicksInRecord() {
		return picksInRecord;
	}
	
	public void addPickInRecord(Pick pick) {
		picksInRecord.add(pick);
	}
	
	public void removePickInRecord(Pick pick) {
		picksInRecord.remove(pick);
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (recordId != null ? recordId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AbstractTeamForSeason)) {
            return false;
        }
        Record other = (Record) object;
        if ((this.recordId == null && other.recordId != null) || (this.recordId != null && !this.recordId.equals(other.recordId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entities.Record[ recordId=" + recordId + " ]";
    }
}