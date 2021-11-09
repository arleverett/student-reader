package models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StudentComparer {
    private String studentNo;
    private String unit;

    public StudentComparer(Student student) {
        this.studentNo = student.getStudentNo();
        this.unit = student.getUnit();
    }
}
