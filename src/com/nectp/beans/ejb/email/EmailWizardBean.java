package com.nectp.beans.ejb.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.ServletContext;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Email;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Week;
import com.nectp.webtools.RomanNumeral;

@Named(value="emailWizardBean")
@ViewScoped
public class EmailWizardBean implements Serializable {
	private static final long serialVersionUID = -3807332246735823521L;
	
	private final String REPLACE_SEASON_NUMERAL = "#SeasonNumeral";
	private final String REPLACE_TITLE = "#Title";
	private final String REPLACE_SUBTITLE = "#Subtitle";
	private final String REPLACE_LEAD = "#TheLead";
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
	private String lead;
	private String imgCaption;
	private String imgUrl;
	private String imgUrlText;
	
	private String summary;
	private String downloadExcel;
	private String downloadPdf;
	
	private String leaderTitle;
	private String leaders;
	
	private String documentText = null;
	private String altText = null;
	
	private TreeMap<RecordAggregator, List<AbstractTeamForSeason>> rankMap;
	
	private Season currentSeason;
	
	private Week currentWeek;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private RecordService recordService;
	
	@EJB
	private EmailService emailService;
	
	@PostConstruct
	public void init() {
		//	Get the current season, and current week information
		currentSeason = seasonService.selectCurrentSeason();
		if (currentSeason != null) {
			int seasonNum = currentSeason.getSeasonNumber();
			RomanNumeral roman = new RomanNumeral(seasonNum);
			seasonNumeral = roman.toString();
			
			currentWeek = currentSeason.getCurrentWeek();
			if (currentWeek != null) {
				NEC subseasonType = currentWeek.getSubseason().getSubseasonType();
				leaderTitle = subseasonType.toString() + " - Week " + currentWeek.getWeekNumber() + ": ";
				
				rankMap = recordService.getPlayerRankedScoresForType(subseasonType, currentSeason, true);
				setLeaders();
			}
		}
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public void setSeasonNumeral(String seasonNumeral) {
		this.seasonNumeral = seasonNumeral;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle == null ? "" : subtitle;
	}
	
	public void setLead(String lead) {
		this.lead = lead == null ? "" : lead;
	}
	
	public void setImgCaption(String imgCaption) {
		this.imgCaption = imgCaption;
	}
	
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl == null ? "" : imgUrl;
	}
	
	public void setImgUrlText(String imgUrlText) {
		this.imgUrlText = imgUrlText == null ? "" : imgUrlText;
	}
	
	/** Replaces the '#Summary' tag in the html template with the input text, wrapped in paragraph tags
	 * 
	 * @param summary the HTML-wrapped concatenation of the user input, wrapped around each newline
	 */
	public void setSummary(String summary) {
		//	Get the wizard input, splitting on input newlines
		String[] paragraphs = summary.split("\n");
		//	Create a stringbuilder & wrap each newline segment in a paragraph style tag & closing tag
		StringBuilder summaryBuilder = new StringBuilder();
		for (String s : paragraphs) {
			if (s != null && !s.isEmpty()) {
				summaryBuilder.append(summaryPTag);
				summaryBuilder.append(s);
				summaryBuilder.append("</p>");
			}
		}
		this.summary = summaryBuilder.toString();
	}
	
	public void setDownloadExcel(String downloadExcel) {
		this.downloadExcel = downloadExcel;
	}
	
	public void setDownloadPdf(String downloadPdf) {
		this.downloadPdf = downloadPdf;
	}
	
	public void setLeaderTitle(String leaderTitle) {
		this.leaderTitle = leaderTitle;
	}
	
	public void setLeaders() {
		List<String> leaders = new ArrayList<String>();
		for (Entry<RecordAggregator, List<AbstractTeamForSeason>> rankEntry : rankMap.entrySet()) {
			List<AbstractTeamForSeason> leaderList = rankEntry.getValue();
			Collections.sort(leaderList);
			for (AbstractTeamForSeason player : leaderList) {
				leaders.add(player.getNickname());
			}
		}
		
		StringBuilder leadersBuilder = new StringBuilder();
		for (String leader : leaders) {
			String leaderLink = "";
			Integer leaderScore = 7;
			leadersBuilder.append(leaderPTag);
			leadersBuilder.append(leaderLink);
			leadersBuilder.append("\">");
			leadersBuilder.append(leader);
			leadersBuilder.append(": ");
			if (leaderScore > 0) {
				leadersBuilder.append("+");
			}
			leadersBuilder.append(leaderScore.toString());
			leadersBuilder.append(" »</a></p>");
		}
		
		this.leaders = leadersBuilder.toString();
	}
	
	public void setDocumentText() {
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
	    	//	TODO update growl that document write failed
	    	return;
	    }
	    //	Get the read document as a single string
	    String inline = document.toString();
	    
	    //	Replace the delimiters with their user-entered values
	    inline = inline.replace(REPLACE_SEASON_NUMERAL, seasonNumeral);
	    inline = inline.replace(REPLACE_TITLE, title);
	    inline = inline.replace(REPLACE_SUBTITLE, subtitle);
	    inline = inline.replace(REPLACE_LEAD, lead);
	    inline = inline.replace(REPLACE_CAPTION, imgCaption);
	    inline = inline.replace(REPLACE_CAPTION_LINK, imgUrl);
	    inline = inline.replace(REPLACE_CAPTION_LINK_TEXT, imgUrlText);
	    inline = inline.replace(REPLACE_SUMMARY, summary);
	    inline = inline.replace(REPLACE_LEADER_TITLE, leaderTitle);
	    inline = inline.replace(REPLACE_LEADERS, leaders);
	    inline = inline.replace(REPLACE_EXCEL_DOWNLOAD_LINK, downloadExcel);
	    inline = inline.replace(REPLACE_PDF_DOWNLOAD_LINK, downloadPdf);
	    
	    this.documentText = inline;
	}
	
	public String getDocumentText() {
		return documentText;
	}
	
	public void setAltText(String altText) {
		this.altText = altText;
	}
	
	public String getAltText() {
		return altText;
	}
	
	public void send() {
		if (currentSeason == null) {
			//	TODO: update growl
			return;
		}
		
		List<PlayerForSeason> players = currentSeason.getPlayers();
		List<Email> addresses = new ArrayList<Email>();
		//	TODO: replace with query
		for (PlayerForSeason player : players) {
			List<Email> emails = player.getPlayer().getEmails();
			for (Email email : emails) {
				if (email.isEmailsRequested()) {
					addresses.add(email);
				}
			}
		}
		
		boolean sent = emailService.sendEmail(addresses, subject, altText, documentText);
		//	TODO: update growl
	}
}
