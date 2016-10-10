package com.nectp.poi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.nectp.jpa.entities.PlayerForSeason;
import com.nectp.jpa.entities.Season;
import com.nectp.jpa.entities.TeamForSeason;
import com.nectp.jpa.entities.Week;

public class ExcelReader {

	private Logger log;
	
	private File xlsFile;
	
	//	Create a map to store players relative to their column index
	private Map<Integer, PlayerForSeason> playerMap;
	
	//  Create a map of the team cities to compare against row titles
    private Map<String, TeamForSeason> teamCities;
    
    //	Create a map of players to their teams picked
    private Map<PlayerForSeason, Set<TeamForSeason>> playerPicks;

    //	Create a map of players to their Two and Out selections
    private Map<PlayerForSeason, Set<TeamForSeason>> tnoPicks;

    //	Create a map of players to their MNF teams picked
    private Map<PlayerForSeason, Set<TeamForSeason>> mnfPicks;

    //	Create a map of players to their TNT teams picked
    private Map<PlayerForSeason, Set<TeamForSeason>> tntPicks;
	
	public ExcelReader() {
		log = Logger.getLogger(ExcelReader.class.getName());
		
	    playerMap = new HashMap<Integer, PlayerForSeason>();
	    teamCities = new HashMap<String, TeamForSeason>();
	    playerPicks = new HashMap<PlayerForSeason, Set<TeamForSeason>>();
	    tnoPicks = new HashMap<PlayerForSeason, Set<TeamForSeason>>();
	    mnfPicks = new HashMap<PlayerForSeason, Set<TeamForSeason>>();
	    tntPicks = new HashMap<PlayerForSeason, Set<TeamForSeason>>();
	}
	
	public File getFile() {
		return xlsFile;
	}
	
	public void setFile(File file) {
		this.xlsFile = file;
		
	}
	
	public void readFile() {
		 //	Check whether the file type is older .xls, or newer .xlsx (& validate extension)
	    boolean xls = xlsFile.getAbsolutePath().endsWith(".xls");
	    if (!xls) {
	        //  TODO: escape & error
	    }
		
		//	Get the excel file as a stream
        FileInputStream fis = new FileInputStream(xlsFile);
        BufferedInputStream bos = new BufferedInputStream(fis);

        //	If .xls, process as HSSF workbook
        HSSFWorkbook workbook = new HSSFWorkbook(bos);

        //	Get the first sheet
        HSSFSheet sheet = workbook.getSheetAt(0);
        
        Season season;
        Week week;

        int titleRowNum = 0;

        //	Get the title row for season & week information
        Row titleRow = sheet.getRow(titleRowNum);
        if (titleRow == null) {
            log.severe("No Title row defined for row: " + titleRowNum + " aborting.");
            return;
        }
        Cell titleCell = titleRow.getCell(0);
        season = getSeasonFromTitle(titleCell.getStringCellValue());
        if (season == null) {
        	log.severe("Failed to retrieve applicable season from title, can not process workbook.");
            return;
        }
        week = getWeekFromTitle(titleCell.getStringCellValue(), season);
        if (week == null) {
        	log.severe("Failed to retrieve applicable week from the title, can nor process workbook.");
            return;
        }
        
        int playerRowNum = 1;
        int teamStartRow = 2;

        //  Add each team from the season to the map of teams
        for (TeamForSeason tfs : season.getTeams()) {
            //  Check team city to conform to sheet style:
            String teamCity = tfs.getTeamCity().toUpperCase();
            if (teamCity.equals("NEW YORK")) {
                String teamAbbr = tfs.getTeamAbbr();
                if (teamAbbr.equals("NYJ")) {
                    teamCity = "NY JETS";
                } else if (teamAbbr.equals("NYG")) {
                    teamCity = "NY GIANTS";
                }
            }

            teamCities.put(teamCity, tfs);
        }

        //  Add each player from the season to the map of players
        for (PlayerForSeason pfs : season.getPlayers()) {
            playerMap.put(pfs.getExcelColumn(), pfs);
        }
        
        //	Get the playerRow
        Row playerRow = sheet.getRow(playerRowNum);
        if (playerRow == null) {
        	log.severe("No Player row defined for row: " + playerRowNum + " aborting.");
            return;
        }
        
        int tnoRowNum = teamStartRow + season.getTeams().size() + 18;
        int mnfRowStart = tnoRowNum + 2;
        int numMnfGames = week.getGamesByDay(GregorianCalendar.MONDAY).size();
        int tntRowStart;
        if (numMnfGames > 2) tntRowStart = mnfRowStart + numMnfGames + 1;
        else tntRowStart = mnfRowStart + 3;
        int numTntGames = week.getGamesByDay(GregorianCalendar.THURSDAY).size();

        //  Get the TNO Pick Row
        Row tnoRow = sheet.getRow(tnoRowNum);
        if (tnoRow == null) {
        	log.severe("No TNO row defined for row: " + tnoRowNum + " aborting.");
            return;
        }
        Cell tnoHeader = tnoRow.getCell(0);
        String tno = tnoHeader.getStringCellValue();
        log.info("TNO Header: " + tno);
        if (!"2-And-Out".equals(tno)) {
        	log.severe("Unexpected TNO row encountered: " + tno + " can't get TNO information.");
        }
        
        //  Loop over each of the player columns
        for (Integer col : playerMap.keySet()) {
            Cell playerCell = playerRow.getCell(col);
            String nickname = playerCell.getStringCellValue();
            PlayerForSeason pfs = PlayerForSeasonDB.selectPlayerForSeasonByNicknameSeason(nickname, season);
            if (pfs == null) {
            	log.severe("No player found for: " + nickname);
                continue;
            }

            int playerPickCount = 0;
            //	Get the player picks
            for (int offset = 0; offset < season.getTeams().size(); ++offset) {
                Row teamRow = sheet.getRow(teamStartRow + offset);
                if (teamRow == null) {
                	log.severe("Null team row, can not process picks.");
                    continue;
                }

                Cell pickCell = teamRow.getCell(col);
                String cellVal = pickCell.getStringCellValue();
                if (cellVal != null && !cellVal.trim().isEmpty()) {
                    playerPickCount++;
                    Cell teamHeader = teamRow.getCell(0);
                    String city = teamHeader.getStringCellValue();
                    if (teamCities.containsKey(city)) {
                        TeamForSeason tfs = teamCities.get(city);
                        if (playerPicks.containsKey(pfs)) {
                            playerPicks.get(pfs).add(tfs);
                        } else {
                            Set<TeamForSeason> picks = new HashSet<TeamForSeason>();
                            picks.add(tfs);
                            playerPicks.put(pfs, picks);
                        }
                    }
                }
            }
            System.out.println("Player picks for: " + pfs.getNickname() + ": " + playerPickCount);

            //	Process TNO pick
            Cell tnoPick = tnoRow.getCell(col);
            processPickForCell(tnoPick, pfs, tnoPicks);

            //	Process MNF picks
            for (int offset = 0; offset < numMnfGames; ++offset) {
                Row mnfRow = sheet.getRow(mnfRowStart + offset);
                //	Check that the row header is correct for the start of MNF picks
                if (offset == 0) {
                    Cell mnfTitle = mnfRow.getCell(0);
                    if (mnfTitle == null || !"MNF'er".equals(mnfTitle.getStringCellValue().trim())) {
                    	log.severe("Unexpected MNF row at row: " + mnfRowStart);
                        break;
                    }
                }

                Cell mnfPick = mnfRow.getCell(col);
                processPickForCell(mnfPick, pfs, mnfPicks);
            }

            //	Process TNT picks
            for (int offset = 0; offset < numTntGames; ++offset) {
                Row tntRow = sheet.getRow(tntRowStart + offset);
                if (offset == 0) {
                    Cell tntTitle = tntRow.getCell(0);
                    if (tntTitle == null || !"TNT'er".equals(tntTitle.getStringCellValue().trim())) {
                    	log.severe("Unexpected TNT row at row: " + tntRowStart);
                        break;
                    }
                }

                Cell tntPick = tntRow.getCell(col);
                processPickForCell(tntPick, pfs, tntPicks);
            }
        }
	}
	
	private Integer getIntegerFromTitle(String titleString, String delimiter, int offset) {
        if (titleString == null) {
            return null;
        }

        //	Split the title string by whitespace to read each part for the delimiter designation
        String[] titleParts = titleString.split(" ");
        for (int i = 0; i < titleParts.length; ++i) {
            //	If delimiter found (not as the last item in the array), parse the attribute
            if (titleParts[i].equals(delimiter) && i < (titleParts.length - offset)) {
                String attribute = titleParts[i + offset];
                //  Check whether or not there is a "-" specifying an offset for the attribute
                String[] attParts = attribute.split("-");
                if (attParts.length == 2) {
                    attribute = attParts[1];
                }
                Integer intVal;
                try {
                    intVal = Integer.parseInt(attribute);
                } catch (NumberFormatException e) {
                    log.severe("Failed to parse attribute: " + delimiter + " for value: " + attribute);
                    return null;
                }
                return intVal;
            }
        }
        //  If no attribute found, return null
        return null;
    }
	
	/**
    *
    * @param titleString
    * @return
    */
   private Season getSeasonFromTitle(String titleString) {
       if (titleString == null) {
           log.severe("Null title string, can not parse applicable season.");
           return null;
       }
       String delimiter = "YEAR";
       int offset = 1;
       Integer seasonNumber = getIntegerFromTitle(titleString, delimiter, offset);
       return SeasonDB.selectSeasonByNumber(seasonNumber);
   }
   
   /**
    *
    * @param titleString
    * @return
    */
   private Week getWeekFromTitle(String titleString, Season season) {
       if (titleString == null) {
    	   log.severe("Null title string, can not parse applicable week.");
           return null;
       }
       String delimiter = "WEEK";
       int offset = 2;
       Integer weekNumber = getIntegerFromTitle(titleString, delimiter, offset);
       return WeekDB.selectWeekInSeason(weekNumber, season);
   }
   
   private void processPickForCell(Cell cell, PlayerForSeason pfs, Map<PlayerForSeason, Set<TeamForSeason>> pickMap) {
       String cellVal = cell.getStringCellValue();
       if (cellVal != null && !cellVal.trim().isEmpty()) {
           cellVal = cellVal.trim().toUpperCase();
           if (teamCities.containsKey(cellVal)) {
               TeamForSeason tfs = teamCities.get(cellVal);
               if (pickMap.containsKey(pfs)) {
                   Set<TeamForSeason> prevTnos = pickMap.get(pfs);
                   if (prevTnos.contains(tfs)) {
                	   log.severe("Team: " + tfs.getTeam().getTeamAbbr() + " is duplicate for " + pfs.getNickname());
                   } else {
                       pickMap.get(pfs).add(tfs);
                       log.info("Team: " + tfs.getTeam().getTeamAbbr() + " selected by: " + pfs.getNickname());
                   }
               } else {
                   Set<TeamForSeason> picks = new HashSet<TeamForSeason>();
                   picks.add(tfs);
                   pickMap.put(pfs, picks);
                   log.info("Team: " + tfs.getTeam().getTeamAbbr() + " selected by: " + pfs.getNickname());
               }
           } else {
        	   log.severe("No team found for: " + cellVal);
           }
       } else {
    	   log.severe("Cell for pick for: " + pfs.getNickname() + " is null, can't register pick");
       }
   }
}
