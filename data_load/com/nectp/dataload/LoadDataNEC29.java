package com.nectp.dataload;

import java.io.File;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.w3c.dom.Element;

import com.nectp.beans.remote.daos.AddressFactory;
import com.nectp.beans.remote.daos.ConferenceFactory;
import com.nectp.beans.remote.daos.DivisionFactory;
import com.nectp.beans.remote.daos.EmailFactory;
import com.nectp.beans.remote.daos.GameFactory;
import com.nectp.beans.remote.daos.PlayerFactory;
import com.nectp.beans.remote.daos.PlayerForSeasonFactory;
import com.nectp.beans.remote.daos.PrizeFactory;
import com.nectp.beans.remote.daos.PrizeForSeasonFactory;
import com.nectp.beans.remote.daos.RecordFactory;
import com.nectp.beans.remote.daos.SeasonFactory;
import com.nectp.beans.remote.daos.StadiumFactory;
import com.nectp.beans.remote.daos.SubseasonFactory;
import com.nectp.beans.remote.daos.TeamFactory;
import com.nectp.beans.remote.daos.TeamForSeasonFactory;
import com.nectp.beans.remote.daos.WeekFactory;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.constants.Timezone;
import com.nectp.jpa.entities.Address;
import com.nectp.jpa.entities.Conference;
import com.nectp.jpa.entities.Email;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Game.GameStatus;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Prize;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.Stadium;
import com.nectp.jpa.entities.Stadium.RoofType;
import com.nectp.jpa.entities.Week.WeekStatus;
import com.nectp.jpa.entities.Subseason;
import com.nectp.jpa.entities.Team;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Conference.ConferenceType;
import com.nectp.jpa.entities.Division;
import com.nectp.jpa.entities.Division.Region;
import com.nectp.webtools.DOMParser;

@Named(value="loadDataNEC29")
@RequestScoped
public class LoadDataNEC29 {
	
	@EJB
	private SeasonFactory seasonFactory;
	
	@EJB
	private SubseasonFactory subseasonFactory;
	
	@EJB
	private PrizeFactory prizeFactory;
	
	@EJB
	private PrizeForSeasonFactory pzfsFactory;
	
	@EJB
	private PlayerFactory playerFactory;
	
	@EJB
	private PlayerForSeasonFactory pfsFactory;
	
	@EJB
	private EmailFactory emailFactory;
	
	@EJB
	private ConferenceFactory conferenceFactory;
	
	@EJB
	private DivisionFactory divisionFactory;
	
	@EJB
	private TeamFactory teamFactory;
	
	@EJB
	private TeamForSeasonFactory tfsFactory;
	
	@EJB
	private StadiumFactory stadiumFactory;
	
	@EJB
	private AddressFactory addressFactory;
	
	@EJB
	private WeekFactory weekFactory;
	
	@EJB
	private GameFactory gameFactory;
	
	@EJB
	private RecordFactory recordFactory;
	
	public void createSeason29() {
		int seasonNumber = 29;
		String seasonYear = "2015";
		boolean currentSeason = true;
		int winValue = 1;
		int lossValue = 1;
		int tieValue = 0;
		int secHalfStart = 10;
		int playStart = 18;
		int sbweek = 22;
		int minPicks = 5;
		Integer maxPicks = null;
		int tnoLosses = 2;
		
		Season nec29 = seasonFactory.generateSeason(seasonNumber, seasonYear, currentSeason, winValue, 
				lossValue, tieValue, secHalfStart, playStart, sbweek, minPicks, maxPicks, tnoLosses);
	
		Subseason firstHalf = subseasonFactory.createSubseasonInSeason(NEC.FIRST_HALF, nec29);
		Subseason secondHalf = subseasonFactory.createSubseasonInSeason(NEC.SECOND_HALF, nec29);
		Subseason playoffs = subseasonFactory.createSubseasonInSeason(NEC.PLAYOFFS, nec29);
		Subseason superbowl = subseasonFactory.createSubseasonInSeason(NEC.SUPER_BOWL, nec29);
		
		Prize pFirstHalf = prizeFactory.createPrize(NEC.FIRST_HALF);
		Prize pSecondHalf = prizeFactory.createPrize(NEC.SECOND_HALF);
		Prize pPlayoffs = prizeFactory.createPrize(NEC.PLAYOFFS);
		Prize pSuperbowl = prizeFactory.createPrize(NEC.SUPER_BOWL);
		Prize pTntMnf = prizeFactory.createPrize(NEC.MNF_TNT);
		Prize pTwoAndOut = prizeFactory.createPrize(NEC.TWO_AND_OUT);
		Prize pMoneyBack = prizeFactory.createPrize(NEC.MONEY_BACK);
		
		PrizeForSeason pfsFirstHalf = pzfsFactory.createPrizeInSeason(NEC.FIRST_HALF, nec29, firstHalf, 175);
		PrizeForSeason pfsSecondHalf = pzfsFactory.createPrizeInSeason(NEC.SECOND_HALF, nec29, secondHalf, 175);
		PrizeForSeason pfsPlayoffs = pzfsFactory.createPrizeInSeason(NEC.PLAYOFFS, nec29, playoffs, 125);
		PrizeForSeason pfsSuperbowl = pzfsFactory.createPrizeInSeason(NEC.SUPER_BOWL, nec29, superbowl, 300);
		PrizeForSeason pfsMnfTnt = pzfsFactory.createPrizeInSeason(NEC.MNF_TNT, nec29, null, 125);
		PrizeForSeason pfsTwoNOut = pzfsFactory.createPrizeInSeason(NEC.TWO_AND_OUT, nec29, null, 100);
		PrizeForSeason pfsMoneyback = pzfsFactory.createPrizeInSeason(NEC.MONEY_BACK, nec29, null, 100);
		
		//	Read the players XML file to create players & pfs
		File playerFile = new File("C:/Users/kbuck_000/workspace/NECTrackerPro/XmlData/NEC29/Players/playersNEC29.xml");
		DOMParser parser = DOMParser.newInstance(playerFile, "pfs");
		LinkedList<Element> elements = parser.generateElementList();
        
        for (Element e : elements) {
        	String name = e.getAttribute("name");
        	String nickname = e.getAttribute("nn");
        	String excelName = e.getAttribute("epl");
        	String xlsCol = e.getAttribute("xlscol");
        	String emailAddress = e.getAttribute("email");
        	String avatar = e.getAttribute("avatar");
        	String since = e.getAttribute("since");
        	
        	Integer excelCol = Integer.parseInt(xlsCol);
        	Integer sinceYear = Integer.parseInt(since);
        	
        	Player player = playerFactory.createPlayer(name, sinceYear, avatar);
        	
        	PlayerForSeason pfs = pfsFactory.createPlayerForSeason(player, nec29, nickname, excelName, excelCol);
        	
        	Email email = emailFactory.createEmailForPlayer(player, emailAddress, true, true);
        }
        
        
        //	Read the stadium XML, creating addresses & stadiums for each
        File stadiumFile = new File("C:/Users/kbuck_000/workspace/NECTrackerPro/XmlData/NEC29/Stadiums/stadiums.xml");
        parser = DOMParser.newInstance(stadiumFile, "stadium");
        elements = parser.generateElementList();
       
        for (Element e : elements) {
        	String name = DOMParser.getTextSubElementByTagName(e, "name");
        	String street = DOMParser.getTextSubElementByTagName(e, "street");
        	String city = DOMParser.getTextSubElementByTagName(e, "city");
        	String state = DOMParser.getTextSubElementByTagName(e, "state");
        	String zip = DOMParser.getTextSubElementByTagName(e, "zip");
        	String country = DOMParser.getTextSubElementByTagName(e, "country");
        	String lat = DOMParser.getTextSubElementByTagName(e, "lat");
        	String lon = DOMParser.getTextSubElementByTagName(e, "long");
        	String cap = DOMParser.getTextSubElementByTagName(e, "capacity");
        	String roof = DOMParser.getTextSubElementByTagName(e, "roof");
        	String timezone = DOMParser.getTextSubElementByTagName(e, "timezone");
        	
        	long capacity = Long.parseLong(cap);
        	
        	BigDecimal latitude = new BigDecimal(lat);
        	BigDecimal longitude = new BigDecimal(lon);
        	
        	RoofType roofType = RoofType.getRoofTypeForString(roof);
        	Timezone tzone = Timezone.getTimezoneForString(timezone);
        	
        	Address address = addressFactory.createAddress(street, city, state, zip, longitude, latitude, country);
        	
        	boolean international = (country != null && !country.equals("US"));
        	
        	Stadium stadium = stadiumFactory.createStadium(name, address, capacity, international, roofType, tzone);
        }
        
        //	Read the teams XML file to create teams & tfs
        File teamFile = new File("C:/Users/kbuck_000/workspace/NECTrackerPro/XmlData/NEC29/Teams/teamsNEC29.xml");
        parser = DOMParser.newInstance(teamFile, "tfs");
        elements = parser.generateElementList();
        System.out.println(elements.size() + " TFS Nodes found");
        for (Element e : elements) {
        	String name = DOMParser.getTextSubElementByTagName(e, "name");
        	String abbr = DOMParser.getTextSubElementByTagName(e, "abbr");
        	String nickname = DOMParser.getTextSubElementByTagName(e, "nickname");
        	String city = DOMParser.getTextSubElementByTagName(e, "city");
        	String excelName = DOMParser.getTextSubElementByTagName(e, "excelName");
        	String conf = DOMParser.getTextSubElementByTagName(e, "conference");
        	String div = DOMParser.getTextSubElementByTagName(e, "division");
        	String stadiumName = DOMParser.getTextSubElementByTagName(e, "stadium");
        	String homeHelmet = DOMParser.getTextSubElementByTagName(e, "homeImg");
        	String awayHelmet = DOMParser.getTextSubElementByTagName(e, "awayImg");
        	
        	Conference conference = conferenceFactory.createConference(ConferenceType.getConferenceTypeForString(conf));
        	Division division = divisionFactory.createDivision(Region.getRegionForString(div), conference);
        	Stadium stadium = stadiumFactory.selectStadiumByName(stadiumName);
        	
        	Team team = teamFactory.createTeam(name, abbr, city);
        	TeamForSeason tfs = tfsFactory.createTeamForSeason(team, nec29, division, stadium, nickname, excelName, homeHelmet, awayHelmet);
        }
        
        //	Read the weeks XML files to create weeks & games (order the insert by week number)
        File weekDir = new File("C:/Users/kbuck_000/workspace/NECTrackerPro/XmlData/NEC29/Weeks");
        File[] xmls = weekDir.listFiles();
        File[] weeksXmls = new File[17];
        for (File xml : xmls) {
        	parser = DOMParser.newInstance(xml);
        	Element weekElement = parser.getRootElement();
        	String weekNum = weekElement.getAttribute("weekNum");
        	Integer weekNumber = Integer.parseInt(weekNum);
        	
        	weeksXmls[weekNumber - 1] = xml;
        }
        
        //	Loop over the ordered array & create weeks & games
        for (int i = 0; i < weeksXmls.length; ++i) {
        	File xmlFile = weeksXmls[i];
        	parser = DOMParser.newInstance(xmlFile, "game");
        	LinkedList<Element> games = parser.generateElementList();
        
        	//	Get or create the week in NEC29
        	Week week = weekFactory.createWeekInSeason((i+1), nec29, WeekStatus.COMPLETED, true);
        	
        	//	Loop over the game elements, getting the attributes & creating the games
        	for (Element e : games) {
        		String homeTeamAbbr = DOMParser.getTextSubElementByTagName(e, "homeTeam");
        		String homeScoreStr = DOMParser.getTextSubElementByTagName(e, "homeScore");
        		String homeFavored1Str = DOMParser.getTextSubElementByTagName(e, "homeFavored");
        		String spread1Str = DOMParser.getTextSubElementByTagName(e, "spread1");
        		String awayScoreStr = DOMParser.getTextSubElementByTagName(e, "awayScore");
        		String awayTeamAbbr = DOMParser.getTextSubElementByTagName(e, "awayTeam");
        		String gameDateStr = DOMParser.getTextSubElementByTagName(e, "date");
        		String gameTimeStr = DOMParser.getTextSubElementByTagName(e, "time");
        		String stadiumName = DOMParser.getTextSubElementByTagName(e, "stadium");
        		
        		TeamForSeason homeTeam = tfsFactory.selectTfsByAbbr(homeTeamAbbr, nec29);
        		TeamForSeason awayTeam = tfsFactory.selectTfsByAbbr(awayTeamAbbr, nec29);
        		
        		BigDecimal spread1 = new BigDecimal(spread1Str);
        		
        		Boolean homeFavoredSpread1 = null;
        		if (BigDecimal.ZERO.compareTo(spread1) != 0) {
        			homeFavoredSpread1 = homeFavored1Str.equals("Y");
        		}
        		
        		Integer homeScore = Integer.parseInt(homeScoreStr);
        		Integer awayScore = Integer.parseInt(awayScoreStr);
        		
        		String[] dateParts = gameDateStr.split("\\.");
        		Integer month = Integer.parseInt(dateParts[0]);
        		Integer day = Integer.parseInt(dateParts[1]);
        		Integer year = Integer.parseInt(dateParts[2]);
        		
        		String[] timeParts = gameTimeStr.split(":");
        		Integer hour = Integer.parseInt(timeParts[0]);
        		Integer mins = Integer.parseInt(timeParts[1]);
        		boolean pm = timeParts[2].toUpperCase().equals("PM");
        		if (pm) {
        			hour = hour + 12;
        		}
        		
        		Calendar gameDate = new GregorianCalendar();
        		gameDate.set(GregorianCalendar.MONTH, month);
        		gameDate.set(GregorianCalendar.DAY_OF_MONTH, day);
        		gameDate.set(GregorianCalendar.YEAR, year);
        		gameDate.set(GregorianCalendar.HOUR_OF_DAY, hour);
        		gameDate.set(GregorianCalendar.MINUTE, mins);
        		gameDate.set(GregorianCalendar.SECOND, 0);
        		
        		Stadium stadium = stadiumFactory.selectStadiumByName(stadiumName);
        		

        		Game game = gameFactory.createGameInWeek(week, homeTeam, awayTeam, homeScore, awayScore, 
        				spread1, null, gameDate, GameStatus.FINAL, homeFavoredSpread1, null, "FINAL", stadium);
        		
        		//	Uncomment the code below to create/update records for the new games - left commented to not multiply records
        		/*
        		Record homeRecord = recordFactory.createWeekRecordForAtfs(week, homeTeam, week.getSubseason().getSubseasonType());
        		Record awayRecord = recordFactory.createWeekRecordForAtfs(week, awayTeam, week.getSubseason().getSubseasonType());
        		if (game.getHomeScore() > game.getAwayScore()) {
        			homeRecord.addWin();
        			awayRecord.addLoss();
        		}
        		else if (game.getHomeScore() < game.getAwayScore()) {
        			homeRecord.addLoss();
        			awayRecord.addWin();
        		}
        		else {
        			homeRecord.addTie();
        			awayRecord.addTie();
        		}
        		
        		recordFactory.update(homeRecord);
        		recordFactory.update(awayRecord);
        		*/
        	}
        }
	}
}
