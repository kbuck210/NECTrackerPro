package com.nectp.beans.ejb.email;

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

	private final String DEFAULT_HEADER_BACKGROUND = "/img/stadium-smaller.png";
	private final String DEFAULT_MAIN_IMAGE = "/img/NECDefaultMainImage.png";
	
	private Logger log;
	
	//	Set the default email service values
	public EmailServiceImpl() {
		log = Logger.getLogger(EmailServiceImpl.class.getName());
	}
	
	@Override
	public boolean sendEmail(List<Email> recipients, String subject, String textBody, String htmlBody) {
		return sendEmail(recipients, subject, textBody, htmlBody, null, null);
	}
	
	@Override
	public boolean sendEmail(List<Email> recipients, String subject, String textBody, String htmlBody, 
			String headerImgRelPath, String mainImgRelPath) {
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
		    
		    //	Create Mixed parent, with alternative child (iOS screws up ordering, only shows images if added after HTML)
		    Multipart mixed = new MimeMultipart("mixed");
		    
		    //	Create an alternative child, and a mimebodypart for the alternative section, adding as a child to the parent
		    Multipart altChild = new MimeMultipart("alternative");
		    MimeBodyPart altPart = new MimeBodyPart();
		    altPart.setContent(altChild);
		    mixed.addBodyPart(altPart);
		    
		    //	Add the text and HTML versions as children of the alternative section
		    if (textBody != null) {
		    	MimeBodyPart textPart = new MimeBodyPart();
			    textPart.setContent(textBody, "text/plain");
			    altChild.addBodyPart(textPart);
		    }
		    if (htmlBody != null) {
		    	Multipart related = new MimeMultipart("related");
		    	MimeBodyPart relChild = new MimeBodyPart();
		    	relChild.setContent(related);
		    	altChild.addBodyPart(relChild);
		    	
		    	MimeBodyPart htmlPart = new MimeBodyPart();
			    htmlPart.setContent(htmlBody, "text/html");
			    related.addBodyPart(htmlPart);
			    
			    ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
			    String relativeBackgroundPath;
			    if (headerImgRelPath == null) {
			    	relativeBackgroundPath = DEFAULT_HEADER_BACKGROUND;
			    }
			    else {
			    	relativeBackgroundPath = headerImgRelPath;
			    }
			    String backgroundPath = ctx.getRealPath(relativeBackgroundPath);
			    
			    String relativeMainImagePath;
			    if (mainImgRelPath == null) {
			    	relativeMainImagePath = DEFAULT_MAIN_IMAGE;
			    }
			    else {
			    	relativeMainImagePath = mainImgRelPath;
			    }
			    String mainImagePath = ctx.getRealPath(relativeMainImagePath);
			    
			    MimeBodyPart background = new MimeBodyPart();
			    MimeBodyPart mainImage = new MimeBodyPart();

			    DataSource bds = new FileDataSource(backgroundPath);
			    background.setDataHandler(new DataHandler(bds));
			    background.setHeader("Content-ID", "<background>");
			    related.addBodyPart(background);
			    
			    DataSource mids = new FileDataSource(mainImagePath);
			    mainImage.setDataHandler(new DataHandler(mids));
			    mainImage.setHeader("Content-ID", "<mainImage>");
			    related.addBodyPart(mainImage);
		    }
		   
		    message.setContent(mixed);
		    
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
