package interface_adapter.recommend_courses;

import use_case.recommend_courses.RecommendCoursesOutputBoundary;
import use_case.recommend_courses.RecommendCoursesOutputData;

public class RecommendCoursesPresenter implements RecommendCoursesOutputBoundary {
    private final RecommendCoursesViewModel viewModel;

    public RecommendCoursesPresenter(RecommendCoursesViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(RecommendCoursesOutputData outputData) {
        viewModel.setRecommendedCourses(outputData.getRecommendedCourses());
    }

    @Override
    public void prepareFailView(String errorMessage) {
        viewModel.fireError(errorMessage);
    }
}