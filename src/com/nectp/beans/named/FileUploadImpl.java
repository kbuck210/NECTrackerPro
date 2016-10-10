package com.nectp.beans.named;

import java.io.Serializable;

import org.primefaces.model.UploadedFile;

import com.nectp.webtools.FileUpload;

public abstract class FileUploadImpl implements Serializable, FileUpload {
	private static final long serialVersionUID = 9197478315738892172L;
	
	protected UploadedFile file;
	
	@Override
	public UploadedFile getFile() {
		return file;
	}

	@Override
	public void setFile(UploadedFile file) {
		this.file = file;
	}

}