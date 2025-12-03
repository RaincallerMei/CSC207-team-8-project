package recommend_courses;
import use_case.recommend_courses.*;

import entity.Course;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecommendCoursesInteractorTest {

    // 1. Fake Data Access Object (Mocking the Gemini API)
    // This allows us to control exactly what "recommendations" are returned
    private static class FakeDAO implements RecommendCoursesDataAccessInterface {
        @Override
        public List<Course> getRecommendations(String interests, List<String> completedCourses, String apiKey) {
            // Simulate a successful response for specific interests
            if (interests.contains("Computer Science")) {
                List<Course> courses = new ArrayList<>();
                courses.add(new Course("CSC207", "Software Design", "Description", "CSC148", 1, "Java", "Because it's cool"));
                return courses;
            }
            // Simulate no results found
            return new ArrayList<>();
        }
    }

    // 2. Fake Presenter (Mocking the UI Controller/ViewModel)
    // This captures the output so we can verify the Interactor sent the right data
    private static class FakePresenter implements RecommendCoursesOutputBoundary {
        RecommendCoursesOutputData successData;
        String errorMessage;

        @Override
        public void prepareSuccessView(RecommendCoursesOutputData outputData) {
            this.successData = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    @Test
    void successTest() {
        // Arrange
        FakeDAO dao = new FakeDAO();
        FakePresenter presenter = new FakePresenter();
        RecommendCoursesInteractor interactor = new RecommendCoursesInteractor(dao, presenter);

        // Note: InputData constructor loads environment variable internally,
        // but for this fake test, the actual key value doesn't matter.
        RecommendCoursesInputData inputData = new RecommendCoursesInputData("Computer Science", new ArrayList<>());

        // Act
        interactor.execute(inputData);

        // Assert
        assertNotNull(presenter.successData); // Ensure success view was triggered
        assertEquals("CSC207", presenter.successData.getRecommendedCourses().get(0).getCourseCode());
        assertNull(presenter.errorMessage);
    }

    @Test
    void failureTest() {
        // Arrange
        FakeDAO dao = new FakeDAO();
        FakePresenter presenter = new FakePresenter();
        RecommendCoursesInteractor interactor = new RecommendCoursesInteractor(dao, presenter);

        // Input that triggers the "empty list" logic in our FakeDAO
        RecommendCoursesInputData inputData = new RecommendCoursesInputData("Cooking", new ArrayList<>());

        // Act
        interactor.execute(inputData);

        // Assert
        assertNull(presenter.successData); // Success view should NOT be triggered
        assertEquals("No courses found for these interests.", presenter.errorMessage);
    }
}