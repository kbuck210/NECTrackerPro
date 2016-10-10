package com.nectp.beans.remote.daos;

import java.util.List;

public interface DataService<T> {

	public boolean insert(T object);
	
	public boolean update(T object);
	
	public boolean remove(T object);
	
	public T selectById(Object id);

	public List<T> findAll();
}
