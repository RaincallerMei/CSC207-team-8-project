package use_case.recommend_courses;

public interface RecommendCoursesOutputBoundary {
    void prepareSuccessView(RecommendCoursesOutputData outputData);
    void prepareFailView(String errorMessage);
}