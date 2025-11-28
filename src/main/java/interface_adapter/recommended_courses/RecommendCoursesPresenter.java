package interface_adapter.recommended_courses;

import use_case.recommended_courses.RecommendCoursesOutputBoundary;
import use_case.recommended_courses.RecommendCoursesOutputData;

/**
 * Presenter for the Recommend Courses Use Case.
 * Takes output data from the Interactor and updates the ViewModel.
 */
public class RecommendCoursesPresenter implements RecommendCoursesOutputBoundary {

    private final RecommendCoursesViewModel viewModel;

    public RecommendCoursesPresenter(RecommendCoursesViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(RecommendCoursesOutputData outputData) {
        viewModel.setCourseCodes(outputData.getCourseCodes());
        viewModel.setCourseTitles(outputData.getCourseTitles());
    }

    public RecommendCoursesViewModel getViewModel() {
        return viewModel;
    }
}
