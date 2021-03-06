package com.nectp.beans.ejb.daos;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;

@Stateless
public class PlayerForSeasonServiceBean extends DataServiceBean<PlayerForSeason> implements PlayerForSeasonService {
	private static final long serialVersionUID = 3227697810038205133L;

	@Override
	public PlayerForSeason selectPlayerInSeason(Player player, Season season) throws NoExistingEntityException {
		Logger log = Logger.getLogger(PlayerForSeasonServiceBean.class.getName());
		PlayerForSeason pfs = null;
		if (player == null || season == null) {
			log.severe("Player or season not defined, can not select PlayerForSeason");
		}
		else {
			TypedQuery<PlayerForSeason> pq = em.createNamedQuery("PlayerForSeason.selectByPlayerSeason", PlayerForSeason.class);
			pq.setParameter("playerId", player.getAbstractTeamId());
			pq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				pfs = pq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple PFS found for " + player.getName() + " in season " + season.getSeasonNumber());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No result found for " + player.getName() + " in season " + season.getSeasonNumber());
				log.warning(e.getMessage());
				throw new NoExistingEntityException(e);
			} catch (Exception e) {
				log.severe("Exception thrown retrieving PFS: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return pfs;
	}

	@Override
	public PlayerForSeason selectPlayerByExcelName(String excelName, Season season) throws NoExistingEntityException {
		Logger log = Logger.getLogger(PlayerForSeasonServiceBean.class.getName());
		PlayerForSeason pfs = null;
		if (excelName == null || season == null) {
			log.severe("Excel name or season not defined, can not select PlayerForSeason");
		}
		else {
			TypedQuery<PlayerForSeason> pq = em.createNamedQuery("PlayerForSeason.selectByExcelName", PlayerForSeason.class);
			pq.setParameter("excelName", excelName);
			pq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				pfs = pq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple PFS found for " + excelName + " in season " + season.getSeasonNumber());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No result found for " + excelName + " in season " + season.getSeasonNumber());
				log.warning(e.getMessage());
				throw new NoExistingEntityException(e);
			} catch (Exception e) {
				log.severe("Exception thrown retrieving PFS: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return pfs;
	}

	@Override
	public PlayerForSeason selectPlayerByExcelCol(int excelCol, Season season) throws NoExistingEntityException {
		Logger log = Logger.getLogger(PlayerForSeasonServiceBean.class.getName());
		PlayerForSeason pfs = null;
		if (season == null) {
			log.severe("Season not defined, can not select PlayerForSeason");
		}
		else {
			TypedQuery<PlayerForSeason> pq = em.createNamedQuery("PlayerForSeason.selectByExcelCol", PlayerForSeason.class);
			pq.setParameter("excelCol", excelCol);
			pq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				pfs = pq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple PFS found for column " + excelCol + " in season " + season.getSeasonNumber());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No result found for column " + excelCol + " in season " + season.getSeasonNumber());
				log.warning(e.getMessage());
				throw new NoExistingEntityException(e);
			} catch (Exception e) {
				log.severe("Exception thrown retrieving PFS: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return pfs;
	}

	@Override
	public PlayerForSeason selectCommishBySeason(Season season) throws NoExistingEntityException {
		Logger log = Logger.getLogger(PlayerForSeasonService.class.getName());
		PlayerForSeason commish = null;
		if (season == null) {
			log.severe("Season not defined, can not select commish!");
		}
		else {
			TypedQuery<PlayerForSeason> pq = em.createNamedQuery("PlayerForSeason.selectCommishBySeason", PlayerForSeason.class);
			pq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				commish = pq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple commisioners found for season " + season.getSeasonNumber() + "!");
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No commisioner found for season " + season.getSeasonNumber());
				log.warning(e.getMessage());
				throw new NoExistingEntityException(e);
			} catch (Exception e) {
				log.severe("Exception thrown retrieving commish: " + e.getMessage());
				e.printStackTrace();
			}
		} 
		
		return commish;
	}

	@Override
	public PlayerForSeason selectPlayerByNickname(String nickname, Season season) throws NoExistingEntityException {
		Logger log = Logger.getLogger(PlayerForSeasonServiceBean.class.getName());
		PlayerForSeason pfs = null;
		if (nickname == null || season == null) {
			log.severe("Nickname or season not defined, can not select PlayerForSeason");
		}
		else {
			TypedQuery<PlayerForSeason> pq = em.createNamedQuery("PlayerForSeason.selectByNickname", PlayerForSeason.class);
			pq.setParameter("nickname", nickname);
			pq.setParameter("seasonNumber", season.getSeasonNumber());
			try {
				pfs = pq.getSingleResult();
			} catch (NonUniqueResultException e) {
				log.severe("Multiple PFS found for " + nickname + " in season " + season.getSeasonNumber());
				log.severe(e.getMessage());
				e.printStackTrace();
			} catch (NoResultException e) {
				log.warning("No result found for " + nickname + " in season " + season.getSeasonNumber());
				log.warning(e.getMessage());
				throw new NoExistingEntityException(e);
			} catch (Exception e) {
				log.severe("Exception thrown retrieving PFS: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return pfs;
	}
}

