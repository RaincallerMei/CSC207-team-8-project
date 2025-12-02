package app;

import data_access.InMemoryCourseDataAccessObject;
import data_access.WhyCoursesDataAccessObject;

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

    private static void createAndShowGUI() {
        // ====== RecommendCourses wiring ======
        RecommendCoursesDataAccessInterface recommendDao =
                new InMemoryCourseDataAccessObject();
        RecommendCoursesViewModel recommendViewModel =
                new RecommendCoursesViewModel();
        RecommendCoursesPresenter recommendPresenter =
                new RecommendCoursesPresenter(recommendViewModel);
        RecommendCoursesInteractor recommendInteractor =
                new RecommendCoursesInteractor(recommendDao, recommendPresenter);
        RecommendCoursesController recommendController =
                new RecommendCoursesController(recommendInteractor);

        // ====== WhyCourses wiring ======
        WhyCoursesDataAccessInterface whyDao = new WhyCoursesDataAccessObject();
        WhyCoursesViewModel whyViewModel = new WhyCoursesViewModel();
        WhyCoursesPresenter whyPresenter = new WhyCoursesPresenter(whyViewModel);
        WhyCoursesInteractor whyInteractor =
                new WhyCoursesInteractor(whyDao, whyPresenter);
        WhyCoursesController whyController =
                new WhyCoursesController(whyInteractor);

        // ====== UI ======
        JFrame frame = new JFrame("UofT Course Explorer & Planner");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        CourseExplorerPanel mainPanel =
                new CourseExplorerPanel(
                        recommendController,
                        recommendViewModel,
                        whyController,
                        whyViewModel
                );
        frame.add(mainPanel);

        frame.pack();
        frame.setSize(1000, 650);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            AppStateStore store = new AppStateStore();
            RecommendCoursesViewModel viewModel = new RecommendCoursesViewModel();

            ProfileController profileController = new ProfileController(store, viewModel);
            RecommendCoursesPresenter presenter = new RecommendCoursesPresenter(viewModel);

            // 1. Create Data Access (No API Key needed at startup)
            GeminiCourseDataAccessObject dao = new GeminiCourseDataAccessObject();

            // 2. Create Interactor
            RecommendCoursesInteractor interactor = new RecommendCoursesInteractor(dao, presenter);

            // 3. Create Controller
            RecommendCoursesController controller = new RecommendCoursesController(interactor);

            // 4. Create View
            JFrame frame = new JFrame("UofT Course Explorer & Planner");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            CourseExplorerPanel mainPanel = new CourseExplorerPanel(controller, profileController, viewModel);
            frame.add(mainPanel);

            frame.pack();
            frame.setSize(1000, 650);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
