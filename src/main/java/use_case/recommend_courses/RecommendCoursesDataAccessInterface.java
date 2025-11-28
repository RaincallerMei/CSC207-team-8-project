package use_case.recommend_courses;

import entity.Course;
import java.util.List;

public interface RecommendCoursesDataAccessInterface {
    List<Course> getRecommendedCourses(List<String> interests, List<String> completedCourses);
}