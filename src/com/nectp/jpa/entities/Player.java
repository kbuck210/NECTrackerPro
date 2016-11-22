package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.LinkedList;
import java.util.List;


/**
 * The persistent class for the player database table.
 * 
 */
@Entity
@Table(name="player")
@DiscriminatorValue("P")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Player.findAll", query="SELECT p FROM Player p"),
	@NamedQuery(name="Player.selectPlayerByName",
				query="SELECT DISTINCT p FROM Player p WHERE p.name = :playerName")
})
public class Player extends AbstractTeam implements Serializable {
	private static final long serialVersionUID = 1L;

	@Basic(optional=false)
	private String name;
	
	@Basic(optional=false)
	private String password;

	@Basic(optional=false)
	private Integer sinceYear;
	
	@Basic(optional=false)
	private String avatarUrl;
	
	@Basic(optional=false)
	private boolean admin = false;

	//bi-directional many-to-one association to Email
	@OneToMany(mappedBy="player")
	private List<Email> emails;

	//bi-directional many-to-one association to PlayerForSeason
	@OneToMany(mappedBy="player")
	private List<PlayerForSeason> playerInstances;

	public Player() {
		emails = new LinkedList<Email>();
		playerInstances = new LinkedList<PlayerForSeason>();
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		//	TODO: do hash function here
		this.password = password;
	}

	public Integer getSinceYear() {
		return this.sinceYear;
	}

	public void setSinceYear(Integer sinceYear) {
		this.sinceYear = sinceYear;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	
	public boolean isAdmin() {
		return admin;
	}
	
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public List<Email> getEmails() {
		return this.emails;
	}

	public void setEmails(List<Email> emails) {
		this.emails = emails;
	}

	public Email addEmail(Email email) {
		getEmails().add(email);
		email.setPlayer(this);

		return email;
	}

	public Email removeEmail(Email email) {
		getEmails().remove(email);
		email.setPlayer(null);

		return email;
	}

	public List<PlayerForSeason> getPlayerInstances() {
		return this.playerInstances;
	}

	public void setPlayerInstances(List<PlayerForSeason> playerInstances) {
		this.playerInstances = playerInstances;
	}

	public PlayerForSeason addPlayerforseason(PlayerForSeason playerforseason) {
		getPlayerInstances().add(playerforseason);
		playerforseason.setPlayer(this);

		return playerforseason;
	}

	public PlayerForSeason removePlayerforseason(PlayerForSeason playerforseason) {
		getPlayerInstances().remove(playerforseason);
		playerforseason.setPlayer(null);

		return playerforseason;
	}

	@Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.sinceYear != null ? this.sinceYear.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Player)) {
            return false;
        }
        Player other = (Player) object;
        if ((this.getAbstractTeamId() == null && other.getAbstractTeamId() != null) || (this.getAbstractTeamId() != null && !this.getAbstractTeamId().equals(other.getAbstractTeamId()))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entities.Player[ teamId=" + getAbstractTeamId() + " ]";
    }
}
