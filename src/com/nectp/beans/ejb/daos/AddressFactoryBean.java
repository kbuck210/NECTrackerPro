package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;

import com.nectp.beans.remote.daos.AddressFactory;
import com.nectp.jpa.entities.Address;

@Stateless
public class AddressFactoryBean extends AddressServiceBean implements AddressFactory {
	private static final long serialVersionUID = -4785059687448745372L;

	@Override
	public Address createAddress(String street, String city, String state, String zip, String longitude,
			String latitude, String country) {
		Logger log = Logger.getLogger(AddressFactoryBean.class.getName());
		Address address = null;
		
		if (street == null || city == null || longitude == null || latitude == null || country == null) {
			log.severe("Parameters not defined, can not create address!");
		}
		else {
			//	Check that the specified address does not already exist
			try {
				address = selectByStreetCity(street, city);
				
				//	Check whether other attributes need to be updated
				boolean update = false;
				if (address.getState() != null && !address.getState().equals(state)) {
					address.setState(state);
					update = true;
				}
				if (address.getZip() != null && !address.getZip().equals(zip)) {
					address.setZip(zip);
					update = true;
				}
				if (!address.getLongitude().equals(longitude)) {
					address.setLongitude(longitude);
					update = true;
				}
				if (!address.getLatitude().equals(latitude)) {
					address.setLatitude(latitude);
					update = true;
				}
				if (!address.getCountry().equals(country)) {
					address.setCountry(country);
					update = true;
				}
				
				if (update) {
					update(address);
				}
			}
			//	If not already existing, create it
			catch (NoExistingEntityException e) {
				address = new Address();
				address.setCity(city);
				address.setCountry(country);
				address.setLatitude(latitude);
				address.setLongitude(longitude);
				address.setState(state);
				address.setStreet(street);
				address.setZip(zip);
				
				boolean success = insert(address);
				if (!success) {
					address = null;
				}
			}
		}
		
		return address;
	}
	
}
