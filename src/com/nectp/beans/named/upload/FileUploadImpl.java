package com.nectp.beans.named.upload;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.model.UploadedFile;

import com.nectp.webtools.FileUpload;

public abstract class FileUploadImpl implements Serializable, FileUpload {
	private static final long serialVersionUID = 9197478315738892172L;
	
	protected List<UploadedFile> files = new ArrayList<UploadedFile>();
	
	protected void empty() {
		files = new ArrayList<UploadedFile>();
	}
	
	@Override
	public List<UploadedFile> getFiles() {
		return files;
	}

	@Override
	public void setFiles(List<UploadedFile> files) {
		this.files = files;
	}

	@Override
	public boolean getDisabled() {
		return files.isEmpty();
	}
}
