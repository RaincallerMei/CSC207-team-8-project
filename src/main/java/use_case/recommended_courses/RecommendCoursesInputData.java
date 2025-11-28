package use_case.recommended_courses;

import java.util.UUID;

/**
 * Input Data class for the Recommend Courses Use Case (Use Case 3.3).
 * This object carries the information needed by the Interactor.
 */
public class RecommendCoursesInputData {

    private final UUID studentId;

    public RecommendCoursesInputData(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getStudentId() {
        return studentId;
    }
}
