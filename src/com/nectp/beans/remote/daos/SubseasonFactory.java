package com.nectp.beans.remote.daos;

import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;

/** Subseason Factory - Generates a new Subseason of the specified type if one does not already exist
 * 
 * @author Kevin C. Buckley
 * @since  9-16-15
 */
public interface SubseasonFactory extends SubseasonService {

	/** Given the specified subseason type & season, create a new Subseason record if one does not already exist
	 * 
	 * @param subseasonType the NEC enum type relating to the new subseason
	 * @param season the Season for which to add the new subseason
	 * @return the newly created subseason for the specified combination, or the existing subseason if created previously
	 */
	public Subseason createSubseasonInSeason(NEC subseasonType, Season season);
	
}
