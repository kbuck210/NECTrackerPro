package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.LinkedList;
import java.util.List;


/**
 * The persistent class for the conference database table.
 * 
 */
@Entity
@Table(name="conference")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Conference.findAll", query="SELECT c FROM Conference c"),
	@NamedQuery(name="Conference.selectByConfType", 
				query="SELECT DISTINCT c FROM Conference c "
					+ "WHERE c.conferenceType = :confType")
})
public class Conference implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum ConferenceType {
		AFC,
		NFC;
		
		@Override
		public String toString() {
			switch(this) {
			case AFC:
				return "AFC";
			case NFC:
				return "NFC";
			default:
				return null;
			}
		}
		
		public static ConferenceType getConferenceTypeForString(String confType) {
			ConferenceType conf = null;
			if (confType != null) {
				for (ConferenceType ct : ConferenceType.values()) {
					if (ct.name().toUpperCase().equals(confType.toUpperCase())) {
						conf = ct;
						break;
					}
				}
			}
			return conf;
		}
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	private Integer conferenceId;

	@Basic(optional=false)
	@Column(unique=true)
	private Integer conferenceType;

	//bi-directional many-to-one association to Division
	@OneToMany(mappedBy="conference")
	private List<Division> divisions;

	public Conference() {
		divisions = new LinkedList<Division>();
	}

	public Integer getConferenceId() {
		return this.conferenceId;
	}

	public void setConferenceId(Integer conferenceId) {
		this.conferenceId = conferenceId;
	}

	public ConferenceType getConferenceType() {
		if (conferenceType != null) {
 			return ConferenceType.values()[conferenceType];
 		}
		else return null;
	}

	public void setConferenceType(ConferenceType conferenceType) {
		if (conferenceType != null) {
			this.conferenceType = conferenceType.ordinal();
		}
	}

	public List<Division> getDivisions() {
		return this.divisions;
	}

	public void setDivisions(List<Division> divisions) {
		this.divisions = divisions;
	}

	public Division addDivision(Division division) {
		getDivisions().add(division);
		division.setConference(this);

		return division;
	}

	public Division removeDivision(Division division) {
		getDivisions().remove(division);
		division.setConference(null);

		return division;
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (conferenceId != null ? conferenceId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Conference)) {
            return false;
        }
        Conference other = (Conference) object;
        if ((this.conferenceId == null && other.conferenceId != null) || (this.conferenceId != null && !this.conferenceId.equals(other.conferenceId))) {
            return false;
        }
        return true;
    }
}