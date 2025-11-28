package app;

import ui.CourseExplorerPanel;
import data_access.GeminiCourseDataAccessObject;
import use_case.recommend_courses.RecommendCoursesDataAccessInterface;

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
        String apiKey = "YOUR_API_KEY_HERE";
        RecommendCoursesDataAccessInterface courseDAO = new GeminiCourseDataAccessObject(apiKey);

        // 2. Inject it into your Interactor
        // RecommendCoursesInteractor interactor = new RecommendCoursesInteractor(courseDAO, presenter);
    }

}