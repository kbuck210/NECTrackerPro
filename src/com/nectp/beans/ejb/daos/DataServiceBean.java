package com.nectp.beans.ejb.daos;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import com.nectp.beans.remote.daos.DataService;

@Stateless
public class DataServiceBean<T> implements Serializable, DataService<T> {
	private static final long serialVersionUID = -3364037456199802111L;
	
	@PersistenceContext(unitName="NECTrackerPro")
	protected EntityManager em;
	
	//	Generic class implementation for the service, allows for common access for any entity type
	private Class<T> type;
	
	public DataServiceBean() {
		
	}
	
	//	Required unchecked cast, Class<T> not known until runtime...
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		//	The following code to get generic parameterized type at Runtime derived from code found here:
		//	http://stackoverflow.com/questions/10589767/class-cannot-be-cast-to-java-lang-reflect-parameterizedtype
		//Get "T" and assign it to this.entityClass
	    Type genericSuperClass = getClass().getGenericSuperclass();

	    ParameterizedType parametrizedType = null;
	    while (parametrizedType == null) {
	        if ((genericSuperClass instanceof ParameterizedType)) {
	            parametrizedType = (ParameterizedType) genericSuperClass;
	        } else {
	            genericSuperClass = ((Class<?>) genericSuperClass).getGenericSuperclass();
	        }
	    }

	    this.type = (Class<T>) parametrizedType.getActualTypeArguments()[0];
	}
	
	@Override
    public boolean insert(final T obj) {
        boolean success = true;
        
        try {
        	em.persist(obj);
            Logger.getLogger(obj.getClass().getName()).log(Level.INFO, "Inserted " + obj.getClass().getSimpleName());
        } catch (Exception e) {
        	Logger.getLogger(obj.getClass().getName()).log(Level.SEVERE,
                    "Rollback inserting " + obj.getClass().getSimpleName() + ": " + e.getMessage(), e);
            success = false;
        }

        return success;
    }

	@Override
    public boolean update(final T obj) {
        boolean success = true;
       
        try {
        	em.merge(obj);
            Logger.getLogger(obj.getClass().getName()).log(Level.INFO,
                    "Merged " + obj.getClass().getSimpleName());
        } catch (Exception e) {
        	Logger.getLogger(obj.getClass().getName()).log(Level.SEVERE,
                    "Rollback merging " + obj.getClass().getSimpleName() + ": " + e.getMessage(), e);
            success = false;
        } 
        
        return success;
    }

	@Override
    public boolean remove(final T obj) {
        boolean success = true;
        
        try {
        	em.remove(obj);
            Logger.getLogger(obj.getClass().getName()).log(Level.INFO,
                    "Removed " + obj.getClass().getSimpleName());
        } catch (Exception e) {
        	Logger.getLogger(obj.getClass().getName()).log(Level.SEVERE,
                    "Rollback removing " + obj.getClass().getSimpleName() + ": " + e.getMessage(), e);
            success = false;
        } 

        return success;
    }
	
	@Override
    public T selectById(Object id) {
        T result = null;
        try {
            result = em.find(type, id);
        } catch (EntityNotFoundException e) {
            Logger.getLogger(type.getName()).log(Level.WARNING,
                    "No result found for select by PK: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            Logger.getLogger(type.getName()).log(Level.SEVERE,
                    "Exception caught in select by PK: " + e.getMessage(), e);
        }
        return result;
    }
}
