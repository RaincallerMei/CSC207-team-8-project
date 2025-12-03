package app;

import interface_adapter.profile.ProfileController;
import interface_adapter.recommend_courses.RecommendCoursesController;
import interface_adapter.recommend_courses.RecommendCoursesPresenter;
import interface_adapter.recommend_courses.RecommendCoursesViewModel;
import interface_adapter.why_courses.WhyCoursesController;
import interface_adapter.why_courses.WhyCoursesPresenter;
import interface_adapter.why_courses.WhyCoursesViewModel;
import use_case.recommend_courses.RecommendCoursesDataAccessInterface;
import use_case.recommend_courses.RecommendCoursesInteractor;
import use_case.why_courses.WhyCoursesDataAccessInterface;
import use_case.why_courses.WhyCoursesInteractor;
import ui.CourseExplorerPanel;
import data_access.GeminiCourseDataAccessObject;
import storage.AppStateStore;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            AppStateStore store = new AppStateStore();

            // 1. View Models
            RecommendCoursesViewModel recommendViewModel = new RecommendCoursesViewModel();
            WhyCoursesViewModel whyViewModel = new WhyCoursesViewModel();

            // 2. Controllers & Presenters
            ProfileController profileController = new ProfileController(store, recommendViewModel);
            RecommendCoursesPresenter recommendPresenter = new RecommendCoursesPresenter(recommendViewModel);
            WhyCoursesPresenter whyPresenter = new WhyCoursesPresenter(whyViewModel);

            // 3. Create Data Access (Shared instance for both interfaces)
            GeminiCourseDataAccessObject geminiDAO = new GeminiCourseDataAccessObject();

            // 4. Create Interactors
            // Recommend Courses Use Case
            RecommendCoursesInteractor recommendInteractor = new RecommendCoursesInteractor(
                    (RecommendCoursesDataAccessInterface) geminiDAO,
                    recommendPresenter
            );

            // Why Courses Use Case
            WhyCoursesInteractor whyInteractor = new WhyCoursesInteractor(
                    (WhyCoursesDataAccessInterface) geminiDAO,
                    whyPresenter
            );

            // 5. Create Controllers
            RecommendCoursesController recommendController = new RecommendCoursesController(recommendInteractor);
            WhyCoursesController whyController = new WhyCoursesController(whyInteractor);

            // 6. Create View
            JFrame frame = new JFrame("UofT Course Explorer & Planner");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            // Injecting controllers.
            // Note: CourseExplorerPanel might need updating to accept whyController if you want to trigger
            // the new use case from the UI (e.g. clicking a button to get specific details).
            CourseExplorerPanel mainPanel = new CourseExplorerPanel(recommendController, profileController, recommendViewModel);
            frame.add(mainPanel);

            frame.pack();
            frame.setSize(1000, 650);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}