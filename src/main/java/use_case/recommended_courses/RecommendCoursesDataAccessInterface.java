package use_case.recommended_courses;

import entity.Course;
import java.util.List;
import java.util.UUID;

/**
 * Data access gateway for fetching recommended courses for a student.
 * The Interactor depends on this interface rather than on a concrete implementation.
 *
 * Different implementations may use:
 * - In-memory dummy data
 * - Database
 * - API calls
 * - LLM-generated recommendations
 */
public interface RecommendCoursesDataAccessInterface {

    List<Course> findRecommendedCourses(UUID studentId);
}
