package com.nectp.beans.named;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import com.nectp.beans.remote.daos.GameService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.beans.remote.daos.WeekService;
import com.nectp.jpa.entities.Address;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Week.WeekStatus;

@Named(value="nextGameBean")
@RequestScoped
public class NextGameBean implements Serializable {
	private static final long serialVersionUID = 1651701974905343726L;

	@EJB
	private WeekService weekService;

	@EJB
	private GameService gameService;

	@EJB
	private TeamForSeasonService teamService;

	@EJB
	private SeasonService seasonService;

	private Season currentSeason;
	private Game nextGame;

	private TeamForSeason displayTeam;
	private TeamForSeason opponent;

	private Logger log;

	public NextGameBean() {
		log = Logger.getLogger(NextGameBean.class.getName());
	}
	
	public void setDisplayTeam(TeamForSeason displayTeam) {
		this.displayTeam = displayTeam;
		if (displayTeam != null) {
			currentSeason = displayTeam.getSeason();
			Week currentWeek = currentSeason.getCurrentWeek();
			if (WeekStatus.COMPLETED.equals(currentWeek.getWeekStatus())) {
				//	Get all of the weeks in the season sorted by week number
				List<Week> weeksInSeason = weekService.listAllWeeksInSeason(currentSeason);
				Collections.sort(weeksInSeason, new Comparator<Week>() {
					@Override
					public int compare(Week w1, Week w2) {
						return w1.getWeekNumber().compareTo(w2.getWeekNumber());
					}
				});
				currentWeek = weeksInSeason.get(weeksInSeason.indexOf(currentWeek) + 1);
			}
			
			nextGame = gameService.selectGameByTeamWeek(displayTeam, currentWeek);
			if (nextGame != null) {
				opponent = nextGame.getOtherTeam(displayTeam);
			}
		}
	}

	public String getOpponentTitle() {
		String title = "N/a";
		if (nextGame != null && opponent != null) {
			title = "";
			if (nextGame.getHomeTeam().equals(opponent)) {
				title += "@ ";
			}
			title += opponent.getTeamCity();
		}
		
		return title;
	}

	public String getOpponentHelmet() {
		if (opponent != null) {
			return opponent.getAwayHelmetUrl();
		}
		else return "img/away-helmet.png";
	}
	
	public String getGameTime() {
		if (nextGame != null) {
			SimpleDateFormat format = new SimpleDateFormat("h:mm a");
			return format.format(nextGame.getGameDate().getTime());
		}
		else return "N/a";
	}

	public String getStadiumAddress() {
		if (nextGame != null) {
			Stadium stadium = nextGame.getStadium();
			Address address = stadium.getAddress();
			StringBuilder sb = new StringBuilder();
			sb.append(stadium.getStadiumName());
			sb.append("\n");
			sb.append(address.getStreet());
			sb.append("\n");
			sb.append(address.getCity() + ", ");
			if (!stadium.getInternational()) {
				sb.append(address.getState() + " ");
				sb.append(address.getZip());
			}
			else {
				sb.append(address.getCountry());
			}
			sb.append("\n");
			sb.append(address.getLatitude().toString() + ", ");
			sb.append(address.getLongitude().toString());
			
			return sb.toString();
		}
		else return "N/a";
	}
	
//	public String getStadiumName() {
//		if (nextGame != null) {
//			Stadium stadium = nextGame.getStadium();
//			return stadium.getStadiumName();
//		}
//		else return "N/a";
//	}
//	
//	public String getStadiumStreet() {
//		if (nextGame != null) {
//			Stadium stadium = nextGame.getStadium();
//			Address address = stadium.getAddress();
//			String street = address.getStreet();
//			return street != null ? street : "N/a";
//		}
//		else return "N/a";
//	}
	
	public String getStadiumCity() {
		if (nextGame != null) {
			Stadium stadium = nextGame.getStadium();
			Address address = stadium.getAddress();
			String city = address.getCity();
			return city != null ? city : "N/a";
		}
		else return "N/a";
	}
	
	public String getLatLon() {
		Address address = null;
		//	If a next game is defined, show the stadium of the next game
		if (nextGame != null) {
			address = nextGame.getStadium().getAddress();	
		}
		//	Otherwise show the home stadium of the display team
		else if (displayTeam != null) {
			address = displayTeam.getStadium().getAddress();
		}
		if (address != null) {
			String latLon = address.getLatitude() + ", " + address.getLongitude();
			return latLon;
		}
		else {
			return "41.368666, -71.836650";	// :-)
		}
	}
}