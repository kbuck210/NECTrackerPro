package com.nectp.rest.fileio;

import java.io.File;
import java.util.logging.Logger;

import javax.activation.FileDataSource;
import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Stateless
@Path("/download")
public class DownloadFile {
	
	private Logger log = Logger.getLogger(DownloadFile.class.getName());

	@GET
	@Path("/excel/{nec}/{wk}")
	@Produces("application/vnd.ms-excel")
	public Response downloadExcelTotals(@PathParam("nec") String seasonNumber, @PathParam("wk") String weekNumber) {
		//	If the season & week are defined, find the excel file to export
		//	TODO: change path to a static dir path on host system
		ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		String relativeExcelPath = "/ExcelData/NEC" + seasonNumber + "/Week" + weekNumber + "-Totals.xls";
		String excelPath = ctx.getRealPath(relativeExcelPath);
		FileDataSource eds = new FileDataSource(excelPath);
		File excelFile = eds.getFile();
		
		ResponseBuilder response = Response.ok((Object) excelFile);
		String headerVal = "attachement; filename=" + excelFile.getName();
		response.header("Content-Disposition", headerVal);
		return response.build();
	}
	
	@GET
	@Path("/pdf/{nec}/{wk}")
	@Produces("application/pdf")
	public Response downloadPdfTotals(@PathParam("nec") String seasonNumber, @PathParam("wk") String weekNumber) {
		//	If the season & week are defined, find the excel file to export
		//	TODO: change path to a static dir path on host system
		ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		String relativePdfPath = "/ExcelData/NEC" + seasonNumber + "/Week" + weekNumber + "-Totals.pdf";
		String pdfPath = ctx.getRealPath(relativePdfPath);
		FileDataSource eds = new FileDataSource(pdfPath);
		File pdfFile = eds.getFile();

		ResponseBuilder response = Response.ok((Object) pdfFile);
		String headerVal = "attachement; filename=" + pdfFile.getName();
		response.header("Content-Disposition", headerVal);
		return response.build();
	}
}
