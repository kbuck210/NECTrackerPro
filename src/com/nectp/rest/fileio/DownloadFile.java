package com.nectp.rest.fileio;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.activation.FileDataSource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.nectp.poi.ExcelSummaryWriter;

@Stateless
@Path("/download")
public class DownloadFile {
	
	private Logger log = Logger.getLogger(DownloadFile.class.getName());
	
	@EJB
	private ExcelSummaryWriter excelWriter;

	@GET
	@Path("/excel/{nec}/{wk}")
	@Produces("application/vnd.ms-excel")
	public Response downloadExcelTotals(@PathParam("nec") String seasonNumber, @PathParam("wk") String weekNumber) {
		//	If the season & week are defined, find the excel file to export
		String path = "/NECTrackerResources/excel/NEC" + seasonNumber + File.separator;
		String filename = "NEC " + seasonNumber + " - Week " + weekNumber + " Totals.xls";
		java.nio.file.Path filePath = Paths.get(path + filename);
		File excelFile = filePath.toFile();
//		ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
//		String relativeExcelPath = "/ExcelData/NEC" + seasonNumber + "/Week" + weekNumber + "-Totals.xls";
//		String excelPath = ctx.getRealPath(relativeExcelPath);
//		FileDataSource eds = new FileDataSource(xlsFile.getAbsolutePath());
//		File excelFile = eds.getFile();
		
		boolean exists = excelFile != null && excelFile.exists();
		
		//	Check whether the file is null or doesn't exist - if so, create it
		if (!exists) {
			excelWriter.setWeek(seasonNumber, weekNumber);
			exists = excelWriter.writeTotals();
		}
		
		ResponseBuilder response;
		if (exists) {
			response = Response.ok((Object) excelFile);
			String headerVal = "attachement; filename=" + excelFile.getName();
			response.header("Content-Disposition", headerVal);
		}
		else {
			URI fail = null;
			try {
				fail = new URI("/seasons.xhtml?msg=no-file");
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			response = Response.temporaryRedirect(fail);
			log.severe("Failed to export excel.");
		}
		
		return response.build();
	}
	
	@GET
	@Path("/pdf/{nec}/{wk}")
	@Produces("application/pdf")
	public Response downloadPdfTotals(@PathParam("nec") String seasonNumber, @PathParam("wk") String weekNumber) {
		//	If the season & week are defined, find the excel file to export
		String path = "/NECTrackerResources/excel/NEC" + seasonNumber + File.separator;
		String filename = "NEC " + seasonNumber + " - Week " + weekNumber + " Totals.pdf";
		java.nio.file.Path filePath = Paths.get(path + filename);
		File pdfFile = filePath.toFile();
//		FileDataSource eds = new FileDataSource(pdfPath);
//		File pdfFile = eds.getFile();

		ResponseBuilder response = Response.ok((Object) pdfFile);
		String headerVal = "attachement; filename=" + pdfFile.getName();
		response.header("Content-Disposition", headerVal);
		return response.build();
	}
}
