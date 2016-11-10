package com.nectp.beans.named.seasons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.nectp.beans.remote.daos.RecordService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.jpa.entities.Season;

@Named(value="necHistoryBean")
@ViewScoped
public class NecHistoryBean implements Serializable {
	private static final long serialVersionUID = 4381690058687809828L;

	@EJB
	private SeasonService seasonService;
	
	@EJB
	private RecordService recordService;
	
	private List<SeasonBean> seasons;
	
	@PostConstruct
	public void init() {
		this.seasons = new ArrayList<SeasonBean>();
		
		for (Season season : seasonService.findAll()) {
			SeasonBean sb = new SeasonBean();
			sb.setSeason(season, recordService);
			seasons.add(sb);
		}
	}
	
	public List<SeasonBean> getSeasons() {
		return seasons;
	}
}
