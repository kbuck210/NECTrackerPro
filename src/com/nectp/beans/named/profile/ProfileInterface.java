package com.nectp.beans.named.profile;

import com.nectp.jpa.entities.Season;

public interface ProfileInterface<T> {

	public String getNec();
	
	/** Set NEC is called in preRender by the f:viewParam tag, setting the NEC & Season class parameters
	 * 
	 * @param nec the String view parameter from the GET request representing the season number
	 */
	public void setNec(String nec);
	
	public Season getSeason();
	
	public T getProfileEntity();
	
	public abstract void setProfileEntity(T profileEntity);
	
	public abstract void initialize();
}
