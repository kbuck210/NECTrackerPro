package com.nectp.beans.ejb.email;

import java.util.List;

import com.nectp.jpa.entities.Email;

public interface EmailService {

	public boolean sendEmail(List<Email> recipients, String subject, String textBody, String htmlBody);
	
}
