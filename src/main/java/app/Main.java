package app;

import data_access.InMemoryCourseDataAccessObject;
import data_access.WhyCoursesDataAccessObject;

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
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }
}
