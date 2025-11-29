public class WhyCoursesController {

    private final WhyCoursesInputBoundary interactor;

    public WhyCoursesController(WhyCoursesInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String courseCode) {
        WhyCoursesInputData inputData = new WhyCoursesInputData(courseCode);
        interactor.execute(inputData);
    }
}
