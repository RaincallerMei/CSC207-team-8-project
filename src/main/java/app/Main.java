package app;

import interface_adapter.profile.ProfileController;
import interface_adapter.recommend_courses.RecommendCoursesController;
import interface_adapter.recommend_courses.RecommendCoursesPresenter;
import interface_adapter.recommend_courses.RecommendCoursesViewModel;
import use_case.recommend_courses.RecommendCoursesDataAccessInterface;
import use_case.recommend_courses.RecommendCoursesInteractor;
import ui.CourseExplorerPanel;
import data_access.GeminiCourseDataAccessObject;
import storage.AppStateStore;
import io.github.cdimascio.dotenv.Dotenv;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            AppStateStore store = new AppStateStore();
            RecommendCoursesViewModel viewModel = new RecommendCoursesViewModel();

            Dotenv dotenv = Dotenv.load();
            String apiKey = dotenv.get("GEMINI_API_KEY");

            ProfileController profileController = new ProfileController(store, viewModel);
            RecommendCoursesPresenter presenter = new RecommendCoursesPresenter(viewModel);

            // 2. Pass the key into the DAO Constructor
            GeminiCourseDataAccessObject dao = new GeminiCourseDataAccessObject(apiKey);

            // 4. Create the Interactor (The "Brain") now accept the Presenter, not just the DAO.
            RecommendCoursesInteractor interactor = new RecommendCoursesInteractor(dao, presenter);

            // 5. Create the Controller (Accepts Input)
            RecommendCoursesController controller = new RecommendCoursesController(interactor);

            // 6. Create the View (The Panel you provided)
            CourseExplorerPanel view = new CourseExplorerPanel(controller, profileController, viewModel);

            // 7. Setup the Frame
            JFrame frame = new JFrame("UofT Course Explorer & Planner");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            CourseExplorerPanel mainPanel = new CourseExplorerPanel(controller, profileController, viewModel);
            frame.add(mainPanel);
            frame.pack();
            frame.setSize(1000, 650); // Set a reasonable default size
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);
        });
    }

}
