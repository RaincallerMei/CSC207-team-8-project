package use_case.recommend_courses;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;

public class RecommendCoursesInputData {
    private final String interests;
    private final List<String> completedCourses;
    private final String api_key;

    public RecommendCoursesInputData(String interests, List<String> completedCourses) {
        this.interests = interests;
        this.completedCourses = completedCourses;
        Dotenv dotenv = Dotenv.load();
        this.api_key = dotenv.get("GEMINI_API_KEY");
    }

    public String getInterests() { return interests; }
    public List<String> getCompletedCourses() { return completedCourses; }
    public String getApi_key() { return api_key; }
}