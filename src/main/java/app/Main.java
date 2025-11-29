package app;

import data_access.InMemoryCourseDataAccessObject;
import storage.AppStateStore;
import interface_adapter.profile.ProfileController; // Import the new controller
import interface_adapter.recommend_courses.RecommendCoursesController;
import interface_adapter.recommend_courses.RecommendCoursesPresenter;
import interface_adapter.recommend_courses.RecommendCoursesViewModel;
//import use_case.recommend_courses.RecommendCoursesDataAccessInterface;
import use_case.recommend_courses.RecommendCoursesInteractor;
import ui.CourseExplorerPanel;
import data_access.GeminiCourseDataAccessObject;
//import use_case.recommend_courses.RecommendCoursesDataAccessInterface;
//import use_case.recommend_courses.RecommendCoursesInteractor;
import io.github.cdimascio.dotenv.Dotenv;

import javax.swing.*;

public class Main {

    private static void createAndShowGUI() {
        // 1. Dependencies (Frameworks/Drivers)
        RecommendCoursesDataAccessInterface dao = new InMemoryCourseDataAccessObject();
        AppStateStore store = new AppStateStore(); // Persistence

        // 2. View Model (Interface Adapter)
        RecommendCoursesViewModel viewModel = new RecommendCoursesViewModel();

        // 3. Presenter (Interface Adapter)
        RecommendCoursesPresenter presenter = new RecommendCoursesPresenter(viewModel);

        // 4. Interactor (Use Case)
        RecommendCoursesInteractor interactor = new RecommendCoursesInteractor(dao, presenter);

        // 5. Controllers (Interface Adapters)
        RecommendCoursesController recommendController = new RecommendCoursesController(interactor);
        ProfileController profileController = new ProfileController(store, viewModel); // New!

        // 6. View (Frameworks/Drivers)
        JFrame frame = new JFrame("UofT Course Explorer & Planner");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Inject both controllers into the View
        CourseExplorerPanel mainPanel = new CourseExplorerPanel(recommendController, profileController, viewModel);
        frame.add(mainPanel);

        frame.pack();
        frame.setSize(1000, 650);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

            // 6. Create the View (The Panel you provided)
            CourseExplorerPanel view = new CourseExplorerPanel(controller, viewModel);

            // 7. Setup the Frame
            JFrame frame = new JFrame("UofT Course Explorer & Planner");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            CourseExplorerPanel mainPanel = new CourseExplorerPanel(controller, viewModel);
            frame.add(mainPanel);
            frame.pack();
            frame.setSize(1000, 650); // Set a reasonable default size
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);
        });
    }

}