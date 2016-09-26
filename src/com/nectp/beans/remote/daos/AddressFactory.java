package com.nectp.beans.remote.daos;

import java.math.BigDecimal;

import com.nectp.jpa.entities.Address;

public interface AddressFactory extends AddressService {

	public Address createAddress(String street, String city, String state, String zip, BigDecimal longitude, BigDecimal latitude, String country);
	
}
