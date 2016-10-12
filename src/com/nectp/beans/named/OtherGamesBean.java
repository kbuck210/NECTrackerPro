package com.nectp.beans.named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.GameContainer;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Named(value="otherGamesBean")
@ViewScoped
public class OtherGamesBean implements Serializable, GameContainer {
	private static final long serialVersionUID = -3248718572270932215L;

	private List<GameBean> gameBeans;
	
	@EJB
	private RecordService recordService;
	
	@Inject
	private PaginationBean pageBean;
	
	@Inject
	private PlayerPicksBean picksBean;

	private Week displayWeek;
	
	private Logger log;
	
	public OtherGamesBean() {
		log = Logger.getLogger(OtherGamesBean.class.getName());
	}
	
	@PostConstruct
	public void init() {
		gameBeans = new ArrayList<GameBean>();
		displayWeek = pageBean.getDisplayWeek();
		
		if (displayWeek != null) {
			log.info("Got displayed week: " + displayWeek.getWeekNumber());
			List<GameBean> pickBeans = picksBean.getGameBeans();
			
			if (pickBeans.isEmpty()) {
				//	Create game beans for each game in the week
				for (Game g : displayWeek.getGames()) {
					gameBeans.add(createGameBean(g));
					log.info("Created game bean for gameId: " + g.getGameId());
				}
			}
			else {
				//	Create game beans for non-picked games
				for (Game g : displayWeek.getGames()) {
					for (GameBean gb : pickBeans) {
						if (!gb.getGame().equals(g)) {
							gameBeans.add(createGameBean(g));
						}
					}
				}
			}
		}
		else {
			log.warning("No display week specified!");
		}
	}
	
	private GameBean createGameBean(Game game) {
		GameBean bean = new GameBean();
		bean.setPlayer(null);
		NEC displayType = NEC.SEASON;
		bean.setGameDisplayType(displayType);
		bean.setGame(game);
		bean.setHomeSelectable(false);
		bean.setAwaySelectable(false);
		TeamForSeason homeTeam = game.getHomeTeam();
		RecordAggregator homeRagg = recordService.getOverallRecordThroughWeekForAtfs(homeTeam, displayWeek, displayType, false);
		bean.setHomeRecord(createRecordString(homeRagg));
		
		TeamForSeason awayTeam = game.getAwayTeam();
		RecordAggregator awayRagg = recordService.getOverallRecordThroughWeekForAtfs(awayTeam, displayWeek, displayType, false);
		bean.setAwayRecord(createRecordString(awayRagg));
		
		return bean;
	}
	
	private String createRecordString(RecordAggregator ragg) {
		String record;
		if (ragg != null) {
			record = "(" + ragg.getRawWins() + "-" + ragg.getRawLosses();
			if (ragg.getRawTies() > 0) {
				record += "-" + ragg.getRawTies();
			}
			record += ")";
		}
		else {
			record = "(N/A)";
		}
		
		return record;
	}

	@Override
	public List<GameBean> getGameBeans() {
		return gameBeans;
	}
	
}
