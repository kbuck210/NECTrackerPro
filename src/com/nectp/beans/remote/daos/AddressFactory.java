package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Address;

public interface AddressFactory extends AddressService {

	public Address createAddress(String street, String city, String state, String zip, String longitude, String latitude, String country);
	
}
