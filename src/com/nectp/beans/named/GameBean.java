package com.nectp.beans.named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.Pick.PickType;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Game.GameStatus;

public class GameBean implements Serializable, Comparable<GameBean> {
	private static final long serialVersionUID = -8726829566294779519L;

	private Game game;
	
	private PlayerForSeason player;
	private TeamForSeason pickedTeam;
	
	private NEC gameDisplayType = NEC.SEASON;
	
	private String rowId;
	private String homeCity;
	private String homeRecord;
	private String homeScore;
	private String homeHelmet;
	private String homeBadgeId;
	private String homeHelmetId;
	private String homeSelected = "";
	private String awayCity;
	private String awayRecord;
	private String awayScore;
	private String awayHelmet;
	private String awayBadgeId;
	private String awayHelmetId;
	private String awaySelected = "";
	private String spread;
	private String gameTime;
	private String timeRemaining;
	private String homeArrow;
	private String awayArrow;
	private String homePickImage = "img/arrows/blank.png";
	private String awayPickImage = "img/arrows/blank.png";
	private String homeSelectable = "unselectable";
	private String awaySelectable = "unselectable";
	private String homeGrayed = "";
	private String awayGrayed = "";
	private String homeTeamUrl = "#";
	private String awayTeamUrl = "#";
	private String singlePick = "";
	
	private PickType spreadType;
	
	//	TODO: Finish  time remaining string getter
	public GameBean() {
	}
	
	public void setPlayer(PlayerForSeason player) {
		this.player = player;
	}
	
	public void setGameDisplayType(NEC gameDisplayType) {
		if (gameDisplayType != null) {
			this.gameDisplayType = gameDisplayType;
		}
		else {
			this.gameDisplayType = NEC.SEASON;
		}
	}
	
	/**
	 * @return the game
	 */
	public Game getGame() {
		return game;
	}
	/**
	 * @param game the game to set
	 */
	public void setGame(Game game) {
		this.game = game;
		TeamForSeason homeTeam = game.getHomeTeam();
		TeamForSeason awayTeam = game.getAwayTeam();
		checkPickedTeam(homeTeam, awayTeam);
		setHomeCity(homeTeam);
		setHomeScore();
		setHomeHelmet(homeTeam);
		setAwayCity(awayTeam);
		setAwayScore();
		setAwayHelmet(awayTeam);
		this.spreadType = setSpread();
		setGameTime();
		setTimeRemaining();
		setArrows();
		setTeamsUrls();
		long gameId = game.getGameId();
		this.rowId = "gameId_" + gameId;
		this.homeBadgeId = "home-badge-" + gameId;
		this.homeHelmetId = "home-helmet-" + gameId;
		this.awayBadgeId = "away-badge-" + gameId;
		this.awayHelmetId = "away-helmet-" + gameId;
		if (pickedTeam != null) {
			if (pickedTeam.equals(game.getHomeTeam())) {
				this.homeSelected = "selected";
			}
			else if (pickedTeam.equals(game.getAwayTeam())) {
				this.awaySelected = "selected";
			}
		}
	}
	
	public void setSinglePick(boolean singlePick) {
		if (singlePick) {
			this.singlePick = "singlePick";
		}
	}
	
	public String getSinglePick() {
		return singlePick;
	}
	
	public void setHomeSelectable(boolean selectable) {
		if (selectable) {
			this.homeSelectable = "selectable";
		}
	}
	
	public String getHomeSelectable() {
		return homeSelectable;
	}
	
	public void setAwaySelectable(boolean selectable) {
		if (selectable) {
			this.awaySelectable = "selectable";
		}
	}
	
	public String getAwaySelectable() {
		return awaySelectable;
	}
	
	public void setHomeGrayed(boolean grayed) {
		if (grayed) {
			this.homeGrayed = "grayed";
		}
	}
	
	public String getHomeGrayed() {
		return homeGrayed;
	}
	
	public void setAwayGrayed(boolean grayed) {
		if (grayed) {
			this.awayGrayed = "grayed";
		}
	}
	
	public String getAwayGrayed() {
		return awayGrayed;
	}
	
	/** 
	 * 
	 * @param homeTeam
	 * @param awayTeam
	 */
	public void checkPickedTeam(TeamForSeason homeTeam, TeamForSeason awayTeam) {
		if (player != null) {
			List<Pick> picksForGame = game.getPicks();
			for (Pick p : picksForGame) {
				if (p.getPlayer().equals(player)) {
					TeamForSeason pickedTeam = p.getPickedTeam();
					if (pickedTeam.equals(homeTeam)) {
						this.pickedTeam = pickedTeam;
						break;
					}
					else if (pickedTeam.equals(awayTeam)) {
						this.pickedTeam = awayTeam;

						break;
					}
				}
			}
		}
	}
	
	public String getRowId() {
		return rowId;
	}
	
	public String getHomeBadgeId() {
		return homeBadgeId;
	}
	
	public String getHomeHelmetId() {
		return homeHelmetId;
	}
	
	public String getHomeSelected() {
		return homeSelected;
	}
	
	public String getHomePickImage() {
		return homePickImage;
	}
	
	public String getAwayBadgeId() {
		return awayBadgeId;
	}
	
	public String getAwayHelmetId() {
		return awayHelmetId;
	}
	
	public String getAwaySelected() {
		return awaySelected;
	}
	
	public String getAwayPickImage() {
		return awayPickImage;
	}
	
	/**
	 * @return the homeCity
	 */
	public String getHomeCity() {
		return homeCity;
	}
	/**
	 * @param homeCity the homeCity to set
	 */
	private void setHomeCity(TeamForSeason homeTeam) {
		this.homeCity = homeTeam.getTeamCity();
	}
	/**
	 * @return the homeRecord
	 */
	public String getHomeRecord() {
		return homeRecord;
	}
	/**
	 * @param homeRecord the homeRecord to set
	 */
	public void setHomeRecord(String homeRecord) {
		this.homeRecord = homeRecord;
	}
	/**
	 * @return the homeScore
	 */
	public String getHomeScore() {
		return homeScore;
	}
	/**
	 * @param homeScore the homeScore to set
	 */
	private void setHomeScore() {
		Integer homeScore = game.getHomeScore();
		String homeScoreStr;
		if (homeScore < 10) {
			homeScoreStr = "0" + homeScore.toString();
		}
		else {
			homeScoreStr = homeScore.toString();
		}
		this.homeScore = homeScoreStr;
	}
	/**
	 * @return the homeHelmet
	 */
	public String getHomeHelmet() {
		return homeHelmet;
	}
	/**
	 * @param homeHelmet the homeHelmet to set
	 */
	private void setHomeHelmet(TeamForSeason homeTeam) {
		this.homeHelmet = homeTeam.getHomeHelmetUrl();
	}
	/**
	 * @return the awayCity
	 */
	public String getAwayCity() {
		return awayCity;
	}
	/**
	 * @param awayCity the awayCity to set
	 */
	private void setAwayCity(TeamForSeason awayTeam) {
		this.awayCity = awayTeam.getTeamCity();
	}
	/**
	 * @return the awayRecord
	 */
	public String getAwayRecord() {
		return awayRecord;
	}
	/**
	 * @param awayRecord the awayRecord to set
	 */
	public void setAwayRecord(String awayRecord) {
		this.awayRecord = awayRecord;
	}
	/**
	 * @return the awayScore
	 */
	public String getAwayScore() {
		return awayScore;
	}
	/**
	 * @param awayScore the awayScore to set
	 */
	private void setAwayScore() {
		Integer awayScore = game.getAwayScore();
		String awayScoreStr;
		if (awayScore < 10) {
			awayScoreStr = "0" + awayScore.toString();
		}
		else {
			awayScoreStr = awayScore.toString();
		}
		this.awayScore = awayScoreStr;
	}
	/**
	 * @return the awayHelmet
	 */
	public String getAwayHelmet() {
		return awayHelmet;
	}
	/**
	 * @param awayHelmet the awayHelmet to set
	 */
	private void setAwayHelmet(TeamForSeason awayTeam) {
		this.awayHelmet = awayTeam.getAwayHelmetUrl();
	}
	/**
	 * @return the spread
	 */
	public String getSpread() {
		return spread;
	}
	/**
	 * @param spread the spread to set
	 */
	private PickType setSpread() {
		PickType pickType = PickType.SPREAD1;
		
		//	If displaying a two and out game, use a straight-up spread
		if (NEC.TWO_AND_OUT.equals(gameDisplayType)) {
			pickType = PickType.STRAIGHT_UP;
		}
		
		//	If not straight up and a spread2 doesn't exist, or a player hasn't been defined, use spread1
		else if (game.getSpread2() == null || player == null) {
			pickType = PickType.SPREAD1;
		}
		//	If a spread2 exists and displaying a player's picks, check that the player 
		//	has this game as a pick, and whether it's a spread2 pick
		else {
			List<Pick> picksForGame = game.getPicks();
			for (Pick p : picksForGame) {
				if (p.getPlayer().equals(player) && p.getPickType().equals(PickType.SPREAD2)) {
					//	Double check whether the spread is even
					if (game.getSpread2().compareTo(BigDecimal.ZERO) == 0) {
						pickType = PickType.STRAIGHT_UP;
					}
					//	If the spread is not even, and using spread 2, set the pick type
					else {
						pickType = PickType.SPREAD2;
					}
					break;
				}
			}
		}
		
		//	Double check if using spread1, that the spread is not even
		if (PickType.SPREAD1.equals(pickType) && game.getSpread1().compareTo(BigDecimal.ZERO) == 0) {
			pickType = PickType.STRAIGHT_UP;
		}
		
		switch (pickType) {
		case SPREAD2:
			this.spread = game.getSpread2().toString();
			break;
		case STRAIGHT_UP:
			this.spread = "--";
			break;
		default:
			this.spread = game.getSpread1().toString();
		}
		
		return pickType;
	}
	
	/**
	 * @return the gameTime
	 */
	public String getGameTime() {
		return gameTime;
	}
	/**
	 * @param gameTime the gameTime to set
	 */
	private void setGameTime() {
		Calendar gameDate = game.getGameDate();
		SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
		this.gameTime = formatter.format(gameDate.getTime());
	}
	/**
	 * @return the timeRemaining
	 */
	public String getTimeRemaining() {
		return timeRemaining;
	}
	/**
	 * @param timeRemaining the timeRemaining to set
	 */
	private void setTimeRemaining() {
		GameStatus gamestatus = game.getGameStatus();
		if (GameStatus.PREGAME.equals(gamestatus)) {
			this.timeRemaining = "Pregame";
		}
		else if (GameStatus.HALFTIME.equals(game.getGameStatus())) {
			this.timeRemaining = "Half";
		}
		else if (GameStatus.FINAL.equals(game.getGameStatus())) {
			this.timeRemaining = "FINAL";
		}
		else {
			this.timeRemaining = game.getTimeRemaining();
		}
	}
	
	private void setArrows() {
		
		String homeAhead = "img/arrows/homeAhead-medDark.png";
		String homeGray = "img/arrows/home-medDark.png";
		String homeBehind = "img/arrows/homeBehind.png";
		String blank = "img/arrows/blank.png";
		String awayAhead = "img/arrows/awayAhead-medDark.png";
		String awayGray = "img/arrows/away-medDark.png";
		String awayBehind = "img/arrows/awayBehind.png";
		
		String check = "img/check-smaller.png";
		String warning = "img/warning.png";
		String out = "img/out-red-smaller.png";
		
		
		boolean isEven = spread.equals("--");
		
		//	Get the spread for the game as a double
		double gameSpread;
		if (isEven) {
			gameSpread = 0;
		}
		else {
			gameSpread = Double.parseDouble(spread);
		}
		
		//	Based on the spread type, get whether the home team is favorite, underdog, or even
		Boolean homeFavored = null;
		if (PickType.STRAIGHT_UP.equals(spreadType)) {
			homeFavored = null;
		}
		else if (PickType.SPREAD2.equals(spreadType)) {
			homeFavored = game.getHomeFavoredSpread2();
		}
		else {
			homeFavored = game.getHomeFavoredSpread1();
		}
		
		
		//	Get the home team's margin over the spread
		Double margin;
		int compare;
		Boolean homeCovering = null;
		//	If is the even case or the home team is favored, get whether the home team is covering
		if (homeFavored == null || homeFavored) {
			margin = new Double(game.getHomeScore() - game.getAwayScore());
			compare = margin.compareTo(gameSpread);
			//	If the home team margin is greater than the spread
			if (compare > 0) {
				homeCovering = true;
			}
			//	Check if home team margin is less than the spread
			else if (compare < 0) {
				homeCovering = false;
			}
		}
		//	If the away team is favored, deduce home covering from away covering
		else {
			margin = new Double(game.getAwayScore() - game.getHomeScore());
			compare = margin.compareTo(gameSpread);
			if (compare > 0) {
				homeCovering = false;
			}
			else if (compare < 0) {
				homeCovering = true;
			}
		}
		
		//	Display the arrow corresponding to home covering status
		//	- For Push Case, display gray arrow for both
		if (homeCovering == null) {
			this.homeArrow = homeGray;
			this.awayArrow = awayGray;
			//	Check if either team picked, display warning icon
			if (pickedTeam != null && pickedTeam.equals(game.getHomeTeam())) {
				this.homeSelected = "selected";
				this.homePickImage = warning;
			}
			else if (pickedTeam != null && pickedTeam.equals(game.getAwayTeam())) {
				this.awaySelected = "selected";
				this.awayPickImage = warning;
			}
		}
		//	- For home covering case, determine arrows to display
		else if (homeCovering) {
			//	If the picked team was the home team
			if (pickedTeam != null && pickedTeam.equals(game.getHomeTeam())) {
				this.homeArrow = homeAhead;
				this.homeSelected = "selected";
				this.homePickImage = check;
				//	Check whether even spread or the home team was favored
				if (isEven || (homeFavored != null && homeFavored)) {
					this.awayArrow = blank;
				}
				//	If the home team was the underdog, set away arrow gray
				else {
					this.awayArrow = awayGray;
				}
			}
			//	If no team picked
			else if (pickedTeam == null) {
				//	Check if even spread or the home team is the favorite
				if (isEven || (homeFavored != null && homeFavored)) {
					this.homeArrow = homeAhead;
					this.awayArrow = blank;
				}
				//	Check if the home team is the underdog
				else {
					this.homeArrow = blank;
					this.awayArrow = awayBehind;
				}
			}
			//	If the picked team was the away team
			else {
				this.awayArrow = awayBehind;
				this.awaySelected = "selected";
				//	Determine if the game is over to decide pick image
				if (GameStatus.FINAL.equals(game.getGameStatus())) {
					this.awayPickImage = out;
				}
				else {
					this.awayPickImage = warning;
				}
				//	Display gray arrow if home team is favored, blank otherwise
				if (homeFavored != null && homeFavored) {
					this.homeArrow = homeGray;
				}
				else {
					this.homeArrow = blank;
				}	
			}
		}
		//	- For home losing case, display red arrow if picked team
		else {
			//	If the picked team was the home team
			if (pickedTeam != null && pickedTeam.equals(game.getHomeTeam())) {
				this.homeArrow = homeBehind;
				this.homeSelected = "selected";
				//	Check whether the game is over to display icon
				if (GameStatus.FINAL.equals(game.getGameStatus())) {
					this.homePickImage = out;
				}
				else {
					this.homePickImage = warning;
				}
				//	Check whether even spread or the home team was favored
				if (isEven || (homeFavored != null && homeFavored)) {
					this.awayArrow = blank;
				}
				//	If the home team was the underdog, set away arrow gray
				else {
					this.awayArrow = awayGray;
				}
			}
			//	If no team picked
			else if (pickedTeam == null) {
				//	Check if it's an even spread
				if (isEven) {
					this.homeArrow = blank;
					this.awayArrow = awayAhead;
				}
				//	Check home team is the favorite
				else if (homeFavored != null && homeFavored) {
					this.homeArrow = homeBehind;
					this.awayArrow = blank;
				}
				//	Check if the home team is the underdog
				else {
					this.homeArrow = blank;
					this.awayArrow = awayAhead;
				}
			}
			//	If the picked team was the away team
			else {
				this.awayArrow = awayAhead;
				this.awaySelected = "selected";
				this.awayPickImage = check;
				//	Display gray arrow if home team is favored, blank otherwise
				if (homeFavored != null && homeFavored) {
					this.homeArrow = homeGray;
				}
				else {
					this.homeArrow = blank;
				}	
			}
		}
	}
	
	/**
	 * @return the homeArrow
	 */
	public String getHomeArrow() {
		return homeArrow;
	}
	
	/**
	 * @return the awayArrow
	 */
	public String getAwayArrow() {
		return awayArrow;
	}
	
	private void setTeamsUrls() {
		if (game != null) {
			TeamForSeason homeTeam = game.getHomeTeam();
			TeamForSeason awayTeam = game.getAwayTeam();
			
			String homeAbbr = homeTeam.getTeamAbbr();
			String awayAbbr = awayTeam.getTeamAbbr();
			
			this.homeTeamUrl = homeAbbr;
			this.awayTeamUrl = awayAbbr;
		}
	}
	
	public String getHomeTeamUrl() {
		return homeTeamUrl;
	}

	public String getAwayTeamUrl() {
		return awayTeamUrl;
	}

	@Override
	public int compareTo(GameBean gb2) {
		Game game1 = this.game;
		Game game2 = gb2.getGame();
		
		//	If this game is complete, but the other is not, put it first
		if (GameStatus.FINAL.equals(game1.getGameStatus()) && 
		   !GameStatus.FINAL.equals(game2.getGameStatus())) {
			return 1;
		}
		//	If the other game is complete, but this is not, put this first
		else if (GameStatus.FINAL.equals(game2.getGameStatus()) && 
				!GameStatus.FINAL.equals(game1.getGameStatus())) {
			return -1;
		}
		//	If neither game is complete, or if both are complete, sort by game time
		else {
			Calendar game1Start = game1.getGameDate();
			Calendar game2Start = game2.getGameDate();
			return game1Start.compareTo(game2Start);
		}
	}

}

