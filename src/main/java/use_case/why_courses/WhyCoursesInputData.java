package use_case.why_courses;

public class WhyCoursesInputData {
    private final String courseCode;

    public WhyCoursesInputData(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseCode() {
        return courseCode;
    }
}