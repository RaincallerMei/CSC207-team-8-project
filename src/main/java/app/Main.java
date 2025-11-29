package app;

import data_access.InMemoryCourseDataAccessObject;
import entity.Course;
import entity.RecommendCoursesUseCase;
import interface_adapter.recommend_courses.RecommendCoursesController;
import interface_adapter.recommend_courses.RecommendCoursesPresenter;
import interface_adapter.recommend_courses.RecommendCoursesViewModel;
import use_case.recommend_courses.RecommendCoursesDataAccessInterface;
import use_case.recommend_courses.RecommendCoursesInteractor;
import ui.CourseExplorerPanel;
import data_access.GeminiCourseDataAccessObject;
import use_case.recommend_courses.RecommendCoursesDataAccessInterface;
import use_case.recommend_courses.RecommendCoursesInteractor;

import javax.swing.*;

public class Main {

//    private static void createAndShowGUI() {
//
//
//        // 1. Create the Data Access Object (Frameworks & Drivers) Switch to Real/Gemini DAO here later when ready
//        RecommendCoursesDataAccessInterface dao = new InMemoryCourseDataAccessObject();
//
//        // 2. Create the View Model (Interface Adapter)
//        RecommendCoursesViewModel viewModel = new RecommendCoursesViewModel();
//
//        // 3. Create the Presenter (Interface Adapter)
//        RecommendCoursesPresenter presenter = new RecommendCoursesPresenter(viewModel);
//
//        // 4. Create the Interactor (Application Business Rules)
//        RecommendCoursesInteractor interactor = new RecommendCoursesInteractor(dao, presenter);
//
//        // 5. Create the Controller (Interface Adapter)
//        RecommendCoursesController controller = new RecommendCoursesController(interactor);
//
//        // 6. Create the Main View (Frameworks & Drivers)
//        // Inject Controller and ViewModel into the UI
//        JFrame frame = new JFrame("UofT Course Explorer & Planner");
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        CourseExplorerPanel mainPanel = new CourseExplorerPanel(controller, viewModel);
//        frame.add(mainPanel);
//        frame.pack();
//        frame.setSize(1000, 650); // Set a reasonable default size
//        frame.setLocationRelativeTo(null); // Center on screen
//        frame.setVisible(true);
//    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. Create the ViewModel (Holds the state of the UI)
            RecommendCoursesViewModel viewModel = new RecommendCoursesViewModel();

//            String apiKey = "";
            // 2. Create the Data Access Object
            GeminiCourseDataAccessObject dao = new GeminiCourseDataAccessObject();

            // 3. Create the Presenter (Updates the ViewModel)
            RecommendCoursesPresenter presenter = new RecommendCoursesPresenter(viewModel);

            // 4. Create the Interactor (The "Brain") now accept the Presenter, not just the DAO.
            RecommendCoursesInteractor interactor = new RecommendCoursesInteractor(dao, presenter);

            // 5. Create the Controller (Accepts Input)
            RecommendCoursesController controller = new RecommendCoursesController(interactor);

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