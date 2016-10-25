package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.TeamService;
import com.nectp.jpa.entities.Team;

@Stateless
public class TeamServiceBean extends DataServiceBean<Team> implements TeamService {
	private static final long serialVersionUID = 7381216334640961056L;

	@Override
	public Team selectTeamByFranchiseId(int franchiseId) throws NoExistingEntityException {
		Logger log = Logger.getLogger(TeamServiceBean.class.getName());
		Team team = null;
		TypedQuery<Team> tq = em.createNamedQuery("Team.selectByFranchiseId", Team.class);
		tq.setParameter("franchiseId", franchiseId);
		try {
			team = tq.getSingleResult();
		} catch (NonUniqueResultException e) {
			log.severe("Multiple franchises found for ID: " + franchiseId);
			log.severe(e.getMessage());
			e.printStackTrace();
		} catch (NoResultException e) {
			log.warning("No franchise found for ID: " + franchiseId);
			log.warning(e.getMessage());
			throw new NoExistingEntityException(e);
		} catch (Exception e) {
			log.severe("Exception caught retrieving franchise: " + e.getMessage());
			e.printStackTrace();
		}
		
		return team;
 	}
}
