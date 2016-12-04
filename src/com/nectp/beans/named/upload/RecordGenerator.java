package com.nectp.beans.named.upload;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import com.nectp.beans.remote.daos.RecordFactory;
import com.nectp.beans.remote.daos.SeasonFactory;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.Week;

@Named(value="recordGenerator")
@RequestScoped
public class RecordGenerator {

	private Logger log;
	
	@EJB
	private SeasonFactory seasonFactory;
	
	@EJB
	private RecordFactory recordFactory;
	
	private Season season;
	
	public RecordGenerator() {
		log = Logger.getLogger(RecordGenerator.class.getName());
	}
	
	public void generateRecords() {
		log.info("action called.");
		season = seasonFactory.selectCurrentSeason();
		//	For each player, create records based on the prizes for the year
		for (PlayerForSeason player : season.getPlayers()) {
			//	Loop over each of the prizes for the season
			for (PrizeForSeason pzfs : season.getPrizes()) {
				//	Get the subseason for the prize, if correlates to a specific subseason, create records for those weeks
				Subseason subseason = pzfs.getSubseason();
				if (subseason != null) {
					for (Week week : subseason.getWeeks()) {
						recordFactory.createWeekRecordForAtfs(week, player, subseason.getSubseasonType());
					}
				}
				//	If not tied to a subseason, only other prize requiring record is two and out (other prizes are determined by aggregation of other records)
				else if (pzfs.getPrize().getPrizeType().equals(NEC.TWO_AND_OUT)){
					for (Subseason ss : season.getSubseasons()) {
						for (Week week : ss.getWeeks()) {
							recordFactory.createWeekRecordForAtfs(week, player, pzfs.getPrize().getPrizeType());
						}
					}
				}
			}
			
			//	Create other statistical records (i.e. MNF/TNT)
			for (Subseason subseason : season.getSubseasons()) {
				for (Week week : subseason.getWeeks()) {
					recordFactory.createWeekRecordForAtfs(week, player, NEC.MNF);
					recordFactory.createWeekRecordForAtfs(week, player, NEC.TNT);
				}
			}
		}
		FacesMessage recordsComplete = new FacesMessage(FacesMessage.SEVERITY_INFO, "Records Generated.", "");
		FacesContext.getCurrentInstance().addMessage(null, recordsComplete);
	}
}