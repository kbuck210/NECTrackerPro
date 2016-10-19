package com.nectp.beans.named.upload;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.w3c.dom.Element;

import com.nectp.beans.remote.daos.AddressFactory;
import com.nectp.beans.remote.daos.StadiumFactory;
import com.nectp.jpa.constants.Timezone;
import com.nectp.jpa.entities.Address;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Stadium.RoofType;
import com.nectp.webtools.DOMParser;

@Named(value="uploadStadiums")
@ViewScoped
public class UploadStadiums extends FileUploadImpl {
	private static final long serialVersionUID = -8245939680303735081L;

	private Logger log;
	
	@EJB
	private AddressFactory addressFactory;
	
	@EJB
	private StadiumFactory stadiumFactory;
	
	public UploadStadiums() {
		log = Logger.getLogger(UploadStadiums.class.getName());
	}
	
	@Override
	public void upload(FileUploadEvent event) {
		files.add(event.getFile());
	}
	
	@Override
	public void submit() {
		for (UploadedFile file : files) {
			if (file != null) {
				try {
					InputStream iStream = file.getInputstream();
					parseStadiums(iStream);
				} catch (IOException e) {
					log.severe("Exception retrieving input stream from uploaded file, can not update week: " + e.getMessage());
					e.printStackTrace();
				}
			}
			else {
				log.warning("No file uploaded! Aborting stadium updates.");
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Upload Error:", "File not found!");
		        FacesContext.getCurrentInstance().addMessage(null, message);
			}
		}
	}

	private void parseStadiums(InputStream iStream) {
		DOMParser parser = DOMParser.newInstance(iStream);
		parser.setQualifiedNodeName("stadium");
		List<Element> stadiums = parser.generateElementList();
		boolean anyErrors = false;
		for (Element e : stadiums) {
			String stadiumName = parser.getTextSubElementByTagName(e, "name");
			String street = parser.getTextSubElementByTagName(e, "street");
			String city = parser.getTextSubElementByTagName(e, "city");
			String state = parser.getTextSubElementByTagName(e, "state");
			String zip = parser.getTextSubElementByTagName(e, "zip");
			String country = parser.getTextSubElementByTagName(e, "country");
			String lat = parser.getTextSubElementByTagName(e, "lat");
			String lon = parser.getTextSubElementByTagName(e, "long");
			String cap = parser.getTextSubElementByTagName(e, "capacity");
			String roof = parser.getTextSubElementByTagName(e, "roof");
			String timezone = parser.getTextSubElementByTagName(e, "timezone");
			
			BigDecimal latitude = new BigDecimal(lat);
			BigDecimal longitude = new BigDecimal(lon);
			
			Long capacity = null;
			try {
				capacity = Long.parseLong(cap);
			} catch (NumberFormatException ex) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Format Error:",  
						"Invalid capacity: " + cap + " for " + stadiumName);
		        FacesContext.getCurrentInstance().addMessage(null, message);
				anyErrors = true;
				log.severe("Invalid capacity format for stadium: " + stadiumName + " - skipping update: " + ex.getMessage());
				continue;
			}
			
			RoofType roofType = RoofType.getRoofTypeForString(roof);
			if (roofType == null) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Format Error:",  
						"Invalid RoofType: " + roof + " for " + stadiumName);
		        FacesContext.getCurrentInstance().addMessage(null, message);
				anyErrors = true;
				log.severe("Invalid roof type string: " + roof + " skipping update");
				continue;
			}
			
			Timezone zone = Timezone.getTimezoneForString(timezone);
			if (zone == null) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Format Error:",  
						"Invalid Timzone: " + timezone + " for " + stadiumName);
		        FacesContext.getCurrentInstance().addMessage(null, message);
				anyErrors = true;
				log.severe("Invalid timezone string: " + timezone + " skipping update");
				continue;
			}
			
			//	Create/update the address as read from the XML file
			Address address = addressFactory.createAddress(street, city, state, zip, longitude, latitude, country);
			if (address == null) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Address Failure!",  
						"Address for " + stadiumName + " could not be created/selected");
		        FacesContext.getCurrentInstance().addMessage(null, message);
				anyErrors = true;
			}
			else {
				boolean international = country != null && !country.trim().toUpperCase().equals("US");
				Stadium stadium = stadiumFactory.createStadium(stadiumName, address, capacity, international, roofType, zone);
				if (stadium == null) {
					FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Write Failure!",  
							"Failed to create/update stadium: " + stadiumName);
			        FacesContext.getCurrentInstance().addMessage(null, message);
					anyErrors = true;
				}
			}
		}
		
		if (!anyErrors) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Upload Success!",  "All stadiums updated.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
}
