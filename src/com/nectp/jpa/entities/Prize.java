package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import com.nectp.jpa.constants.NEC;

import java.util.LinkedList;
import java.util.List;


/**
 * The persistent class for the prize database table.
 * 
 */
@Entity
@Table(name="prize")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Prize.findAll", query="SELECT p FROM Prize p"),
	@NamedQuery(name="Prize.selectPrizeByType", 
				query="SELECT DISTINCT p from Prize p "
					+ "WHERE p.prizeType = :prizeType")
})
public class Prize implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	private Integer prizeId;

	@Basic(optional=false)
	@Column(unique=true)
	private Integer prizeType;

	//bi-directional many-to-one association to PrizeForSeason
	@OneToMany(mappedBy="prize")
	private List<PrizeForSeason> prizeHistory;

	public Prize() {
		prizeHistory = new LinkedList<PrizeForSeason>();
	}

	public Integer getPrizeId() {
		return this.prizeId;
	}

	public void setPrizeId(Integer prizeId) {
		this.prizeId = prizeId;
	}

	public NEC getPrizeType() {
		if (prizeType != null) {
			return NEC.values()[prizeType];
		}
		else return null;
	}

	public void setPrizeType(NEC prizeType) {
		if (prizeType != null) {
			this.prizeType = prizeType.ordinal();
		}
	}

	public List<PrizeForSeason> getPrizeHistory() {
		return this.prizeHistory;
	}

	public void setPrizeHistory(List<PrizeForSeason> prizeHistory) {
		this.prizeHistory = prizeHistory;
	}

	public PrizeForSeason addPrizeHistory(PrizeForSeason prizeforseason) {
		getPrizeHistory().add(prizeforseason);
		prizeforseason.setPrize(this);

		return prizeforseason;
	}

	public PrizeForSeason removePrizeHistory(PrizeForSeason prizeforseason) {
		getPrizeHistory().remove(prizeforseason);
		prizeforseason.setPrize(null);

		return prizeforseason;
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (prizeId != null ? prizeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Prize)) {
            return false;
        }
        Prize other = (Prize) object;
        if ((this.prizeId == null && other.prizeId != null) || (this.prizeId != null && !this.prizeId.equals(other.prizeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entities.Prize[ prizeId=" + prizeId + " ]";
    }
}