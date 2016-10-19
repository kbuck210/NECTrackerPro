package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * The persistent class for the email database table.
 * 
 */
@Entity
@Table(name="email")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Email.findAll", query="SELECT e FROM Email e"),
	@NamedQuery(name="Email.selectByAddressName", 
				query="SELECT DISTINCT e FROM Email e "
					+ "INNER JOIN FETCH e.player p "
					+ "WHERE e.emailAddress = :emailAddress "
					+ "AND p.abstractTeamId = :playerId"),
	@NamedQuery(name="selectByAddress",
				query="SELECT e FROM Email e WHERE e.emailAddress = :emailAddress"),
	@NamedQuery(name="selectAllByPlayer", 
				query="SELECT e FROM Email e "
					+ "INNER JOIN FETCH e.player p "
					+ "WHERE p.abstractTeamId = :playerId"),
	@NamedQuery(name="Email.selectUserForPrimaryEmail",
				query="SELECT DISTINCT e.player FROM Email e "
					+ "WHERE e.emailAddress = :emailAddress "
					+ "AND e.primaryAddress = true"),
	@NamedQuery(name="Email.selectAllRecipientsBySeason",
				query="SELECT e FROM Email e INNER JOIN FETCH e.player p "
					+ "INNER JOIN FETCH p.playerInstances pfs "
					+ "INNER JOIN FETCH pfs.season s "
					+ "WHERE e.emailsRequested = true "
					+ "AND s.seasonNumber = :seasonNumber")
})
public class Email implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	private Long emailId;

	@Basic(optional=false)
	private String emailAddress;
	
	@Basic(optional=false)
	private boolean primaryAddress = true;
	
	@Basic(optional=false)
	private boolean emailsRequested = true;

	//bi-directional many-to-one association to Player
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="player_abstractTeamId")
	private Player player;

	public Email() {
	}

	public Long getEmailId() {
		return this.emailId;
	}

	public void setEmailId(Long emailId) {
		this.emailId = emailId;
	}

	public String getEmailAddress() {
		return this.emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Player getPlayer() {
		return this.player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public boolean isPrimaryAddress() {
		return primaryAddress;
	}
	
	public void setPrimaryAddress(boolean primaryAddress) {
		this.primaryAddress = primaryAddress;
	}
	
	public boolean isEmailsRequested() {
		return emailsRequested;
	}
	
	public void setEmailsRequested(boolean emailsRequested) {
		this.emailsRequested = emailsRequested;
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (emailId != null ? emailId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Division)) {
            return false;
        }
        Email other = (Email) object;
        if ((this.emailId == null && other.emailId != null) || (this.emailId != null && !this.emailId.equals(other.emailId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entities.Email[ emailId=" + emailId + " ]";
    }
}