package com.nectp.beans.ejb.email;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import com.nectp.jpa.entities.Email;

@Stateless
public class WeeklyEmailService {

	@PostConstruct
	public void init() {
		
	}
}
