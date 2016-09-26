package com.nectp.beans.named;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;

import javax.validation.constraints.Size;

@Named(value="repeatingInput")
@ConversationScoped
public class RepeatingInput implements Serializable {
	private static final long serialVersionUID = -4772951641002007206L;
	
	@Size(min=1)
	private List<InputItem> items;

	public RepeatingInput() {
		items = new LinkedList<InputItem>();
	}
	
	public List<InputItem> getItems() {
		return items;
	}
	
	public void setItems(List<InputItem> items) {
		this.items = items;
	}
	
	public void addItem(String label, String value) {
		InputItem newItem = new InputItem();
		newItem.setLabel(label);
		newItem.setValue(value);
		items.add(newItem);
	}
	
	public void removeItem(InputItem item) {
		items.remove(item);
	}

}
