package services;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class XlsService {
    private static final String SHEET_STUDENTS_FILTERED = "Students Filtered";
    private static final String SHEET_STUDENTS_MERGED = "Merged with Historic, max marks only";
    private static final String SHEET_UNIQUE_STUDENTS = "Unique Students";
    private static final String SHEET_DUPLICATES = "Duplicates";
    private static final String SHEET_TES_CALCULATION = "TES Calculation";
    private static final String SHEET_HISTORICMARKSONLY = "Historic attempts at subjects";
    private static final String SHEET_ACTIVEOLD_STUDENTS = "All Active Historic Results";
    private static final String SHEET_HISTORICDUPS_STUDENTS = "Historic Duplicate Marks";
    private static final String SHEET_STATS = "Analysis Awarded Marks";

    private final String fileName;
    private final String sourcePath;
    private final String destinationPath;
    private final String fileNameClean;
    private final String sourcePathComplete;
    private final String backupPathComplete;
    private final String transformedPathComplete;
    private final String tesPathComplete;
    private final String historicPathComplete;

    private FileInputStream fileInput;
    private HSSFWorkbook workbook;

    public XlsService(String fileName, String sourcePath, String destinationPath) {
        this.fileName = fileName;
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;

        fileNameClean = fileName.replace(".xls","");
        sourcePathComplete = sourcePath;
        backupPathComplete = destinationPath + fileNameClean + "_backup.xls";
        transformedPathComplete = destinationPath + fileNameClean + "_sorted.xls";
        tesPathComplete = destinationPath + fileNameClean + "_tes.xls";
        historicPathComplete = destinationPath + fileNameClean + "_historic.xls";
    }

    public void openXls() throws IOException {
        fileInput = new FileInputStream(sourcePathComplete);
        workbook = new HSSFWorkbook(fileInput);
    }

    public void closeXls() throws IOException {
        fileInput.close();
    }

    public void saveBackup() throws IOException {
        FileOutputStream fileOut = new FileOutputStream(backupPathComplete);
        workbook.write(fileOut);
        fileOut.close();
    }

    public void saveSortedChangesToXls() throws IOException {
        FileOutputStream fileOut = new FileOutputStream(transformedPathComplete);
        workbook.write(fileOut);
        fileOut.close();
    }

    public void saveTesChangesToXls() throws IOException {
        FileOutputStream fileOut = new FileOutputStream(tesPathComplete);
        workbook.write(fileOut);
        fileOut.close();
    }

    public void saveHistoricChangesToXls() throws IOException {
        FileOutputStream fileOut = new FileOutputStream(historicPathComplete);
        workbook.write(fileOut);
        fileOut.close();
    }

    public HSSFSheet getStudentSheet() {
        return workbook.getSheetAt(1);
    }

    public HSSFSheet getHistoricSheet() {
        return workbook.getSheetAt(2);
    }

    public HSSFSheet getDuplicatesSheet() {
        return findOrCreateSheet(SHEET_DUPLICATES);
    }

    public HSSFSheet getHistoricDuplicatesSheet() {
        return findOrCreateSheet(SHEET_HISTORICDUPS_STUDENTS);
    }

    public HSSFSheet getStudentsFilteredSheet() {
        return findOrCreateSheet(SHEET_STUDENTS_FILTERED);
    }

    public HSSFSheet getUniqueStudentsSheet() {
        return findOrCreateSheet(SHEET_UNIQUE_STUDENTS);
    }

    public HSSFSheet getStatsSheet() {
        return findOrCreateSheet(SHEET_STATS);
    }

    public HSSFSheet getMergedStudentsSheet() {
        return findOrCreateSheet(SHEET_STUDENTS_MERGED);
    }

    public HSSFSheet getActiveHistoricStudentsSheet() {
        return findOrCreateSheet(SHEET_ACTIVEOLD_STUDENTS);
    }

    public HSSFSheet getTesCalculationSheet() {
        return findOrCreateSheet(SHEET_TES_CALCULATION);
    }

    public HSSFSheet getHistoricmarksSheet() {
        return findOrCreateSheet(SHEET_HISTORICMARKSONLY);
    }

    public HSSFSheet getUnitSheet(String unit) {
        return findOrCreateSheet(unit);
    }

    private HSSFSheet findOrCreateSheet(String name) {
        HSSFSheet newSheet = workbook.getSheet(name);
        if(workbook.getSheet(name)==null) {
            newSheet = workbook.createSheet(name);
        }
        return newSheet;
    }
}
