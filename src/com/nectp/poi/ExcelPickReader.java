package com.nectp.poi;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.nectp.beans.ejb.daos.NoExistingEntityException;
import com.nectp.beans.remote.daos.GameService;
import com.nectp.beans.remote.daos.PickFactory;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.RecordFactory;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.TeamForSeasonService;
import com.nectp.beans.remote.daos.WeekService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Record;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;
import com.nectp.jpa.entities.Pick.PickType;

@Stateless
public class ExcelPickReader {
	
	//	Player column map is redundant with PFS attribute for ensuring that columns weren't swapped for this particular sheet
	private Map<Integer, PlayerForSeason> playerColMap;
	
	//	Maps to store the read data prior to making the player picks
	private Map<PlayerForSeason, List<TeamForSeason>> playerPickMap;
	private Map<PlayerForSeason, TeamForSeason> tnoPickMap;
	private Map<PlayerForSeason, List<TeamForSeason>> mnfPickMap;
	private Map<PlayerForSeason, List<TeamForSeason>> tntPickMap;
	
	//	Row indices & current week
	private int maxTeamRow = -1;
	private int tnoRow = -1;
	private int mnfRow = -1;
	private int tntRow = -1;
	private Week currentWeek;
	private Season season;
	
	private Logger log = Logger.getLogger(ExcelPickReader.class.getName());
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private WeekService weekService;
	
	@EJB
	private PlayerForSeasonService pfsService;
	
	@EJB
	private TeamForSeasonService tfsService;
	
	@EJB
	private PickFactory pickFactory;
	
	@EJB
	private RecordFactory recordFactory;
	
	@EJB
	private GameService gameService;
	
	public ExcelPickReader() {
		this.log = Logger.getLogger(ExcelPickReader.class.getName());
	}
	
	public boolean processFile(String filename, InputStream excelStream) {
		if (excelStream != null) {
			this.currentWeek = null;
			this.season = null;
			
			//	Player column map is redundant with PFS attribute for ensuring that columns weren't swapped for this particular sheet
			playerColMap = new HashMap<Integer, PlayerForSeason>();

			//	Maps to store the read data prior to making the player picks
			playerPickMap = new HashMap<PlayerForSeason, List<TeamForSeason>>();
			tnoPickMap = new HashMap<PlayerForSeason, TeamForSeason>();
			mnfPickMap = new HashMap<PlayerForSeason, List<TeamForSeason>>();
			tntPickMap = new HashMap<PlayerForSeason, List<TeamForSeason>>();

			//	Row indicies
			maxTeamRow = -1;
			tnoRow = -1;
			mnfRow = -1;
			tntRow = -1;

			boolean xls = filename.toLowerCase().endsWith(".xls");
			boolean xlsx = filename.toLowerCase().endsWith(".xlsx");
			
			Workbook workbook = null;
			Sheet sheet = null;
			if (xls) {
				workbook = processXlsFormat(excelStream);
			}
			else if (xlsx) {
				workbook = processXlsxFormat(excelStream);
			}
			else {
				log.severe("Invalid file format! Can not process picks!");
			}
			
			if (workbook != null) {
				sheet = workbook.getSheetAt(0);
			}
			
			boolean sheetSuccess = false;
			if (sheet != null) {
				setSheetFormat(sheet);
				sheetSuccess = processSheet(sheet);
			}
			
			//	If the sheet was successfully processed, return the result of making picks, false otherwise
			boolean picksMade = false;
			if (sheetSuccess) {
				picksMade = makePicks();
				
				//	If the picks were made, update non-specific records (i.e. mnf/tnt, moneyback, 
			}
			
			//	After picks attempted to be made, close the workbook
			try {
				workbook.close();
			} catch (IOException e) {
				log.warning("Exception thrown trying to close the workbook: " + e.getMessage());
				log.warning("There may be a resource leak.");
				e.printStackTrace();
			}
			
			return picksMade;
		}
		else {
			log.severe("Null input file! can not process picks!");
			return false;
		}
	}
	
	private void setSheetFormat(Sheet sheet) {
		//	First iterate through the sheet to get the relative row information
		Iterator<Row> rowIt = sheet.iterator();
		while (rowIt.hasNext()) {
			Row row = rowIt.next();
			//	Skip the first 2 rows
			if (row.getRowNum() < 2) continue;
			else {
				Iterator<Cell> cellIt = row.cellIterator();
				Cell titleCell = cellIt.next();
				String title = titleCell.getStringCellValue();
				//	Get the first blank row
				if (maxTeamRow == -1) {
					if (title == null || title.trim().isEmpty()) {
						maxTeamRow = row.getRowNum() - 1;
					}
				}
				else {
					if (mnfRow == -1 && title != null && title.trim().toUpperCase().equals("MNF'ER")) {
						mnfRow = row.getRowNum();
					}
					if (tnoRow == -1 && title != null && title.trim().toUpperCase().equals("2-AND-OUT")) {
						tnoRow = row.getRowNum();
					}
					if (tntRow == -1 && title != null && title.trim().toUpperCase().equals("TNT'ER")) {
						tntRow = row.getRowNum();
					}
				}
			}
		}
	}
	
	private boolean processSheet(Sheet sheet) {
		Iterator<Row> rowIt = sheet.iterator();
		//	Iterate over the rows in the sheet
		while(rowIt.hasNext()) {
			//	Get the next row, and an iterator to its cells
			Row row = rowIt.next();
			Iterator<Cell> cellIt = row.cellIterator();

			int rowNum = row.getRowNum();
			//	Check if this is the first row (sheet title)
			if (rowNum == 0) {
				//	The first cell in the first row contains the title
				Cell titleCell = cellIt.next();
				String title = titleCell.getStringCellValue();
				Integer seasonNum = getSeasonFromTitle(title);
				Integer weekNum = getWeekFromTitle(title);
				
				season = seasonService.selectById(seasonNum);
				if (season != null) {
					try {
						currentWeek = weekService.selectWeekByNumberInSeason(weekNum, season);
					} catch (NoExistingEntityException e) {
						log.severe("Failed to retrieve week " + weekNum + " in season " + seasonNum + ", can not process picks.");
						log.severe(e.getMessage());
						return false;
					}
				}
			}
			//	Check if this is the second row (player row)
			else if (rowNum == 1) {
				while (cellIt.hasNext()) {
					Cell playerCell = cellIt.next();
					String playerName = playerCell.getStringCellValue();
					if (playerName == null || playerName.trim().isEmpty()) {
						continue;
					}
					else {
						PlayerForSeason player = null;
						//	Get the player for the specified nickname
						try {
							player = pfsService.selectPlayerByExcelName(playerName, season);
						} catch (NoExistingEntityException e) {
							log.severe("Failed to retrieve PFS for " + playerName + " - can not upload picks!");
							log.severe(e.getMessage());
							return false;
						}
						
						if (player != null) {
							playerColMap.put(playerCell.getColumnIndex(), player);
							playerPickMap.put(player, new ArrayList<TeamForSeason>());
							mnfPickMap.put(player, new ArrayList<TeamForSeason>());
							tntPickMap.put(player, new ArrayList<TeamForSeason>());
						}
						else{
							log.severe("Failed to retrieve PFS for " + playerName + " - can not upload picks!");
							return false;
						}
					}
				}
			}
			//	If greater than player row, but within team range, get picks
			else if (rowNum <= maxTeamRow) {
				Cell teamCell = cellIt.next();
				String teamCity = teamCell.getStringCellValue();
				TeamForSeason team = getTfsForCity(teamCity, season);
				
				if (team != null) {
					while (cellIt.hasNext()) {
						Cell pickCell = cellIt.next();
						if (pickCell.getCellType() == Cell.CELL_TYPE_STRING) {
							String pick = pickCell.getStringCellValue();
							if (pick != null && !pick.trim().isEmpty()) {
								Integer colNum = pickCell.getColumnIndex();
								PlayerForSeason player = playerColMap.get(colNum);
								if (playerPickMap.containsKey(player)) {
									playerPickMap.get(player).add(team);
								}
							}
						}
					} 
				}
				else {
					log.severe("Failed to retrieve TFS for " + teamCity + " - can not upload picks!");
					return false;
				}
			}
			//	If on the two and out row, get picks
			else if (rowNum == tnoRow) {
				cellIt.next();	//	pull the title off the stack
				while (cellIt.hasNext()) {
					Cell pickCell = cellIt.next();
					if (pickCell.getCellType() == Cell.CELL_TYPE_STRING) {
						String pick = pickCell.getStringCellValue();
						if (pick != null && !pick.trim().isEmpty() && !pick.trim().equals("-")) {
							Integer colNum = pickCell.getColumnIndex();
							PlayerForSeason player = playerColMap.get(colNum);
							TeamForSeason team = getTfsForCity(pick, season);
							if (player != null && team != null) {
								tnoPickMap.put(player, team);
							}
							else {
								log.warning("Failed to create Two and Out pick! Check teams/players & rerun.");
								return false;
							}
						}
					}
				}
			}
			//	If on the mnf rows, get picks
			else if (mnfRow > -1 && rowNum >= mnfRow && rowNum < (tntRow - 1)) {
				cellIt.next();
				while (cellIt.hasNext()) {
					Cell pickCell = cellIt.next();
					if (pickCell.getCellType() == Cell.CELL_TYPE_STRING) {
						String pick = pickCell.getStringCellValue();
						if (pick != null && !pick.trim().isEmpty() && !pick.trim().equals("-")) {
							Integer colNum = pickCell.getColumnIndex();
							PlayerForSeason player = playerColMap.get(colNum);
							TeamForSeason team = getTfsForCity(pick, season);
							if (mnfPickMap.containsKey(player) && team != null) {
								mnfPickMap.get(player).add(team);
							}
							else {
								log.warning("Failed to create MNF pick! Check teams/players & rerun.");
								return false;
							}
						}
					}
				}
			}
			//	If tnt row or greater, get picks
			else if (tntRow > -1 && rowNum >= tntRow) {
				cellIt.next();
				while (cellIt.hasNext()) {
					Cell pickCell = cellIt.next();
					if (pickCell.getCellType() == Cell.CELL_TYPE_STRING) {
						String pick = pickCell.getStringCellValue();
						if (pick != null && !pick.trim().isEmpty() && !pick.trim().equals("-")) {
							Integer colNum = pickCell.getColumnIndex();
							PlayerForSeason player = playerColMap.get(colNum);
							TeamForSeason team = getTfsForCity(pick, season);
							if (tntPickMap.containsKey(player) && team != null) {
								tntPickMap.get(player).add(team);
							}
							else {
								log.warning("Failed to create TNT pick! Check teams/players & rerun.");
								return false;
							}
						}
					}
				}
			}
		}
		
		return true;
	}
	
	private TeamForSeason getTfsForCity(String teamCity, Season season) {
		TeamForSeason team = null;
		//	Get the Team for this row (adjusting team city if is the giants or jets
		if (teamCity != null && teamCity.trim().toUpperCase().equals("NY GIANTS")) {
			try {
				team = tfsService.selectTfsByAbbrSeason("NYG", season);
			} catch (NoExistingEntityException e) {
				log.severe("Could not select the Giants for season: " + season.getSeasonNumber() + " - can not upload picks!");
				log.severe(e.getMessage());
			}
		}
		else if (teamCity != null && teamCity.trim().toUpperCase().equals("NY JETS")) {
			try {
				team = tfsService.selectTfsByAbbrSeason("NYJ", season);
			} catch (NoExistingEntityException e) {
				log.severe("Could not select the Jets for season: " + season.getSeasonNumber() + " - can not upload picks!");
				log.severe(e.getMessage());
			}
		}
		else if (teamCity != null) {
			try {
				team = tfsService.selectTfsByCitySeason(teamCity, season);
			} catch (NoExistingEntityException e) {
				log.severe("Could not select " + teamCity + " for season: " + season.getSeasonNumber() + " - can not upload picks!");
				log.severe(e.getMessage());
			}
		}
		return team;
	}
	
	/** If the uploaded file is of '.xls' format (earlier than 2007 Excel), get a row iterator for the HSSFWorkbook
	 * 
	 * @param iStream the InputStream for the uploaded file
	 * @return the HSSFWorkbook
	 */
	private Workbook processXlsFormat(InputStream iStream) {
		HSSFWorkbook workbook = null;
		try { workbook = new HSSFWorkbook(iStream); }
		catch (IOException e) {
			log.severe("Failed to open HSSFWorkbook: " + e.getMessage());
			return null;
		}
		
		//	Return the first sheet
		return workbook;
	}
	
	/** If the uploaded file is of '.xlsx' format (2007 Excel or later), get a row iterator for the XSSFWorkbook
	 * 
	 * @param iStream the InputStream for the uploaded file
	 * @return the XSSFWorkbook
	 */
	private Workbook processXlsxFormat(InputStream iStream) {
		//	If .xlsx, process as XSSF workbook
		XSSFWorkbook workbook = null;
		try { workbook = new XSSFWorkbook(iStream); }
		catch (IOException e) {
			log.severe("Failed to open HSSFWorkbook: " + e.getMessage());
			return null;
		}

		//	Return the first sheet
		return workbook;
	}
	
	/** Based on the title string in the Excel file, parse the season number & retrieve the Season entity
     * 
     * @param titleString the Title string from the excel workbook
     * @return the Season entity corresponding to the season number in the title
     */
    private Integer getSeasonFromTitle(String titleString) {
    	if (titleString == null) {
    		log.severe("Null title string, can not parse applicable season.");
    		return null;
    	}
    	String delimiter = "YEAR";
    	int offset = 1;
    	Integer seasonNumber = getIntegerFromTitle(titleString, delimiter, offset);
    	return seasonNumber;
    }
    
    /** Based on the title string in the Excel file, parse the week number & retrieve the Week entity
     * 
     * @param titleString the Title string from the Excel workbook
     * @return the Week entity corresponding to the week number in the title
     */
    private Integer getWeekFromTitle(String titleString) {
    	if (titleString == null) {
    		log.severe("Null title string, can not parse applicable week.");
    		return null;
    	}
    	String delimiter = "WEEK";
    	int offset = 2;
    	Integer weekNumber = getIntegerFromTitle(titleString, delimiter, offset);
    	return weekNumber;
    }
    
    /** Parses an integer from the title string based on the given delimiter & offset
     * 
     * @param titleString the title string to parse
     * @param delimiter the specified delimiter after which to find the integer
     * @param offset the number of array elements after the delimiter the attribute is found
     * @return the parsed integer, or null on exception or no attribute found
     */
    private Integer getIntegerFromTitle(String titleString, String delimiter, int offset) {
    	if (titleString == null) return null;
    	
    	//	Split the title string by whitespace to read each part for the delimiter designation
    	String[] titleParts = titleString.split(" ");
		for (int i = 0; i < titleParts.length; ++i) {
			//	If delimiter found (not as the last item in the array), parse the attribute
			if (titleParts[i].equals(delimiter) && i < (titleParts.length - offset)) {
				String attribute =  titleParts[i+offset];
				
				//	Check whether the attribute has a "-" in it (i.e. in case of 'WEEK 1-10')
				String[] dashSplit = attribute.split("-");
				if (dashSplit.length > 1) {
					attribute = dashSplit[1];
				}
				
				Integer intVal;
				try { intVal = Integer.parseInt(attribute); }
				catch (NumberFormatException e) {
					log.severe("Failed to parse attribute: " + delimiter + " for value: " + attribute);
					return null;
				}
				return intVal;
			}
		}
		//	If no attribute found, return null
		return null;
    }
    
	private boolean makePicks() {
		boolean success = true;
		
		//	First make the player picks
		for (PlayerForSeason player : playerPickMap.keySet()) {
			List<TeamForSeason> pickedTeams = playerPickMap.get(player);
			if (pickedTeams.isEmpty()) {
				log.warning("No teams picked for: " + player.getNickname() + "!");
				continue;
			}
			PickType pickType = useSpread2(player, pickedTeams) ? PickType.SPREAD2 : PickType.SPREAD1;
			NEC subseasonType = currentWeek.getSubseason().getSubseasonType();
			
			//	Create the player record if not already exists, otherwise retrieve existing
			Record subseasonRecord = recordFactory.createWeekRecordForAtfs(currentWeek, player, subseasonType);
			if (subseasonRecord == null) {
				log.severe("Failed to create/retrieve record for: " + player.getNickname() + " can't create picks!");
				continue;
			}
			//	Check if picks already exist for this record (i.e. a reupload of the pick sheet), if so, remove old picks
			else if (!subseasonRecord.getPicksInRecord().isEmpty()) {
				List<Pick> failedDeletes = pickFactory.removePicksForReplacement(subseasonRecord);
				if (!failedDeletes.isEmpty()) {
					log.severe("Failed to delete existing picks before replacement! Can not replace picks.");
					for (Pick p : failedDeletes) {
						log.severe("FAILED DELETE: PickID " + p.getPickId());
					}
					return false;
				}
			}
			
			for (TeamForSeason pick : pickedTeams) {
				Game game = null;
				try {
					game = gameService.selectGameByTeamWeek(pick, currentWeek);
				} catch (NoExistingEntityException e) {
					log.severe("Failed to get game for the picked team! can not create pick");
					log.severe(e.getMessage());
					continue;
				}
				//	Create pick for the subseason
				Pick p = pickFactory.createPlayerPickForRecord(subseasonRecord, game, pick, pickType);
				if (p == null) {
					success = false;
				}
				else {
					recordFactory.updateRecordForPlayerPick(p);
				}
			}
		}
		
		//	Next make the TNO picks
		for (PlayerForSeason player : tnoPickMap.keySet()) {
			TeamForSeason pickedTeam = tnoPickMap.get(player);
			if (pickedTeam == null) {
				log.warning("No Two and Out team picked for: " + player.getNickname() + "!");
				continue;
			}
			
			//	Create the player record if not already exists
			Record record = recordFactory.createWeekRecordForAtfs(currentWeek, player, NEC.TWO_AND_OUT);
			if (record == null) {
				log.severe("Failed to create/retrieve TNO record for: " + player.getNickname() + " can't create picks!");
				continue;
			}
			//	Check if picks already exist for this record (i.e. a reupload of the pick sheet), if so, remove old picks
			else if (!record.getPicksInRecord().isEmpty()) {
				List<Pick> failedDeletes = pickFactory.removePicksForReplacement(record);
				if (!failedDeletes.isEmpty()) {
					log.severe("Failed to delete existing picks before replacement! Can not replace picks.");
					for (Pick p : failedDeletes) {
						log.severe("FAILED DELETE: PickID " + p.getPickId());
					}
					return false;
				}
			}
			
			Game game = null;
			try {
				game = gameService.selectGameByTeamWeek(pickedTeam, currentWeek);
			} catch (NoExistingEntityException e) {
				log.severe("Failed to get game for the picked TNO team! can not create pick");
				log.severe(e.getMessage());
				continue;
			}
			Pick p = pickFactory.createPlayerPickForRecord(record, game, pickedTeam, PickType.STRAIGHT_UP);
//			Pick p = pickFactory.createPlayerPickInWeek(player, pickedTeam, currentWeek, NEC.TWO_AND_OUT, PickType.STRAIGHT_UP);
			if (p == null) {
				success = false;
			}
			else {
				recordFactory.updateRecordForPlayerPick(p);
			}
		}
		
		//	Next make the MNF picks
		for (PlayerForSeason player : mnfPickMap.keySet()) {
			List<TeamForSeason> pickedTeams = mnfPickMap.get(player);
			if (pickedTeams.isEmpty()) {
				log.warning("No MNF teams picked for: " + player.getNickname() + "!");
				continue;
			}
			PickType pickType = useSpread2(player, pickedTeams) ? PickType.SPREAD2 : PickType.SPREAD1;
		
			//	Create the record if it doesn't already exist
			Record record = recordFactory.createWeekRecordForAtfs(currentWeek, player, NEC.MNF);
			if (record == null) {
				log.severe("Failed to create/retrieve MNF record for: " + player.getNickname() + " can't create picks!");
				continue;
			}
			//	Check if picks already exist for this record (i.e. a reupload of the pick sheet), if so, remove old picks
			else if (!record.getPicksInRecord().isEmpty()) {
				List<Pick> failedDeletes = pickFactory.removePicksForReplacement(record);
				if (!failedDeletes.isEmpty()) {
					log.severe("Failed to delete existing picks before replacement! Can not replace picks.");
					for (Pick p : failedDeletes) {
						log.severe("FAILED DELETE: PickID " + p.getPickId());
					}
					return false;
				}
			}
			
			for (TeamForSeason pick : pickedTeams) {
				Game game = null;
				try {
					game = gameService.selectGameByTeamWeek(pick, currentWeek);
				} catch (NoExistingEntityException e) {
					log.severe("Failed to get game for the picked MNF team! can not create pick");
					log.severe(e.getMessage());
					continue;
				}
				Pick p = pickFactory.createPlayerPickForRecord(record, game, pick, pickType);
//				Pick p = pickFactory.createPlayerPickInWeek(player, pick, currentWeek, subseasonType, pickType);
				if (p == null) {
					success = false;
				}
				else {
					recordFactory.updateRecordForPlayerPick(p);
				}
			}
		}
		
		//	Next make the TNT picks
		for (PlayerForSeason player : tntPickMap.keySet()) {
			List<TeamForSeason> pickedTeams = tntPickMap.get(player);
			if (pickedTeams.isEmpty()) {
				log.warning("No TNT teams picked for: " + player.getNickname() + "!");
				continue;
			}
			PickType pickType = useSpread2(player, pickedTeams) ? PickType.SPREAD2 : PickType.SPREAD1;
			
			//	Create the record if it doesn't already exist
			Record record = recordFactory.createWeekRecordForAtfs(currentWeek, player, NEC.TNT);
			if (record == null) {
				log.severe("Failed to create/retrieve TNT record for: " + player.getNickname() + " can't create picks!");
				continue;
			}
			//	Check if picks already exist for this record (i.e. a reupload of the pick sheet), if so, remove old picks
			else if (!record.getPicksInRecord().isEmpty()) {
				List<Pick> failedDeletes = pickFactory.removePicksForReplacement(record);
				if (!failedDeletes.isEmpty()) {
					log.severe("Failed to delete existing picks before replacement! Can not replace picks.");
					for (Pick p : failedDeletes) {
						log.severe("FAILED DELETE: PickID " + p.getPickId());
					}
					return false;
				}
			}
			
			for (TeamForSeason pick : pickedTeams) {
				Game game = null;
				try {
					game = gameService.selectGameByTeamWeek(pick, currentWeek);
				} catch (NoExistingEntityException e) {
					log.severe("Failed to get game for the picked TNT team! can not create pick");
					log.severe(e.getMessage());
					continue;
				}
				Pick p = pickFactory.createPlayerPickForRecord(record, game, pick, pickType);
//				Pick p = pickFactory.createPlayerPickInWeek(player, pick, currentWeek, subseasonType, pickType);
				if (p == null) {
					success = false;
				}
				else {
					recordFactory.updateRecordForPlayerPick(p);
				}
			}
		}
		return success;
	}
	
	private boolean useSpread2(PlayerForSeason player, List<TeamForSeason> pickedTeams) {    	
		//	If there are games that may have a spread 2, check whether the player picked any
		for (Game g : currentWeek.getEarlyGames()) {	//	NOTE: this is typically limited to 1-3 games, so not major impact on O()
			for (TeamForSeason tfs : pickedTeams) {
				//	If the picked team is in a game earlier than Sunday, and there exists a SPREAD2,
				//	use the SPREAD2 values for this players picks for the week
    			if ((g.getHomeTeam().equals(tfs) || g.getAwayTeam().equals(tfs)) 
    					&& g.getSpread2() != null) {
    				return true;
    			}
    		}
		}
    	return false;
    }
}
