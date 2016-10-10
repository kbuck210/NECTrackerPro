package com.nectp.beans.named;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.ApplicationState;
import com.nectp.beans.ejb.LoginService;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.Season;

@Named(value="headerBean")
@RequestScoped
public class HeaderBean implements Serializable {
	private static final long serialVersionUID = -6289129960600725794L;

	private String buttonText;
	
	private boolean buttonDisabled;
	
	private String emailAddress;
	
	private String password;
	
	private String seasonTitle;
	
	@Inject
	private LoginService loginService;
	
	@Inject
	private ApplicationState appState;
	
	@PostConstruct
	public void init() {
		//	Sets the login button disabled if the application currently has a user
		Player user = appState.getUser();
		if (user == null) {
			buttonText = "Login:";
			buttonDisabled = false;
		}
		else {
			String name = user.getName();
			String[] nameParts = name.split(" ");
			String firstName = nameParts[0];
			buttonText = "Hi " + firstName + "!";
			buttonDisabled = true;
		}
		
		Season currentSeason = appState.getCurrentSeason();
		if (currentSeason != null) {
			seasonTitle = " - Season " + currentSeason.getSeasonNumber();
		}
		else seasonTitle = "";
	}
	
	/**
	 * @return the buttonText
	 */
	public String getButtonText() {
		return buttonText;
	}

	/**
	 * @param buttonText the buttonText to set
	 */
	public void setButtonText(String buttonText) {
		this.buttonText = buttonText;
	}

	/**
	 * @return the buttonDisabled
	 */
	public boolean isButtonDisabled() {
		return buttonDisabled;
	}

	/**
	 * @param buttonDisabled the buttonDisabled to set
	 */
	public void setButtonDisabled(boolean buttonDisabled) {
		this.buttonDisabled = buttonDisabled;
	}

	/**
	 * @return the emailAddress
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * @param emailAddress the emailAddress to set
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getSeasonTitle() {
		return seasonTitle;
	}

	public void login() {
		loginService.login(emailAddress, password);
	}
	
	public void logout() {
		loginService.logout();
	}
}
