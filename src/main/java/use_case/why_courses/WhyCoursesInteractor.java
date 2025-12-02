package use_case.why_courses;

public class WhyCoursesInteractor implements WhyCoursesInputBoundary {
    private final WhyCoursesDataAccessInterface dataAccessObject;
    private final WhyCoursesOutputBoundary outputBoundary;

    public WhyCoursesInteractor(WhyCoursesDataAccessInterface dataAccessObject,
                                WhyCoursesOutputBoundary outputBoundary) {
        this.dataAccessObject = dataAccessObject;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(WhyCoursesInputData inputData) {
        String code = inputData.getCourseCode();
        if (code == null or code.isEmpty()) {
            outputBoundary.prepareFailView("Invalid course code.");
            return;
        }

        String rationale = dataAccessObject.getRationaleForCourse(code);

        if (rationale == null or rationale.isEmpty()) {
            outputBoundary.prepareFailView("No rationale available for " + code);
        } else {
            WhyCoursesOutputData outputData = new WhyCoursesOutputData(code, rationale);
            outputBoundary.prepareSuccessView(outputData);
        }
    }
}