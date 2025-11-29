package use_case_why_courses;

public interface WhyCoursesOutputBoundary {
    void prepareSuccessView(WhyCoursesOutputData outputData);
    void prepareFailView(String errorMessage);
}
