package com.nectp.beans.ejb.email;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import com.nectp.jpa.entities.Email;

@Named(value="emailTestBean")
@RequestScoped
public class EmailTestBean {

	private String messageText;
	
	@EJB
	private EmailService emailService;
	
	public String getMessageText() {
		return messageText;
	}
	
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	
	public void send() {
		List<Email> addresses = new ArrayList<Email>();
		Email email = new Email();
		email.setEmailAddress("kbuck210@gmail.com");
		email.setEmailsRequested(true);
		email.setPrimaryAddress(true);
		addresses.add(email);
		
		Email dad = new Email();
		dad.setEmailAddress("bbuckley20@comcast.net");
		dad.setEmailsRequested(true);
		dad.setPrimaryAddress(true);
		addresses.add(dad);
		
		FacesMessage message = null;
		if (messageText == null || messageText.trim().isEmpty()) {
			message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Message text is null!");
		}
		else {
			boolean sent = emailService.sendEmail(addresses, "NEC Tracker Pro - Week 5 Results: (Test Email)", "I didn't get the HTML text...", messageText);
			if (sent) {
				message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "Message was sent!");
			}
			else {
				message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Message failed to send!");
			}
		}
		if (message != null) {
			FacesContext.getCurrentInstance().addMessage(null, message);
		}	
	}
	
}
