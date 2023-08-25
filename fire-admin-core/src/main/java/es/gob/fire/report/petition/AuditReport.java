package es.gob.fire.report.petition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.persistence.entity.AuditTransaction;
import es.gob.fire.persistence.entity.AuditSignature;
import es.gob.fire.report.common.Report;

public class AuditReport extends Report implements IAuditReport{
	
	/**
	 * Constant that represents the parameter log.
	 */
	private static final Logger LOGGER = Logger.getLogger(AuditReport.class);
	
	private Map<String, List<AuditSignature>> listSignatures = new HashMap<String, List<AuditSignature>>();

	private List<AuditTransaction> listTransactions = new ArrayList<AuditTransaction>();
	
	/**
	 * Attribute that represents the width of the first column.
	 */
	private short columnStandardWidth = NumberConstants.NUM30;
	
	/**
	 * Attribute that represents the width of the first column.
	 */
	private short columnWideWidth = NumberConstants.NUM40;
	
	/**
	 * Attribute that represents the width of the first column.
	 */
	private short columnNarrowWidth = NumberConstants.NUM23;
	
	private int[] columnWidths = {columnStandardWidth, columnStandardWidth, columnStandardWidth, columnNarrowWidth, columnStandardWidth, columnNarrowWidth, columnNarrowWidth, columnNarrowWidth, columnNarrowWidth, columnNarrowWidth, columnStandardWidth, columnStandardWidth, columnStandardWidth};
	
	public AuditReport(Map<Object, Object> parametersParam) {
		super(parametersParam);
	}

	@Override
	public byte[] getReport() throws Exception {
		LOGGER.info("Report generation process starts");
		try {
			getData();
			LOGGER.info("Data has been obtained");
			byte[] report = generateReport();
			LOGGER.info("Report has been generated");
			return report;
		} catch (Exception e){
			LOGGER.error("There was an exception during the generation process of PetitionsReport");
			throw new Exception(e);
		}
	}
	
	/**
	 * Method that gets all the information needed to generate the Petition report.
	 */
	private void getData() {
		if (getParameters() != null){
			if (getParameters().containsKey("listSignatures")){
				List<AuditSignature> listAS = null;
				if (getParameters().get("listSignatures") instanceof List<?>) {
					listAS = (List<AuditSignature>) getParameters().get("listSignatures");
					
					for (AuditSignature pbs : listAS){
						String idTransaction = pbs.getIdTransaction();
						
						if (listSignatures.containsKey(idTransaction)){
							listSignatures.get(idTransaction).add(pbs);
						} else {
							List<AuditSignature> listASAssociated = new ArrayList<AuditSignature>();
							listASAssociated.add(pbs);
							listSignatures.put(idTransaction, listASAssociated);
						}
					}
				}
			}
			
			if (getParameters().containsKey("listTransactions")){
				listTransactions = (List<AuditTransaction>) getParameters().get("listTransactions");
			} 
		}
	}
	
	/**
	 * Method that creates a complete report and writes the values in the corresponding cells.
	 * @return a bytes array that represents the report.
	 * @throws IOException If the method fails.
	 */
	private byte[] generateReport() throws IOException{
		
		byte[] bytes = null;
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		
		createPetitionsSheet(workbook);
		
		workbook.write(out);
		bytes = out.toByteArray();
		
		out.close();
		
		return bytes;
	}

	private void createPetitionsSheet(HSSFWorkbook workbook) {
		//Create sheet
		HSSFSheet sheet = workbook.createSheet("Transacciones");
		
		//Create custom color palette
		HSSFPalette palette = createPalette(workbook);
		
		//Create custom font
		Map<String, HSSFFont> fonts = createFonts(workbook);
		
		//Create custom styles
		Map<String, HSSFCellStyle> cellStyles = createCellStyles(workbook, fonts);
		
		
		if (listTransactions.size() > 0){
			//Create title space
			createTitle(sheet, cellStyles);
			
			//Creation of the header
			createHeader(sheet, cellStyles);
			
			//Set columns width
			for (int i = 0; i < columnWidths.length; i++) {
				sheet.setColumnWidth((short) i, (short) (columnWidths[i] * 300));
			}
			
			fillReportWithData(sheet, cellStyles);
			
			createLastRow(sheet, cellStyles);
		}
	}

	private HSSFPalette createPalette(HSSFWorkbook workbook) {
		
		HSSFPalette palette = workbook.getCustomPalette();
		
		//Title Background Color
		palette.setColorAtIndex(HSSFColor.HSSFColorPredefined.BRIGHT_GREEN.getIndex(),
		        (byte) 255,  //RGB red (0-255)
		        (byte) 255,    //RGB green
		        (byte) 255     //RGB blue
		);
		
		//Title Font Color
		palette.setColorAtIndex(HSSFColor.HSSFColorPredefined.CORAL.getIndex(),
		        (byte) 0,  //RGB red (0-255)
		        (byte) 0,    //RGB green
		        (byte) 0     //RGB blue
		);
		
		//Header Background Color
		palette.setColorAtIndex(HSSFColor.HSSFColorPredefined.BLUE.getIndex(),
		        (byte) 0,  //RGB red (0-255)
		        (byte) 102,    //RGB green
		        (byte) 204     //RGB blue
		);
		
		//Header Font Color
		palette.setColorAtIndex(HSSFColor.HSSFColorPredefined.WHITE.getIndex(),
				(byte) 255,  //RGB red (0-255)
		        (byte) 255,    //RGB green
		        (byte) 255     //RGB blue
		);
		
		//Data Uneven Background Color
		palette.setColorAtIndex(HSSFColor.HSSFColorPredefined.ROSE.getIndex(),
		        (byte) 234,  //RGB red (0-255)
		        (byte) 234,    //RGB green
		        (byte) 234     //RGB blue
		);
		
		//Data Even Background Color
		palette.setColorAtIndex(HSSFColor.HSSFColorPredefined.AQUA.getIndex(),
		        (byte) 255,  //RGB red (0-255)
		        (byte) 255,    //RGB green
		        (byte) 255     //RGB blue
		);
		
		//Data Font Color
		palette.setColorAtIndex(HSSFColor.HSSFColorPredefined.CORNFLOWER_BLUE.getIndex(),
		        (byte) 0,  //RGB red (0-255)
		        (byte) 0,    //RGB green
		        (byte) 0     //RGB blue
		);
		
		return palette;
	}
	
	private Map<String, HSSFFont> createFonts(HSSFWorkbook workbook) {
		Map<String, HSSFFont> fonts = new HashMap<String, HSSFFont>();
		
		//Title font
		HSSFFont titleFont = workbook.createFont();
		titleFont.setColor(HSSFColor.HSSFColorPredefined.CORAL.getIndex());
		titleFont.setFontName("Frutiger-Light");
		titleFont.setFontHeightInPoints((short)(14));
		titleFont.setBold(true);
		
		fonts.put("title", titleFont);
		
		HSSFFont headerFont = workbook.createFont();
		headerFont.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
		headerFont.setFontName("Frutiger-Light");
		headerFont.setFontHeightInPoints((short)(12));
		titleFont.setBold(true);
		
		fonts.put("header", headerFont);
		
		HSSFFont lastRowFont = workbook.createFont();
		lastRowFont.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
		lastRowFont.setFontName("Frutiger-Light");
		lastRowFont.setFontHeightInPoints((short) 11);
		
		fonts.put("lastRow", lastRowFont);
		
		HSSFFont dataFont = workbook.createFont();
		dataFont.setColor(HSSFColor.HSSFColorPredefined.CORNFLOWER_BLUE.getIndex());
		dataFont.setFontName("Frutiger-Light");
		dataFont.setFontHeightInPoints((short) 10);
		
		fonts.put("data", dataFont);
		
		return fonts;
	}
	
	private Map<String, HSSFCellStyle> createCellStyles(HSSFWorkbook workbook, Map<String, HSSFFont> fonts) {
		Map<String, HSSFCellStyle> cellStyles = new HashMap<String, HSSFCellStyle>();
		
		//Title style
		HSSFCellStyle titleCellStyle = workbook.createCellStyle();
		titleCellStyle.setAlignment(HorizontalAlignment.CENTER);
		titleCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		titleCellStyle.setFont(fonts.get("title"));
		titleCellStyle.setWrapText(true);
		titleCellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.BRIGHT_GREEN.getIndex());
		titleCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		cellStyles.put("title", titleCellStyle);
		
		//Header style
		HSSFCellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(fonts.get("header"));
		headerCellStyle.setWrapText(true);
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
		headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerCellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
		headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		cellStyles.put("header", headerCellStyle);
		
		//Last row style
		HSSFCellStyle lastRowCellStyle = workbook.createCellStyle();
		lastRowCellStyle.setFont(fonts.get("lastRow"));
		lastRowCellStyle.setWrapText(true);
		lastRowCellStyle.setAlignment(HorizontalAlignment.LEFT);
		lastRowCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		lastRowCellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
		lastRowCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		cellStyles.put("lastRow", lastRowCellStyle);
				
		//Data style uneven
		HSSFCellStyle dataCellStyleUneven = workbook.createCellStyle();
		dataCellStyleUneven.setFont(fonts.get("data"));
		dataCellStyleUneven.setWrapText(true);
		dataCellStyleUneven.setAlignment(HorizontalAlignment.CENTER);
		dataCellStyleUneven.setVerticalAlignment(VerticalAlignment.CENTER);
		dataCellStyleUneven.setFillForegroundColor(HSSFColor.HSSFColorPredefined.ROSE.getIndex());
		dataCellStyleUneven.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		dataCellStyleUneven.setBorderLeft(BorderStyle.THIN);
		dataCellStyleUneven.setLeftBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		dataCellStyleUneven.setBorderRight(BorderStyle.THIN);
		dataCellStyleUneven.setRightBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		
		cellStyles.put("dataUneven", dataCellStyleUneven);
		
		//Data style even
		HSSFCellStyle dataCellStyleEven = workbook.createCellStyle();
		dataCellStyleEven.setFont(fonts.get("data"));
		dataCellStyleEven.setWrapText(true);
		dataCellStyleEven.setAlignment(HorizontalAlignment.CENTER);
		dataCellStyleEven.setVerticalAlignment(VerticalAlignment.CENTER);
		dataCellStyleEven.setFillForegroundColor(HSSFColor.HSSFColorPredefined.AQUA.getIndex());
		dataCellStyleEven.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		dataCellStyleEven.setBorderLeft(BorderStyle.THIN);
		dataCellStyleEven.setLeftBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		dataCellStyleEven.setBorderRight(BorderStyle.THIN);
		dataCellStyleEven.setRightBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		
		cellStyles.put("dataEven", dataCellStyleEven);
		
		return cellStyles;
	}
	
	private void createTitle(HSSFSheet sheet, Map<String, HSSFCellStyle> cellStyles) {
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, (short) (0 + NumberConstants.NUM12)));
		HSSFRow titleRow = sheet.createRow(1);
		titleRow.setHeight((short) 600);
		HSSFCell titleCell = titleRow.createCell(0);
		titleCell.setCellStyle(cellStyles.get("title"));
		titleCell.setCellValue("Listado de Transacciones");
	}
	
	private void createHeader(HSSFSheet sheet, Map<String, HSSFCellStyle> cellStyles) {
		HSSFRow headerRow = sheet.createRow(2);
		headerRow.setHeight((short) 450);
		
		HSSFCell headerCell = headerRow.createCell(0);
		headerCell.setCellStyle(cellStyles.get("header"));
		headerCell.setCellValue("Fecha");
		
		headerCell = headerRow.createCell(1);
		headerCell.setCellStyle(cellStyles.get("header"));
		headerCell.setCellValue("Aplicacion");
		
		headerCell = headerRow.createCell(2);
		headerCell.setCellStyle(cellStyles.get("header"));
		headerCell.setCellValue("Id transaccion");
		
		headerCell = headerRow.createCell(3);
		headerCell.setCellStyle(cellStyles.get("header"));
		headerCell.setCellValue("Operacion");
		
		headerCell = headerRow.createCell(4);
		headerCell.setCellStyle(cellStyles.get("header"));
		headerCell.setCellValue("Operacion criptografica");
		
		headerCell = headerRow.createCell(5);
		headerCell.setCellStyle(cellStyles.get("header"));
		headerCell.setCellValue("Algoritmo");
		
		headerCell = headerRow.createCell(6);
		headerCell.setCellStyle(cellStyles.get("header"));
		headerCell.setCellValue("Formato");
		
		headerCell = headerRow.createCell(7);
		headerCell.setCellStyle(cellStyles.get("header"));
		headerCell.setCellValue("Formato actualizado");
		
		headerCell = headerRow.createCell(8);
		headerCell.setCellStyle(cellStyles.get("header"));
		headerCell.setCellValue("Proveedor");
		
		headerCell = headerRow.createCell(9);
		headerCell.setCellStyle(cellStyles.get("header"));
		headerCell.setCellValue("Proveedor forzado");
		
		headerCell = headerRow.createCell(10);
		headerCell.setCellStyle(cellStyles.get("header"));
		headerCell.setCellValue("Navegador");
		
		headerCell = headerRow.createCell(11);
		headerCell.setCellStyle(cellStyles.get("header"));
		headerCell.setCellValue("Nodo");
		
		headerCell = headerRow.createCell(12);
		headerCell.setCellStyle(cellStyles.get("header"));
		headerCell.setCellValue("Resultado");
	}

	private void fillReportWithData(HSSFSheet sheet, Map<String, HSSFCellStyle> cellStyles) {
		
		for (int i = 0; i < listTransactions.size(); i++){
			HSSFRow dataRow = sheet.createRow(3 + i);
			AuditTransaction auditTransaction = listTransactions.get(i);
			
			//Fecha
			HSSFCell dataCell = dataRow.createCell(0);
			if (i % 2 == 0){
				dataCell.setCellStyle(cellStyles.get("dataEven"));
			} else {
				dataCell.setCellStyle(cellStyles.get("dataUneven"));
			}
			dataCell.setCellValue(auditTransaction.getDate().toString());
			
			//App
			dataCell = dataRow.createCell(1);
			if (i % 2 == 0){
				dataCell.setCellStyle(cellStyles.get("dataEven"));
			} else {
				dataCell.setCellStyle(cellStyles.get("dataUneven"));
			}
			dataCell.setCellValue(auditTransaction.getNameApp());
			
			//Id transaccion
			dataCell = dataRow.createCell(2);
			if (i % 2 == 0){
				dataCell.setCellStyle(cellStyles.get("dataEven"));
			} else {
				dataCell.setCellStyle(cellStyles.get("dataUneven"));
			}
			dataCell.setCellValue(auditTransaction.getIdTransaction());
			
			//Operacion
			dataCell = dataRow.createCell(3);
			if (i % 2 == 0){
				dataCell.setCellStyle(cellStyles.get("dataEven"));
			} else {
				dataCell.setCellStyle(cellStyles.get("dataUneven"));
			}
			dataCell.setCellValue(auditTransaction.getOperation());
			
			//Operacion criptografica
			dataCell = dataRow.createCell(4);
			if (i % 2 == 0){
				dataCell.setCellStyle(cellStyles.get("dataEven"));
			} else {
				dataCell.setCellStyle(cellStyles.get("dataUneven"));
			}
			dataCell.setCellValue(auditTransaction.getCryptoOperation());
			
			//Algoritmo
			dataCell = dataRow.createCell(5);
			if (i % 2 == 0){
				dataCell.setCellStyle(cellStyles.get("dataEven"));
			} else {
				dataCell.setCellStyle(cellStyles.get("dataUneven"));
			}
			dataCell.setCellValue(auditTransaction.getAlgorithm());
			
			//Formato
			dataCell = dataRow.createCell(6);
			if (i % 2 == 0){
				dataCell.setCellStyle(cellStyles.get("dataEven"));
			} else {
				dataCell.setCellStyle(cellStyles.get("dataUneven"));
			}
			dataCell.setCellValue(auditTransaction.getFormat());
			
			//Formato actualizado
			dataCell = dataRow.createCell(7);
			if (i % 2 == 0){
				dataCell.setCellStyle(cellStyles.get("dataEven"));
			} else {
				dataCell.setCellStyle(cellStyles.get("dataUneven"));
			}
			dataCell.setCellValue(auditTransaction.getUpdateFormat());
			
			//Proveedor
			dataCell = dataRow.createCell(8);
			if (i % 2 == 0){
				dataCell.setCellStyle(cellStyles.get("dataEven"));
			} else {
				dataCell.setCellStyle(cellStyles.get("dataUneven"));
			}
			dataCell.setCellValue(auditTransaction.getProvider());
			
			//Proveedor forzado
			dataCell = dataRow.createCell(9);
			if (i % 2 == 0){
				dataCell.setCellStyle(cellStyles.get("dataEven"));
			} else {
				dataCell.setCellStyle(cellStyles.get("dataUneven"));
			}
			dataCell.setCellValue(auditTransaction.getForcedProvider() ? "SI" : "NO");
			
			//Navegador
			dataCell = dataRow.createCell(10);
			if (i % 2 == 0){
				dataCell.setCellStyle(cellStyles.get("dataEven"));
			} else {
				dataCell.setCellStyle(cellStyles.get("dataUneven"));
			}
			dataCell.setCellValue(auditTransaction.getBrowser());
			
			//Nodo
			dataCell = dataRow.createCell(11);
			if (i % 2 == 0){
				dataCell.setCellStyle(cellStyles.get("dataEven"));
			} else {
				dataCell.setCellStyle(cellStyles.get("dataUneven"));
			}
			dataCell.setCellValue(auditTransaction.getNode());
			
			//Resultado
			dataCell = dataRow.createCell(12);
			if (i % 2 == 0){
				dataCell.setCellStyle(cellStyles.get("dataEven"));
			} else {
				dataCell.setCellStyle(cellStyles.get("dataUneven"));
			}
			
			String resultValue = "";
			
			if (!auditTransaction.getOperation().equals("BATCH")){
				resultValue = auditTransaction.getResult() ? "OK" : "ERROR";
			} else {
				Integer numberOfErrors = 0;
				if (listSignatures.get(auditTransaction.getIdTransaction()) != null){
					for (AuditSignature pbs : listSignatures.get(auditTransaction.getIdTransaction())){
						if (pbs.getResult() == false){
							numberOfErrors++;
						}
					}
				}
				resultValue = String.format("Errores: %d", numberOfErrors);
			}
			
			dataCell.setCellValue(resultValue);
		}
	}
	
	private void createLastRow(HSSFSheet sheet, Map<String, HSSFCellStyle> cellStyles) {
		Integer lastRowIndex = 2 + listTransactions.size() + 1;
		
		HSSFRow lastRow = sheet.createRow(2 + listTransactions.size() + 1);
		lastRow.setHeight((short) 450);
		
		sheet.addMergedRegion(new CellRangeAddress(lastRowIndex, lastRowIndex, 0, (short) (0 + NumberConstants.NUM12)));
		
		HSSFCell lastRowCell = lastRow.createCell(0);
		lastRowCell.setCellStyle(cellStyles.get("lastRow"));
		lastRowCell.setCellValue("Numero de peticiones: " + listTransactions.size());
	}
}
