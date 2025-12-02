package use_case.recommend_courses;

import entity.Course;
import java.util.List;

public class RecommendCoursesInteractor implements RecommendCoursesInputBoundary {
    final RecommendCoursesDataAccessInterface dataAccessObject;
    final RecommendCoursesOutputBoundary outputBoundary;

    public RecommendCoursesInteractor(RecommendCoursesDataAccessInterface dataAccessObject,
                                      RecommendCoursesOutputBoundary outputBoundary) {
        this.dataAccessObject = dataAccessObject;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(RecommendCoursesInputData inputData) {
        // 1. Fetch data
        List<Course> recommendations = dataAccessObject.getRecommendations(
                inputData.getInterests(),
                inputData.getCompletedCourses(),
                inputData.getApiKey()
        );

        // 2. Apply business logic (e.g., empty check)
        if (recommendations.isEmpty()) {
            outputBoundary.prepareFailView("No courses found for these interests.");
        } else {
            // 3. Prepare output
            RecommendCoursesOutputData output = new RecommendCoursesOutputData(recommendations);
            outputBoundary.prepareSuccessView(output);
        }
    }
}