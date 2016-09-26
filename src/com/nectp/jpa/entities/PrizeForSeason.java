package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * The persistent class for the prizeforseason database table.
 * 
 */
@Entity
@Table(name="prizeforseason")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="PrizeForSeason.findAll", query="SELECT p FROM PrizeForSeason p"),
	@NamedQuery(name="PrizeForSeason.selectPrizeForSeason", 
				query="SELECT DISTINCT p FROM PrizeForSeason p "
					+ "INNER JOIN FETCH p.prize pz "
					+ "INNER JOIN FETCH p.season s "
					+ "WHERE pz.prizeType = :prizeType "
					+ "AND s.seasonNumber = :seasonNumber"),
	@NamedQuery(name="PrizeForSeason.selectAllPrizesInSeason",
				query="SELECT p FROM PrizeForSeason p WHERE p.season.seasonNumber = :seasonNumber")
})
public class PrizeForSeason implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	private Long prizeForSeasonId;

	@Basic(optional=false)
	private int prizeAmount;

	//bi-directional many-to-one association to PlayerForSeason
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="winner_abstractTeamForSeasonId")
	private PlayerForSeason winner;

	//bi-directional many-to-one association to Prize
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="prize_prizeId", nullable=false)
	private Prize prize;

	//bi-directional many-to-one association to Season
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="season_seasonNumber", nullable=false)
	private Season season;

	//bi-directional many-to-one association to Subseason
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="subseason_subseasonId")
	private Subseason subseason;

	public PrizeForSeason() {
	}

	public Long getPrizeForSeasonId() {
		return this.prizeForSeasonId;
	}

	public void setPrizeForSeasonId(Long prizeForSeasonId) {
		this.prizeForSeasonId = prizeForSeasonId;
	}

	public int getPrizeAmount() {
		return this.prizeAmount;
	}

	public void setPrizeAmount(int prizeAmount) {
		this.prizeAmount = prizeAmount;
	}

	public PlayerForSeason getWinner() {
		return this.winner;
	}

	public void setWinner(PlayerForSeason winner) {
		this.winner = winner;
	}

	public Prize getPrize() {
		return this.prize;
	}

	public void setPrize(Prize prize) {
		this.prize = prize;
	}

	public Season getSeason() {
		return this.season;
	}

	public void setSeason(Season season) {
		this.season = season;
	}

	public Subseason getSubseason() {
		return this.subseason;
	}

	public void setSubseason(Subseason subseason) {
		this.subseason = subseason;
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (prizeForSeasonId != null ? prizeForSeasonId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PrizeForSeason)) {
            return false;
        }
        PrizeForSeason other = (PrizeForSeason) object;
        if ((this.prizeForSeasonId == null && other.prizeForSeasonId != null) || (this.prizeForSeasonId != null && !this.prizeForSeasonId.equals(other.prizeForSeasonId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entites.PrizeForSeason[ prizeForSeasonId=" + prizeForSeasonId + " ]";
    }
}