package com.nectp.beans.remote.daos;

public interface DataService<T> {

	public boolean insert(T object);
	
	public boolean update(T object);
	
	public boolean remove(T object);
	
	public T selectById(Object id);
}
