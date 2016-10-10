package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * The persistent class for the abstractteam database table.
 * 
 */
@Entity
@Table(name="abstractteam")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="Type")
@XmlRootElement
@NamedQuery(name="AbstractTeam.findAll", query="SELECT a FROM AbstractTeam a")
public class AbstractTeam implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	private Integer abstractTeamId;

	public AbstractTeam() {
	}

	public Integer getAbstractTeamId() {
		return this.abstractTeamId;
	}

	public void setAbstractTeamId(Integer abstractTeamId) {
		this.abstractTeamId = abstractTeamId;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		hash += (abstractTeamId != null ? abstractTeamId.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof AbstractTeam)) {
			return false;
		}
		AbstractTeam other = (AbstractTeam) object;
		if ((this.abstractTeamId == null && other.abstractTeamId != null) || 
			(this.abstractTeamId != null && !this.abstractTeamId.equals(other.abstractTeamId))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.nectp.jpa.entities.AbstractTeam[ abstractTeamId=" + abstractTeamId + " ]";
	}

}
