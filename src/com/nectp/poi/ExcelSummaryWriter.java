package com.nectp.poi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.ejb.daos.RecordAggregator;
import com.nectp.beans.remote.daos.GameService;
import com.nectp.beans.remote.daos.PickService;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.PrizeForSeasonService;
import com.nectp.beans.remote.daos.RecordService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.WeekService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.PrizeForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Pick.PickType;
import com.nectp.webtools.RomanNumeral;

public class ExcelSummaryWriter {
	
	private Workbook workbook;
	private Sheet totalsSheet;
	private Sheet tnoSheet;
	private Sheet mnfTntSheet;
	
	private CellStyle titleStyle;
	private CellStyle teamStyle;
	private CellStyle totalsStyle;
	private CellStyle headerStyle;
	private CellStyle playerStyle;
	private CellStyle winStyle;
	private CellStyle lossStyle;
	private CellStyle pushStyle;
	
	private Season season;
	private Week week;
	private NEC subseasonType;
	
	private Map<Integer, PlayerForSeason> playerColMap;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private WeekService weekService;
	
	@EJB
	private PlayerForSeasonService pfsService;
	
	@EJB
	private GameService gameService;
	
	@EJB
	private PickService pickService;
	
	@EJB
	private RecordService recordService;

	@EJB
	private PrizeForSeasonService pzfsService;
	
	private Logger log = Logger.getLogger(ExcelSummaryWriter.class.getName());
	
	@PostConstruct
	public void init() {
		//	Create a map to locally store the player/column mapping for faster lookups
		playerColMap = new HashMap<Integer, PlayerForSeason>();
		
		//	Get the season & week from the request parameters, if none defined, use current week of current season
		String seasonNum = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("nec");
		String weekNum = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("week");
		
		//	If the season number was defined, get the season
		if (seasonNum != null) {
			Integer nec = null;
			try {
				nec = Integer.parseInt(seasonNum);
			} catch (NumberFormatException e) {
				log.severe("Season number defined, but is of invalid format, can not get Season.");
				log.severe(e.getMessage());
				return;
			}
			
			season = seasonService.selectById(nec);
		}
		//	If the week number was defined & the season was defined, set the week & season
		if (weekNum != null && season != null) {
			Integer weekNumber = null;
			try {
				weekNumber = Integer.parseInt(weekNum);
			} catch (NumberFormatException e) {
				log.severe("Week number defined, but is of invalid format, can not get week in season.");
				log.severe(e.getMessage());
				return;
			}
			
			try {
				week = weekService.selectWeekByNumberInSeason(weekNumber, season);
			} catch (NoExistingEntityException e) {
				log.severe("No week found for week " + weekNum + " in season " + seasonNum + " can not write totals.");
				log.severe(e.getMessage());
				return;
			}
		}
		//	If neither the week or season defined, get the current values
		else if (weekNum == null && seasonNum == null) {
			log.warning("No season/week parameters defined, using the current values for each.");
			season = seasonService.selectCurrentSeason();
			if (season == null) {
				log.severe("No current season found! can not create totals.");
				return;
			}
			else {
				week = weekService.selectCurrentWeekInSeason(season);
				if (week == null) {
					log.severe("No current week found! can not create totals.");
					return;
				}
			}
		}
		
		//	If the season & week were defined, create the player/column mapping & the subseason type
		if (season != null && week != null) {
			subseasonType = week.getSubseason().getSubseasonType();
			
			for (PlayerForSeason player : season.getPlayers()) {
				playerColMap.put(player.getExcelColumn(), player);
			}
			//	Double check that each player had a unique column number, if not, reload with a default ordering
			if (playerColMap.keySet().size() != season.getPlayers().size()) {
				log.warning("Multiple players found with same column index: using default ordering.");
				playerColMap = new HashMap<Integer, PlayerForSeason>();
				int i = 1;
				for (PlayerForSeason player : season.getPlayers()) {
					playerColMap.put(i, player);
					i += 1;
				}
			}
		}
	}
	
	public boolean writeTotals() {
		String path = System.getProperty("user.home") + File.separator + "NECTrackerResources" + 
				 File.separator + "Excel" + File.separator + "Totals" + 
				 File.separator + "NEC" + season.getSeasonNumber() + File.separator;
		String filename = "NEC " + season.getSeasonNumber() + " - Week " + week.getWeekNumber() + " Totals.xls";
		File xlsFile = new File(path + filename);
		
		workbook = new HSSFWorkbook();
		
		//	Create workbook styles
		createWorkbookStyles();
		
		//	Construct and write the totals Sheet
		boolean totalsCreated = createTotalsSheet();
		
		//	Construct and write the Two and Out & MNF/TNT sheets if the totals sheet was constructed
		if (totalsCreated) {
			createTnoSheet();
			createMnfSheet();
			
			//	With the fully constructed sheet, write the output file
			try {
				OutputStream outStream = new FileOutputStream(xlsFile);
				BufferedOutputStream bos = new BufferedOutputStream(outStream);
				workbook.write(bos);
				bos.close();
				return true;
			} catch (IOException e) {
				log.severe("Failed writing workbook: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		}
		else {
			log.severe("Failed to create totals sheet. Aborting document write.");
			return false;
		}
	}
	
	/** Creates the specific styles used within this workbook
	 * 
	 */
	private void createWorkbookStyles() {
		//	Create the style used for the title
		titleStyle = createCellStyle(CellStyle.ALIGN_CENTER, HSSFCellStyle.BORDER_THIN, 
									HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, 
									HSSFCellStyle.BORDER_THIN, "Geneva", HSSFColor.BLACK.index, 
									Font.BOLDWEIGHT_BOLD, (short)9);
		//	Create the style used for Team name cells
		teamStyle = createCellStyle(CellStyle.ALIGN_LEFT, HSSFCellStyle.BORDER_NONE, 
									HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_THIN, 
									HSSFCellStyle.BORDER_NONE, "Geneva", HSSFColor.BLACK.index, 
									Font.BOLDWEIGHT_NORMAL, (short)9);
		//	Create the style used for the Player name cells
		playerStyle = createCellStyle(CellStyle.ALIGN_CENTER, HSSFCellStyle.BORDER_NONE, 
									HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE,
									HSSFCellStyle.BORDER_NONE, "Geneva", HSSFColor.BLACK.index, 
									Font.BOLDWEIGHT_BOLD, (short)8);
		//	Create the style used for the Totals header cells
		headerStyle = createCellStyle(CellStyle.ALIGN_CENTER, HSSFCellStyle.BORDER_NONE,
									HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_THIN,
									HSSFCellStyle.BORDER_NONE, "Geneva", HSSFColor.BLACK.index,
									Font.BOLDWEIGHT_BOLD, (short)9);
		//	Create the style used for the numerical totals for the various categories
		totalsStyle = createCellStyle(CellStyle.ALIGN_CENTER, HSSFCellStyle.BORDER_NONE,
									HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE,
									HSSFCellStyle.BORDER_NONE, "Geneva", HSSFColor.BLACK.index,
									Font.BOLDWEIGHT_NORMAL, (short)9);
		//	Create the styles used for the win, loss and tie colored fonts
		winStyle = createCellStyle(CellStyle.ALIGN_CENTER, HSSFCellStyle.BORDER_NONE, 
									HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, 
									HSSFCellStyle.BORDER_NONE, "Geneva", HSSFColor.GREEN.index,
									Font.BOLDWEIGHT_BOLD, (short)9);
		lossStyle = createCellStyle(CellStyle.ALIGN_CENTER, HSSFCellStyle.BORDER_NONE, 
									HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, 
									HSSFCellStyle.BORDER_NONE, "Geneva", HSSFColor.RED.index,
									Font.BOLDWEIGHT_BOLD, (short)9);
		pushStyle = createCellStyle(CellStyle.ALIGN_CENTER, HSSFCellStyle.BORDER_NONE, 
									HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, 
									HSSFCellStyle.BORDER_NONE, "Geneva", HSSFColor.LIGHT_BLUE.index,
									Font.BOLDWEIGHT_BOLD, (short)9);
	}
	
	/** Creates the Rows and Cells for the Totals workbook sheet
	 * 
	 * @return true if no serious errors occurred, false otherwise
	 */
	private boolean createTotalsSheet() {
		//	Create totals sheet:
		String totalsSheetTitle = "NEC" + season.getSeasonNumber() + "_Week" + week.getWeekNumber();
		totalsSheet = workbook.createSheet(totalsSheetTitle);
		
		//	Create title row
		String mainTitle = getMainTitle();
		String rulesTitle = getRulesTitle();
		String weekNum = week.getWeekNumber().toString();
		String totalsTitle = mainTitle + " - " + rulesTitle + " - WEEK # " + weekNum;
		Row titleRow = totalsSheet.createRow(0);
		Cell titleCell = titleRow.createCell(1);
		titleCell.setCellValue(totalsTitle);
		titleCell.setCellStyle(titleStyle);
		
		//	Create the players row
		Row playerRow = totalsSheet.createRow(1);
		int numPlayers = season.getPlayers().size();
		
		//	Merge the title cell across all of the player columns
		CellRangeAddress mergedTitle = new CellRangeAddress(0,0,1,numPlayers);
		totalsSheet.addMergedRegion(mergedTitle);
		
		//	Create the player cells
		for (int i = 1; i <= numPlayers; i++) {
			PlayerForSeason player = playerColMap.get(i);
			if (player == null) {
				log.severe("No player found with Excel column index: " + i + ", can not create sheet.");
				return false;
			}
			Cell playerCell = playerRow.createCell(i);
			playerCell.setCellValue(player.getExcelPrintName().toUpperCase());
			playerCell.setCellStyle(playerStyle);
		}
		
		//	Sort the teams by their Excel name, and process the list of teams
		List<TeamForSeason> teams = season.getTeams();
		Collections.sort(teams, new Comparator<TeamForSeason>() {
			@Override
			public int compare(TeamForSeason tfs1, TeamForSeason tfs2) {
				return tfs1.getExcelPrintName().compareTo(tfs2.getExcelPrintName());
			}
		});
		
		//	Create the team row & player picks for the game if they have any
		int curRowNum = playerRow.getRowNum() + 1;
		for (TeamForSeason team : teams) {
			Row teamRow = totalsSheet.createRow(curRowNum);
			Cell teamCell = teamRow.createCell(0);
			teamCell.setCellValue(team.getExcelPrintName().toUpperCase());
			teamCell.setCellStyle(teamStyle);
			
			//	For each player, determine wether they picked this team this week
			for (int i = 1; i <= numPlayers; i++) {
				PlayerForSeason player = playerColMap.get(i);
				Cell pickCell = teamRow.createCell(i);
				Game game = gameService.selectGameByTeamWeek(team, week);
				if (game != null) {
					Pick pick;
					try {
						pick = pickService.selectPlayerPickForGameForType(player, game, subseasonType);
					} catch (NoExistingEntityException e) {
						//	Player has no pick for this team, continue to next player
						continue;
					}
					
					//	Set the cell value for the picked team of 'X' (with 'TH' added if the game is a thursday night game)
					String cellVal = "X";
					if (game.getGameDate().get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.THURSDAY) {
						cellVal += " (TH)";
					}
					pickCell.setCellValue(cellVal);
					
					//	Get the winner based on the pick type, and the result
					Boolean wlt = null;
					TeamForSeason winner;
					switch(pick.getPickType()) {
					case STRAIGHT_UP:
						winner = game.getWinner();
						break;
					case SPREAD2:
						winner = game.getWinnerATS2();
						break;
					default:
						winner = game.getWinnerATS1();
					}
					//	If the winner is null, set wlt to tie condition (null), otherwise choose whether winner matches team
					wlt = winner != null ? winner.equals(team) : null;
					
					//	Based on wlt condition, set cell style
					if (wlt == null) pickCell.setCellStyle(pushStyle);
					else if (wlt) pickCell.setCellStyle(winStyle);
					else pickCell.setCellStyle(lossStyle);
				}
				else {
					log.warning(team.getTeamAbbr() + " has no game for Week " + week.getWeekNumber() + " - on bye week.");
				}
			}
			//	Increment the current row
			curRowNum += 1;
		}
		
		//	Create the wins row (after incrementing current row to get a row spacer)
		curRowNum += 1;
		Row winsRow = totalsSheet.createRow(curRowNum);
		Cell winsTitle = winsRow.createCell(0);
		winsTitle.setCellValue("WINS");
		winsTitle.setCellStyle(headerStyle);
		
		//	Create the loss row
		curRowNum += 1;
		Row lossRow = totalsSheet.createRow(curRowNum);
		Cell lossTitle = lossRow.createCell(0);
		lossTitle.setCellValue("LOSSES");
		lossTitle.setCellStyle(headerStyle);
		
		//	Create the ties row
		curRowNum += 1;
		Row tiesRow = totalsSheet.createRow(curRowNum);
		Cell tiesTitle = tiesRow.createCell(0);
		tiesTitle.setCellValue("TIES");
		tiesTitle.setCellStyle(headerStyle);
		
		//	Create the points for week row
		curRowNum += 2;
		Row weekPointsRow = totalsSheet.createRow(curRowNum);
		Cell weekPointsTitle = weekPointsRow.createCell(0);
		weekPointsTitle.setCellValue("Points For Week");
		weekPointsTitle.setCellStyle(playerStyle);
		
		//	Create the total points row
		curRowNum += 1;
		Row halfPointsRow = totalsSheet.createRow(curRowNum);
		Cell halfPointsTitle = halfPointsRow.createCell(0);
		String cellVal = "Tot. Pts. ";
		if (subseasonType.equals(NEC.FIRST_HALF)) {
			cellVal += "1st Half";
		}
		else if (subseasonType.equals(NEC.SECOND_HALF)) {
			cellVal += "2nd Half";
		}
		else if (subseasonType.equals(NEC.PLAYOFFS)) {
			cellVal += "Playoffs";
		}
		halfPointsTitle.setCellValue(cellVal);
		halfPointsTitle.setCellStyle(playerStyle);
		
		//	Create the AFC row
		curRowNum += 2;
		Row afcRow = totalsSheet.createRow(curRowNum);
		Cell afcTitle = afcRow.createCell(0);
		afcTitle.setCellValue("AFC");
		afcTitle.setCellStyle(headerStyle);
		
		//	Create the NFC row
		curRowNum += 1;
		Row nfcRow = totalsSheet.createRow(curRowNum);
		Cell nfcTitle = nfcRow.createCell(0);
		nfcTitle.setCellValue("NFC");
		nfcTitle.setCellStyle(headerStyle);
		
		//	Create the Favs row
		curRowNum += 2;
		Row favsRow = totalsSheet.createRow(curRowNum);
		Cell favsTitle = favsRow.createCell(0);
		favsTitle.setCellValue("FAVORITES");
		favsTitle.setCellStyle(headerStyle);
		
		//	Create the Dogs row
		curRowNum += 1;
		Row dogsRow = totalsSheet.createRow(curRowNum);
		Cell dogsTitle = dogsRow.createCell(0);
		dogsTitle.setCellValue("'DOGS");
		dogsTitle.setCellStyle(headerStyle);
		
		//	Create the Even row
		curRowNum += 1;
		Row evenRow = totalsSheet.createRow(curRowNum);
		Cell evenTitle = evenRow.createCell(0);
		evenTitle.setCellValue("EVEN");
		evenTitle.setCellStyle(headerStyle);
		
		//	Create the TnO row
		curRowNum += 2;
		Row tnoRow = totalsSheet.createRow(curRowNum);
		Cell tnoTitle = tnoRow.createCell(0);
		tnoTitle.setCellValue("2-And-Out");
		tnoTitle.setCellStyle(headerStyle);
		
		//	Create the base MNF row
		List<Game> mondayGames = week.getGamesInWeekForDay(GregorianCalendar.MONDAY);
		Row[] mnfRows = null;
		if (mondayGames.size() > 0) {
			curRowNum += 2;
			//	Create an array of rows to store each of the monday games in order
			mnfRows = new Row[mondayGames.size()];
			for (int i = 0; i < mnfRows.length; ++i) {
				Row mnfRow = totalsSheet.createRow(curRowNum + i);
				mnfRows[i] = mnfRow;
				//	For the first monday row, create the title header
				if (i == 0) {
					Cell mnfTitle = mnfRow.createCell(0);
					mnfTitle.setCellValue("MNF'er");
					mnfTitle.setCellStyle(headerStyle);
				}
			}
		}
	
		//	Create the TNT row (if TNT available)
		List<Game> thursdayGames = week.getGamesInWeekForDay(GregorianCalendar.THURSDAY);
		Row[] tntRows = null;
		if (thursdayGames.size() > 0) {
			curRowNum += (mondayGames.size() + 1);
			tntRows = new Row[thursdayGames.size()];
			for (int i = 0; i < tntRows.length; ++i) {
				Row tntRow = totalsSheet.createRow(curRowNum + i);
				tntRows[i] = tntRow;
				if (i == 0) {
					Cell tntTitle = tntRow.createCell(0);
					tntTitle.setCellValue("TNT'er");
					tntTitle.setCellStyle(headerStyle);
				}
			}
		}
		
		//	For each player, get their week & aggregate record, and fill in the stat rows
		for (int i = 1; i <= numPlayers; i++) {
			PlayerForSeason player = playerColMap.get(i);
			Record weekRecord;
			try {
				weekRecord = recordService.selectWeekRecordForAtfs(week, player, subseasonType);
			} catch (NoExistingEntityException e) {
				log.severe("No record found for " + player.getNickname() + " in week " + week.getWeekNumber() + " can not create sheet!");
				log.severe(e.getMessage());
				return false;
			}
			//	Get the aggregate record for this player through this week
			int firstWeekNum = week.getSubseason().getFirstWeek().getWeekNumber();
			RecordAggregator ragg = recordService.getRecordForConcurrentWeeksForAtfs(player, firstWeekNum, week.getWeekNumber(), subseasonType, true);
			
			//	Get the W/L/T values & calculate the weekly score
			int wins = weekRecord.getWinsATS1() + weekRecord.getWinsATS2();
			int totWins = ragg.getWinsATS1() + ragg.getWinsATS2();
			int losses = weekRecord.getLossesATS1() + weekRecord.getLossesATS2();
			int totLoss = ragg.getLossATS1() + ragg.getLossATS2();
			int ties = weekRecord.getTiesATS1() + weekRecord.getTiesATS2();
			int totTies = ragg.getTiesATS1() + ragg.getTiesATS2();
			int pointsForWeek = (wins * season.getWinValue()) + (ties * season.getTieValue()) - (losses * season.getLossValue());
			
			//	Populate the W/L/T & points cells
			Cell winsCell = winsRow.createCell(i);
			String winsVal = wins + " (" + totWins + ")";
			winsCell.setCellValue(winsVal);
			winsCell.setCellStyle(totalsStyle);
			
			Cell lossCell = lossRow.createCell(i);
			String lossVal = losses + " (" + totLoss + ")";
			lossCell.setCellValue(lossVal);
			lossCell.setCellStyle(totalsStyle);
			
			Cell tiesCell = tiesRow.createCell(i);
			String tiesVal = ties + " (" + totTies + ")";
			tiesCell.setCellValue(tiesVal);
			tiesCell.setCellStyle(totalsStyle);
			
			Cell weekPointsCell = weekPointsRow.createCell(i);
			weekPointsCell.setCellValue(Integer.toString(pointsForWeek));
			weekPointsCell.setCellStyle(totalsStyle);
			
			Cell halfPointsCell = halfPointsRow.createCell(i);
			halfPointsCell.setCellValue(ragg.getTotalScore().toString());
			halfPointsCell.setCellStyle(totalsStyle);
			
			//	Process Two and Out Cell
			Cell tnoCell = tnoRow.createCell(i);
			Record tnoRecord = null;
			try {
				tnoRecord = recordService.selectWeekRecordForAtfs(week, player, NEC.TWO_AND_OUT);
				Pick tnoPick = tnoRecord.getPicksInRecord().get(0);
				tnoCell.setCellValue(tnoPick.getPickedTeam().getExcelPrintName());
				if (tnoRecord.getWins() > 0) tnoCell.setCellStyle(winStyle);
				else if (tnoRecord.getLosses() > 0) tnoCell.setCellStyle(lossStyle);
				else tnoCell.setCellStyle(pushStyle);
			} catch (NoExistingEntityException e) {
				tnoCell.setCellValue("'-");
				tnoCell.setCellStyle(totalsStyle);
			}
			
			//	Process MNF rows
			if (mnfRows != null) {
				Record mnfRecord = null;
				try {
					mnfRecord = recordService.selectWeekRecordForAtfs(week, player, NEC.MNF);
					List<Pick> mnfPicks = mnfRecord.getPicksInRecord();
					Collections.sort(mnfPicks);
					//	Loop over the mnf rows, filling in the mnf picks
					for (int j = 0; j < mnfRows.length; ++j) {
						Cell mnfCell = mnfRows[j].createCell(i);
						if (j < mnfPicks.size()) {
							Pick mnfPick = mnfPicks.get(j);
							mnfCell.setCellValue(mnfPick.getPickedTeam().getExcelPrintName());
							TeamForSeason winner = null;
							if (mnfPick.getPickType().equals(PickType.SPREAD2)) {
								winner = mnfPick.getGame().getWinnerATS2();
							}
							else {
								winner = mnfPick.getGame().getWinnerATS1();
							}
							if (winner == null) {
								mnfCell.setCellStyle(pushStyle);
							}
							else if (winner.equals(mnfPick.getPickedTeam())) {
								mnfCell.setCellStyle(winStyle);
							}
							else {
								mnfCell.setCellStyle(lossStyle);
							}
						}
						else {
							mnfCell.setCellValue("-");
							mnfCell.setCellStyle(totalsStyle);
						}
					}
				} catch (NoExistingEntityException e) {
					log.warning("No MNF picks for " + player.getNickname() + " in week " + week.getWeekNumber() + "!");
					for (int j = 0; i < mnfRows.length; ++j) {
						Cell mnfCell = mnfRows[j].createCell(i);
						mnfCell.setCellStyle(totalsStyle);
						mnfCell.setCellValue("-");
					}
				}
			}
			
			//	Process TNT Rows
			if (tntRows != null) {
				Record tntRecord = null;
				try {
					tntRecord = recordService.selectWeekRecordForAtfs(week, player, NEC.TNT);
					List<Pick> tntPicks = tntRecord.getPicksInRecord();
					Collections.sort(tntPicks);
					//	Loop over the tnt rows, filling in the tnt picks
					for (int j = 0; j < tntRows.length; ++j) {
						Cell tntCell = tntRows[j].createCell(i);
						if (j < tntPicks.size()) {
							Pick tntPick = tntPicks.get(j);
							tntCell.setCellValue(tntPick.getPickedTeam().getExcelPrintName());
							TeamForSeason winner = null;
							if (tntPick.getPickType().equals(PickType.SPREAD2)) {
								winner = tntPick.getGame().getWinnerATS2();
							}
							else {
								winner = tntPick.getGame().getWinnerATS1();
							}
							if (winner == null) {
								tntCell.setCellStyle(pushStyle);
							}
							else if (winner.equals(tntPick.getPickedTeam())) {
								tntCell.setCellStyle(winStyle);
							}
							else {
								tntCell.setCellStyle(lossStyle);
							}
						}
						else {
							tntCell.setCellValue("-");
							tntCell.setCellStyle(totalsStyle);
						}
					}
				} catch (NoExistingEntityException e) {
					log.info("No TNT picks for " + player.getNickname() + " in week " + week.getWeekNumber());
					for (int j = 0; i < tntRows.length; ++j) {
						Cell tntCell = tntRows[j].createCell(i);
						tntCell.setCellStyle(totalsStyle);
						tntCell.setCellValue("-");
					}
				}
			}
		}
		
		totalsSheet.setZoom(130);
		totalsSheet.createFreezePane(1, 2);
        for (int i = 0; i <= numPlayers; ++i) {
        	totalsSheet.autoSizeColumn(i);
        }
		
		return true;
	}
	
	/** Creates the Two and Out sheet format, and populates its data
	 * 
	 */
	private void createTnoSheet() {
		//	Create two and out sheet:
		String tnoSheetTitle = "NEC " + season.getSeasonNumber() + " - Two-and-Out";
		tnoSheet = workbook.createSheet(tnoSheetTitle);

		//	Create title row
		String mainTitle = getMainTitle();
		String weekNum = week.getWeekNumber().toString();
		String tnoSubtitle = "\"TWO-AND-OUT\" - THROUGH WEEK # " + weekNum;
		
		Row titleRow = tnoSheet.createRow(0);
		Cell titleCell = titleRow.createCell(1);
		titleCell.setCellValue(mainTitle);
		titleCell.setCellStyle(titleStyle);
		
		Row subTitleRow = tnoSheet.createRow(2);
		Cell subTitleCell = subTitleRow.createCell(1);
		subTitleCell.setCellValue(tnoSubtitle);
		subTitleCell.setCellStyle(titleStyle);

		//	Create the players row
		Row playerRow = tnoSheet.createRow(4);
		int numPlayers = season.getPlayers().size();

		//	Merge the title cell across all of the player columns
		CellRangeAddress mergedTitle = new CellRangeAddress(0,0,1,numPlayers);
		tnoSheet.addMergedRegion(mergedTitle);
		
		CellRangeAddress mergedSubtitle = new CellRangeAddress(2,2,1,numPlayers);
		tnoSheet.addMergedRegion(mergedSubtitle);
		
		//	Create the player cells
		for (int i = 1; i <= numPlayers; i++) {
			PlayerForSeason player = playerColMap.get(i);
			Cell playerCell = playerRow.createCell(i);
			playerCell.setCellValue(player.getExcelPrintName().toUpperCase());
			playerCell.setCellStyle(playerStyle);
		}
		
		//	Sort the teams by their Excel name, and process the list of teams
		List<TeamForSeason> teams = season.getTeams();
		Collections.sort(teams, new Comparator<TeamForSeason>() {
			@Override
			public int compare(TeamForSeason tfs1, TeamForSeason tfs2) {
				return tfs1.getExcelPrintName().compareTo(tfs2.getExcelPrintName());
			}
		});
		
		//	Create the team row & player picks for the game if they have any
		int curRowNum = playerRow.getRowNum() + 1;
		for (TeamForSeason team : teams) {
			Row teamRow = tnoSheet.createRow(curRowNum);
			Cell teamCell = teamRow.createCell(0);
			teamCell.setCellValue(team.getExcelPrintName().toUpperCase());
			teamCell.setCellStyle(teamStyle);
			
			//	For each player, determine whether they picked this team this week
			for (int i = 1; i <= numPlayers; i++) {
				Cell pickCell = teamRow.createCell(i);
				PlayerForSeason player = playerColMap.get(i);
				//	Get the player picks & determine whether this team has been picked, and if so, get the result
				List<Pick> tnoPicks = pickService.selectPlayerPicksForType(player, NEC.TWO_AND_OUT);
				for (Pick pick : tnoPicks) {
					if (pick.getPickedTeam().equals(team)) {
						TeamForSeason winner = pick.getGame().getWinner();
						if (winner == null) {
							pickCell.setCellValue("P - Wk." + week.getWeekNumber());
							pickCell.setCellStyle(pushStyle);
						}
						else if (winner.equals(team)) {
							pickCell.setCellValue("W - Wk." + week.getWeekNumber());
							pickCell.setCellStyle(winStyle);
						}
						else {
							pickCell.setCellValue("L - Wk." + week.getWeekNumber());
							pickCell.setCellStyle(lossStyle);
						}
						break;
					}
				}
			}
			
			//	Increment the current row
			curRowNum += 1;
		}
		
		//	Create the totals Row
		curRowNum += 2;
		Row totalsRow = tnoSheet.createRow(curRowNum);
		Cell totalsTitle = totalsRow.createCell(0);
		totalsTitle.setCellValue("2-'n-OUT Totals");
		totalsTitle.setCellStyle(headerStyle);
		
		for (int i = 1; i <= numPlayers; i++) {
			Cell totalCell = totalsRow.createCell(i);
			PlayerForSeason player = playerColMap.get(i);
			RecordAggregator ragg = recordService.getAggregateRecordForAtfsForType(player, NEC.TWO_AND_OUT, false);
			totalCell.setCellValue("(" + ragg.getRawWins() + "-" + ragg.getRawLosses() + "-" + ragg.getRawTies() + ")");
			totalCell.setCellStyle(totalsStyle);
		}
		
		//	Create the congratulations row
		curRowNum += 3;
		Row congratsRow = tnoSheet.createRow(curRowNum);
		PrizeForSeason tno;
		String winner;
		try {
			tno = pzfsService.selectPrizeForSeason(NEC.TWO_AND_OUT, season);
			winner = tno.getWinner() != null ? tno.getWinner().getNickname() : "?????";
		} catch (NoExistingEntityException e) {
			log.warning("No prize created for Two and Out in season " + season.getSeasonNumber());
			log.warning("Can't display winner: " + e.getMessage());
			winner = "?????";
		}
		Cell congratsCell = congratsRow.createCell(1);
		congratsCell.setCellValue("Congratulations to " + winner + " for winning the NEC " + season.getSeasonNumber() + " Two-and-Out Pool!");
		congratsCell.setCellStyle(playerStyle);
		
		CellRangeAddress mergedCongrats = new CellRangeAddress(curRowNum, curRowNum, 1, numPlayers);
		tnoSheet.addMergedRegion(mergedCongrats);
		
		//	Create the quick glance title row
		curRowNum += 2;
		Row qgTitleRow = tnoSheet.createRow(curRowNum);
		Cell qgTitle = qgTitleRow.createCell(1);
		qgTitle.setCellValue("2'n-Out - Who You've Taken at a Quick Glance:");
		qgTitle.setCellStyle(titleStyle);
		
		CellRangeAddress mergedQgTitle = new CellRangeAddress(curRowNum, curRowNum, 1, numPlayers);
		tnoSheet.addMergedRegion(mergedQgTitle);
		
		//	Create the quick glance week rows
		curRowNum += 2;
		List<Week> weeks = weekService.selectConcurrentWeeksInRangeInSeason(season, 1, (season.getPlayoffStartWeek() - 1));
		for (Week w : weeks) {
			Row weekRow = tnoSheet.createRow(curRowNum);
			Cell weekTitle = weekRow.createCell(0);
			weekTitle.setCellValue("Week " + w.getWeekNumber());
			weekTitle.setCellStyle(teamStyle);
			
			for (int i = 1; i <= numPlayers; i++) {
				Cell pickCell = weekRow.createCell(i);
				PlayerForSeason player = playerColMap.get(i);
				List<Pick> playerTno = pickService.selectPlayerPicksForWeekForType(player, w, NEC.TWO_AND_OUT);
				if (!playerTno.isEmpty()) {
					Pick tnoPick = playerTno.get(0);
					pickCell.setCellValue(tnoPick.getPickedTeam().getExcelPrintName());
					TeamForSeason gameWinner = tnoPick.getGame().getWinner();
					if (gameWinner == null) {
						pickCell.setCellStyle(pushStyle);
					}
					else if (gameWinner.equals(tnoPick.getPickedTeam())) {
						pickCell.setCellStyle(winStyle);
					}
					else {
						pickCell.setCellStyle(lossStyle);
					}
				}
			}
			
			curRowNum += 1;
		}
		
		//	Create the legend rows
		curRowNum += 2;
		Row winLegendRow = tnoSheet.createRow(curRowNum);
		Cell winLegend = winLegendRow.createCell(0);
		winLegend.setCellValue("X = Win");
		winLegend.setCellStyle(winStyle);
		
		curRowNum += 1;
		Row lossLegendRow = tnoSheet.createRow(curRowNum);
		Cell lossLegend = lossLegendRow.createCell(0);
		lossLegend.setCellValue("X = Loss");
		lossLegend.setCellStyle(lossStyle);
		
		curRowNum += 1;
		Row pushLegendRow = tnoSheet.createRow(curRowNum);
		Cell pushLegend = pushLegendRow.createCell(0);
		pushLegend.setCellValue("X = Push");
		pushLegend.setCellStyle(pushStyle);
		
		tnoSheet.setZoom(130);
		tnoSheet.createFreezePane(1, 2);
        for (int i = 0; i <= numPlayers; ++i) {
        	tnoSheet.autoSizeColumn(i);
        }
	}
	
	private void createMnfSheet() {
		//	Create MNF/TNT sheet:
		String mnfSheetTitle = "NEC " + season.getSeasonNumber() + " - MNF/TNT";
		mnfTntSheet = workbook.createSheet(mnfSheetTitle);

		//	Create title row
		String mainTitle = getMainTitle();
		String weekNum = week.getWeekNumber().toString();
		String mnfSubtitle = "\"MNF/TNT'er\" (Monday Night Football w/ optional TNT picks) - Through Week # " + weekNum;

		Row titleRow = tnoSheet.createRow(0);
		Cell titleCell = titleRow.createCell(1);
		titleCell.setCellValue(mainTitle);
		titleCell.setCellStyle(titleStyle);

		Row subTitleRow = tnoSheet.createRow(2);
		Cell subTitleCell = subTitleRow.createCell(1);
		subTitleCell.setCellValue(mnfSubtitle);
		subTitleCell.setCellStyle(titleStyle);

		//	Create the players row
		Row playerRow = mnfTntSheet.createRow(4);
		int numPlayers = season.getPlayers().size();

		//	Merge the title cell across all of the player columns
		CellRangeAddress mergedTitle = new CellRangeAddress(0,0,1,numPlayers);
		mnfTntSheet.addMergedRegion(mergedTitle);

		CellRangeAddress mergedSubtitle = new CellRangeAddress(2,2,1,numPlayers);
		mnfTntSheet.addMergedRegion(mergedSubtitle);

		//	Create the player cells
		for (int i = 1; i <= numPlayers; i++) {
			PlayerForSeason player = playerColMap.get(i);
			Cell playerCell = playerRow.createCell(i);
			playerCell.setCellValue(player.getExcelPrintName().toUpperCase());
			playerCell.setCellStyle(playerStyle);
		}

		int curRowNum = playerRow.getRowNum() + 1;
		Row mnfTitleRow = mnfTntSheet.createRow(curRowNum);
		Cell mnfTitle = mnfTitleRow.createCell(0);
		mnfTitle.setCellValue("Monday Night");
		mnfTitle.setCellStyle(headerStyle);
		
		curRowNum += 1;
		Row mnfTitle2Row = mnfTntSheet.createRow(curRowNum);
		Cell mnfTitle2 = mnfTitle2Row.createCell(0);
		mnfTitle2.setCellValue("Football");
		mnfTitle2.setCellStyle(headerStyle);
		
		//	Create the MNF rows & player picks for the game if they have any
		curRowNum += 1;
		List<Week> regSeasonWeeks = weekService.selectConcurrentWeeksInRangeInSeason(season, 1, (season.getPlayoffStartWeek() - 1));
		for (Week w : regSeasonWeeks) {
			//	First create the rows & headers
			int numMnfGames = w.getGamesInWeekForDay(GregorianCalendar.MONDAY).size();
			Row[] mnfRows = new Row[numMnfGames];
			for (int i = 0; i < numMnfGames; ++i) {
				Row mnfWeekRow = mnfTntSheet.createRow(curRowNum + i);
				mnfRows[i] = mnfWeekRow;
				//	If the first of the MNF games, set the row header
				if (i == 0) {
					Cell mnfWeekTitle = mnfWeekRow.createCell(0);
					mnfWeekTitle.setCellValue("Week " + w.getWeekNumber());
					mnfWeekTitle.setCellStyle(totalsStyle);
				}
			}
			
			//	For each player, get their mnf picks & place in the rows
			for (int i = 1; i <= numPlayers; ++i) {
				//	Get their players & their picks, and sort by name
				PlayerForSeason player = playerColMap.get(i);
				List<Pick> mnfPicks = pickService.selectPlayerPicksForWeekForType(player, w, NEC.MNF);
				Collections.sort(mnfPicks);
				//	Loop over the MNF rows created for this week
				for (int j = 0; j < mnfRows.length; j++) {
					Row mnfRow = mnfRows[j];
					Cell mnfPickCell = mnfRow.createCell(i);
					//	If the player has an MNF pick for this row, get the picked team & fill the value
					if (mnfPicks.size() > j) {
						Pick mnfPick = mnfPicks.get(j);
						mnfPickCell.setCellValue(mnfPick.getPickedTeam().getExcelPrintName());
						//	Get the winner to determine cell style
						TeamForSeason winner = null;
						if (mnfPick.getPickType().equals(PickType.STRAIGHT_UP)) winner = mnfPick.getGame().getWinner();
						else if (mnfPick.getPickType().equals(PickType.SPREAD2)) winner = mnfPick.getGame().getWinnerATS2();
						else winner = mnfPick.getGame().getWinnerATS1();
						
						if (winner == null) mnfPickCell.setCellStyle(pushStyle);
						else if (winner.equals(mnfPick.getPickedTeam())) mnfPickCell.setCellStyle(winStyle);
						else mnfPickCell.setCellStyle(lossStyle);
					}
					//	If the player doesn't have an MNF pick for this row, mark as dash
					else {
						mnfPickCell.setCellValue("-");
						mnfPickCell.setCellStyle(totalsStyle);
					}
				}
			}
			curRowNum += numMnfGames;
		}
		
		//	Check whether TNT records are used for this season
		boolean useTnt = true;
		try {
			pzfsService.selectPrizeForSeason(NEC.MNF_TNT, season);
		}catch (NoExistingEntityException e) {
			useTnt = false;
		}
		
		if (useTnt) {
			curRowNum += 2;
			Row tntTileRow = mnfTntSheet.createRow(curRowNum);
			Cell tntTitle = tntTileRow.createCell(0);
			tntTitle.setCellValue("* Thurs. Night");
			tntTitle.setCellStyle(headerStyle);
			
			curRowNum += 1;
			Row tntTitleRow2 = mnfTntSheet.createRow(curRowNum);
			Cell tntTitle2 = tntTitleRow2.createCell(0);
			tntTitle2.setCellValue("Twist:");

			for (Week w : regSeasonWeeks) {
				//	First create the rows & headers
				int numTntGames = w.getGamesInWeekForDay(GregorianCalendar.THURSDAY).size();
				Row[] tntRows = new Row[numTntGames];
				for (int i = 0; i < numTntGames; ++i) {
					Row tntWeekRow = mnfTntSheet.createRow(curRowNum + i);
					tntRows[i] = tntWeekRow;
					//	If the first of the MNF games, set the row header
					if (i == 0) {
						Cell tntWeekTitle = tntWeekRow.createCell(0);
						tntWeekTitle.setCellValue("Week " + w.getWeekNumber());
						tntWeekTitle.setCellStyle(totalsStyle);
					}
				}

				//	For each player, get their mnf picks & place in the rows
				for (int i = 1; i <= numPlayers; ++i) {
					//	Get their players & their picks, and sort by name
					PlayerForSeason player = playerColMap.get(i);
					List<Pick> tntPicks = pickService.selectPlayerPicksForWeekForType(player, w, NEC.TNT);
					Collections.sort(tntPicks);
					//	Loop over the TNT rows created for this week
					for (int j = 0; j < tntRows.length; j++) {
						Row tntRow = tntRows[j];
						Cell tntPickCell = tntRow.createCell(i);
						//	If the player has an TNT pick for this row, get the picked team & fill the value
						if (tntPicks.size() > j) {
							Pick tntPick = tntPicks.get(j);
							tntPickCell.setCellValue(tntPick.getPickedTeam().getExcelPrintName());
							//	Get the winner to determine cell style
							TeamForSeason winner = null;
							if (tntPick.getPickType().equals(PickType.STRAIGHT_UP)) winner = tntPick.getGame().getWinner();
							else if (tntPick.getPickType().equals(PickType.SPREAD2)) winner = tntPick.getGame().getWinnerATS2();
							else winner = tntPick.getGame().getWinnerATS1();

							if (winner == null) tntPickCell.setCellStyle(pushStyle);
							else if (winner.equals(tntPick.getPickedTeam())) tntPickCell.setCellStyle(winStyle);
							else tntPickCell.setCellStyle(lossStyle);
						}
						//	If the player doesn't have an TNT pick for this row, mark as dash
						else {
							tntPickCell.setCellValue("-");
							tntPickCell.setCellStyle(totalsStyle);
						}
					}
				}
				curRowNum += numTntGames;
			}
		}
		
		curRowNum += 3;
		Row mnfTotalsRow = mnfTntSheet.createRow(curRowNum);
		Cell mnfTotalTitle = mnfTotalsRow.createCell(0);
		mnfTotalTitle.setCellValue("MNF Totals");
		mnfTotalTitle.setCellStyle(headerStyle);
		
		Row tntTotalsRow = null;
		Row totalsRow = null;
		if (useTnt) {
			curRowNum += 1;
			tntTotalsRow = mnfTntSheet.createRow(curRowNum);
			Cell tntTotalTitle = tntTotalsRow.createCell(0);
			tntTotalTitle.setCellValue("TNT Totals");
			tntTotalTitle.setCellStyle(headerStyle);
			
			curRowNum += 1;
			totalsRow = mnfTntSheet.createRow(curRowNum);
			Cell totalsTitle = totalsRow.createCell(0);
			totalsTitle.setCellValue("Totals");
			totalsTitle.setCellStyle(headerStyle);
		}
		
		for (int i = 1; i <= numPlayers; ++i) {
			PlayerForSeason player = playerColMap.get(i);
			Cell mnfTotalCell = mnfTotalsRow.createCell(i);
			RecordAggregator ragg = recordService.getAggregateRecordForAtfsForType(player, NEC.MNF, true);
			mnfTotalCell.setCellValue("(" + ragg.getTotalWinCount() + "-" + ragg.getTotalLossCount() + "-" + ragg.getTotalTieCount() + ")");
			mnfTotalCell.setCellStyle(totalsStyle);
			
			if (tntTotalsRow != null) {
				Cell tntTotalCell = tntTotalsRow.createCell(i);
				RecordAggregator tntRagg = recordService.getAggregateRecordForAtfsForType(player, NEC.TNT, true);
				tntTotalCell.setCellValue("(" + tntRagg.getTotalWinCount() + "-" + tntRagg.getTotalLossCount() + "-" + tntRagg.getTotalTieCount() + ")");
				tntTotalCell.setCellStyle(totalsStyle);
				
				Cell totalCell = totalsRow.createCell(i);
				RecordAggregator totRagg = RecordAggregator.combine(ragg, tntRagg);
				totalCell.setCellValue("(" + totRagg.getTotalWinCount() + "-" + totRagg.getTotalLossCount() + "-" + totRagg.getTotalTieCount() + ")");
				totalCell.setCellStyle(headerStyle);
			}
		}
		
		curRowNum += 2;
		Row congratsRow = mnfTntSheet.createRow(curRowNum);
		Cell congratsCell = congratsRow.createCell(1);
		String winner;
		try {
			PrizeForSeason mnfTnt;
			if (useTnt) {
				mnfTnt = pzfsService.selectPrizeForSeason(NEC.MNF_TNT, season);
			}
			else {
				mnfTnt = pzfsService.selectPrizeForSeason(NEC.MNF, season);
			}
			if (mnfTnt != null) {
				winner = mnfTnt.getWinner() != null ? mnfTnt.getWinner().getNickname() : "?????";
			}
			else winner = "?????";
		} catch (NoExistingEntityException e) {
			log.warning("No prize found for MNF/TNT for season: " + season.getSeasonNumber());
			winner = "?????";
		}
		String message = "Congratulations to " + winner + " for winning the NEC " + season.getSeasonNumber() + " MNF";
		if (useTnt) {
			message += "/TNT";
		}
		message += "er Pool!";
		congratsCell.setCellValue(message);
		congratsCell.setCellStyle(headerStyle);
		
		CellRangeAddress mergedCongrats = new CellRangeAddress(curRowNum, curRowNum, 1, numPlayers);
		mnfTntSheet.addMergedRegion(mergedCongrats);
		
		curRowNum += 1;
		Row winLegendRow = mnfTntSheet.createRow(curRowNum);
		Cell winLegend = winLegendRow.createCell(0);
		winLegend.setCellValue("X = Win");
		winLegend.setCellStyle(winStyle);
		
		curRowNum += 1;
		Row lossLegendRow = mnfTntSheet.createRow(curRowNum);
		Cell lossLegend = lossLegendRow.createCell(0);
		lossLegend.setCellValue("X = Loss");
		lossLegend.setCellStyle(lossStyle);
		
		curRowNum += 1;
		Row pushLegendRow = mnfTntSheet.createRow(curRowNum);
		Cell pushLegend = pushLegendRow.createCell(0);
		pushLegend.setCellValue("X = Push");
		pushLegend.setCellStyle(pushStyle);
		
		mnfTntSheet.setZoom(130);
		mnfTntSheet.createFreezePane(1, 2);
        for (int i = 0; i <= numPlayers; ++i) {
        	mnfTntSheet.autoSizeColumn(i);
        }
	}
		
	private String getMainTitle() {
		StringBuilder titleBuilder = new StringBuilder();
		//	Get string in format: NEC XXIX - 2015-16 SEASON - YEAR 29
		titleBuilder.append("NEC ");
		RomanNumeral numeral;
		try {
			numeral = new RomanNumeral(season.getSeasonNumber());
		} catch (NumberFormatException e) {
			log.severe("Invalid numeral format: " + e.getMessage() + " can not create title.");
			return null;
		}
		titleBuilder.append(numeral.toString());
		titleBuilder.append(" - ");
		Integer year;
		try {
			year = Integer.parseInt(season.getSeasonYear());
		} catch (NumberFormatException e) {
			log.severe("Invalid season year format: " + e.getMessage() + " can not create title.");
			return null;
		}
		Integer year2 = year + 1;
		String year2Str = year2.toString().substring(year2.toString().length() - 2);	//	Gets the last 2 chars of the year2 string
		titleBuilder.append(season.getSeasonYear() + "-" + year2Str);
		titleBuilder.append(" SEASON - YEAR ");
		titleBuilder.append(season.getSeasonNumber());
		
		return titleBuilder.toString();
	}

	private String getRulesTitle() {
		//	Determine the minimum & maximum pick values
		Integer minPicks = season.getMinPicks();
		Integer maxPicks = season.getMaxPicks();
		
		StringBuilder rulesBuilder = new StringBuilder();
		//	If minimum is null, must pick all games
		//	TODO: fix pick create logic to support this
		if (minPicks == null) {
			rulesBuilder.append("\"MAX THE MAX!\"");
			
		}
		//	If a minimum & maximum  are defined
		else if (maxPicks != null) {
			rulesBuilder.append("\"MIN " + minPicks.toString() + " - MAX " + maxPicks.toString() + "\"");
		}
		//	Otherwise, if min defined but not max
		else {
			rulesBuilder.append("\"MIN " + minPicks.toString() + " - MAX the MAX\"");
		}
		
		return rulesBuilder.toString();
	}
	
	private CellStyle createCellStyle(short alignment, short borderBottom, short borderLeft, short borderRight, short borderTop, 
									  String fontName, short color, short boldweight, short height) 
	{
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(alignment);
		style.setFont(createFont(fontName, color, boldweight, height));
		style.setBorderBottom(borderBottom);
		style.setBorderLeft(borderLeft);
		style.setBorderRight(borderRight);
		style.setBorderTop(borderTop);
		return style;
	}
	
	private Font createFont(String fontName, short color, short boldweight, short height) {
		Font font = workbook.createFont();
		font.setFontName(fontName);
		font.setColor(color);
		font.setBoldweight(boldweight);
		font.setFontHeightInPoints(height);
		return font;
	}
	
	public Season getSeason() {
		return season;
	}
	
	public void setSeason(Season season) {
		this.season = season;
	}
	
	public Week getWeek() {
		return week;
	}
	
	public void setWeek(Week week) {
		this.week = week;
	}
}
