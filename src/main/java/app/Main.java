package app;

import interface_adapter.profile.ProfileController;
import interface_adapter.recommend_courses.RecommendCoursesController;
import interface_adapter.recommend_courses.RecommendCoursesPresenter;
import interface_adapter.recommend_courses.RecommendCoursesViewModel;

import interface_adapter.why_courses.WhyCoursesController;     // NEW
import interface_adapter.why_courses.WhyCoursesPresenter;    // NEW

import use_case.recommend_courses.RecommendCoursesInteractor;
import use_case.why_courses.WhyCoursesInteractor;              // NEW

import ui.CourseExplorerPanel;
import data_access.GeminiCourseDataAccessObject;
import storage.AppStateStore;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            AppStateStore store = new AppStateStore();
            RecommendCoursesViewModel viewModel = new RecommendCoursesViewModel();

            ProfileController profileController = new ProfileController(store, viewModel);
            RecommendCoursesPresenter recommendPresenter = new RecommendCoursesPresenter(viewModel);

            // Data Access
            GeminiCourseDataAccessObject dao = new GeminiCourseDataAccessObject();

            // Main recommendation use case
            RecommendCoursesInteractor recommendInteractor = new RecommendCoursesInteractor(dao, recommendPresenter);
            RecommendCoursesController recommendController = new RecommendCoursesController(recommendInteractor);

            // === NEW: WhyCourses use case ===
            WhyCoursesPresenter whyPresenter = new WhyCoursesPresenter();
            WhyCoursesInteractor whyInteractor = new WhyCoursesInteractor(dao, whyPresenter);
            WhyCoursesController whyController = new WhyCoursesController(whyInteractor);

            // View
            JFrame frame = new JFrame("UofT Course Explorer & Planner");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            CourseExplorerPanel mainPanel = new CourseExplorerPanel(
                    recommendController,
                    profileController,
                    viewModel,
                    whyController          // Pass the new controller
            );

            frame.add(mainPanel);
            frame.pack();
            frame.setSize(1000, 650);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}