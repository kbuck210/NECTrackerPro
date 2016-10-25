package com.nectp.beans.ejb.daos;

import javax.ejb.ApplicationException;

@ApplicationException(rollback=false)
public class NoExistingEntityException extends RuntimeException {
	private static final long serialVersionUID = -507110418163811904L;
	
	public NoExistingEntityException(Throwable cause) {
		initCause(cause);
	}
}
