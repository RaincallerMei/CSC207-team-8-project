package interface_adapter.profile;

import interface_adapter.recommend_courses.RecommendCoursesViewModel;
import storage.AppStateStore;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ProfileController {

    final AppStateStore appStateStore;
    final RecommendCoursesViewModel viewModel;

    public ProfileController(AppStateStore appStateStore, RecommendCoursesViewModel viewModel) {
        this.appStateStore = appStateStore;
        this.viewModel = viewModel;
    }

    /**
     * User Action: Load the saved profile from disk.
     * Updates the ViewModel, which will trigger the View to update.
     */
    public void loadProfile() {
        List<String> courses = appStateStore.loadCoursesTaken();
        String interests = appStateStore.loadLastInterests();

        // Update ViewModel state
        viewModel.setProfileState(courses, interests);
    }

    /**
     * User Action: Save the current profile to disk.
     */
    public void saveProfile(List<String> courses, String interests) {
        appStateStore.saveCoursesAndInterests(courses, interests);
    }

    /**
     * User Action: Save the API Key securely.
     */
    public void saveApiKey(String apiKey) throws Exception {try {
        Path envPath = Paths.get(".env");
        String key = "GEMINI_API_KEY";

        // 1. Read existing lines (create file if it doesn't exist)
        List<String> lines = Files.exists(envPath) ? Files.readAllLines(envPath) : new ArrayList<>();
        List<String> newLines = new ArrayList<>();
        boolean keyFound = false;

        // 2. Iterate through lines to find and update the key
        for (String line : lines) {
            if (line.startsWith(key + "=")) {
                newLines.add(key + "=" + apiKey); // Update existing
                keyFound = true;
            } else {
                newLines.add(line); // Keep other lines
            }
        }

        // 3. If key wasn't found, append it
        if (!keyFound) {
            newLines.add(key + "=" + apiKey);
        }

        // 4. Write back to file
        Files.write(envPath, newLines);

    } catch (IOException e) {
        e.printStackTrace();
        // Handle error (e.g., show popup)
    }
    }
}