package interface_adapter.profile;

import interface_adapter.recommend_courses.RecommendCoursesViewModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import storage.AppStateStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfileControllerTest {

    private ProfileController controller;
    private AppStateStoreStub storeStub;
    private RecommendCoursesViewModel viewModel;

    // For testing API Key saving safely
    private final Path envPath = Paths.get(".env");
    private List<String> originalEnvContent;
    private boolean envExistedBefore;

    @BeforeEach
    void setUp() throws IOException {
        // 1. Setup Stubs/Mocks
        storeStub = new AppStateStoreStub();
        viewModel = new RecommendCoursesViewModel();
        controller = new ProfileController(storeStub, viewModel);

        // 2. Backup existing .env file to protect your real API key
        if (Files.exists(envPath)) {
            envExistedBefore = true;
            originalEnvContent = Files.readAllLines(envPath);
        } else {
            envExistedBefore = false;
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Restore the original .env file
        if (envExistedBefore) {
            Files.write(envPath, originalEnvContent);
        } else {
            Files.deleteIfExists(envPath);
        }
    }

    @Test
    void testLoadProfile() {
        // Arrange
        storeStub.setMockData(Arrays.asList("CSC207", "CSC148"), "AI and Data Science");

        // Act
        controller.loadProfile();

        // Assert
        // Verify ViewModel was updated
        assertEquals("AI and Data Science", viewModel.getInterestsState());
        assertEquals(2, viewModel.getCompletedCoursesState().size());
        assertTrue(viewModel.getCompletedCoursesState().contains("CSC207"));
    }

    @Test
    void testSaveProfile() {
        // Arrange
        List<String> courses = Arrays.asList("MAT135", "MAT136");
        String interests = "Mathematics";

        // Act
        controller.saveProfile(courses, interests);

        // Assert
        // Verify Store was called
        assertEquals("Mathematics", storeStub.lastInterests);
        assertEquals(2, storeStub.lastCourses.size());
        assertEquals("MAT135", storeStub.lastCourses.get(0));
    }

    @Test
    void testSaveApiKeyCreatesNewFile() throws Exception {
        // Arrange: Ensure .env is gone
        Files.deleteIfExists(envPath);

        // Act
        controller.saveApiKey("AIzaSyTestKey123");

        // Assert
        assertTrue(Files.exists(envPath));
        List<String> lines = Files.readAllLines(envPath);
        assertTrue(lines.contains("GEMINI_API_KEY=AIzaSyTestKey123"));
    }

    @Test
    void testSaveApiKeyUpdatesExistingFile() throws Exception {
        // Arrange: Create a dummy .env
        Files.write(envPath, Arrays.asList("SOME_OTHER_VAR=xyz", "GEMINI_API_KEY=OldKey"));

        // Act
        controller.saveApiKey("AIzaSyNewKey456");

        // Assert
        List<String> lines = Files.readAllLines(envPath);
        boolean foundNew = false;
        boolean foundOld = false;
        for (String line : lines) {
            if (line.equals("GEMINI_API_KEY=AIzaSyNewKey456")) foundNew = true;
            if (line.equals("GEMINI_API_KEY=OldKey")) foundOld = true;
        }
        assertTrue(foundNew, "Should contain the new key");
        assertFalse(foundOld, "Should not contain the old key");
    }

    /**
     * A simple Stub to intercept storage calls so we don't write to the real app.properties.
     * This simulates the Gateway (AppStateStore).
     */
    static class AppStateStoreStub extends AppStateStore {
        List<String> lastCourses = new ArrayList<>();
        String lastInterests = "";

        // Override constructor to avoid creating real files
        public AppStateStoreStub() {
            // No-op, don't call super() logic if possible,
            // but Java forces super call. Since super creates directories,
            // we let it be but override methods to do nothing with files.
        }

        // Setup method for the test
        void setMockData(List<String> courses, String interests) {
            this.lastCourses = courses;
            this.lastInterests = interests;
        }

        @Override
        public List<String> loadCoursesTaken() {
            return lastCourses;
        }

        @Override
        public String loadLastInterests() {
            return lastInterests;
        }

        @Override
        public void saveCoursesAndInterests(List<String> courses, String interests) {
            this.lastCourses = courses;
            this.lastInterests = interests;
        }
    }
}