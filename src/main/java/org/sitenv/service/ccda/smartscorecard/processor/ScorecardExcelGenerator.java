package org.sitenv.service.ccda.smartscorecard.processor;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	
	private static final Map<Integer, String> HEADER_INDEX_AND_VALUE;
	private static final int NUMBER_OF_COLUMNS; 
	
	static {
		HEADER_INDEX_AND_VALUE = new HashMap<Integer, String>();
		final List<String> headers = new ArrayList<String>(Arrays.asList("C-CDA Version", "Filename", "C-CDA Document Type", "Sender", 
				"Is One Click Scorecard", "Time Scored", "DocScore", "PatientScore", "AllergiesScore", "EncountersScore",  "ImmunizationsScore", 
				"MedicationsScore", "ProblemsScore", "ProceduresScore", "SocialhistoryScore", "VitalsScore",  "ResultsScore", "MiscScore", 
				"PatientIssues", "AllergiesIssues", "EncountersIssues", "ImmunizationsIssues", "MedicationsIssues", "ProblemsIssues", 
				"ProceduresIssues", "SocialhistoryIssues", "VitalsIssues", "ResultsIssues", "MiscIssues"));
		NUMBER_OF_COLUMNS = headers.size();
		for(int i = 0; i < NUMBER_OF_COLUMNS; i++) {
			HEADER_INDEX_AND_VALUE.put(i, headers.get(i));
		}
	}
	
	public HSSFWorkbook exportToExcel(List<ScorecardStatistics> excelRows) throws IOException {
		HSSFWorkbook workBook = new HSSFWorkbook();
		HSSFSheet sheet = workBook.createSheet("Scorecard_Data");
		writeToExcel(excelRows, workBook, sheet);
		return workBook;
	}

	private static void writeToExcel(List<ScorecardStatistics> excelRows, HSSFWorkbook workBook, HSSFSheet sheet) {
		createHeader(workBook, sheet);
		createContents(excelRows, workBook, sheet);
		adjustColumnWidth(sheet, NUMBER_OF_COLUMNS);
	}
	
	private static void createCell(HSSFRow row, HSSFCell cell, int index, HSSFCellStyle style, Object value) {
		cell = row.createCell(index);
		cell.setCellStyle(style);
		if(value instanceof Integer) {
			cell.setCellValue((int) value);
		} else if(value instanceof String) {
			cell.setCellValue((String) value);
		} else if(value instanceof Boolean) {
			cell.setCellValue((boolean) value);
		} else if(value instanceof Timestamp) {
			cell.setCellValue((Timestamp) value);
		} else if(value instanceof Long) {
			cell.setCellValue((long) value);
		}
	}

	private static void createHeader(HSSFWorkbook workBook, HSSFSheet sheet) {
		HSSFRow row = sheet.createRow(0);
		HSSFCellStyle columnHdrStyle = createCellStyleForColumnHeading(workBook);
		HSSFCell cell = null;
		for(int colIndex = 0; colIndex < NUMBER_OF_COLUMNS; colIndex++) {
			createCell(row, cell, colIndex, columnHdrStyle, HEADER_INDEX_AND_VALUE.get(colIndex));
		}
	}

	private static void createContents(List<ScorecardStatistics> excelRows, HSSFWorkbook workBook, HSSFSheet sheet) {		
		HSSFCellStyle cellStyle = createCellStyleForRows(workBook);
		HSSFCellStyle timeCellStyle = createCelStyleForTime(workBook);
		if (excelRows != null) {
			int rowCount = 1;
			Iterator<ScorecardStatistics> excelRowIterator = excelRows.iterator();

			while (excelRowIterator.hasNext()) {
				ScorecardStatistics excelRow = (ScorecardStatistics) excelRowIterator.next();
				HSSFRow row = sheet.createRow(rowCount++);
				HSSFCell cell = null;
				
				if (!ApplicationUtil.isEmpty(excelRow.getDoctype())) {
					createCell(row, cell, 0, cellStyle, excelRow.getDoctype());
				}
				if (!ApplicationUtil.isEmpty(excelRow.getDocname())) {
					createCell(row, cell, 1, cellStyle, excelRow.getDocname());
				}
				if (!ApplicationUtil.isEmpty(excelRow.getCcdaDocumentType())) {
					createCell(row, cell, 2, cellStyle, excelRow.getCcdaDocumentType());
				}				
				if (!ApplicationUtil.isEmpty(excelRow.getDirectEmailAddress())) {
					createCell(row, cell, 3, cellStyle, excelRow.getDirectEmailAddress());
				}
				
				createCell(row, cell, 4, cellStyle, excelRow.isOneClickScorecard());

				if (excelRow.getCreateTimestamp() != null) {
					createCell(row, cell, 5, timeCellStyle, excelRow.getCreateTimestamp());
				} 
				
				createCell(row, cell, 6, cellStyle, excelRow.getDocscore());
				createCell(row, cell, 7, cellStyle, excelRow.getPatientScore());
				createCell(row, cell, 8, cellStyle, excelRow.getAllergiesSectionScore());
				createCell(row, cell, 9, cellStyle, excelRow.getEncountersSectionScore());
				createCell(row, cell, 10, cellStyle, excelRow.getImmunizationsSectionScore());				
				createCell(row, cell, 11, cellStyle, excelRow.getMedicationsSectionScore());
				createCell(row, cell, 12, cellStyle, excelRow.getProblemsSectionScore());
				createCell(row, cell, 13, cellStyle, excelRow.getProceduresSectionScore());
				createCell(row, cell, 14, cellStyle, excelRow.getSocialhistorySectionScore());
				createCell(row, cell, 15, cellStyle, excelRow.getVitalsSectionScore());
				createCell(row, cell, 16, cellStyle, excelRow.getResultsSectionScore());
				createCell(row, cell, 17, cellStyle, excelRow.getMiscScore());
				
				if(excelRow.getPatientIssues() != null) {
					createCell(row, cell, 18, cellStyle, excelRow.getPatientIssues());
				}				
				if(excelRow.getAllergiesSectionIssues() != null) {
					createCell(row, cell, 19, cellStyle, excelRow.getAllergiesSectionIssues());
				}				
				if(excelRow.getEncountersSectionIssues() != null) {
					createCell(row, cell, 20, cellStyle, excelRow.getEncountersSectionIssues());
				}				
				if(excelRow.getImmunizationsSectionIssues() != null) {
					createCell(row, cell, 21, cellStyle, excelRow.getImmunizationsSectionIssues());
				}
				if(excelRow.getMedicationsSectionIssues() != null) {
					createCell(row, cell, 22, cellStyle, excelRow.getMedicationsSectionIssues());
				}				
				if(excelRow.getProblemsSectionIssues() != null) {
					createCell(row, cell, 23, cellStyle, excelRow.getProblemsSectionIssues());
				}				
				if(excelRow.getProceduresSectionIssues() != null) {
					createCell(row, cell, 24, cellStyle, excelRow.getProceduresSectionIssues());
				}				
				if(excelRow.getSocialhistorySectionIssues() != null) {
					createCell(row, cell, 25, cellStyle, excelRow.getSocialhistorySectionIssues());
				}				
				if(excelRow.getVitalsSectionIssues() != null) {
					createCell(row, cell, 26, cellStyle, excelRow.getVitalsSectionIssues());
				}				
				if(excelRow.getResultsSectionIssues() != null) {
					createCell(row, cell, 27, cellStyle, excelRow.getResultsSectionIssues());
				}				
				if(excelRow.getMiscIssues() != null) {
					createCell(row, cell, 28, cellStyle, excelRow.getMiscIssues());
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
