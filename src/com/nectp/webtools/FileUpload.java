package com.nectp.webtools;

import org.primefaces.model.UploadedFile;

public interface FileUpload {

	public UploadedFile getFile();
	
	public void setFile(UploadedFile file);
	
	public void upload();
	
}