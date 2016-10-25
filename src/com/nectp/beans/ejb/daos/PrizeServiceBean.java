package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.PrizeService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Prize;

@Stateless
public class PrizeServiceBean extends DataServiceBean<Prize> implements PrizeService {
	private static final long serialVersionUID = -8556516531996052943L;

	@Override
	public Prize selectPrizeByType(NEC prizeType) throws NoExistingEntityException {
		Logger log = Logger.getLogger(PrizeServiceBean.class.getName());
		Prize prize = null;
		if (prizeType == null) {
			log.severe("No prize type specified, can not retrieve prize");
		}
		else {
			TypedQuery<Prize> pq = em.createNamedQuery("Prize.selectPrizeByType", Prize.class);
			pq.setParameter("prizeType", prizeType.ordinal());
			try {
				prize = pq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple prize entities found for: " + prizeType.name());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No results found for: " + prizeType.name());
				log.warning(e.getMessage());
				throw new NoExistingEntityException(e);
			} catch (Exception e) {
				log.severe("Exception caught retrieving prize: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return prize;
	}

}
