package interface_adapter.why_courses;

import use_case.why_courses.WhyCoursesOutputBoundary;
import use_case.why_courses.WhyCoursesOutputData;

public class WhyCoursesPresenter implements WhyCoursesOutputBoundary {

    private final WhyCoursesViewModel viewModel;

    public WhyCoursesPresenter(WhyCoursesViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(WhyCoursesOutputData outputData) {
        viewModel.setReason(outputData.getCourseCode(), outputData.getReason());
    }

    @Override
    public void prepareFailView(String errorMessage) {
        viewModel.setError(errorMessage);
    }
}
