package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import com.nectp.jpa.constants.NEC;

import java.util.LinkedList;
import java.util.List;


/**
 * The persistent class for the subseason database table.
 * 
 */
@Entity
@Table(name="subseason")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Subseason.findAll", query="SELECT s FROM Subseason s"),
	@NamedQuery(name="Subseason.selectSubseasonByTypeInSeason", 
				query="SELECT DISTINCT ss FROM Subseason ss "
					+ "INNER JOIN FETCH ss.season s "
					+ "WHERE ss.subseasonType = :subseasonType "
					+ "AND s.seasonNumber = :seasonNumber")
})
public class Subseason implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	private Long subseasonId;

	@Basic(optional=false)
	private Integer subseasonType;

	//bi-directional many-to-one association to PrizeForSeason
	@OneToMany(mappedBy="subseason")
	private List<PrizeForSeason> prizesForSubseason;

	//bi-directional many-to-one association to Season
	@ManyToOne
	@JoinColumn(name="season_seasonNumber", nullable=false)
	private Season season;

	//bi-directional many-to-one association to Week
	@OneToMany(mappedBy="subseason")
	private List<Week> weeks;

	public Subseason() {
		prizesForSubseason = new LinkedList<PrizeForSeason>();
		weeks = new LinkedList<Week>();
	}

	public Long getSubseasonId() {
		return this.subseasonId;
	}

	public void setSubseasonId(Long subseasonId) {
		this.subseasonId = subseasonId;
	}

	public NEC getSubseasonType() {
		return NEC.values()[this.subseasonType];
	}

	public void setSubseasonType(NEC subseasonType) {
		this.subseasonType = subseasonType.ordinal();
	}

	public List<PrizeForSeason> getPrizesForSubseason() {
		return this.prizesForSubseason;
	}

	public void setPrizeforseasons(List<PrizeForSeason> prizesForSubseason) {
		this.prizesForSubseason = prizesForSubseason;
	}

	public PrizeForSeason addPrizeForSubseason(PrizeForSeason prizeForSubseason) {
		getPrizesForSubseason().add(prizeForSubseason);
		prizeForSubseason.setSubseason(this);

		return prizeForSubseason;
	}

	public PrizeForSeason removePrizeForSubseason(PrizeForSeason prizeForSubseason) {
		getPrizesForSubseason().remove(prizeForSubseason);
		prizeForSubseason.setSubseason(null);

		return prizeForSubseason;
	}

	public Season getSeason() {
		return this.season;
	}

	public void setSeason(Season season) {
		this.season = season;
	}

	public List<Week> getWeeks() {
		return this.weeks;
	}

	public void setWeeks(List<Week> weeks) {
		this.weeks = weeks;
	}

	public Week addWeek(Week week) {
		getWeeks().add(week);
		week.setSubseason(this);

		return week;
	}

	public Week removeWeek(Week week) {
		getWeeks().remove(week);
		week.setSubseason(null);

		return week;
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (subseasonId != null ? subseasonId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Subseason)) {
            return false;
        }
        Subseason other = (Subseason) object;
        if ((this.subseasonId == null && other.subseasonId != null) || (this.subseasonId != null && !this.subseasonId.equals(other.subseasonId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entities.Subseason[ subseasonId=" + subseasonId + " ]";
    }
}