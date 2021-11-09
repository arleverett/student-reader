package utils;

import models.Student;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class StudentUtils {
    private StudentUtils(){}

    public static boolean studentsAreSame(Student s, Student ss) {
        return s.getStudentNo().equals(ss.getStudentNo()) && s.getUnit().equals(ss.getUnit());
    }

    public static boolean IDsAreSame(Student s, Student ss) {
        return s.getStudentNo().equals(ss.getStudentNo());
    }

    public static boolean studentisinunit(Student s, String units) {
        return s.getUnit().equals(units);
    }

    public static List<Student> removeCTandWN(List<Student> Students) {
        Students.removeIf(Currentstudent -> Currentstudent.getAwarded().equals("WN") | Currentstudent.getAwarded().equals("CT") | Currentstudent.getAwarded().equals("NS"));
        return Students;
    }

    public static List<Double> setmarkstodouble(List<String> marks) {
        List<Double> numeric = new ArrayList<>();
        for (int k=0; k<marks.size(); k++) {
            numeric.add((double) 0);
        }
        for (int i=0; i<marks.size(); i++) {
            if (marks.get(i).equals(" ") | marks.get(i).equals("") | marks.get(i).isEmpty()) {
                numeric.set(i, (double) 0);
            } else {
                numeric.set(i,Double.parseDouble(marks.get(i)));
            }
        }
        return numeric;
    }

    public static double calcmean (List<String> marks) {
       List<Double> marksdouble = setmarkstodouble(marks);
        return marksdouble.stream().mapToDouble(val -> val).average().orElse(0.0);
    }

    public static double calcmeandoubles (List<Double> marksdouble) {
        return marksdouble.stream().mapToDouble(val -> val).average().orElse(0.0);
    }

    public static double calcStdDev (List<Double> marks, boolean population) {
        if (marks.size() <= 1) {
            return 0.0;
        }
        double mean = calcmeandoubles(marks);
        double sum = 0;
        double N = marks.size();
        for (int i = 0; i< marks.size(); i++) {
            sum += Math.pow((marks.get(i) - mean),2);
        }
        double argument;
        if (population) {
            argument = sum / N;
        } else {
            argument = sum / (N - 1);
        }
        return Math.sqrt(argument);
    }

    public static Map<String, Double> calcStdDevmap (Map<String, List<Double>> marks, boolean population) {
        Map<String, Double> means = getmapavg(marks);
        Map<String, Double> allstdevs = new HashMap<>();
        for (String unit : marks.keySet()) {
            double mean = means.get(unit);
            List<Double> markslist = marks.get(unit);
            double stdev = calcStdDev(markslist, population);
            allstdevs.put(unit, stdev);
        }
        return allstdevs;
    }

    public static boolean ismarkapass(Student check) {
        double passmark = 50;
        if (check.getAwarded().equals("") | check.getAwarded().equals(" ")) {
            return false;
        } else if (check.getAwardedAsDouble() >= passmark) {
            return true;
        } else {
            return false;
        }
    }

    public static Map<String, Double> getmapavg (Map<String, List<Double>> marksper) {
        Map<String, Double> markavg = new HashMap<>();
        for (String key : marksper.keySet()) {
            List<Double> marks = marksper.get(key);
            double avg = calcmeandoubles(marks);
            BigDecimal rounded = new BigDecimal(avg).setScale(3, RoundingMode.HALF_UP);
            markavg.put(key, rounded.doubleValue());
        }
        return markavg;
    }

    public static Map<String, List<Double>> getzscores (List<Student> filteredstudents, List<Student> uniqueID, List<String> allunits, Map<String, Double> stdevs, Map<String, Double> means) {
        Map<String, List<Double>> allz = new HashMap<>();
        for (Student ID : uniqueID) {
            List<Double> zscores = new ArrayList<>();
            for (String unit : allunits) {
                int count = 0;
                double mark = 0;
                double z = 0;
                for (Student student : filteredstudents) {
                    if (student.getStudentNo().equals(ID.getStudentNo())) {
                        if (student.getUnit().equals(unit)) {
                            count++;
                            mark = (student.getAwardedAsDouble());
                            z = (mark - means.get(unit))/stdevs.get(unit);
                        }
                    }
                }
                if (count >= 1) {
                    zscores.add(z);
                } else {
                    zscores.add((double) 0);
                }
            }
            allz.put(ID.getStudentNo(), zscores);
        }
        return allz;
    }

    public static List<Double> linearregression (List<Double> xval, List<Double> yval) {
        double sumx = 0;
        double sumy = 0;
        double sumxsqr = 0;
        double sumysqr = 0;
        double sumxy = 0;
        int n = xval.size();
        for (double x : xval) {
            sumx += x;
            sumxsqr += Math.pow(x, 2);
        }
        for (double y : yval) {
            sumy += y;
            sumysqr += Math.pow(y, 2);
        }
        for (int i = 0; i< xval.size(); i++) {
            sumxy += xval.get(i)* yval.get(i);
        }
        double a;
        double b;
        a = (sumy*sumxsqr-sumx*sumxy)/(n*sumxsqr-Math.pow(sumx, 2));
        b = (n*sumxy-sumx*sumy)/(n*sumxsqr-Math.pow(sumx, 2));
        List<Double> params = new ArrayList<>();
        params.add(a);
        params.add(b);
        return params;
    }

    public static boolean isIDunitpresent(List<Student> duplicates, Student checkID, boolean IDonly) {
        if (IDonly) {
            if (duplicates.size() == 0) {
                return false;
            } else {
                for (int i = 0; i < duplicates.size(); i++) {
                    if (duplicates.get(i).getStudentNo().equalsIgnoreCase(checkID.getStudentNo())) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            if (duplicates.size() == 0) {
                return false;
            } else {
                for (int i = 0; i < duplicates.size(); i++) {
                    if (studentsAreSame(duplicates.get(i), checkID)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static Map<Integer,Student> getDuplicateStudents(List<Student> students) {
        Map<Integer, Student> duplicatedStudents = new TreeMap<>();
        for(int i=0; i<students.size(); ++i) {
            Student currentStudent = students.get(i);
            for(int j=1+(i); j<students.size(); ++j) {
                Student nextStudent = students.get(j);
                if(StudentUtils.studentsAreSame(currentStudent,nextStudent)) {
                    duplicatedStudents.put(i,currentStudent);
                    duplicatedStudents.put(j,nextStudent);
                }
            }
        }
        return duplicatedStudents;
    }

    public static List<Student> getDuplicateStudentshistoric(List<Student> students, List<Student> Historicstudents) {
        List<Student> duplicatedStudents = new ArrayList<>();
        for(int i=0; i<students.size(); ++i) {
            int count = 0;
            Student currentStudent = students.get(i);
            for(int j=1; j<Historicstudents.size(); ++j) {
                Student nextStudent = Historicstudents.get(j);
                if(StudentUtils.studentsAreSame(currentStudent,nextStudent) && count == 0) {
                    duplicatedStudents.add(currentStudent);
                    duplicatedStudents.add(nextStudent);
                    count++;
                } else if (StudentUtils.studentsAreSame(currentStudent,nextStudent)) {
                    duplicatedStudents.add(nextStudent);
                }
            }
        }
        return duplicatedStudents;
    }

    public static List<Student> gethistoricmarksonly(List<Student> students, List<Student> Historicstudents) {
        List<Student> historicmarks = new ArrayList<>();
        historicmarks.add(students.get(0));
//        int count = 1;
        for(int i=0; i<students.size(); ++i) {
            Student currentStudent = students.get(i);
            for(int j=1; j<Historicstudents.size(); ++j) {
                Student nextStudent = Historicstudents.get(j);
                if(StudentUtils.studentsAreSame(currentStudent,nextStudent)) {
                    historicmarks.add(nextStudent);
//                    count++;
                }
            }
        }
        return historicmarks;
    }

    public static List<Student> getmatchedIDs(List<Student> students, List<Student> Historicstudents) {
        List<Student> historicmarks = new ArrayList<>();
        historicmarks.add(students.get(0));
        int count = 1;
        for(int i=0; i<students.size(); ++i) {
            Student currentStudent = students.get(i);
            for(int j=1; j<Historicstudents.size(); j++) {
                Student nextStudent = Historicstudents.get(j);
                if(StudentUtils.IDsAreSame(currentStudent,nextStudent)) {
                    historicmarks.add(Historicstudents.get(j));
                    count++;
                }
            }
        }
        return historicmarks;
    }

    public static ArrayList<String> getStudentmarkstes(List<Student> students, Student studentcheck, List<String> units) {
        ArrayList<String> marks = new ArrayList<>();
        boolean IDknown = false;
        for (int p = 0; p<units.size(); p++) {
            marks.add("");
        }
        for(int i=0; i<students.size(); ++i) {
            Student currentStudent = students.get(i);
            if(currentStudent.getStudentNo().equals(studentcheck.getStudentNo())) {
                if (!IDknown) {
                    IDknown = true;
                }
                for(int k=0; k<units.size(); k++) {
                    if (StudentUtils.studentisinunit(currentStudent, units.get(k))) {
                        marks.set(k, currentStudent.getScaled());
                    }
                }
            }
        }
        return marks;
    }

    public static Map<String, Map<String, Double>> getallmarksasmap(List<Student> students, List<Student> uniqueID, List<String> units) {
        Map<String, Map<String, Double>> allmarks = new HashMap<>();
        //System.out.println(units);
        for (Student ID : uniqueID) {
            Map<String, Double> marksperID = new HashMap<>();
            List<Student> entriesofID = new ArrayList<>();
            for (Student student : students) {
                if (ID.getStudentNo().equals(student.getStudentNo())) {
                    entriesofID.add(student);
                }
                //System.out.println(student.getStudentNo());
            }
            for (String unit : units) {
                for (Student entry : entriesofID) {
                    if (entry.getUnit().equals(unit)) {
                        if (entry.getAwardedAsDouble() >= 15) {
                            marksperID.put(unit, entry.getAwardedAsDouble());
                        }
                        break;
                    } else {
                        marksperID.put(unit, (double) 0);
                    }
                }
            }
            allmarks.put(ID.getStudentNo(), marksperID);
        }
        return allmarks;
    }

    public static Map<String, Map<String, Double>> getremoveonlyonesubject(Map<String, Map<String, Double>> allmarks) {
        Map<String, Map<String, Double>> marks = allmarks;
        List<String> IDstoremove= new ArrayList<>();
        for (String ID : marks.keySet()) {
            Map<String, Double> entries = allmarks.get(ID);
            int count = 0;
            for (String unit : entries.keySet()) {
                if (entries.get(unit) >= 1) {
                    count++;
                }
            }
//            System.out.println(count);
            if (count <= 1) {
                IDstoremove.add(ID);
            }
        }

        for (String delID : IDstoremove) {
            marks.remove(delID);
        }
        return marks;
    }

    public static Map<String, List<String>> getallstudentsperunit(List<Student> students, List<String> units) {
        Map<String, List<String>> allstudents = new HashMap<>();
        for (String unit : units) {
            List<String> studentlist = new ArrayList<>();
            for (Student student : students) {
                //remove >= 15 if not required
                if (unit.equals(student.getUnit())) {
                    if (student.getAwardedAsDouble() >= 15) {
                        studentlist.add(student.getStudentNo());
                    }
                }
            }
            allstudents.put(unit, studentlist);
        }

        return allstudents;
    }

    public static Map<String, List<Double>> getstudentsavgperunit(Map<String, Double> studentavgs, Map<String, List<String>> allstudentsperunit, List<String> units) {
        Map<String, List<Double>> studentavgperunit = new HashMap<>();
        List<Double> averages= new ArrayList<>();
        for (String unit : units) {
            List<String> IDlist = allstudentsperunit.get(unit);
            List<Double> Tjforunit = new ArrayList<>();
            for (String ID : IDlist) {
                if (studentavgs.get(ID) == null) {
                    continue;
                }
                double avgofID = studentavgs.get(ID);
                Tjforunit.add(avgofID);
            }
            studentavgperunit.put(unit, Tjforunit);
        }
        return studentavgperunit;
    }

    public static Map<String, List<Double>> getunitmarkssameorder(Map<String, List<Double>> studentavgsperunit,  Map<String, List<String>> allstudentsperunit, Map<String, Map<String, Double>> allmarksasmap, List<String> units) {
        Map<String, List<Double>> marksperunit = new HashMap<>();
        for (String unit : units) {
            List<String> IDlist = allstudentsperunit.get(unit);
            List<Double> marksinunit = new ArrayList<>();
            for (String ID : IDlist) {
                if (allmarksasmap.get(ID) == null) {
                    continue;
                }
                Map<String, Double> fromID = allmarksasmap.get(ID);
                double mark = fromID.get(unit);
                marksinunit.add(mark);
            }
            marksperunit.put(unit, marksinunit);
        }
        return marksperunit;
    }

    public static Map<String, List<Double>> getstudentawardedmarksformeancalc (Map<String, Map<String, Double>> allmarksasmap, List<String> allunits, List<Student> uniqueIDs) {
        Map<String, List<Double>> marksperID = new HashMap<>();
        int count = 0;
        for (Student ID : uniqueIDs) {
            if (allmarksasmap.get(ID.getStudentNo()) == null) {
                continue;
            }
            Map<String, Double> markslist = allmarksasmap.get(ID.getStudentNo());
            List<Double> templist = new ArrayList<>();
            for (String unit : allunits) {
                if (markslist.get(unit) == null) {
                    continue;
                }
                if (markslist.get(unit) > (double) 0) {
                    templist.add(markslist.get(unit));
                }
            }
            marksperID.put(ID.getStudentNo(), templist);
        }
        return marksperID;
    }
    /*
    public static Map<String, List<Double>> getstudentawardedmarks (Map<String, Map<String, Double>> allmarksasmap, List<String> allunits, List<Student> uniqueIDs) {
        Map<String, List<Double>> marksperID = new HashMap<>();
        for (Student ID : uniqueIDs) {
            Map<String, Double> markslist = allmarksasmap.get(ID.getStudentNo());
            List<Double> templist = new ArrayList<>();
            for (String unit : allunits) {
                templist.add(markslist.get(unit));
            }
            marksperID.put(ID.getStudentNo(), templist);
        }
        return marksperID;
    }
    */

    public static List<String> getcrosscheckmarks(String unit ,String crosscheckunit, List<Student> filteredstudents) {
        List<String> crosscheckmarks = new ArrayList<>();
        List<Student> holdID = new ArrayList<>();
            for (Student student : filteredstudents) {
                if (student.getUnit().equals(unit)) {
                    holdID.add(student);
                }
            }
            for (Student student1 : filteredstudents) {
                for (Student checkstudent : holdID) {
                    if (student1.getStudentNo().equals(checkstudent.getStudentNo()) & student1.getUnit().equals(crosscheckunit)) {
                        crosscheckmarks.add(student1.getAwarded());
                    }
                }
            }
        return crosscheckmarks;
    }

    public static Map<String, String> crosscheckunit (List<Student> filteredstudents, List<String> allunits, String checkunit) {
        Map<String, String> allunitscrosscheck = new HashMap<>();
        for (String unit : allunits) {
            if (unit.equals(checkunit)) {
                allunitscrosscheck.put(unit, "");
            } else {
                List<String> marks = getcrosscheckmarks(checkunit, unit, filteredstudents);
                double average = calcmean(marks);
                BigDecimal rounded = new BigDecimal(average).setScale(2, RoundingMode.HALF_UP);
                if (average == 0.0) {
                    allunitscrosscheck.put(unit, "");
                } else {
                    allunitscrosscheck.put(unit, Double.toString(rounded.doubleValue()));
                }
            }
        }
        return allunitscrosscheck;
    }

    public static Map<String, Boolean> marksandpass(List<Student> students, Student studentcheck, List<String> units) {
        Map<String, Boolean> ispass = new HashMap<>();
        int count = 0;
        for(int i=0; i<students.size(); ++i) {
            Student currentStudent = students.get(i);
            if(currentStudent.getStudentNo().equals(studentcheck.getStudentNo())) {
                for (int k = 0; k < units.size(); k++) {
                    if (StudentUtils.studentisinunit(currentStudent, units.get(k))) {
                        boolean temp = ismarkapass(currentStudent);
                        if (ispass.get(currentStudent.getScaled()) != null) {
                            ispass.put(Integer.toString(k), temp);
                        } else {
                            ispass.put(currentStudent.getScaled(), temp);
                        }
                    }
                }
            }
        }
        return ispass;
    }

    public static List<Student> getStudentsWithallDuplicatesRemoved(List<Student> students) {
        List<Student> students1 = removeCTandWN(students);
        List<Student> filteredStudents = new ArrayList<>();
        filteredStudents.add(students1.get(0));
        List<Student> currentdups = new ArrayList<>();
        for(int i=1; i<students1.size(); ++i) {
            int count = 0;
            Student studentmaxmark = students1.get(i);
            Student currentStudent = students1.get(i);
            if (isIDunitpresent(currentdups, currentStudent, false)) {
                continue;
            }
            double mark = currentStudent.getScaledAsDouble();
            for(int j=1+(i); j<students1.size(); ++j) {
                Student nextStudent = students1.get(j);
                if(StudentUtils.studentsAreSame(currentStudent, nextStudent)) {
                    if (nextStudent.getScaledAsDouble() > mark) {
                        studentmaxmark = nextStudent;
                        mark = nextStudent.getScaledAsDouble();
                        count = 1;
                    }
                }
            }
            if (count == 0) {
                filteredStudents.add(currentStudent);;
            } else {
                filteredStudents.add(studentmaxmark);
            }
            currentdups.add(currentStudent);
        }
        return filteredStudents;
    }


    public static List<Student> mergestudents(List<Student> students, List<Student> studentshistoric) {
        for (Student student : studentshistoric) {
            students.add(student);
        }
        return students;
    }

    public static List<Student> getUniqueStudentIDs(List<Student> students) {
        List<Student> filteredStudents = new ArrayList<>();
        for(int i=0; i<students.size(); ++i) {
            Student currentStudent = students.get(i);
            boolean duplicate = true;
            if (isIDunitpresent(filteredStudents, currentStudent, true)) {
                continue;
            }
            filteredStudents.add(currentStudent);
        }
        return filteredStudents;
    }

    public static Map<String, Integer> countUnits(List<Student> filteredStudents) {
        Map<String, Integer> result = new HashMap<>();
        int unitCount = 0;
        List<String> studentIdCounted = new ArrayList<>();
        for(Student currentStudent : filteredStudents) {
            if(!studentIdCounted.contains(currentStudent.getStudentNo())) {
                unitCount = 0;
                for (Student nextStudent : filteredStudents) {
                    if(currentStudent.getStudentNo().equalsIgnoreCase(nextStudent.getStudentNo())) {
                        unitCount++;
                    }
                }
                result.put(currentStudent.getStudentNo(), unitCount);
                studentIdCounted.add(currentStudent.getStudentNo());
            }
        }
        return result;
    }

    public static List<String> getAllUnits(List<Student> students) {
        List<String> units = students.stream().map(Student::getUnit).distinct().sorted().collect(Collectors.toList());
        // set TPCEGL002 to second position and Unit (heading) to first position;
        String firstunit = units.get(0);
        String secondunit = units.get(1);
        int unitlocation = 0;
        int EGL002location = 0;
        if (units.get(0).equals("Unit") & units.get(1).equals("TPCEGL002")) {
            return units;
        } else {
            for (int i = 0; i<units.size(); i++) {
                if(units.get(i).equals("Unit")) {
                    unitlocation = i;
                }
            }
        }
        units.set(0, units.get(unitlocation));
        units.set(unitlocation, firstunit);
        for (int i = 0; i<units.size(); i++) {
            if(units.get(i).equals("TPCEGL002")) {
                EGL002location = i;
            }
        }
        units.set(1, units.get(EGL002location));
        units.set(EGL002location, secondunit);
        return units;
    }

    public static boolean TESeligible(Integer unitcount, Map<String, Boolean> passfail, List<String> marks) {
        int countpasses = 0;
        if (unitcount < 3) {
            return false;
        } else if (passfail.get(marks.get(0)) == null) {
            return false;
        } else if (!passfail.get(marks.get(0))) {
            return false;
        } else {
            for (boolean i : passfail.values()) {
                if (i) {
                    countpasses++;
                }
            }
        }
        return countpasses >= 3;
    }

    public static double calctes(List<String> marks) {
        List<Double> numeric = setmarkstodouble(marks);
        for (int j = 1; j<numeric.size(); j++) {
            int temp =j;
            double num = numeric.get(j);
            for (int i = j+1; i<numeric.size(); i++) {
                if (numeric.get(i) > num) {
                    num = numeric.get(i);
                    temp = i;
                }
            }
            numeric.set(temp, numeric.get(j));
            numeric.set(j, num);
        }
        return numeric.get(0)+numeric.get(1)+numeric.get(2);
    }
}
