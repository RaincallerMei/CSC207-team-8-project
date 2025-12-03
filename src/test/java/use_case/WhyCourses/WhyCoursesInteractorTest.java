package use_case.WhyCourses;

import org.junit.jupiter.api.Test;
import use_case.why_courses.*;

import static org.junit.jupiter.api.Assertions.*;

class WhyCoursesInteractorTest {

    // ----- Fake DAO -----
    private static class FakeDAO implements WhyCoursesDataAccessInterface {
        private final String expectedCode;
        private final String expectedRationale;

        FakeDAO(String expectedCode, String expectedRationale) {
            this.expectedCode = expectedCode;
            this.expectedRationale = expectedRationale;
        }

        @Override
        public String getRationaleForCourse(String courseCode) {
            if (courseCode.equals(expectedCode)) {
                return expectedRationale;
            }
            return null;
        }
    }

    // ----- Fake Presenter -----
    private static class FakePresenter implements WhyCoursesOutputBoundary {
        String receivedCode;
        String receivedRationale;
        String errorMessage;

        @Override
        public void prepareSuccessView(WhyCoursesOutputData outputData) {
            receivedCode = outputData.getCourseCode();
            receivedRationale = outputData.getRationale();
        }


        @Override
        public void prepareFailView(String error) {
            errorMessage = error;
        }
    }

    @Test
    void successWhenRationaleExists() {
        FakeDAO dao = new FakeDAO("CSC207", "Because it matches your interest in software design.");
        FakePresenter presenter = new FakePresenter();
        WhyCoursesInteractor interactor = new WhyCoursesInteractor(dao, presenter);
        WhyCoursesInputData input = new WhyCoursesInputData("CSC207");

        interactor.execute(input);

        assertEquals("CSC207", presenter.receivedCode);
        assertEquals("Because it matches your interest in software design.", presenter.receivedRationale);
        assertNull(presenter.errorMessage);
    }

    @Test
    void failWhenCourseCodeIsEmpty() {
        FakeDAO dao = new FakeDAO("CSC207", "whatever");
        FakePresenter presenter = new FakePresenter();
        WhyCoursesInteractor interactor = new WhyCoursesInteractor(dao, presenter);
        WhyCoursesInputData input = new WhyCoursesInputData("");

        interactor.execute(input);

        assertNull(presenter.receivedCode);
        assertNull(presenter.receivedRationale);
        assertEquals("Invalid course code.", presenter.errorMessage);
    }

    @Test
    void failWhenNoRationaleFound() {
        FakeDAO dao = new FakeDAO("CSC207", null);
        FakePresenter presenter = new FakePresenter();
        WhyCoursesInteractor interactor = new WhyCoursesInteractor(dao, presenter);
        WhyCoursesInputData input = new WhyCoursesInputData("CSC207");

        interactor.execute(input);

        assertNull(presenter.receivedRationale);
        assertEquals("No rationale available for CSC207", presenter.errorMessage);
    }
}
