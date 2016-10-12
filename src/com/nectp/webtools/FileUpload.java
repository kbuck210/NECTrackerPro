package com.nectp.webtools;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

public interface FileUpload {

	public UploadedFile getFile();
	
	public void setFile(UploadedFile file);
	
	public void upload(FileUploadEvent event);
	
	public String getFilename();
	
	public boolean getDisabled();
	
}