package com.nectp.beans.named.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.event.FileUploadEvent;

import com.nectp.beans.remote.daos.PickFactory;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.SeasonService;
import com.nectp.beans.remote.daos.WeekService;
import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Game;
import com.nectp.jpa.entities.Pick.PickType;
import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

@Named(value="uploadExcelPicks")
@RequestScoped
public class UploadExcelPicks extends FileUploadImpl {
	private static final long serialVersionUID = -8901195250246314655L;

	private Logger log;
	
	@EJB
	private SeasonService seasonService;
	
	@EJB
	private WeekService weekService;
	
	@EJB
	private PlayerForSeasonService pfsService;
	
	@EJB
	private PickFactory pickFactory;
	
	private Workbook workbook;
	
	private Week week;
	
	private Map<PlayerForSeason, List<TeamForSeason>> playerPicks;
	
	private Map<PlayerForSeason, List<TeamForSeason>> mnfPicks;
	
	private Map<PlayerForSeason, List<TeamForSeason>> tntPicks;
	
	private List<Game> earlyGames;
 	
	public UploadExcelPicks() {
		log = Logger.getLogger(UploadExcelPicks.class.getName());
		playerPicks = new HashMap<PlayerForSeason, List<TeamForSeason>>();
		mnfPicks = new HashMap<PlayerForSeason, List<TeamForSeason>>();
		tntPicks = new HashMap<PlayerForSeason, List<TeamForSeason>>();
	}
	
	@Override
	public void upload(FileUploadEvent event) {
		file = event.getFile();
		if (file != null) {
			try {
				InputStream iStream = file.getInputstream();
				String filename = file.getFileName();
				boolean xls = filename.toLowerCase().endsWith(".xls");
				boolean xlsx = filename.toLowerCase().endsWith(".xlsx");
				
				Iterator<Row> rowIt = null;
				if (xls) {
					rowIt = processXlsFormat(iStream);
				}
				else if (xlsx) {
					rowIt = processXlsxFormat(iStream);
				}
				else {
					log.severe("Invalid file format! Can not process picks!");
				}
				
				if (rowIt != null) {
					boolean successfulRead = processPicks(rowIt);
					if (successfulRead) {
						submitPicks();
					}
				}
			} catch (IOException e) {
				log.severe("Exception retrieving input stream from uploaded file, can not update players: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/** If the uploaded file is of '.xls' format (earlier than 2007 Excel), get a row iterator for the HSSFWorkbook
	 * 
	 * @param iStream the InputStream for the uploaded file
	 * @return an Iterator to the rows in the HSSFWorkbook
	 */
	private Iterator<Row> processXlsFormat(InputStream iStream) {
		try { workbook = new HSSFWorkbook(iStream); }
		catch (IOException e) {
			log.severe("Failed to open HSSFWorkbook: " + e.getMessage());
			return null;
		}
		
		//	Get the first sheet
		HSSFSheet sheet = (HSSFSheet)workbook.getSheetAt(0);
		
		//	Get an iterator to all rows & cells in the sheet
		return sheet.iterator();
	}
	
	/** If the uploaded file is of '.xlsx' format (2007 Excel or later), get a row iterator for the XSSFWorkbook
	 * 
	 * @param iStream the InputStream for the uploaded file
	 * @return an Iterator to the rows in the XSSFWorkbook
	 */
	private Iterator<Row> processXlsxFormat(InputStream iStream) {
		//	If .xlsx, process as XSSF workbook
		try { workbook = new XSSFWorkbook(iStream); }
		catch (IOException e) {
			log.severe("Failed to open HSSFWorkbook: " + e.getMessage());
			return null;
		}

		//	Get the first sheet
		XSSFSheet sheet = (XSSFSheet)workbook.getSheetAt(0);

		//	Get an iterator to all rows & cells in the sheet
		return sheet.iterator();
	}
	
	private boolean processPicks(Iterator<Row> rowIt) {
		Season season = null;
		Week week = null;
		boolean successfulRead = false;
		if (rowIt != null) {
			Map<String, TeamForSeason> teamMap = new HashMap<String, TeamForSeason>();
			//	Iterate over the rows in the sheet
			while(rowIt.hasNext()) {
				//	Get the next row, and an iterator to its cells
				Row row = rowIt.next();
				Iterator<Cell> cellIt = row.cellIterator();
				
				//	Check if this is the first row (sheet title)
				if (row.getRowNum() == 0) {
					//	The first cell in the first row contains the title
					Cell titleCell = cellIt.next();
					season = getSeasonFromTitle(titleCell.getStringCellValue());
					if (season == null) {
						log.severe("Failed to retrieve applicable season from title, can not process workbook.");
						break;
					}
					
					week = getWeekFromTitle(titleCell.getStringCellValue(), season);
					if (week == null) {
						log.severe("Failed to retrieve applicable week from the title, can nor process workbook.");
						break;
					}
					//	Add each team from the season to the map of teams
					for (TeamForSeason tfs : season.getTeams()) {
						//	Check team city to conform to sheet style:
						String teamCity = tfs.getTeamCity().toUpperCase();
						if (teamCity.equals("New York")) {
							String teamAbbr = tfs.getTeamAbbr();
							if (teamAbbr.equals("NYJ")) teamCity = "NY JETS";
							else if (teamAbbr.equals("NYG")) teamCity = "NY GIANTS";
						}
						
						teamMap.put(teamCity, tfs);
					}
					earlyGames = week.getEarlyGames();
				}
				
				//	After row 2, process all remaining rows
				else if (row.getRowNum() > 1) {
					Cell rowTitleCell = cellIt.next();
					String rowTitle = rowTitleCell.getStringCellValue();
					//	Skip blank rows
					if (rowTitle == null || rowTitle.trim().isEmpty()) continue;
					else rowTitle = rowTitle.trim().toUpperCase();
					
					//	Check whether or not the team map contains the row title (regular pick)
					if (teamMap.containsKey(rowTitle)) {
						//	If team name found, mark each non-empty cell as a pick
						while(cellIt.hasNext()) {
							Cell pick = cellIt.next();
							String pickText = pick.getStringCellValue();
							//	Skip to next cell if cell is blank
							if (pickText == null || pickText.trim().isEmpty()) continue;
							else {
								//	If the cell is not blank, get the player from the column index, and the team from the city, and add pick to map
								PlayerForSeason player = null;
								try {
									player = pfsService.selectPlayerByExcelCol(pick.getColumnIndex(), season);
								} catch (NoResultException e) {
									log.severe("No player found for excel column! cannot create pick.");
									log.severe(e.getMessage());
									continue;
								}
								TeamForSeason tfs = teamMap.get(rowTitle);
								if (playerPicks.containsKey(player)) {
									playerPicks.get(player).add(tfs);
								}
								else {
									List<TeamForSeason> pickedTeams = new ArrayList<TeamForSeason>();
									pickedTeams.add(tfs);
									playerPicks.put(player, pickedTeams);
								}
//								NEC subseasonType = week.getSubseason().getSubseasonType();
//								pickFactory.createPlayerPickInWeek(player, tfs, week, subseasonType);
							}
						}
					}
					
					//	If row header is for Two and Out picks, parse teams selected into tnoPicks map (null value for eliminated players)
					else if ("2-AND-OUT".equals(rowTitle)) {
						//	If 2 and out found, mark each non-empty cell as a TNO pick
						while(cellIt.hasNext()) {
							Cell pick = cellIt.next();
							String pickText = pick.getStringCellValue();
							//	Skip to next cell if cell is blank, or is '-'
							if (pickText == null || pickText.trim().isEmpty() || pickText.trim().equals("-")) {
								continue;
							}
							else { 
								//	Get the TFS from the city name & the player from the excel column
								makePickForTeamName(pick, teamMap, pickText, season, week, NEC.TWO_AND_OUT, PickType.STRAIGHT_UP);
							}
						}
					}
					else if ("MNF'ER".equals(rowTitle)) {
						//	If MNF row found, mark each non-empty cell as a MNF pick
						PlayerForSeason player = null;
						TeamForSeason tfs = null;
						while(cellIt.hasNext()) {
							Cell pick = cellIt.next();
							String pickText = pick.getStringCellValue();
							
							//	Skip to next cell if cell is blank or is '-'
							if (pickText == null || pickText.trim().isEmpty() || pickText.trim().equals("-")) {
								continue;
							}
							else {
					    		try {
					    			player = pfsService.selectPlayerByExcelCol(pick.getColumnIndex(), season);
					    		} catch (NoResultException e) {
					    			log.severe("No player found for excel column! cannot create pick.");
					    			log.severe(e.getMessage());
					    			continue;
					    		}
					    		tfs = teamMap.get(pickText);
					    		if (tfs == null) {
					    			log.severe("Team not recognized for: " + pickText + " can not create MNF pick!");
					    			continue;
					    		}
					    		if (mnfPicks.containsKey(player)) {
					    			mnfPicks.get(player).add(tfs);
					    		}
					    		else {
					    			List<TeamForSeason> pickedTeams = new ArrayList<TeamForSeason>();
					    			pickedTeams.add(tfs);
					    			mnfPicks.put(player, pickedTeams);
					    		}
//								makePickForTeamName(pick, teamMap, pickText, season, week, NEC.MNF);
							}
						}
						
						//	Loop over any remaining MNF rows:
						while(rowIt.hasNext()) {
							//	Get the next row, and it's first non-header cell (column 2)
							Row nextRow = rowIt.next();
							Iterator<Cell> nextCellIt = nextRow.cellIterator();
							while(nextCellIt.hasNext()) {
								Cell nextCell = nextCellIt.next();
								String cellText = nextCell.getStringCellValue();
								if (cellText == null || cellText.trim().isEmpty()) {
									break;
								}
								else if (cellText.trim().equals("-")) {
									continue;
								}
								else {
									tfs = teamMap.get(cellText);
						    		if (tfs == null) {
						    			log.severe("Team not recognized for: " + cellText + " can not create MNF pick!");
						    			continue;
						    		}
									if (mnfPicks.containsKey(player)) {
						    			mnfPicks.get(player).add(tfs);
						    		}
						    		else {
						    			List<TeamForSeason> pickedTeams = new ArrayList<TeamForSeason>();
						    			pickedTeams.add(tfs);
						    			mnfPicks.put(player, pickedTeams);
						    		}
								}
							}
						}
					}
					else if ("TNT'ER".equals(rowTitle)) {
						//	If TNT row found, mark each non-empty cell as a TNT pick
						PlayerForSeason player = null;
						TeamForSeason tfs = null;
						while(cellIt.hasNext()) {
							Cell pick = cellIt.next();
							String pickText = pick.getStringCellValue();
							//	Skip to next cell if cell is blank or is '-'
							if (pickText == null || pickText.trim().isEmpty() || pickText.trim().equals("-")) {
								continue;
							}
							else {
								try {
					    			player = pfsService.selectPlayerByExcelCol(pick.getColumnIndex(), season);
					    		} catch (NoResultException e) {
					    			log.severe("No player found for excel column! cannot create pick.");
					    			log.severe(e.getMessage());
					    			continue;
					    		}
					    		tfs = teamMap.get(pickText);
					    		if (tfs == null) {
					    			log.severe("Team not recognized for: " + pickText + " can not create MNF pick!");
					    			continue;
					    		}
								if (tntPicks.containsKey(player)) {
									tntPicks.get(player).add(tfs);
								}
								else {
									List<TeamForSeason> pickedTeams = new ArrayList<TeamForSeason>();
									pickedTeams.add(tfs);
									tntPicks.put(player, pickedTeams);
								}
							}
						}
						
						//	Loop over each remaining TNT row:
						while(rowIt.hasNext()) {
							//	Get the next row, and it's first non-header cell (column 2)
							Row nextRow = rowIt.next();
							Iterator<Cell> nextCellIt = nextRow.cellIterator();
							while(nextCellIt.hasNext()) {
								Cell nextCell = nextCellIt.next();
								String cellText = nextCell.getStringCellValue();
								if (cellText == null || cellText.trim().isEmpty()) {
									break;
								}
								else if (cellText.trim().equals("-")) {
									continue;
								}
								else {
									tfs = teamMap.get(cellText);
						    		if (tfs == null) {
						    			log.severe("Team not recognized for: " + cellText + " can not create TNT pick!");
						    			continue;
						    		}
									if (tntPicks.containsKey(player)) {
										tntPicks.get(player).add(tfs);
									}
									else {
										List<TeamForSeason> pickedTeams = new ArrayList<TeamForSeason>();
										pickedTeams.add(tfs);
										tntPicks.put(player, pickedTeams);
									}
								}
							}
						}
					}
				}
			}
			successfulRead = true;
		}
		
		return successfulRead;
	}
	
	/** Based on the title string in the Excel file, parse the season number & retrieve the Season entity
     * 
     * @param titleString the Title string from the excel workbook
     * @return the Season entity corresponding to the season number in the title
     */
    private Season getSeasonFromTitle(String titleString) {
    	if (titleString == null) {
    		log.severe("Null title string, can not parse applicable season.");
    		return null;
    	}
    	String delimiter = "YEAR";
    	int offset = 1;
    	Integer seasonNumber = getIntegerFromTitle(titleString, delimiter, offset);
    	Season season = null;
    	try {
    		season = seasonService.selectById(seasonNumber);
    	} catch (NoResultException e) {
    		log.severe("No Season found for season number: " + seasonNumber.toString());
    		log.severe(e.getMessage());
    	}
    	return season;
    }
    
    /** Based on the title string in the Excel file, parse the week number & retrieve the Week entity
     * 
     * @param titleString the Title string from the Excel workbook
     * @return the Week entity corresponding to the week number in the title
     */
    private Week getWeekFromTitle(String titleString, Season season) {
    	if (titleString == null) {
    		log.severe("Null title string, can not parse applicable week.");
    		return null;
    	}
    	String delimiter = "WEEK";
    	int offset = 2;
    	Integer weekNumber = getIntegerFromTitle(titleString, delimiter, offset);
    	Week week = null;
    	try {
    		week = weekService.selectWeekByNumberInSeason(weekNumber, season);
    	} catch (NoResultException e) {
    		log.severe("No Week found for week " + weekNumber + " in NEC: " + season.getSeasonNumber());
    	}
    	
    	return week;
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
    
    private void submitPicks() {
    	//	Create the player season picks
    	NEC subType = week.getSubseason().getSubseasonType();
    	makePicks(playerPicks, subType);
    	
    	//	Create the player MNF Picks
    	makePicks(mnfPicks, NEC.MNF);
    	
    	//	Create the player TNT picks
    	makePicks(tntPicks, NEC.TNT);
    }
    
    private void makePicks(Map<PlayerForSeason, List<TeamForSeason>> pickMap, NEC pickFor) {
    	//	Create the player season picks
    	for (PlayerForSeason player : pickMap.keySet()) {
    		boolean useSpread2 = useSpread2(player);
    		
    		//	Set the appropriate pick type based on whether the player is playing any early games
    		PickType pickType;
    		if (useSpread2) pickType = PickType.SPREAD2;
    		else pickType = PickType.SPREAD1;
    		
    		List<TeamForSeason> pickedTeams = pickMap.get(player);
    		for (TeamForSeason tfs : pickedTeams) {
    			pickFactory.createPlayerPickInWeek(player, tfs, week, pickFor, pickType);
    		}
    	}
    }
    
    private boolean useSpread2(PlayerForSeason player) {
    	boolean useSpread2 = false;
    	List<TeamForSeason> pickedTeams = playerPicks.get(player);
		
		//	If there are games that may have a spread 2, check whether the player picked any
		for (Game g : earlyGames) {	//	NOTE: this is typically limited to 1-3 games, so not major impact on O()
			for (TeamForSeason tfs : pickedTeams) {
				//	If the picked team is in a game earlier than Sunday, and there exists a SPREAD2,
				//	use the SPREAD2 values for this players picks for the week
    			if ((g.getHomeTeam().equals(tfs) || g.getAwayTeam().equals(tfs)) 
    					&& g.getSpread2() != null) {
    				useSpread2 = true;
    				break;
    			}
    		}
		}
    	return useSpread2;
    }
    
    /** For MNF/TNT/TNO, get the team name from the pick text, and 
     * 
     * @param pick
     * @param teamMap
     * @param pickText
     * @param season
     * @param week
     * @param pickType
     */
    private void makePickForTeamName(Cell pick, Map<String, TeamForSeason> teamMap, String pickText, Season season, Week week, NEC pickFor, PickType pickType) {
    	//	Get the TFS from the city name & the player from the excel column
    	TeamForSeason tfs = teamMap.get(pickText);
    	if (tfs != null) {
    		PlayerForSeason player = null;
    		try {
    			player = pfsService.selectPlayerByExcelCol(pick.getColumnIndex(), season);
    		} catch (NoResultException e) {
    			log.severe("No player found for excel column! cannot create pick.");
    			log.severe(e.getMessage());
    		}

    		pickFactory.createPlayerPickInWeek(player, tfs, week, pickFor, pickType);
    	}
    	else {
    		log.severe("Could not find team: " + pickText + "! can not create pick.");
    	}
    }
}
