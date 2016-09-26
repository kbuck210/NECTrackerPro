package com.nectp.beans.named;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.primefaces.event.FlowEvent;

@Named(value="playerWizard")
@RequestScoped
public class PlayerWizard implements Serializable {
	private static final long serialVersionUID = 3235824235220680765L;
	
	private String firstName;
	private String lastName;
	private String sinceYear;
	private String nickname;
	private String password;
	private String passConfirm;
	private List<String> emailAddresses;
	
	public PlayerWizard() {
		emailAddresses = new LinkedList<String>();
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getSinceYear() {
		return sinceYear;
	}

	public void setSinceYear(String sinceYear) {
		this.sinceYear = sinceYear;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassConfirm() {
		return passConfirm;
	}

	public void setPassConfirm(String passConfirm) {
		this.passConfirm = passConfirm;
	}
	
	public List<String> getEmailAddresses() {
		return emailAddresses;
	}
	
	public void addEmailAddress(String emailAddress) {
		emailAddresses.add(emailAddress);
	}
	
	public void removeEmailAddress(String emailAddress) {
		emailAddresses.remove(emailAddress);
	}
	
	public String onFlowProcess(FlowEvent event) {
		return event.getNewStep();
    }
	
	public void save() {
		//	TODO: user upload attempt
	}
}
