package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.LinkedList;
import java.util.List;


/**
 * The persistent class for the abstractteamforseason database table.
 * 
 */
@Entity
@Table(name="abstractteamforseason")
@XmlRootElement
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="Type")
@NamedQuery(name="AbstractTeamForSeason.findAll", query="SELECT a FROM AbstractTeamForSeason a")
public class AbstractTeamForSeason implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	private Long abstractTeamForSeasonId;

	@Basic(optional=false)
	private String nickname;
	
	@Basic(optional=false)
	private String excelPrintName;

	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="season_seasonNumber", nullable=false)
    private Season season;

	//bi-directional many-to-one association to Record
	@OneToMany(mappedBy="team")
	private List<Record> records;

	public AbstractTeamForSeason() {
		records = new LinkedList<Record>();
	}

	public Long getAbstractTeamForSeasonId() {
		return this.abstractTeamForSeasonId;
	}

	public void setAbstractTeamForSeasonId(Long abstractTeamForSeasonId) {
		this.abstractTeamForSeasonId = abstractTeamForSeasonId;
	}

	public String getNickname() {
		return this.nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getExcelPrintName() {
		return excelPrintName;
	}

	public void setExcelPrintName(String excelPrintName) {
		this.excelPrintName = excelPrintName;
	}

	public Season getSeason() {
		return season;
	}

	public void setSeason(Season season) {
		this.season = season;
	}

	public List<Record> getRecords() {
		return this.records;
	}

	public void setRecords(List<Record> records) {
		this.records = records;
	}

	public Record addRecord(Record record) {
		getRecords().add(record);
		record.setTeam(this);

		return record;
	}

	public Record removeRecord(Record record) {
		getRecords().remove(record);
		record.setTeam(null);

		return record;
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (abstractTeamForSeasonId != null ? abstractTeamForSeasonId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AbstractTeamForSeason)) {
            return false;
        }
        AbstractTeamForSeason other = (AbstractTeamForSeason) object;
        if ((this.abstractTeamForSeasonId == null && other.abstractTeamForSeasonId != null) || (this.abstractTeamForSeasonId != null && !this.abstractTeamForSeasonId.equals(other.abstractTeamForSeasonId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entities.AbstractTeamForSeason[ abstractTeamForSeasonId=" + abstractTeamForSeasonId + " ]";
    }
}