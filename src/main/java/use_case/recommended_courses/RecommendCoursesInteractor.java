package use_case.recommended_courses;

import entity.Course;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Interactor for the Recommend Courses Use Case (Use Case 3.3).
 * This class contains the main business logic for retrieving and
 * presenting recommended courses.
 */
public class RecommendCoursesInteractor implements RecommendCoursesInputBoundary {

    private final RecommendCoursesDataAccessInterface courseRepository;
    private final RecommendCoursesOutputBoundary presenter;

    public RecommendCoursesInteractor(RecommendCoursesDataAccessInterface courseRepository,
                                      RecommendCoursesOutputBoundary presenter) {
        this.courseRepository = courseRepository;
        this.presenter = presenter;
    }

    @Override
    public void execute(RecommendCoursesInputData inputData) {
        UUID studentId = inputData.getStudentId();

        // 1. Get recommended courses from the data source
        List<Course> recommendedCourses =
                courseRepository.findRecommendedCourses(studentId);

        // 2. Convert Course entities into simple strings for output
        List<String> codes = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        for (Course course : recommendedCourses) {
            codes.add(course.getCode());
            titles.add(course.getTitle());
        }

        // 3. Package into output data
        RecommendCoursesOutputData outputData =
                new RecommendCoursesOutputData(codes, titles);

        // 4. Pass to presenter
        presenter.present(outputData);
    }
}