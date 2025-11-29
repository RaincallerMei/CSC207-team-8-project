package entity;

public class Course {
    private final String courseCode;
    private final String courseName;
    private final String description;
    private final String prerequisiteCodes;
    private final int courseRank;
    private final String courseKeywords;
    private final String courseDescription;

    public Course(String courseCode, String courseName, String description,
                  String prerequisiteCodes, int courseRank,
                  String courseKeywords, String courseDescription) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.description = description;
        this.prerequisiteCodes = prerequisiteCodes;
        this.courseRank = courseRank;
        this.courseKeywords = courseKeywords;
        this.courseDescription = courseDescription;
    }

    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public String getDescription() { return description; }
    public String getPrerequisiteCodes() { return prerequisiteCodes; }
    public int getCourseRank() { return courseRank; }
    public String getCourseKeywords() { return courseKeywords; }
    public String getCourseDescription() { return courseDescription; }

    @Override
    public String toString() {
        return courseCode + ": " + courseName;
    }
}