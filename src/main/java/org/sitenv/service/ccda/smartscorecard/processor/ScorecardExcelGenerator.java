package org.sitenv.service.ccda.smartscorecard.processor;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.sitenv.service.ccda.smartscorecard.entities.postgres.ScorecardStatistics;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class ScorecardExcelGenerator {
	
	public HSSFWorkbook exportToExcel(List<ScorecardStatistics> excelRows) throws IOException {
		HSSFWorkbook workBook = new HSSFWorkbook();
		HSSFSheet sheet = workBook.createSheet("Scorecard_Data");
		writeToExcel(excelRows, workBook, sheet);
		return workBook;
	}

	private static void writeToExcel(List<ScorecardStatistics> excelRows, HSSFWorkbook workBook, HSSFSheet sheet) {
		createHeader(workBook, sheet);
		createContents(excelRows, workBook, sheet);
		adjustColumnWidth(sheet, 28);
	}

	private static void createHeader(HSSFWorkbook workBook, HSSFSheet sheet) {
		HSSFRow row = sheet.createRow(0);
		HSSFCellStyle columnHdrStyle = createCellStyleForColumnHeading(workBook);
		
		HSSFCell cell = row.createCell(0);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("DocType");
		
		cell = row.createCell(1);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("DocName");
		
		cell = row.createCell(2);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("ccdaDocType");
		
		cell = row.createCell(3);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("Is Once click scorecard");
		
		cell = row.createCell(4);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("Scored Time");
		
		cell = row.createCell(5);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("DocScore");
		
		cell = row.createCell(6);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("PatientScore");
		
		cell = row.createCell(7);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("AllergiesScore");
		
		cell = row.createCell(8);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("EncountersScore");
		
		cell = row.createCell(9);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("ImmunizationsScore");
		
		cell = row.createCell(10);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("MedicationsScore");
		
		cell = row.createCell(11);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("ProblemsScore");
		
		cell = row.createCell(12);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("ProceduresScore");
		
		cell = row.createCell(13);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("SocialhistoryScore");
		
		cell = row.createCell(14);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("VitalsScore");
		
		cell = row.createCell(15);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("ResultsScore");
		
		cell = row.createCell(16);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("MiscScore");
		
		cell = row.createCell(17);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("PatientIssues");
		
		cell = row.createCell(18);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("AllergiesIssues");
		
		cell = row.createCell(19);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("EncountersIssues");
		
		cell = row.createCell(20);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("ImmunizationsIssues");
		
		cell = row.createCell(21);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("MedicationsIssues");
		
		cell = row.createCell(22);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("ProblemsIssues");
		
		cell = row.createCell(23);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("ProceduresIssues");
		
		cell = row.createCell(24);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("SocialhistoryIssues");
		
		cell = row.createCell(25);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("VitalsIssues");
		
		cell = row.createCell(26);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("ResultsIssues");
		
		cell = row.createCell(27);
		cell.setCellStyle(columnHdrStyle);
		cell.setCellValue("MiscIssues");
		
	}

	private static void createContents(List<ScorecardStatistics> excelRows, HSSFWorkbook workBook, HSSFSheet sheet) {
		HSSFCellStyle cellStyle = createCellStyleForRows(workBook);
		HSSFCellStyle timeCellStyle = createCelStyleForTime(workBook);
		if (excelRows != null) {
			int rowCount = 1;
			Iterator<ScorecardStatistics> excelRowIterator = excelRows.iterator();

			while (excelRowIterator.hasNext()) {
				ScorecardStatistics excelRow = (ScorecardStatistics)excelRowIterator.next();
				HSSFRow row = sheet.createRow(rowCount++);
				HSSFCell cell;
				
				if (!ApplicationUtil.isEmpty(excelRow.getDoctype())) {
					cell = row.createCell(0);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getDoctype());
				}

				if (!ApplicationUtil.isEmpty(excelRow.getDocname())) {
					cell = row.createCell(1);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getDocname());
				}
				
				if (!ApplicationUtil.isEmpty(excelRow.getCcdaDocumentType())) {
					cell = row.createCell(2);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getCcdaDocumentType());
				}
				
				cell = row.createCell(3);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(excelRow.isOneClickScorecard());
				
				if (excelRow.getCreateTimestamp()!=null) {
					
					cell = row.createCell(4);
					cell.setCellStyle(timeCellStyle);
					cell.setCellValue(excelRow.getCreateTimestamp());
				} 
				
				cell = row.createCell(5);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(excelRow.getDocscore());
				
				cell = row.createCell(6);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(excelRow.getPatientScore());
				
				cell = row.createCell(7);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(excelRow.getAllergiesSectionScore());
				
				cell = row.createCell(8);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(excelRow.getEncountersSectionScore());
				
				cell = row.createCell(9);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(excelRow.getImmunizationsSectionScore());
				
				cell = row.createCell(10);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(excelRow.getMedicationsSectionScore());
				
				cell = row.createCell(11);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(excelRow.getProblemsSectionScore());
				
				cell = row.createCell(12);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(excelRow.getProceduresSectionScore());
				
				cell = row.createCell(13);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(excelRow.getSocialhistorySectionScore());
				
				cell = row.createCell(14);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(excelRow.getVitalsSectionScore());
				
				cell = row.createCell(15);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(excelRow.getResultsSectionScore());
				
				cell = row.createCell(16);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(excelRow.getMiscScore());
				
				if(excelRow.getPatientIssues()!= null){
					cell = row.createCell(17);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getPatientIssues());
				}
				
				if(excelRow.getAllergiesSectionIssues()!= null){
					cell = row.createCell(18);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getAllergiesSectionIssues());
				}
				
				if(excelRow.getEncountersSectionIssues()!= null){
					cell = row.createCell(19);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getEncountersSectionIssues());
				}
				
				if(excelRow.getImmunizationsSectionIssues()!= null){
					cell = row.createCell(20);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getImmunizationsSectionIssues());
				}
				
				if(excelRow.getMedicationsSectionIssues()!= null){
					cell = row.createCell(21);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getMedicationsSectionIssues());
				}
				
				if(excelRow.getProblemsSectionIssues()!= null){
					cell = row.createCell(22);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getProblemsSectionIssues());
				}
				
				if(excelRow.getProceduresSectionIssues()!= null){
					cell = row.createCell(23);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getProceduresSectionIssues());
				}
				
				if(excelRow.getSocialhistorySectionIssues()!= null){
					cell = row.createCell(24);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getSocialhistorySectionIssues());
				}
				
				if(excelRow.getVitalsSectionIssues()!= null){
					cell = row.createCell(25);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getVitalsSectionIssues());
				}
				
				if(excelRow.getResultsSectionIssues()!= null){
					cell = row.createCell(26);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getResultsSectionIssues());
				}
				
				if(excelRow.getMiscIssues()!= null){
					cell = row.createCell(27);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(excelRow.getMiscIssues());
				}
				
			}
		}

	}

	private static void adjustColumnWidth(Sheet sheet, int numberOfColumns) {
		for (int i = 0; i < numberOfColumns; ++i) {
			sheet.autoSizeColumn(i);
		}
	}

	public static HSSFCellStyle createCellStyleForColumnHeading(HSSFWorkbook workBook) {
		HSSFCellStyle cellStyle = workBook.createCellStyle();
		HSSFFont fontObj = workBook.createFont();
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setFillBackgroundColor(Short.valueOf("22").shortValue());
		cellStyle.setFillPattern(FillPatternType.BIG_SPOTS);
		cellStyle.setFillForegroundColor(Short.valueOf("22").shortValue());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		fontObj.setFontName("Calibri");
		fontObj.setFontHeightInPoints(Short.valueOf("12").shortValue());
		fontObj.setBold(true);
		fontObj.setColor(Short.valueOf("8").shortValue());
		cellStyle.setFont(fontObj);
		return cellStyle;
	}

	public static HSSFCellStyle createCellStyleForRows(HSSFWorkbook workBook) {
		HSSFCellStyle cellStyle = workBook.createCellStyle();
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		return cellStyle;
	}
	
	public static HSSFCellStyle createCelStyleForTime(HSSFWorkbook workBook)
	{
		HSSFCellStyle cellStyle = workBook.createCellStyle();
		CreationHelper createHelper = workBook.getCreationHelper();
		cellStyle.setDataFormat(
		    createHelper.createDataFormat().getFormat("m/d/yy h:mm:ss"));
		return cellStyle;
	}

	public static HSSFCellStyle createCellStyleForComRepNotSetUp(HSSFWorkbook workBook) {
		HSSFCellStyle cellStyle = workBook.createCellStyle();
		HSSFFont fontObj = workBook.createFont();
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		fontObj.setFontName("Calibri");
		fontObj.setFontHeightInPoints(Short.valueOf("12").shortValue());
		fontObj.setBold(true);
		fontObj.setColor((short) 10);
		cellStyle.setFont(fontObj);
		return cellStyle;
	}

	public static HSSFCellStyle createCellStyleForComRepSetUp(HSSFWorkbook workBook) {
		HSSFCellStyle cellStyle = workBook.createCellStyle();
		HSSFFont fontObj = workBook.createFont();
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		fontObj.setFontName("Calibri");
		fontObj.setFontHeightInPoints(Short.valueOf("12").shortValue());
		fontObj.setBold(true);
		fontObj.setColor((short) 17);
		cellStyle.setFont(fontObj);
		return cellStyle;
	}

}
