package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;

import com.nectp.beans.remote.daos.PrizeFactory;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Prize;

@Stateless
public class PrizeFactoryBean extends PrizeServiceBean implements PrizeFactory {
	private static final long serialVersionUID = 3291818969654006837L;

	@Override
	public Prize createPrize(NEC prizeType) {
		Logger log = Logger.getLogger(PrizeFactoryBean.class.getName());
		Prize prize;
		if (prizeType == null) {
			prize = null;
			log.severe("No prize type specified, can not create prize!");
		}
		else {
			//	Check whether the specified prize type already exists in the database
			try {
				prize = selectPrizeByType(prizeType);
			} 
			//	If no prize found for the specified type, create a new one
			catch (NoExistingEntityException e) {
				prize = new Prize();
				prize.setPrizeType(prizeType);
				
				boolean success = insert(prize);
				if (!success) {
					prize = null;
				}
			}
		}
		
		return prize;
	}
	
}
