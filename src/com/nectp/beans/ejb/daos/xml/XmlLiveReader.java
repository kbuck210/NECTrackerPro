package com.nectp.beans.ejb.daos.xml;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import org.w3c.dom.Element;

public class XmlLiveReader {

	private static final Logger log = Logger.getLogger(XmlLiveReader.class.getName());
	
	public static Calendar parseDate(Element game) {
		Calendar gameDate = null;
		String date = game.getAttribute("eid");
		String kickoffTime = game.getAttribute("t");
		
		if (date != null && !date.trim().isEmpty() && 
				kickoffTime != null && !kickoffTime.trim().isEmpty()) {
			String[] timeParts = kickoffTime.split(":");
			
			Integer hour = null, min = null;
			if (timeParts.length == 2) {
				try {
					hour = Integer.parseInt(timeParts[0]);
					min = Integer.parseInt(timeParts[1]);
				} catch (NumberFormatException e) {
					log.severe("Exception parsing hour: " + timeParts[0] + " and minute: " + timeParts[1]);
					log.severe(e.getMessage());
					e.printStackTrace();
				}
			}
			
			Integer year = null, month = null, day = null;
			if (date.length() >= 9) {
				String yearStr = date.substring(0, 4);
				String monthStr = date.substring(4, 6);
				String dayStr = date.substring(6, 8);
				try {
					year = Integer.parseInt(yearStr);
					month = Integer.parseInt(monthStr);
					day = Integer.parseInt(dayStr);
				} catch (NumberFormatException e) {
					log.severe("Exception parsing year: " + yearStr + " month: " + monthStr + " day: " + dayStr);
					log.severe(e.getMessage());
					e.printStackTrace();
				}
			}
			
			//	If all of the date attributes are defined, create the calendar to return
			if (hour != null && min != null && year != null && month != null && day != null) {
				gameDate = new GregorianCalendar(year, month, day, hour, min);
			}
		}
		
		return gameDate;
	}
	
	public static Integer parseInteger(String intString) {
		Integer integer = null;
		try {
			integer = Integer.parseInt(intString);
		} catch (NumberFormatException e) {
			log.severe("Exception parsing int: " + intString);
			log.severe(e.getMessage());
		}
		return integer;
	}
}
