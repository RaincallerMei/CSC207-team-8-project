package use_case.why_courses;

public class WhyCoursesInteractor implements WhyCoursesInputBoundary {

    private final WhyCoursesDataAccessInterface dataAccess;
    private final WhyCoursesOutputBoundary presenter;

    public WhyCoursesInteractor(WhyCoursesDataAccessInterface dataAccess,
                                WhyCoursesOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(WhyCoursesInputData inputData) {
        String code = inputData.getCourseCode();

        if (code == null || code.isEmpty()) {
            presenter.prepareFailView("Invalid course code.");
            return;
        }

        String reason = dataAccess.getReasonForCourse(code);

        if (reason == null) {
            presenter.prepareFailView("No reason found for course: " + code);
            return;
        }

        WhyCoursesOutputData output = new WhyCoursesOutputData(code, reason);
        presenter.prepareSuccessView(output);
    }
}
