package com.nectp.beans.ejb.email;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.UploadedFile;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.named.upload.FileUploadImpl;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Email;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.webtools.RomanNumeral;

@Named(value="emailWizardBean")
@ViewScoped
public class EmailWizardBean extends FileUploadImpl implements Serializable {
	private static final long serialVersionUID = -3807332246735823521L;
	
	private final String REPLACE_SEASON_NUMERAL = "#SeasonNumeral";
	private final String REPLACE_TITLE = "#Title";
	private final String REPLACE_SUBTITLE = "#Subtitle";
	private final String REPLACE_HEADLINE = "#TheLead";
	private final String REPLACE_CAPTION = "#ImgCaption";
	private final String REPLACE_CAPTION_LINK = "#CaptionLink";
	private final String REPLACE_CAPTION_LINK_TEXT = "#LinkText";
	private final String REPLACE_SUMMARY = "#Summary";
	private final String REPLACE_LEADER_TITLE = "#LeaderTitle";
	private final String REPLACE_LEADERS = "#Leaders";
	private final String REPLACE_EXCEL_DOWNLOAD_LINK = "#ExcelDownloadLink";
	private final String REPLACE_PDF_DOWNLOAD_LINK = "#PdfDownloadLink";
	
	private final String summaryPTag = "<p style=\"Margin: 0; Margin-bottom: 10px; color: #0a0a0a; font-family: Helvetica, Arial, sans-serif; font-size: 16px; font-weight: normal; line-height: 1.3; margin: 0; margin-bottom: 10px; padding: 0; text-align: left;\">";
	private final String leaderPTag = "<p style=\"Margin: 0; Margin-bottom: 10px; color: #0a0a0a; font-family: Helvetica, Arial, sans-serif; font-size: 16px; font-weight: normal; line-height: 1.3; margin: 0; margin-bottom: 10px; padding: 0; text-align: left;\"><a style=\"Margin: 0; color: #2199e8; font-family: Helvetica, Arial, sans-serif; font-weight: normal; line-height: 1.3; margin: 0; padding: 0; text-align: left; text-decoration: none;\" href=\"";
	
	private String subject;
	private String seasonNumeral;
	private String title;
	private String subtitle;
	private String headline;
	private String imgCaption;
	private String imgUrlText;
	
	private String downloadExcel;
	private String downloadPdf;
	
	private String leaderTitle;
	private String leaders;
	
	private String altText = null;
	private String messageText;
	private String emailContent;
	
	private String mainImgUrl;
	
	private boolean useDefaultImage = true;
	
	private String captionDestination;
	private List<SelectItem> pageDestinations;
	private Map<String, String> labelMap;
	
	private boolean sendToAll = true;
	
	private String[] selectedRecipients;
	private List<SelectItem> recipients;
	
	private String documentText;
	
	private TreeMap<RecordAggregator, List<AbstractTeamForSeason>> rankMap;
	
	private Season currentSeason;
	
	private Week currentWeek;
	
	private Logger log = Logger.getLogger(EmailWizardBean.class.getName());
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private RecordService recordService;
	
	@EJB
	private EmailService emailService;
	
	@EJB
	private com.nectp.beans.remote.daos.EmailService emailRetrieval;
	
	@PostConstruct
	public void init() {
		//	Get the current season, and current week information
		currentSeason = seasonService.selectCurrentSeason();
		if (currentSeason != null) {
			int seasonNum = currentSeason.getSeasonNumber();
			RomanNumeral roman = new RomanNumeral(seasonNum);
			seasonNumeral = roman.toString();
			
			labelMap = new HashMap<String, String>();
			SelectItemGroup players = new SelectItemGroup("Players:");
			ArrayList<SelectItem> playerItems = new ArrayList<SelectItem>();
			recipients = new ArrayList<SelectItem>();
			for (PlayerForSeason player : currentSeason.getPlayers()) {
				String playerPath = "/nec" + seasonNum + "/players/" + player.getNickname();
				SelectItem playerItem = new SelectItem(playerPath, player.getNickname());
				playerItems.add(playerItem);
				labelMap.put(playerPath, player.getNickname());
				
				List<Email> playerEmails = emailRetrieval.selectAllByPlayer(player.getPlayer());
				for (Email email : playerEmails) {
					String display = player.getNickname() + " (" + email.getEmailAddress() + ")";
					SelectItem playerEmail = new SelectItem(display, email.getEmailAddress());
					recipients.add(playerEmail);
				}
			}
			players.setSelectItems(playerItems.toArray(new SelectItem[playerItems.size()]));
			
			SelectItemGroup teams = new SelectItemGroup("Teams:");
			ArrayList<SelectItem> teamItems = new ArrayList<SelectItem>();
			for (TeamForSeason team : currentSeason.getTeams()) {
				String teamPath = "/nec" + seasonNum + "/teams/" + team.getTeamAbbr();
				String teamLabel;
				if (team.getTeamAbbr().equals("NYJ") || team.getTeamAbbr().equals("NYG")) {
					teamLabel = "NY " + team.getName();
				}
				else {
					teamLabel = team.getTeamCity();
				}
				SelectItem teamItem = new SelectItem(teamPath, teamLabel);
				teamItems.add(teamItem);
				labelMap.put(teamPath, team.getTeamCity());
			}
			teams.setSelectItems(teamItems.toArray(new SelectItem[teamItems.size()]));
			
			pageDestinations = new ArrayList<SelectItem>();
			pageDestinations.add(players);
			pageDestinations.add(teams);
			
			currentWeek = currentSeason.getCurrentWeek();
			if (currentWeek != null) {
				NEC subseasonType = currentWeek.getSubseason().getSubseasonType();
				leaderTitle = subseasonType.toString() + " - Week " + currentWeek.getWeekNumber() + ": ";
				
				rankMap = recordService.getPlayerRankedScoresForType(subseasonType, currentSeason, true);
				setLeaders();
				
				downloadExcel = "http://24.147.36.125:8080/NECTrackerPro/rest/download/excel/" + seasonNum + "/" + currentWeek.getWeekNumber();
//				downloadExcel = "/download/excel/" + seasonNum + "/" + currentWeek.getWeekNumber();
//				downloadPdf = "/download/pdf/" + seasonNum + "/" + currentWeek.getWeekNumber();
			}
		}
	}
	
	@Override
	public void upload(FileUploadEvent event) {
		UploadedFile file = event.getFile();
		if (file != null) {
			try {
				InputStream iStream = file.getInputstream();
				
//				String path = "/NECTrackerResources/images/" + file.getFileName();
				String path = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("upload.images") + file.getFileName();
				
				Path filePath = Paths.get(path);
				File newFile = filePath.toFile();
				log.info("Writing to file: " + newFile.getAbsolutePath());
				
			    OutputStream copyStream = new FileOutputStream(newFile);
			    
			    int read = 0;
			    byte[] bytes = new byte[1024];
			    while ((read = iStream.read(bytes)) != -1) {
			    	copyStream.write(bytes, 0, read);
			    }
			    iStream.close();
			    copyStream.flush();
			    copyStream.close();
			    
			    mainImgUrl = newFile.getAbsolutePath();
			} catch (IOException e) {
				log.warning("Upload failed: reverting to default image.");
				log.warning(e.getMessage());
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Upload Error:", e.getMessage());
		        FacesContext.getCurrentInstance().addMessage(null, message);
				e.printStackTrace();
				mainImgUrl = null;
				useDefaultImage = true;
			}
		}
		else {
			log.warning("No file uploaded! Using default image.");
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Upload Error:", "File not found!");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
	
	@Override
	public void submit() { /* Not used for implementation purposes... */ }

	
	/* Getters & Setters for Wizard */
	
	public boolean isSendToAll() {
		return sendToAll;
	}
	
	public void setSendToAll(boolean sendToAll) {
		this.sendToAll = sendToAll;
	}
	
	public String[] getSelectedRecipients() {
		return selectedRecipients;
	}
	
	public void setSelectedRecipients(String[] selectedRecipients) {
		this.selectedRecipients = selectedRecipients;
	}
	
	public List<SelectItem> getRecipients() {
		return recipients;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getSubtitle() {
		return subtitle;
	}
	
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle == null ? "" : subtitle;
	}
	
	public String getHeadline() {
		return headline;
	}
	
	public void setHeadline(String headline) {
		this.headline = headline == null ? "" : headline;
	}
	
	public boolean getUseDefaultImage() {
		return useDefaultImage;
	}
	
	public void setUseDefaultImage(boolean useDefaultImage) {
		this.useDefaultImage = useDefaultImage;
	}
	
	public String getImgCaption() {
		return imgCaption;
	}
	
	public void setImgCaption(String imgCaption) {
		this.imgCaption = imgCaption;
	}
	
	public String getCaptionDestination() {
		return captionDestination;
	}
	
	public void setCaptionDestination(String destination) {
		this.captionDestination = destination;
		this.imgUrlText = labelMap.get(destination) + " &raquo;";
	}
	
	public List<SelectItem> getPageDestinations() {
		return pageDestinations;
	}
	
	public String getMessageText() {
		return messageText;
	}
	
	public void setMessageText(String messageText) {
		this.altText = messageText;
		StringBuilder paragraph = new StringBuilder();
		paragraph.append(summaryPTag);
		String rftHtml = rtfToHtml(new StringReader(messageText));
		paragraph.append(rftHtml);
		paragraph.append("</p>");
//		this.messageText = rtfToHtml(new StringReader(messageText));
		this.messageText = paragraph.toString();
	}
	
	public void setEmailContent(String emailContent) {
		this.emailContent = emailContent;
	}
	
	public String getEmailContent() {
		return emailContent;
	}
	
//	/** Replaces the '#Summary' tag in the html template with the input text, wrapped in paragraph tags
//	 * 
//	 * @param summary the HTML-wrapped concatenation of the user input, wrapped around each newline
//	 */
//	public void setMessageText(String summary) {
//		//	Get the wizard input, splitting on input newlines
//		String[] paragraphs = summary.split("\n");
//		//	Create a stringbuilder & wrap each newline segment in a paragraph style tag & closing tag
//		StringBuilder summaryBuilder = new StringBuilder();
//		for (String s : paragraphs) {
//			if (s != null && !s.isEmpty()) {
//				summaryBuilder.append(summaryPTag);
//				summaryBuilder.append(s);
//				summaryBuilder.append("</p>");
//			}
//		}
//		this.summary = summaryBuilder.toString();
//	}
	
	private void setLeaders() {
		StringBuilder leadersBuilder = new StringBuilder();
		for (Entry<RecordAggregator, List<AbstractTeamForSeason>> rankEntry : rankMap.entrySet()) {
			List<AbstractTeamForSeason> leaderList = rankEntry.getValue();
			Collections.sort(leaderList);
			for (AbstractTeamForSeason player : leaderList) {
				Integer seasonNum = player.getSeason().getSeasonNumber();
				String leaderLink = "/nec" + seasonNum + "/players/" + player.getNickname();
				Integer leaderScore = rankEntry.getKey().getTotalScore();
				leadersBuilder.append(leaderPTag);
				leadersBuilder.append(leaderLink);
				leadersBuilder.append("\">");
				leadersBuilder.append(player.getNickname());
				leadersBuilder.append(": ");
				if (leaderScore > 0) {
					leadersBuilder.append("+");
				}
				leadersBuilder.append(leaderScore.toString());
				leadersBuilder.append(" &raquo;</a></p>");
			}
		}
		
		this.leaders = leadersBuilder.toString();
	}
	
	private String getDocumentText() {
		//	Get the document template 
		ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
	    String relativeTemplatePath = "/inlinedEmail.html";
	    StringBuilder document = new StringBuilder();
	    try {
	    	InputStream iStream = ctx.getResourceAsStream(relativeTemplatePath);
		    BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
		    
		    String line;
		    while ((line = br.readLine()) != null) {
		    	document.append(line);
		    }
		    br.close();
	    } catch (IOException e) {
	    	FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Inline DocRead Error:", e.getMessage());
	        FacesContext.getCurrentInstance().addMessage(null, message);
	    	return null;
	    }
	    //	Get the read document as a single string
	    String inline = document.toString();
	    
	    //	Replace the delimiters with their user-entered values
	    inline = inline.replace(REPLACE_SEASON_NUMERAL, seasonNumeral);
	    inline = inline.replace(REPLACE_TITLE, title);
	    inline = inline.replace(REPLACE_SUBTITLE, subtitle);
	    inline = inline.replace(REPLACE_HEADLINE, headline);
	    inline = inline.replace(REPLACE_CAPTION, imgCaption);
	    inline = inline.replace(REPLACE_CAPTION_LINK, captionDestination);
	    inline = inline.replace(REPLACE_CAPTION_LINK_TEXT, imgUrlText);
	    inline = inline.replace(REPLACE_SUMMARY, emailContent);
	    inline = inline.replace(REPLACE_LEADER_TITLE, leaderTitle);
	    inline = inline.replace(REPLACE_LEADERS, leaders);
	    inline = inline.replace(REPLACE_EXCEL_DOWNLOAD_LINK, downloadExcel);
	    inline = inline.replace(REPLACE_PDF_DOWNLOAD_LINK, downloadPdf);
	    
	    return inline;
	}
	
	public void saveListener() {
		emailContent = emailContent.replaceAll("\\r|\\n", "");  
//		  
//        final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Content",  
//        		emailContent.length() > 150 ? emailContent.substring(0, 100) : emailContent);  
//  
//        FacesContext.getCurrentInstance().addMessage(null, msg);  
	}
	
	public boolean isSendDisabled() {
		return emailContent == null || emailContent.isEmpty();
	}
	
	public String send() {
		if (currentSeason == null) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Send Error:", "Current season not defined!");
	        FacesContext.getCurrentInstance().addMessage(null, message);
			return "/dataLoad.xhtml";
		}
		
		List<Email> addresses;
		if (sendToAll) {
			addresses = emailRetrieval.selectAllRecipientsBySeason(currentSeason);
		}
		else {
			addresses = new ArrayList<Email>();
			for (String selectedAddress : selectedRecipients) {
				log.info("Selected: " + selectedAddress);
				int addStart = selectedAddress.indexOf("(") + 1;
				int addEnd = selectedAddress.indexOf(")");
				String address = selectedAddress.substring(addStart, addEnd);
				log.info("Got address: " + address);
				List<Email> selectedEmail = emailRetrieval.selectByAddress(address);
				addresses.addAll(selectedEmail);
			}
		}
	
		documentText = getDocumentText();
		if (documentText == null) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Document Error:", "Failed to get inline document!");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
		
		for (Email email : addresses) {
			log.info("Sending to: " + email.getEmailAddress());
		}
		log.info("Message content: " + emailContent);
		
		boolean sent = emailService.sendEmail(addresses, subject, altText, documentText, null, mainImgUrl);
		if (sent) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "Email sent successfully.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
		else {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Email failed to send.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
		
		return "/dataLoad.xhtml";
	}
	
	public String onFlowProcess(FlowEvent event) {
		return event.getNewStep();
	}
	
	
	public static String rtfToHtml(Reader rtf) {
		JEditorPane p = new JEditorPane();
		Document doc = p.getDocument();
		p.setContentType("text/rtf");
		EditorKit kitRtf = p.getEditorKitForContentType("text/rtf");
		try {
			kitRtf.read(rtf, doc, 0);
			EditorKit kitHtml = p.getEditorKitForContentType("text/html");
			Writer writer = new StringWriter();
			kitHtml.write(writer, doc, 0, doc.getLength());
			return writer.toString();
		} catch (BadLocationException | IOException e) {
			return null;
		}
	}
}