package com.nectp.webtools;

import java.util.List;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

public interface FileUpload {

	public List<UploadedFile> getFiles();
	
	public void setFiles(List<UploadedFile> files);
	
	public void upload(FileUploadEvent event);
	
	public boolean getDisabled();
	
	public void submit();
}
