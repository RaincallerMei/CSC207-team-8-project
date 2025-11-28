package interface_adapter.recommended_courses;

import use_case.recommended_courses.RecommendCoursesInputBoundary;
import use_case.recommended_courses.RecommendCoursesInputData;

import java.util.UUID;

/**
 * Controller for the Recommend Courses Use Case.
 * Called by the UI layer to trigger the use case.
 */
public class RecommendCoursesController {

    private final RecommendCoursesInputBoundary interactor;

    public RecommendCoursesController(RecommendCoursesInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void recommendForStudent(UUID studentId) {
        RecommendCoursesInputData inputData =
                new RecommendCoursesInputData(studentId);
        interactor.execute(inputData);
    }
}
