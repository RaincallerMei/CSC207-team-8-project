package interface_adapter.recommend_courses;

import use_case.recommend_courses.RecommendCoursesInputBoundary;
import use_case.recommend_courses.RecommendCoursesInputData;
import java.util.List;

public class RecommendCoursesController {
    final RecommendCoursesInputBoundary interactor;

    public RecommendCoursesController(RecommendCoursesInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String interests, List<String> completedCourses) {
        RecommendCoursesInputData input = new RecommendCoursesInputData(interests, completedCourses);
        interactor.execute(input);
    }
}