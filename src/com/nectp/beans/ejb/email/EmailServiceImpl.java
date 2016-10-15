//package com.nectp.beans.ejb.email;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Properties;
//import java.util.logging.Logger;
//
//import javax.ejb.Stateless;
//import javax.mail.Authenticator;
//import javax.mail.MessagingException;
//import javax.mail.Multipart;
//import javax.mail.PasswordAuthentication;
//import javax.mail.Session;
//import javax.mail.Transport;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeBodyPart;
//import javax.mail.internet.MimeMessage;
//import javax.mail.internet.MimeMultipart;
//import javax.mail.internet.MimeMessage.RecipientType;
//
//import com.nectp.jpa.entities.Email;
//
//@Stateless
//public class EmailServiceImpl implements EmailService {
//
//	public enum Protocol {
//		SMTP,
//		SMTPS,
//		TLS
//	}
//	
//	private int port = 465;
//	private String host = "smtp.gmail.com";
//	private String from = "nectpAdmin@gmail.com";
//	private boolean authenticate = true;
//	private String username = "nectrackerpro@gmail.com";
//	private String password = "NECTrackerPro!2016";
//	private Protocol protocol = Protocol.SMTPS;
//	private boolean debug = true;
//	
//	private Logger log;
//	
//	//	Set the default email service values
//	public EmailServiceImpl() {
//		log = Logger.getLogger(EmailServiceImpl.class.getName());
//	}
//	
//	@Override
//	public boolean sendEmail(List<Email> recipients, String subject, String textBody, String htmlBody) {
//		Properties props = new Properties();
//		props.put("mail.smtp.host", host);
//		props.put("mail.smtp.port", port);
//		switch (protocol) {
//		    case SMTPS:
//		        props.put("mail.smtp.ssl.enable", true);
//		        break;
//		    case TLS:
//		        props.put("mail.smtp.starttls.enable", true);
//		        break;
//		    default:
//		    	break;
//		}
//		
//		Authenticator authenticator = null;
//		if (authenticate) {
//		    props.put("mail.smtp.auth", true);
//		    authenticator = new Authenticator() {
//		        private PasswordAuthentication pa = new PasswordAuthentication(username, password);
//		        @Override
//		        public PasswordAuthentication getPasswordAuthentication() {
//		            return pa;
//		        }
//		    };
//		}
//		
//		Session session = Session.getInstance(props, authenticator);
//		MimeMessage message = new MimeMessage(session);
//		try {
//		    message.setFrom(new InternetAddress(from));
//		    
//		    List<InternetAddress> addresses = new ArrayList<InternetAddress>();
//		    for (Email email : recipients) {
//		    	if (email.isEmailsRequested()) {
//		    		InternetAddress address = new InternetAddress(email.getEmailAddress());
//		    		addresses.add(address);
//		    	}
//		    }
//		    
//		    InternetAddress[] addressArray = addresses.toArray(new InternetAddress[addresses.size()]);
//		    message.setRecipients(RecipientType.TO, addressArray);
//		    message.setSubject(subject);
//		    message.setSentDate(new Date());
//		    
//		    //	Alternative indicates multiple versions of same content
//		    Multipart multipart = new MimeMultipart("alternative");
//		    
//		    //	Text content added in case recipient can't support HTML
//		    if (textBody != null) {
//		    	MimeBodyPart textPart = new MimeBodyPart();
//			    textPart.setText(textBody);
//			    multipart.addBodyPart(textPart);
//		    }
//		    
//		    //	Desired format goes last
//		    if (htmlBody != null) {
//		    	MimeBodyPart htmlPart = new MimeBodyPart();
//			    htmlPart.setContent(htmlBody, "text/html");
//			    multipart.addBodyPart(htmlPart);
//		    }
//		   
//		    message.setContent(multipart);
//		    
//		    Transport.send(message);
//		    return true;
//		} catch (MessagingException ex) {
//			log.severe("Exception sending message: " + ex.getMessage());
//		    ex.printStackTrace();
//		    return false;
//		}
//	}
//	
//	public int getPort() {
//		return port;
//	}
//	
//	public void setPort(int port) {
//		this.port = port;
//	}
//	
//	public String getHost() {
//		return host;
//	}
//	
//	public void setHost(String host) {
//		this.host = host;
//	}
//	
//	public String getFrom() {
//		return from;
//	}
//	
//	public void setFrom(String from) {
//		this.from = from;
//	}
//	
//	public boolean isAuthenticate() {
//		return authenticate;
//	}
//	
//	public void setAuthenticate(boolean authenticate) {
//		this.authenticate = authenticate;
//	}
//	
//	public String getUsername() {
//		return username;
//	}
//	
//	public void setUsername(String username) {
//		this.username = username;
//	}
//	
//	public void setPassword(String password) {
//		this.password = password;
//	}
//	
//	public Protocol getProtocol() {
//		return protocol;
//	}
//	
//	public void setProtocol(Protocol protocol) {
//		this.protocol = protocol;
//	}
//	
//	public boolean isDebug() {
//		return debug;
//	}
//	
//	public void setDebug(boolean debug) {
//		this.debug = debug;
//	}
//}
