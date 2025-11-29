package use_case_why_courses;

public class WhyCoursesInteractor implements WhyCoursesInputBoundary {

    private final WhyCoursesDataAccessInterface dataAccess;
    private final WhyCoursesOutputBoundary outputBoundary;

    public WhyCoursesInteractor(WhyCoursesDataAccessInterface dataAccess,
                                WhyCoursesOutputBoundary outputBoundary) {
        this.dataAccess = dataAccess;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(WhyCoursesInputData inputData) {

    }
}
