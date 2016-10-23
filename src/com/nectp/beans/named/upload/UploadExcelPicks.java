package com.nectp.beans.named.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.nectp.poi.ExcelPickReader;

@Named(value="uploadExcelPicks")
@ViewScoped
public class UploadExcelPicks extends FileUploadImpl {
	private static final long serialVersionUID = -8901195250246314655L;
	
	private Logger log;
	
	@EJB
	private ExcelPickReader excelReader;
	
	@Override
	public void upload(FileUploadEvent event) {
		synchronized(this) {
			files.add(event.getFile());
		}
	}

	@Override
	public void submit() {
		for (UploadedFile uploaded : files) {
			String filename = null;
			InputStream iStream = null;
			try {
				filename = uploaded.getFileName();
				iStream = uploaded.getInputstream();
			} catch (IOException e) {
				log.severe("Failed to get uploaded file information, can not parse excel file.");
				log.severe(e.getMessage());
				e.printStackTrace();
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Error!",  "Some errors occurred uploading picks, check the logs for details.");
		        FacesContext.getCurrentInstance().addMessage(null, message);
		        continue;
			}
			boolean success = excelReader.processFile(filename, iStream);
			if (!success) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!",  "All picks read successfully!");
		        FacesContext.getCurrentInstance().addMessage(null, message);
			}
			else {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Error!",  "Some errors occurred uploading picks, check the logs for details.");
		        FacesContext.getCurrentInstance().addMessage(null, message);
			}
		}
		empty();
	}
}

