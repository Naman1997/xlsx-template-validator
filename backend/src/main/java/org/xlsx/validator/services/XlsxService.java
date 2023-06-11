package org.xlsx.validator.services;

import org.apache.poi.ss.formula.DataValidationEvaluator;
import org.apache.poi.ss.formula.WorkbookEvaluatorProvider;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Singleton
public class XlsxService {

    private static final int HEADER_ROW = 0;

    public XSSFWorkbook copyFileUsingTemplate(String contentFilePath, String consolidatedFilePath, Map<Integer, String> templateHeaders) throws Exception {
        Path contentPath = Paths.get(contentFilePath);
        XSSFWorkbook contentWorkbook = new XSSFWorkbook(Files.newInputStream(contentPath));
        Path consolidatedWorkbookPath = Paths.get(consolidatedFilePath);
        XSSFWorkbook consolidatedWorkbook = new XSSFWorkbook(Files.newInputStream(consolidatedWorkbookPath));
        Sheet consolidatedWorksheet = consolidatedWorkbook.getSheetAt(0);

        // Validate headers in all sheets in the workbook
        contentWorkbook.sheetIterator().forEachRemaining(contentsSheet -> {
            boolean hasValidHeaders = validateHeaders(templateHeaders, contentsSheet);
            if (!hasValidHeaders) {
                throw new RuntimeException("Content File: '" + contentPath.getFileName()
                        + "' contains invalid headers in sheet: '" + contentsSheet.getSheetName());
            }

            // Append rows to consolidated sheet
            copyRows(contentsSheet, consolidatedWorksheet);
        });

        // Close the content workbook
        contentWorkbook.close();

        // Validate consolidated sheet
        consolidatedWorkbook.setSheetName(0, "Consolidated");
        validateSheet(consolidatedWorkbook, consolidatedWorksheet, contentPath);
        return consolidatedWorkbook;
    }

    private static void validateSheet(XSSFWorkbook workbook, Sheet sheet, Path path) {
        WorkbookEvaluatorProvider provider = new XSSFFormulaEvaluator(workbook);
        DataValidationEvaluator validationEvaluator = new DataValidationEvaluator(workbook, provider);
        for (int i = 1; i <= sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    CellReference cellReference = new CellReference(cell);
                    if (!validationEvaluator.isValidCell(cellReference)) {
                        throw new RuntimeException("File '" + path.getFileName() + "' did not pass validations!");
                    }
                }
            }
        }
    }

    private void copyRows(Sheet sourceSheet, Sheet destSheet) {
        // Iterate over the rows in the source sheet
        int endRow = sourceSheet.getLastRowNum();
        for (int i = 1; i <= endRow; i++) {
            Row sourceRow = sourceSheet.getRow(i);
            if (sourceRow == null || sourceRow.getLastCellNum() == 0 || isRowEmpty(sourceRow)) {
                // If the row is null, break the loop
                break;
            }
            // Create a new row in the destination sheet
            Row destRow = destSheet.createRow(destSheet.getLastRowNum() + 1);

            // Copy the cell values and styles from the source row to the destination row
            for (int j = sourceRow.getFirstCellNum(); j < sourceRow.getLastCellNum(); j++) {
                Cell sourceCell = sourceRow.getCell(j);
                if (sourceCell != null && sourceCell.getCellType() != CellType.BLANK) {
                    Cell destCell = destRow.createCell(j);
                    updateCellValue(sourceCell, destCell);
                }
            }
        }
    }

    private static boolean isRowEmpty(Row row) {
        boolean isEmpty = true;
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if (cell.getCellType() != CellType.BLANK) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }


    private void updateCellValue(Cell sourceCell, Cell targetCell) {
        if (sourceCell != null) {
            if (sourceCell.getLocalDateTimeCellValue() != null) {
                targetCell.setCellValue(sourceCell.getLocalDateTimeCellValue());
            } else if (sourceCell.getDateCellValue() != null) {
                targetCell.setCellValue(sourceCell.getDateCellValue());
            } else if (sourceCell.getRichStringCellValue() != null) {
                targetCell.setCellValue(sourceCell.getRichStringCellValue());
            } else if (sourceCell.getCellType() == CellType.BOOLEAN) {
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
            } else if (sourceCell.getCellType() == CellType.NUMERIC) {
                targetCell.setCellValue(sourceCell.getNumericCellValue());
            } else if (sourceCell.getCellType() == CellType.STRING) {
                targetCell.setCellValue(sourceCell.getStringCellValue());
            } else if (sourceCell.getCellType() == CellType.FORMULA) {
                targetCell.setCellFormula(sourceCell.getCellFormula());
            } else {
                targetCell.setCellValue("");
            }
        }
    }

    public Sheet validateTemplate(String templatePath) throws IOException {
        // Check that the filename ends with ".xlsx"
        if (!templatePath.endsWith(".xlsx")) {
            throw new RuntimeException("Invalid file extension");
        }

        // Load template workbook
        Path path = Paths.get(templatePath);
        XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(path));

        // Check that the workbook has only one worksheet
        int numSheets = workbook.getNumberOfSheets();
        if (numSheets != 1) {
            throw new RuntimeException("Invalid number of worksheets");
        }

        return workbook.getSheetAt(0);
    }

    public Map<Integer, String> getHeadersFromFirstRow(Sheet templateWorksheet) {
        Map<Integer, String> headers = new HashMap<>();
        Row headerRow = templateWorksheet.getRow(HEADER_ROW);
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            String header = cell.getStringCellValue();
            headers.put(i, header);
        }
        return headers;
    }

    private static boolean validateHeaders(Map<Integer, String> templateHeaders, Sheet worksheet) {
        Row headerRow = worksheet.getRow(0);
        int headerCount = headerRow.getLastCellNum();
        if (headerCount != templateHeaders.size()) {
            return false;
        }
        for (int i = 0; i < headerCount; i++) {
            Cell cell = headerRow.getCell(i);
            if (!templateHeaders.containsKey(i)) {
                return false;
            }
            String expectedHeader = templateHeaders.get(i);
            String actualHeader = cell.getStringCellValue().trim();
            if (!actualHeader.equals(expectedHeader)) {
                return false;
            }
        }
        return true;
    }
}
