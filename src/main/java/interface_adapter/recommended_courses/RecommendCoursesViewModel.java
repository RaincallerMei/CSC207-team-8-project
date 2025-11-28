package interface_adapter.recommended_courses;

import java.util.List;

/**
 * ViewModel for the Recommend Courses Use Case.
 * Holds the data that the UI will display.
 */
public class RecommendCoursesViewModel {

    private List<String> courseCodes;
    private List<String> courseTitles;

    public void setCourseCodes(List<String> courseCodes) {
        this.courseCodes = courseCodes;
    }

    public void setCourseTitles(List<String> courseTitles) {
        this.courseTitles = courseTitles;
    }

    public List<String> getCourseCodes() {
        return courseCodes;
    }

    public List<String> getCourseTitles() {
        return courseTitles;
    }
}
