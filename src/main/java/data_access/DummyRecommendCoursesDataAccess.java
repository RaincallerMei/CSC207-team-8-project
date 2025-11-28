package app.data_access;

import entity.Course;
import use_case.recommended_courses.RecommendCoursesDataAccessInterface;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * A dummy in-memory implementation of RecommendCoursesDataAccessInterface.
 * This allows the app to run without a real backend or LLM.
 */
public class DummyRecommendCoursesDataAccess implements RecommendCoursesDataAccessInterface {

    @Override
    public List<Course> findRecommendedCourses(UUID studentId) {
        // For now, just return hard-coded example courses
        return Arrays.asList(
                new Course("CSC207", "Software Design"),
                new Course("CSC165", "Mathematical Expression and Reasoning for CS"),
                new Course("CSC148", "Introduction to Computer Science"),
                new Course("CSC258", "Computer Organization")
        );
    }
}