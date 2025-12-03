package use_case.why_courses;

public class WhyCoursesOutputData {
    private final String courseCode;
    private final String rationale;

    public WhyCoursesOutputData(String courseCode, String rationale) {
        this.courseCode = courseCode;
        this.rationale = rationale;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getRationale() {
        return rationale;
    }
}