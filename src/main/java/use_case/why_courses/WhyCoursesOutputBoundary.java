package use_case.why_courses;

public interface WhyCoursesOutputBoundary {
    void prepareSuccessView(WhyCoursesOutputData outputData);
    void prepareFailView(String errorMessage);
}
