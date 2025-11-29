package use_case_why_courses;

public class WhyCoursesOutputData {

    private final String courseCode;
    private final String reason;

    public WhyCoursesOutputData(String courseCode, String reason) {
        this.courseCode = courseCode;
        this.reason = reason;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getReason() {
        return reason;
    }
}
