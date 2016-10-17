package com.nectp.beans.ejb.email;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletContext;
import javax.mail.internet.MimeMessage.RecipientType;

import com.nectp.jpa.entities.Email;

@Stateless
public class EmailServiceImpl implements EmailService {

	private Logger log;
	
	//	Set the default email service values
	public EmailServiceImpl() {
		log = Logger.getLogger(EmailServiceImpl.class.getName());
	}
	
	@Override
	public boolean sendEmail(List<Email> recipients, String subject, String textBody, String htmlBody) {
		Properties props = System.getProperties();
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		
		Session session = Session.getDefaultInstance(props, null);
		
		MimeMessage message = new MimeMessage(session);
		try {
		    message.setFrom(new InternetAddress("nectrackerpro@gmail.com"));
		    
		    List<InternetAddress> addresses = new ArrayList<InternetAddress>();
		    for (Email email : recipients) {
		    	if (email.isEmailsRequested()) {
		    		InternetAddress address = new InternetAddress(email.getEmailAddress());
		    		addresses.add(address);
		    	}
		    }
		    
		    InternetAddress[] addressArray = addresses.toArray(new InternetAddress[addresses.size()]);
		    message.setRecipients(RecipientType.TO, addressArray);
		    message.setSubject(subject);
		    message.setSentDate(new Date());
		    
		    //	Alternative indicates multiple versions of same content
		    Multipart multipart = new MimeMultipart("alternative");
		    
		    //	Text content added in case recipient can't support HTML
		    if (textBody != null) {
		    	MimeBodyPart textPart = new MimeBodyPart();
			    textPart.setText(textBody);
			    multipart.addBodyPart(textPart);
		    }
		    
		    //	Desired format goes last
		    if (htmlBody != null) {
		    	MimeBodyPart htmlPart = new MimeBodyPart();
			    htmlPart.setContent(htmlBody, "text/html");
			    multipart.addBodyPart(htmlPart);
			    
			    MimeBodyPart background = new MimeBodyPart();
			    MimeBodyPart mainImage = new MimeBodyPart();
			    ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
			    String relativeBackgroundPath = "/img/stadium-smaller.png";
			    String backgroundPath = ctx.getRealPath(relativeBackgroundPath);
			    String relativeMainImagePath = "/img/tombrady.jpg";
			    String mainImagePath = ctx.getRealPath(relativeMainImagePath);
			    DataSource bds = new FileDataSource(backgroundPath);
			    background.setDataHandler(new DataHandler(bds));
			    background.setHeader("Content-ID", "<background>");
			    multipart.addBodyPart(background);
			    
			    DataSource mids = new FileDataSource(mainImagePath);
			    mainImage.setDataHandler(new DataHandler(mids));
			    mainImage.setHeader("Content-ID", "<mainImage>");
			    multipart.addBodyPart(mainImage);
		    }
		   
		    message.setContent(multipart);
		    
		    Transport transport = session.getTransport("smtp");
		    transport.connect("smtp.gmail.com", "nectrackerpro@gmail.com", "NECTrackerPro!2016");
		    transport.sendMessage(message, addressArray);
		    transport.close();
		    return true;
		} catch (MessagingException ex) {
			log.severe("Exception sending message: " + ex.getMessage());
		    ex.printStackTrace();
		    return false;
		}
	}
}
