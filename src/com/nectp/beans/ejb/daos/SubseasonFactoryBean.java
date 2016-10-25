package com.nectp.beans.ejb.daos;

import javax.ejb.Stateless;

import com.nectp.beans.remote.daos.SubseasonFactory;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;

@Stateless
public class SubseasonFactoryBean extends SubseasonServiceBean implements SubseasonFactory {
	private static final long serialVersionUID = 5188240506741947086L;
	
	@Override
	public Subseason createSubseasonInSeason(NEC subseasonType, Season season) {
		Subseason subseason = null;
		//	Check if the specified subseason already exists, if so, return the existing subseason
		try {
			subseason = selectSubseasonInSeason(subseasonType, season);
		} catch (NoExistingEntityException e) {
			subseason = new Subseason();
			subseason.setSeason(season);
			subseason.setSubseasonType(subseasonType);
			season.addSubseason(subseason);
			
			//	Insert the newly created subseason to the database, on error, set the return value null
			boolean success = insert(subseason);
			if (!success) {
				subseason = null;
			}
		}
		
		return subseason;
	}
}
