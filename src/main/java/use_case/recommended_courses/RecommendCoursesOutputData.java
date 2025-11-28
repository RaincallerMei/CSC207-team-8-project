package use_case.recommended_courses;

import java.util.List;

/**
 * Output Data class for the Recommend Courses Use Case (Use Case 3.3).
 * This carries the recommended courses information from the Interactor
 * to the Presenter.
 */
public class RecommendCoursesOutputData {

    private final List<String> courseCodes;
    private final List<String> courseTitles;

    public RecommendCoursesOutputData(List<String> courseCodes,
                                      List<String> courseTitles) {
        this.courseCodes = courseCodes;
        this.courseTitles = courseTitles;
    }

    public List<String> getCourseCodes() {
        return courseCodes;
    }

    public List<String> getCourseTitles() {
        return courseTitles;
    }
}
