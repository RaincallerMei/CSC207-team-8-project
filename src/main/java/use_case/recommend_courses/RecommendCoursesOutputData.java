package use_case.recommend_courses;

import entity.Course;
import java.util.List;

public class RecommendCoursesOutputData {
    private final List<Course> recommendedCourses;

    public RecommendCoursesOutputData(List<Course> recommendedCourses) {
        this.recommendedCourses = recommendedCourses;
    }

    public List<Course> getRecommendedCourses() { return recommendedCourses; }
}