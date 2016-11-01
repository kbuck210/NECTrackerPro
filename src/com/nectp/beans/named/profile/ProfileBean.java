package com.nectp.beans.named.profile;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import com.nectp.jpa.entities.Season;

@Named(value="profileBean")
@Dependent
public abstract class ProfileBean<T> implements ProfileInterface<T>, Serializable {
	private static final long serialVersionUID = 1609481673492424376L;
	
	protected T profileEntity;
	
	protected Season season;
	
	protected String nec;
	
	protected Logger log = Logger.getLogger(ProfileBean.class.getName());

	public String getNec() {
		return nec;
	}
	
	/** Set NEC is called in preRender by the f:viewParam tag, setting the NEC & Season class parameters
	 * 
	 * @param nec the String view parameter from the GET request representing the season number
	 */
	public void setNec(String nec) {
		this.nec = nec;
	}
	
	public Season getSeason() {
		return season;
	}
	
	public T getProfileEntity() {
		return profileEntity;
	}
	
	public abstract void setProfileEntity(T profileEntity);
	
	public abstract void initialize();
}
