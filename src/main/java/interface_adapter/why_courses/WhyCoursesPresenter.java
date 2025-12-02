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
        // Update the View Model with the rationale text
        viewModel.setRationale(outputData.getCourseCode(), outputData.getRationale());
    }

    @Override
    public void prepareFailView(String error) {
        viewModel.fireError(error);
    }
}