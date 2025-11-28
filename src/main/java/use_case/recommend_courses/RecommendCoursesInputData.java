package use_case.recommend_courses;

import java.util.List;

public class RecommendCoursesInputData {
    private final String interests;
    private final List<String> completedCourses;

    public RecommendCoursesInputData(String interests, List<String> completedCourses) {
        this.interests = interests;
        this.completedCourses = completedCourses;
    }

    public String getInterests() { return interests; }
    public List<String> getCompletedCourses() { return completedCourses; }
}