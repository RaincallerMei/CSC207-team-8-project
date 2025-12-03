package interface_adapter.why_courses;

import use_case.why_courses.WhyCoursesInputBoundary;
import use_case.why_courses.WhyCoursesInputData;

public class WhyCoursesController {
    final WhyCoursesInputBoundary interactor;

    public WhyCoursesController(WhyCoursesInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String courseCode) {
        WhyCoursesInputData inputData = new WhyCoursesInputData(courseCode);
        interactor.execute(inputData);
    }
}