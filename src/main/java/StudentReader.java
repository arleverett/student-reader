import models.Student;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import services.XlsService;
import utils.StudentUtils;

import javax.print.DocFlavor;
import java.io.IOException;
import java.util.*;

public class StudentReader {
    private static final String CELL_POSITION = "Position";

    private XlsService xlsService;

    public StudentReader(String fileName, String sourcePath, String destinationPath) throws IOException {
        xlsService = new XlsService(fileName, sourcePath, destinationPath);

        /*long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime-startTime) + "ms");*/
    }
    /*
    public void executehistoricmarks() throws IOException {
        xlsService.openXls();
        List<Student> students = readStudentsFromSheet(xlsService.getStudentSheet());
        List<Student> Historicstudents = readStudentsFromSheet(xlsService.getHistoricSheet());
        xlsService.closeXls();
        xlsService.saveBackup();
        List<Student> filteredFirstPass = StudentUtils.getStudentsWithallDuplicatesRemoved(students);
        List<Student> filteredFirstPassHistoric = StudentUtils.getStudentsWithallDuplicatesRemoved(Historicstudents);
        List<Student> uniqueStudents = StudentUtils.getUniqueStudentIDs(filteredFirstPass);
        List<Student> MatchedHistoricID = StudentUtils.getmatchedIDs(uniqueStudents, filteredFirstPassHistoric);
        List<String> allunits = StudentUtils.getAllUnits(filteredFirstPass);
        allunits.remove(0);
        List<Student> duplicatedhistoricStudents = StudentUtils.getDuplicateStudentshistoric(filteredFirstPass, filteredFirstPassHistoric);
        List<Student> historicmarksonly = StudentUtils.gethistoricmarksonly(filteredFirstPass, filteredFirstPassHistoric);
        writeSheetUniqueStudents(uniqueStudents);
        writeActiveHistoricStudents(MatchedHistoricID);
        writeHistoricDuplicates(filteredFirstPass, duplicatedhistoricStudents);
        writehistoricmarks (historicmarksonly);
        allunits.forEach(unit -> {
            writeSheetUnit(unit, historicmarksonly);
        });
        xlsService.saveHistoricChangesToXls();
    }
    */

    public void executeStudentTes() throws IOException {
        xlsService.openXls();
        List<Student> students = readStudentsFromSheet(xlsService.getStudentSheet());
        List<Student> Historicstudents = readStudentsFromSheet(xlsService.getHistoricSheet());
        xlsService.closeXls();
        xlsService.saveBackup();
        List<Student> filteredFirstPass = StudentUtils.getStudentsWithallDuplicatesRemoved(students);
        List<Student> filteredFirstPassHistoric = StudentUtils.getStudentsWithallDuplicatesRemoved(Historicstudents);
        List<Student> uniqueStudents = StudentUtils.getUniqueStudentIDs(filteredFirstPass);
        List<Student> MatchedHistoricID = StudentUtils.getmatchedIDs(uniqueStudents, filteredFirstPassHistoric);
        MatchedHistoricID.remove(0);
//        List<Student> historicmarksonly = StudentUtils.gethistoricmarksonly(filteredFirstPass, filteredFirstPassHistoric);
        List<Student> mergedstudents = StudentUtils.mergestudents(filteredFirstPass, MatchedHistoricID);
        List<Student> filteredmerged = StudentUtils.getStudentsWithallDuplicatesRemoved(mergedstudents);
        Map<String, Integer> units = StudentUtils.countUnits(filteredmerged);
//        List<Student> filteredStudents = StudentUtils.getUniqueStudentIDs(filteredmerged);
        List<String> allunits = StudentUtils.getAllUnits(filteredmerged);
        allunits.remove(0);
        writeSheetUniqueStudents(uniqueStudents);
        writeSheetMergedFiltered(filteredmerged);
        writeSheetTesCalculation(uniqueStudents, units, allunits, filteredmerged);
        xlsService.saveTesChangesToXls();
    }

    public void executeStudentTransform() throws IOException {
        xlsService.openXls();
        List<Student> students = readStudentsFromSheet(xlsService.getStudentSheet());
        xlsService.closeXls();
        xlsService.saveBackup();
        Map<Integer, Student> duplicatedStudents = StudentUtils.getDuplicateStudents(students);
        List<Student> filteredStudents = StudentUtils.getStudentsWithallDuplicatesRemoved(students);
        List<Student> uniqueStudents = StudentUtils.getUniqueStudentIDs(filteredStudents);
        List<String> units = StudentUtils.getAllUnits(students);
        //note units must contain TPCEGL002, otherwise edit the code for the function getAllUnits
        units.remove(0);
        Map<String, Map<String, Double>> allmarks = StudentUtils.getallmarksasmap(filteredStudents, uniqueStudents, units);
        Map<String, Map<String, Double>> newallmarks = StudentUtils.getremoveonlyonesubject(allmarks);
        // newall marks removes scores less than 15 and students with only one subject as this cannot be used for analysis

        //the printouts below are for debugging
        Map<String, List<Double>> marksperstudent = StudentUtils.getstudentawardedmarksformeancalc(newallmarks, units, uniqueStudents);
        //System.out.println("1");
        Map<String, Double> studentaverages = StudentUtils.getmapavg(marksperstudent);
        //System.out.println("2");
        Map<String, List<String>> allstudentsperunit = StudentUtils.getallstudentsperunit(filteredStudents, units);
        //System.out.println("3");
        Map<String, List<Double>> studentavgperunit = StudentUtils.getstudentsavgperunit(studentaverages, allstudentsperunit, units);
        //System.out.println("4");
        Map<String, List<Double>> unitmarksordered = StudentUtils.getunitmarkssameorder(studentavgperunit, allstudentsperunit, newallmarks, units);
        Map<String, Double> unitaverages = StudentUtils.getmapavg(unitmarksordered);
        //System.out.println("6");
        Map<String, Double> Tjperunit = StudentUtils.getmapavg(studentavgperunit);
        //System.out.println("7");
        Map<String, Double> stdevs = StudentUtils.calcStdDevmap(unitmarksordered, true);
        //System.out.println("8");
        Map<String, List<Double>> zscores = StudentUtils.getzscores(filteredStudents, uniqueStudents, units, stdevs, unitaverages);
        //This is the least squares analysis
        Map<String, List<Double>> paramatersperunit = new HashMap<>();
        for (String unit : units) {
            List<Double> awarded = unitmarksordered.get(unit);
            List<Double> tjlist = studentavgperunit.get(unit);
            List<Double> params = StudentUtils.linearregression(awarded, tjlist);
            paramatersperunit.put(unit, params);
            double new100 = params.get(1)*100+params.get(0);
        }
        //end least squares analysis
        /*begin zscore standardisation
        //end zscore standardisation*/
        Map<String, Map<String, String>> allsunitscrossed = new HashMap<>();
        for (String unit : units) {
            Map<String, String> crosscheckmarksinunit = StudentUtils.crosscheckunit(filteredStudents, units, unit);
            allsunitscrossed.put(unit, crosscheckmarksinunit);
        }
        writeSheetStats(units, unitaverages, allsunitscrossed, Tjperunit, stdevs, paramatersperunit);
        writeSheetDuplicates(students, duplicatedStudents);
        writeSheetUniqueStudents(uniqueStudents);
        writeSheetStudentsFiltered(filteredStudents);
        units.forEach(unit -> writeSheetUnit(unit, filteredStudents));
        xlsService.saveSortedChangesToXls();
    }

    private void writeSheetDuplicates(List<Student> students, Map<Integer, Student> duplicatedStudents) {
        HSSFSheet newSheet = xlsService.getDuplicatesSheet();
        int rowNumber = 0;
        Row row = newSheet.createRow(rowNumber++);
        writeStudentToSheet(row, students.get(0), true);
        row.createCell(11).setCellValue(CELL_POSITION);
        for (Map.Entry<Integer, Student> entry : duplicatedStudents.entrySet()) {
            row = newSheet.createRow(rowNumber);
            writeStudentToSheet(row, entry.getValue(), false);
            row.createCell(11).setCellValue(entry.getKey()+1);
            rowNumber++;
        }
    }

    private void writeHistoricDuplicates(List<Student> students, List<Student> duplicatedStudents) {
        HSSFSheet newSheet = xlsService.getHistoricDuplicatesSheet();
        int rowNumber = 0;
        Row row = newSheet.createRow(rowNumber++);
        writeStudentToSheet(row, students.get(0), true);
        for (Student student : duplicatedStudents) {
                row = newSheet.createRow(rowNumber);
                writeStudentToSheet(row, student, false);
                rowNumber++;
        }
    }

    private void writehistoricmarks(List<Student> duplicatedStudents) {
        HSSFSheet newSheet = xlsService.getHistoricmarksSheet();
        int rowNumber = 0;
        Row row;
        for (Student student : duplicatedStudents) {
            if (rowNumber == 0) {
                row = newSheet.createRow(rowNumber);
                writeStudentToSheet(row, student, true);
                rowNumber++;
            } else {
                row = newSheet.createRow(rowNumber);
                writeStudentToSheet(row, student, false);
                rowNumber++;
            }
        }
    }

    private void writeSheetStudentsFiltered(List<Student> filteredStudents) {
        HSSFSheet newSheet = xlsService.getStudentsFilteredSheet();
        int rowNumber = 0;
        Row row;
        for (Student student : filteredStudents) {
            if (rowNumber == 0) {
                row = newSheet.createRow(rowNumber);
                writeStudentToSheet(row, student, true);
                rowNumber++;
            } else {
                row = newSheet.createRow(rowNumber);
                writeStudentToSheet(row, student, false);
                rowNumber++;
            }
        }
    }

    private void writeSheetMergedFiltered(List<Student> filteredStudents) {
        HSSFSheet newSheet = xlsService.getMergedStudentsSheet();
        int rowNumber = 0;
        Row row;
        for (Student student : filteredStudents) {
            if (rowNumber == 0) {
                row = newSheet.createRow(rowNumber);
                writeStudentToSheet(row, student, true);
                rowNumber++;
            } else {
                row = newSheet.createRow(rowNumber);
                writeStudentToSheet(row, student, false);
                rowNumber++;
            }
        }
    }

    private void writeSheetTesCalculation(List<Student> uniquestudents, Map<String, Integer> units, List<String> allunits, List<Student> filteredFirstPass) {
        HSSFSheet newSheet = xlsService.getTesCalculationSheet();

        int rowNumber = 0;
        Row row;
        for (Student student : uniquestudents) {
            row = newSheet.createRow(rowNumber);
            if(rowNumber == 0) {
                List<String> marks = StudentUtils.getStudentmarkstes(filteredFirstPass, student, allunits);
                Map<String, Boolean> Passfail = StudentUtils.marksandpass(filteredFirstPass, student, allunits);
                writeTes(row, student, units.get(student.getStudentNo()), allunits, marks, true, Passfail);
                rowNumber++;
            } else {
                List<String> marks = StudentUtils.getStudentmarkstes(filteredFirstPass, student, allunits);
                Map<String, Boolean> Passfail = StudentUtils.marksandpass(filteredFirstPass, student, allunits);
                writeTes(row, student, units.get(student.getStudentNo()), allunits, marks, false, Passfail);
                rowNumber++;
            }
        }
    }

    private void writeSheetUniqueStudents(List<Student> filteredStudents) {
        HSSFSheet newSheet = xlsService.getUniqueStudentsSheet();
        int rowNumber = 0;
        Row row;
        for (Student student : filteredStudents) {
            if (rowNumber == 0) {
                row = newSheet.createRow(rowNumber);
                writeStudentToSheet(row, student, true);
                rowNumber++;
            } else {
                row = newSheet.createRow(rowNumber);
                writeStudentToSheet(row, student, false);
                rowNumber++;
            }
        }
    }

    private void writeSheetStats(List<String> allunits, Map<String, Double> averages, Map<String, Map<String, String>> allunitscrosscheck, Map<String, Double> Tj, Map<String, Double> stdevs, Map<String, List<Double>> paramatersperunit) {
        HSSFSheet newSheet = xlsService.getStatsSheet();
        int rowNumber = 0;
        boolean heading;
        boolean table;
        Row row;
        while (rowNumber <= allunits.size()*2+1) {
            heading = rowNumber == 0 | rowNumber == allunits.size()+1;
            table = rowNumber >= allunits.size()+1;
                row = newSheet.createRow(rowNumber);
                writeStatstosheet(row, rowNumber, heading, allunits, averages, Tj, stdevs, paramatersperunit, table, allunitscrosscheck);
                rowNumber++;
        }
    }

    private void writeActiveHistoricStudents(List<Student> filteredStudents) {
        HSSFSheet newSheet = xlsService.getActiveHistoricStudentsSheet();
        int rowNumber = 0;
        Row row;
        for (Student student : filteredStudents) {
            if (rowNumber == 0) {
                row = newSheet.createRow(rowNumber);
                writeStudentToSheet(row, student, true);
                rowNumber++;
            } else {
                row = newSheet.createRow(rowNumber);
                writeStudentToSheet(row, student, false);
                rowNumber++;
            }
        }
    }

    private void writeSheetUnit(String unit, List<Student> filteredStudents) {
        HSSFSheet newSheet = xlsService.getUnitSheet(unit);
        int rowNumber = 0;
        Row row = newSheet.createRow(rowNumber++);
        writeUnitToSheet(row, filteredStudents.get(0), true);
        for (Student student : filteredStudents) {
            if(unit.equals(student.getUnit())) {
                row = newSheet.createRow(rowNumber);
                writeUnitToSheet(row, student, false);
                rowNumber++;
            }
        }
    }

    private List<Student> readStudentsFromSheet(HSSFSheet sheet) throws IOException {
        List<Student> students = new ArrayList<>();
        for(Row row : sheet) {
            List<String> values = new ArrayList<>();
            for(int i=0; i<row.getLastCellNum(); ++i) {
                Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
                addCellValue(values, cell);
            }
            students.add(new Student(values));
        }
        return students;
    }

    private void writeStudentToSheet(Row row, Student student, boolean heading) {
        int i = 0;
        if (heading) {
            row.createCell(i++).setCellValue(student.getStudentNo());
            row.createCell(i++).setCellValue(student.getFirst());
            row.createCell(i++).setCellValue(student.getFamily());
            row.createCell(i++).setCellValue(student.getInstanceNumber());
            row.createCell(i++).setCellValue(student.getCollege());
            row.createCell(i++).setCellValue(student.getUnit());
            row.createCell(i++).setCellValue(student.getAwarded());
            row.createCell(i++).setCellValue(student.getOriginalScaled());
            row.createCell(i++).setCellValue(student.getScaled());
            row.createCell(i++).setCellValue(student.getParamA());
            row.createCell(i).setCellValue(student.getParamB());
        } else {
            row.createCell(i++).setCellValue(student.getStudentNo());
            row.createCell(i++).setCellValue(student.getFirst());
            row.createCell(i++).setCellValue(student.getFamily());
            row.createCell(i++).setCellValue(student.getInstanceNumber());
            row.createCell(i++).setCellValue(student.getCollege());
            row.createCell(i++).setCellValue(student.getUnit());
            row.createCell(i++).setCellValue(Double.parseDouble(student.getAwarded()));
            row.createCell(i++).setCellValue(Double.parseDouble(student.getOriginalScaled()));
            row.createCell(i++).setCellValue(Double.parseDouble(student.getScaled()));
            row.createCell(i++).setCellValue(student.getParamA());
            row.createCell(i).setCellValue(student.getParamB());
        }
    }

    private void writeUnitToSheet(Row row, Student student, boolean heading) {
        int i = 0;
        if (heading) {
            row.createCell(i++).setCellValue(student.getStudentNo());
            row.createCell(i++).setCellValue(student.getFirst());
            row.createCell(i++).setCellValue(student.getFamily());
            row.createCell(i++).setCellValue(student.getInstanceNumber());
            row.createCell(i++).setCellValue(student.getCollege());
            row.createCell(i++).setCellValue(student.getUnit());
            row.createCell(i++).setCellValue(student.getAwarded());
            row.createCell(i++).setCellValue(student.getOriginalScaled());
            row.createCell(i++).setCellValue("diff");
            row.createCell(i++).setCellValue("New Scaled");
            row.createCell(i).setCellValue("diff");
        } else {
            row.createCell(i++).setCellValue(student.getStudentNo());
            row.createCell(i++).setCellValue(student.getFirst());
            row.createCell(i++).setCellValue(student.getFamily());
            row.createCell(i++).setCellValue(student.getInstanceNumber());
            row.createCell(i++).setCellValue(student.getCollege());
            row.createCell(i++).setCellValue(student.getUnit());
            row.createCell(i++).setCellValue(Double.parseDouble(student.getAwarded()));
            row.createCell(i++).setCellValue(Double.parseDouble(student.getOriginalScaled()));
            double diff = Double.parseDouble(student.getOriginalScaled())-Double.parseDouble(student.getAwarded());
            row.createCell(i++).setCellValue(diff);
            row.createCell(i++).setCellValue("");
            row.createCell(i).setCellFormula("J2-G2");
        }
    }

    private void writeStatstosheet (Row row, int rowNumber, boolean heading, List<String> allunits, Map<String, Double> averages, Map<String, Double> Tj, Map<String, Double> stdevs, Map<String, List<Double>> paramatersperunit, boolean table, Map<String, Map<String, String>> unitcrosscheck) {
        int i = 0;
        if (heading & !table) {
            row.createCell(i++).setCellValue("Unit");
            row.createCell(i++).setCellValue("Unit average");
            row.createCell(i++).setCellValue("Tj");
            row.createCell(i++).setCellValue("Amt to move mean");
            row.createCell(i++).setCellValue("stdevs");
            row.createCell(i++).setCellValue("Suggested A");
            row.createCell(i).setCellValue("Suggested B");
        } else if (!table) {
            int whichunit = rowNumber-1;
            row.createCell(i++).setCellValue(allunits.get(whichunit));
            row.createCell(i++).setCellValue(averages.get(allunits.get(whichunit)));
            row.createCell(i++).setCellValue(Tj.get(allunits.get(whichunit)));
            row.createCell(i++).setCellValue(Tj.get(allunits.get(whichunit))-averages.get(allunits.get(whichunit)));
            row.createCell(i++).setCellValue(stdevs.get(allunits.get(whichunit)));
            List<Double> params = paramatersperunit.get(allunits.get(whichunit));
            row.createCell(i++).setCellValue(params.get(0));
            row.createCell(i).setCellValue(params.get(1));
        } else if (heading) {
            row.createCell(i++).setCellValue("");
            for (String unit : allunits) {
                row.createCell(i++).setCellValue(unit);
            }
        } else {
            int whichunit = rowNumber-(allunits.size()+2);
            row.createCell(i++).setCellValue(allunits.get(whichunit));
            Map<String, String> crosscheck = unitcrosscheck.get(allunits.get(whichunit));

            for (String unit : allunits) {
                row.createCell(i++).setCellValue(crosscheck.get(unit));
            }
        }
    }

    private void writeTes(Row row, Student student, Integer unitcount, List <String> allunits, List<String> marks, boolean heading, Map<String, Boolean> passfail) {

        int i = 0;
        row.createCell(i++).setCellValue(student.getStudentNo());
        row.createCell(i++).setCellValue(student.getFirst());
        row.createCell(i++).setCellValue(student.getFamily());
        row.createCell(i++).setCellValue(student.getCollege());

        if (heading) {
            row.createCell(i++).setCellValue("TES");
            row.createCell(i++).setCellValue("Units attempted");
            row.createCell(i++).setCellValue("Units Passed");
            for (int k = 0; k < allunits.size(); k++) {
                row.createCell(i++).setCellValue(allunits.get(k));
            }
        } else {
            if(StudentUtils.TESeligible(unitcount, passfail, marks)) {
                row.createCell(i++).setCellValue(StudentUtils.calctes(marks));
            } else {
                row.createCell(i++).setCellValue("N/A");
            }
            row.createCell(i++).setCellValue(unitcount.toString());
            int t = 0;
            for (boolean item : passfail.values()) {
                if(item) {
                   t++;
                }
            }
            row.createCell(i++).setCellValue(Integer.toString(t));
            for (int n = 0; n < marks.size(); n++) {
                if (passfail.get(marks.get(n)) == null) {
                    row.createCell(i++).setCellValue(marks.get(n));
                } else if (passfail.get(marks.get(n))) {
                    row.createCell(i++).setCellValue(marks.get(n));
                } else {
                    row.createCell(i).setCellValue(marks.get(n)+" F");
                    i++;
                }
            }
        }
    }

    private static void addCellValue(List<String> values, Cell cell) {
        switch(cell.getCellType()) {
        case Cell.CELL_TYPE_NUMERIC:
            values.add(Double.toString(cell.getNumericCellValue())); break;
        case Cell.CELL_TYPE_FORMULA:
            if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_ERROR) {
                System.out.println("Formula Cell in error");
                System.out.println(cell.getErrorCellValue());
                values.add(""); break;
            }
                values.add(Double.toString(cell.getNumericCellValue())); break;
        case Cell.CELL_TYPE_STRING: values.add(cell.getStringCellValue()); break;
        case Cell.CELL_TYPE_BLANK:
            values.add(""); break;
        case Cell.CELL_TYPE_ERROR:
            values.add(""); break;
        default:
            System.out.printf("%s - %s r:%s h:%s%n", cell.getCellComment(), cell.getCellType(), cell.getRowIndex(), cell.getColumnIndex());
            values.add(""); break;
        }
    }
}
