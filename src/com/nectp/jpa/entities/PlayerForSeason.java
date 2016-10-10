package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.LinkedList;
import java.util.List;


/**
 * The persistent class for the playerforseason database table.
 * 
 */
@Entity
@Table(name="playerforseason")
@DiscriminatorValue("PFS")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="PlayerForSeason.findAll", query="SELECT p FROM PlayerForSeason p"),
	@NamedQuery(name="PlayerForSeason.selectByPlayerSeason", 
				query="SELECT DISTINCT p FROM PlayerForSeason p "
					+ "INNER JOIN FETCH p.player pl "
					+ "INNER JOIN FETCH p.season s "
					+ "WHERE pl.abstractTeamId = :playerId "
					+ "AND s.seasonNumber = :seasonNumber"),
	@NamedQuery(name="PlayerForSeason.selectByExcelName",
				query="SELECT DISTINCT p FROM PlayerForSeason p "
					+ "INNER JOIN FETCH p.season s "
					+ "WHERE p.excelPrintName = :excelName "
					+ "AND s.seasonNumber = :seasonNumber"),
	@NamedQuery(name="PlayerForSeason.selectByExcelCol",
				query="SELECT DISTINCT p FROM PlayerForSeason p "
					+ "INNER JOIN FETCH p.season s "
					+ "WHERE p.excelColumn = :excelCol "
					+ "AND s.seasonNumber = :seasonNumber")
})
public class PlayerForSeason extends AbstractTeamForSeason implements Serializable {
	private static final long serialVersionUID = 1L;

	@Basic(optional=true)
	private Integer excelColumn;
	
	@Basic(optional=false)
	private Boolean commish;
	
	//bi-directional many-to-one association to Pick
	@OneToMany(mappedBy="player")
	private List<Pick> picks;

	//bi-directional many-to-one association to Player
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="player_abstractTeamId")
	private Player player;

	//bi-directional many-to-one association to Season
	@ManyToOne
	@JoinColumn(name="seasonNumber")
	private Season season;

	//bi-directional many-to-one association to PrizeForSeason
	@OneToMany(mappedBy="winner")
	private List<PrizeForSeason> prizesWon;

	public PlayerForSeason() {
		picks = new LinkedList<Pick>();
		prizesWon = new LinkedList<PrizeForSeason>();
	}
	
	public Boolean isCommish() {
		return commish;
	}
	
	public void setCommish(boolean commish) {
		this.commish = commish;
	}

	public Integer getExcelColumn() {
		return excelColumn;
	}

	public void setExcelColumn(Integer excelColumn) {
		this.excelColumn = excelColumn;
	}

	public List<Pick> getPicks() {
		return this.picks;
	}

	public void setPicks(List<Pick> picks) {
		this.picks = picks;
	}

	public Pick addPick(Pick pick) {
		getPicks().add(pick);
		pick.setPlayer(this);

		return pick;
	}

	public Pick removePick(Pick pick) {
		getPicks().remove(pick);
		pick.setPlayer(null);

		return pick;
	}

	public Player getPlayer() {
		return this.player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Season getSeason() {
		return this.season;
	}

	public void setSeason(Season season) {
		this.season = season;
	}

	public List<PrizeForSeason> getPrizesWon() {
		return this.prizesWon;
	}

	public void setPrizesWon(List<PrizeForSeason> prizesWon) {
		this.prizesWon = prizesWon;
	}

	public PrizeForSeason addPrizeforseason(PrizeForSeason prizeWon) {
		getPrizesWon().add(prizeWon);
		prizeWon.setWinner(this);

		return prizeWon;
	}

	public PrizeForSeason removePrizeforseason(PrizeForSeason prizeWon) {
		getPrizesWon().remove(prizeWon);
		prizeWon.setWinner(null);

		return prizeWon;
	}

	@Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.player != null ? this.player.hashCode() : 0);
        hash = 79 * hash + (this.getSeason() != null ? this.getSeason().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PlayerForSeason)) {
            return false;
        }
        PlayerForSeason other = (PlayerForSeason) object;
        if ((this.getAbstractTeamForSeasonId() == null && other.getAbstractTeamForSeasonId() != null) || (this.getAbstractTeamForSeasonId() != null && !this.getAbstractTeamForSeasonId().equals(other.getAbstractTeamForSeasonId()))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entities.PlayerForSeason[ abstractTeamForSeasonId=" + getAbstractTeamForSeasonId() + " ]";
    }
}
