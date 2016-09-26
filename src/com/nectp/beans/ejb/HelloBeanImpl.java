package com.nectp.beans.ejb;

import com.nectp.beans.remote.HelloBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Session Bean implementation class HelloBeanImpl
 */
@Stateless
public class HelloBeanImpl implements HelloBean {

    @PersistenceContext
    private EntityManager em;
	
    private String response;
	
    public HelloBeanImpl() {
        // TODO Auto-generated constructor stub
    }

	public String getResponse() {
		return "Hello World!";
	}

}
