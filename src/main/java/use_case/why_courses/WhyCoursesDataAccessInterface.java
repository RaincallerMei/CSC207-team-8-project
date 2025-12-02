package use_case.why_courses;

public interface WhyCoursesDataAccessInterface {
    /**
     * Retrieves the AI-generated rationale for a specific course.
     * @param courseCode The code of the course (e.g., "CSC207").
     * @return The explanation string, or null if not found.
     */
    String getRationaleForCourse(String courseCode);
}