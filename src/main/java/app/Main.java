package app;

import ui.CourseExplorerPanel;
import data_access.GeminiCourseDataAccessObject;
import use_case.recommend_courses.RecommendCoursesDataAccessInterface;
import use_case.recommend_courses.RecommendCoursesInteractor;

import javax.swing.*;

public class Main {

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("UofT Course Explorer & Planner");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Our UI is now a panel:
        frame.add(new CourseExplorerPanel());

        frame.pack();
        frame.setLocationRelativeTo(null); // center
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
        // 1. Create the Data Access Object
        String apiKey = "AIzaSyBE6rg-j3hyXHvz9uYX01BbbjFDDm-vgKk";
        RecommendCoursesDataAccessInterface courseDAO = new GeminiCourseDataAccessObject(apiKey);

        // 2. TODO: Display recommended courses in the UI
        RecommendCoursesInteractor interactor = new RecommendCoursesInteractor(courseDAO);
        // Example usage:
        var recommendedCourses = interactor.recommendCourses(
                java.util.List.of("machine learning", "data science"),
                java.util.List.of("CSC108", "MAT137")
        );
        recommendedCourses.forEach(System.out::println);
    }

}