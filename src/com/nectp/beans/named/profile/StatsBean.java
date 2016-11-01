package com.nectp.beans.named.profile;

import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.named.RecordDisplay;
import com.nectp.jpa.entities.AbstractTeamForSeason;
import com.nectp.jpa.entities.Season;

@Named(value="statsBean")
@Dependent
public abstract class StatsBean<T> implements Serializable {
	private static final long serialVersionUID = 8806687509125861670L;

	protected T profileEntity;
	
	protected Season season;
	
	public void setProfileEntity(T profileEntity) {
		this.profileEntity = profileEntity;
		if (profileEntity instanceof AbstractTeamForSeason) {
			season = ((AbstractTeamForSeason) profileEntity).getSeason();
			
			calculateStats();
		}
	}
	
	public T getProfileEntity() {
		return profileEntity;
	}
	
	public Season getSeason() {
		return season;
	}
	
	protected abstract void calculateStats();
	
	protected RecordDisplay getRankedRecordDisplay(TreeMap<RecordAggregator, List<AbstractTeamForSeason>> rankMap, 
			T rankedEntity, boolean againstSpread) 
	{
		RecordDisplay rankedDisplay = null;
		for (RecordAggregator ragg : rankMap.keySet()) {
			if (rankMap.get(ragg).contains(rankedEntity)) {
				rankedDisplay = new RecordDisplay(ragg, againstSpread);
				rankedDisplay.setRank(rankMap);
				break;
			}
		}
		return rankedDisplay;
	}
}
