package com.nectp.jpa.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.math.BigDecimal;


/**
 * The persistent class for the address database table.
 * 
 */
@Entity
@Table(name="address")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name="Address.findAll", query="SELECT a FROM Address a"),
	@NamedQuery(name="Address.selectByLatLon", 
				query="SELECT DISTINCT a FROM Address a "
					+ "WHERE a.latitude = :latitude "
					+ "AND a.longitude = :longitude"),
	@NamedQuery(name="Address.selectByStreetCity", 
				query="SELECT DISTINCT a FROM Address a "
					+ "WHERE a.street = :street "
					+ "AND a.city = :city")
})
public class Address implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	private Long addressId;

	@Basic(optional=false)
	private String city;

	@Basic(optional=false)
	private String country;

	@Basic(optional=false)
	private BigDecimal latitude;

	@Basic(optional=false)
	private BigDecimal longitude;

	@Basic(optional=true)
	private String state;

	@Basic(optional=false)
	private String street;

	@Basic(optional=true)
	private String zip;

	public Address() {
	}

	public Long getAddressId() {
		return this.addressId;
	}

	public void setAddressId(Long addressId) {
		this.addressId = addressId;
	}

	public String getCity() {
		return this.city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return this.country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public BigDecimal getLatitude() {
		return this.latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return this.longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getStreet() {
		return this.street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getZip() {
		return this.zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (addressId != null ? addressId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AbstractTeamForSeason)) {
            return false;
        }
        Address other = (Address) object;
        if ((this.addressId == null && other.addressId != null) || (this.addressId != null && !this.addressId.equals(other.addressId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.nectp.jpa.entities.Address[ addressId=" + addressId + " ]";
    }

}