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
					+ "AND r.recordType = :recordType"),
	@NamedQuery(name="Record.selectHomeRecordForTfs",
				query="SELECT r FROM Record r, Week w "
					+ "INNER JOIN FETCH w.game g "
					+ "WHERE r.week.weekId = w.weekId "
					+ "AND g.week.weekId = w.weekId "
					+ "AND g.homeTeam.abstractTeamForSeasonId = :atfsId")
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
	private int winsATS1 = 0;
	
	@Basic(optional=false)
	private int winsATS2 = 0;
	
	@Basic(optional=false)
	private int winModifier = 0;

	@Basic(optional=false)
	private int losses = 0;

	@Basic(optional=false)
	private int lossesATS1 = 0;
	
	@Basic(optional=false)
	private int lossesATS2 = 0;
	
	@Basic(optional=false)
	private int lossModifier = 0;
	
	@Basic(optional=false)
	private int ties = 0;
	
	@Basic(optional=false)
	private int tiesATS1 = 0;
	
	@Basic(optional=false)
	private int tiesATS2 = 0;
	
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

	public NEC getRecordType() {
		return NEC.values()[this.recordType];
	}

	public void setRecordType(NEC recordType) {
		this.recordType = recordType.ordinal();
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
	
	public int getWinsATS1() {
		return this.winsATS1;
	}
	
	public void setWinsATS1(int winsATS) {
		this.winsATS1 = winsATS;
	}
	
	public void addWinATS1() {
		winsATS1 += 1;
	}
	
	public int getWinsATS2() {
		return winsATS2;
	}
	
	public void setWinsATS2(int winsATS) {
		this.winsATS2 = winsATS;
	}
	
	public void addWinATS2() {
		this.winsATS2 += 1;
	}
	
	public int getWinModifier() {
		return winModifier;
	}
	
	public void setWinModifier(int winModifier) {
		this.winModifier = winModifier;
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
	
	public int getLossesATS1() {
		return lossesATS1;
	}
	
	public void setLossesATS1(int lossesATS) {
		this.lossesATS1 = lossesATS;
	}
	
	public void addLossATS1() {
		this.lossesATS1 += 1;
	}
	
	public int getLossesATS2() {
		return lossesATS2;
	}
	
	public void setLossesATS2(int lossesATS) {
		this.lossesATS2 = lossesATS;
	}
	
	public void addLossATS2() {
		this.lossesATS2 += 1;
	}
	
	public int getLossModifier() {
		return lossModifier;
	}
	
	public void setLossModifier(int lossModifier) {
		this.lossModifier = lossModifier;
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
	
	public int getTiesATS1() {
		return tiesATS1;
	}
	
	public void setTiesATS1(int tiesATS) {
		this.tiesATS1 = tiesATS;
	}
	
	public void addTieATS1() {
		this.tiesATS1 += 1;
	}
	
	public int getTiesATS2() {
		return tiesATS2;
	}
	
	public void setTiesATS2(int tiesATS) {
		this.tiesATS2 = tiesATS;
	}
	
	public void addTieATS2() {
		this.tiesATS2 += 1;
	}
	
	public int getTieModifier() {
		return tieModifier;
	}
	
	public void setTieModifier(int tieModifier) {
		this.tieModifier = tieModifier;
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
