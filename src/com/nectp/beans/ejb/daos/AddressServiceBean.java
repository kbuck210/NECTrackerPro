package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.AddressService;
import com.nectp.jpa.entities.Address;

@Stateless
public class AddressServiceBean extends DataServiceBean<Address> implements AddressService {
	private static final long serialVersionUID = 7816644381265595880L;

	@Override
	public Address selectByLatLon(long latitude, long longitude) throws NoExistingEntityException {
		Logger log = Logger.getLogger(AddressServiceBean.class.getName());
		Address address = null;
		TypedQuery<Address> aq = em.createNamedQuery("Address.selectByLatLon", Address.class);
		aq.setParameter("latitude", latitude);
		aq.setParameter("longitude", longitude);
		try {
			address = aq.getSingleResult();
		} catch (NonUniqueResultException e) {
			log.severe("Multiple results found for lat: " + latitude + " long: " + longitude + " - invalid address.");
			log.severe(e.getMessage());
			e.printStackTrace();
		} catch (NoResultException e) {
			log.warning("No results found for lat: " + latitude + " long: " + longitude);
			log.warning(e.getMessage());
			throw new NoExistingEntityException(e);
		} catch (Exception e) {
			log.severe("Exception caught retrieving address: " + e.getMessage());
			e.printStackTrace();
		}
		
		return address;
	}

	@Override
	public Address selectByStreetCity(String street, String city) throws NoExistingEntityException {
		Logger log = Logger.getLogger(AddressServiceBean.class.getName());
		Address address = null;
		if (street == null || city == null) {
			log.severe("No street/city specified, can not get address!");
		}
		else {
			TypedQuery<Address> aq = em.createNamedQuery("Address.selectByStreetCity", Address.class);
			aq.setParameter("street", street);
			aq.setParameter("city", city);
			try {
				address = aq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple results found for street: " + street + " city: " + city + " - invalid address.");
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No results found for street: " + street + " city: " + city);
				log.warning(e.getMessage());
				throw new NoExistingEntityException(e);
			} catch (Exception e) {
				log.severe("Exception caught retrieving address: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return address;
	}

}
