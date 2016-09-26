package com.nectp.beans.remote.daos;
import com.nectp.beans.remote.daos.DataService;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

public interface GameService extends DataService<Game> {

	public Game selectGameByTeamsWeek(TeamForSeason homeTeam, TeamForSeason awayTeam, Week week);
	
	public Game selectGameByTeamWeek(TeamForSeason team, Week week);
	
}
