package com.nectp.beans.ejb.daos;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.DataService;

@Stateless
public class DataServiceBean<T> implements Serializable, DataService<T> {
	private static final long serialVersionUID = -3364037456199802111L;
	
	@PersistenceContext(unitName="NECTrackerPro")
	protected EntityManager em;
	
	//	Generic class implementation for the service, allows for common access for any entity type
	private Class<T> type;
	
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
        Logger log = Logger.getLogger(obj.getClass().getName());
        try {
        	em.persist(obj);
            log.info("Inserted " + obj.getClass().getSimpleName());
        } catch (Exception e) {
        	log.severe("Rollback inserting " + obj.getClass().getSimpleName() + ": " + e.getMessage());
        	e.printStackTrace();
            success = false;
        }

        return success;
    }

	@Override
    public boolean update(final T obj) {
        boolean success = true;
        Logger log = Logger.getLogger(obj.getClass().getName());
        try {
        	em.merge(obj);
            log.info("Merged " + obj.getClass().getSimpleName());
        } catch (Exception e) {
        	log.severe("Rollback merging " + obj.getClass().getSimpleName() + ": " + e.getMessage());
        	e.printStackTrace();
            success = false;
        } 
        
        return success;
    }

	@Override
    public boolean remove(final T obj) {
        boolean success = true;
        Logger log = Logger.getLogger(obj.getClass().getName());
        try {
        	em.remove(obj);
        	log.info("Removed " + obj.getClass().getSimpleName());
        } catch (Exception e) {
        	log.severe("Rollback removing " + obj.getClass().getSimpleName() + ": " + e.getMessage());
        	e.printStackTrace();
            success = false;
        } 

        return success;
    }
	
	@Override
    public T selectById(Object id) {
		Logger log = Logger.getLogger(type.getName());
        T result = null;
        try {
            result = em.find(type, id);
        } catch (EntityNotFoundException e) {
        	log.warning("No result found for select by PK: " + e.getMessage());
        } catch (IllegalArgumentException e) {
        	log.severe("Exception caught in select by PK: " + e.getMessage());
        	e.printStackTrace();
        }
        return result;
    }
	
	@Override
	public List<T> findAll() {
		Logger log = Logger.getLogger(type.getName());
		List<T> results;
		String className = type.getSimpleName();
		TypedQuery<T> tq = em.createNamedQuery(className + ".findAll", type);
		try {
			results = tq.getResultList();
		} catch (Exception e) {
			log.severe("Exception caught retrieving all " + className + "'s with message: " + e.getMessage());
			e.printStackTrace();
			results = new ArrayList<T>();
		}
		
		return results;
	}
}

