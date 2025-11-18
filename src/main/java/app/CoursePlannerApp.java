package app;

import javax.swing.SwingUtilities;

public class CoursePlannerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Wire up the use case with a dummy recommender.
            RecommendCoursesUseCase.CourseRecommender recommender =
                    new RecommendCoursesUseCase.DummyCourseRecommender();

            RecommendCoursesUseCase useCase =
                    new RecommendCoursesUseCase(recommender);

            MainFrame frame = new MainFrame(useCase);
            frame.setVisible(true);
        });
    }
}