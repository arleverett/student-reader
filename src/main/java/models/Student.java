package models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Student {
    private String studentNo;
    private String first;
    private String family;
    private String instanceNumber;
    private String college;
    private String unit;
    private String awarded;
    private String originalScaled;
    private String scaled;
    private String paramA;
    private String paramB;

    public Student(List<String> values) {
        int i = 0;
        studentNo = values.get(i++);
        first = values.get(i++);
        family = values.get(i++);
        instanceNumber = values.get(i++);
        college = values.get(i++);
        unit = values.get(i++);
        awarded = values.get(i++);
        originalScaled = values.get(i++);
        scaled = values.get(i++);
        paramA = values.get(i++);
        paramB = values.get(i++);
    }

    public double getScaledAsDouble() {
        return Double.parseDouble(scaled);
    }
    public double getAwardedAsDouble() {
        return Double.parseDouble(awarded);
    }
}
