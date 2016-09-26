package com.nectp.jpa.constants;

import java.util.TimeZone;

public enum Timezone {

	ALASKA,
    ALEUTIAN,
    ARIZONA,
    CENTRAL,
    EAST_INDIANA,
    EASTERN,
    HAWAII,
    INDIANA_STARKE,
    MICHIGAN,
    MOUNTAIN,
    PACIFIC,
    PACIFIC_NEW,
    SAMOA,
    EU_LONDON,
    GMT;

    public static TimeZone getTimeZone(Timezone zone) {
        if (zone != null) return TimeZone.getTimeZone(zone.toString());
        else return null;
    }

    @Override
    public String toString() {
        switch (this) {
        case ALASKA:
            return "US/Alaska";
        case ALEUTIAN:
            return "US/Aleutian";
        case ARIZONA:
            return "US/Arizona";
        case CENTRAL:
            return "US/Central";
        case EAST_INDIANA:
            return "US/East-Indiana";
        case EASTERN:
            return "US/Eastern";
        case HAWAII:
            return "US/Hawaii";
        case INDIANA_STARKE:
            return "US/Indiana-Starke";
        case MICHIGAN:
            return "US/Michigan";
        case MOUNTAIN:
            return "US/Mountain";
        case PACIFIC:
            return "US/Pacific";
        case PACIFIC_NEW:
            return "US/Pacific-New";
        case SAMOA:
            return "US/Samoa";
        case EU_LONDON:
            return "Europe/London";
        case GMT:
            return "GMT";
        default:
            return null;
        }
    }
    
    public static Timezone getTimezoneForString(String timezone) {
    	Timezone zone = null;
    	for (Timezone tz : Timezone.values()) {
    		if (tz.name().toUpperCase().equals(timezone.toUpperCase())) {
    			zone = tz;
    			break;
    		}
    	}
    	return zone;
    }
}
