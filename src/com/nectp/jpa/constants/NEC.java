package com.nectp.jpa.constants;


/** Common enum values for NEC Tracker
 * 
 * @author Kevin C. Buckley
 *
 */
public enum NEC {
	
	SEASON,
	FIRST_HALF,
	MNF,
	MNF_TNT,
	MONEY_BACK,
	PLAYOFFS,
	SECOND_HALF,
	SUPER_BOWL,
	TNT,
	TWO_AND_OUT, 
	ONE_AND_OUT,
	POW;
	
	@Override
	public String toString() {
		switch(this) {
		case SEASON:
			return "Season";
		case FIRST_HALF:
			return "First Half";
		case SECOND_HALF:
			return "Second Half";
		case PLAYOFFS:
			return "Playoffs";
		case SUPER_BOWL:
			return "NEC Champion";
		case MNF:
			return "MNF";
		case TNT:
			return "TNT";
		case MNF_TNT:
			return "MNF_TNT";
		case TWO_AND_OUT:
			return "Two and Out";
		case MONEY_BACK:
			return "Moneyback Bowl";
		case ONE_AND_OUT:
			return "One and Out";
		case POW:
			return "Pick of the week";
		default:
			return null;
		}
	}
	
	public static NEC getNECForString(final String necString) {
		if (necString == null || necString.trim().isEmpty()) return null;
		
		for (NEC nec : NEC.values()) {
			if (nec.toString().toLowerCase().trim().equals(necString.toLowerCase().trim())) {
				return nec;
			}
		}
		
		return null;
	}
}
