package com.nectp.beans.named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nectp.beans.ejb.ApplicationState;
import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.GameContainer;
import com.nectp.beans.remote.daos.PickService;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Named(value="playerPicksBean")
@RequestScoped
public class PlayerPicksBean implements Serializable, GameContainer {
	private static final long serialVersionUID = -1214887938429991546L;

	private List<GameBean> gameBeans;
	
	private Week displayWeek;
	
	@EJB
	private PickService pickService;
	
	@EJB
	private RecordService recordService;
	
	@Inject
	private PaginationBean pageBean;
	
	@Inject
	private ApplicationState appState;
	
	private PlayerForSeason user;
	
	public PlayerPicksBean() {
		gameBeans = new ArrayList<GameBean>();
	}

	@PostConstruct
	public void init() {
		user = appState.getUserInstance();
		displayWeek = pageBean.getDisplayWeek();
		
		if (user != null && displayWeek != null) {
			NEC picksFor = displayWeek.getSubseason().getSubseasonType();
			List<Pick> userPicks = pickService.selectPlayerPicksForWeekForType(user, displayWeek, picksFor);
			for (Pick p : userPicks) {
				GameBean bean = new GameBean();
				Game game = p.getGame();
				bean.setPlayer(user);
				NEC displayType = p.getApplicableRecord().getRecordType();
				boolean againstSpread = (displayType != NEC.TWO_AND_OUT && displayType != NEC.ONE_AND_OUT);
				bean.setGameDisplayType(displayType);
				bean.setGame(game);
				bean.setHomeSelectable(false);
				bean.setAwaySelectable(false);
				TeamForSeason homeTeam = game.getHomeTeam();
				RecordAggregator homeRagg = recordService.getAggregateRecordForAtfsForType(homeTeam, displayType, againstSpread);
				bean.setHomeRecord(createRecordString(homeRagg));
				
				TeamForSeason awayTeam = game.getAwayTeam();
				RecordAggregator awayRagg = recordService.getAggregateRecordForAtfsForType(awayTeam, displayType, againstSpread);
				bean.setAwayRecord(createRecordString(awayRagg));
				
				gameBeans.add(bean);
			}
		}
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
	
	public void setDisplayWeek(Week week) {
		displayWeek = week;
	}
 	
	@Override
	public List<GameBean> getGameBeans() {
		return gameBeans;
	}
	
}
