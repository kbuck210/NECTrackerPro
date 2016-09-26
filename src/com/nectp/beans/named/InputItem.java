package com.nectp.beans.named;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named(value="inputItem")
@RequestScoped
public class InputItem implements Serializable {
	private static final long serialVersionUID = 3398349884196500487L;
	
	private String label;
	private String value;
	
	public InputItem() {
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
}
