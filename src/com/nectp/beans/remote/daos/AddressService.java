package com.nectp.beans.remote.daos;

import com.nectp.jpa.entities.Address;

public interface AddressService extends DataService<Address> {

	public Address selectByLatLon(long latitude, long longitude);
	
	public Address selectByStreetCity(String street, String city);
	
}
