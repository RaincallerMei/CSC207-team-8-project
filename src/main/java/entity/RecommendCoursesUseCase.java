package entity;

import java.util.Arrays;
import java.util.List;

public class RecommendCoursesUseCase {

    /**
     * Domain interface.
     * In the real app this would call your LLM / backend.
     */
    public interface CourseRecommender {
        List<String> recommend(String interests, List<String> completedCourses);
    }

    private final CourseRecommender recommender;

    public RecommendCoursesUseCase(CourseRecommender recommender) {
        this.recommender = recommender;
    }

    /**
     * Application use-case method.
     * UI calls this, but doesn't know how recommendations are produced.
     */
    public List<String> execute(String interests, List<String> completedCourses) {
        return recommender.recommend(interests, completedCourses);
    }

    /**
     * Temporary dummy implementation so the UI works.
     * Replace this with a real implementation later.
     */
    public static class DummyCourseRecommender implements CourseRecommender {
        @Override
        public List<String> recommend(String interests, List<String> completedCourses) {
            // For now we ignore the inputs and return a fixed list.
            // This keeps the UI working while backend is under development.
            return Arrays.asList(
                    "CSC207 – Software Design",
                    "CSC165 – Mathematical Expression and Reasoning for CS",
                    "CSC240 – Enriched Introduction to the Theory of Computation",
                    "CSC301 – Introduction to Software Engineering"
            );
        }
    }
}
