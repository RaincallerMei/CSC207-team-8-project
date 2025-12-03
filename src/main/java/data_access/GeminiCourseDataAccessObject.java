package data_access;

import entity.Course;
import use_case.recommend_courses.RecommendCoursesDataAccessInterface;
import use_case.why_courses.WhyCoursesDataAccessInterface;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiCourseDataAccessObject implements RecommendCoursesDataAccessInterface, WhyCoursesDataAccessInterface {

    private static final String GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private final HttpClient httpClient;

    // Cache for explanations: CourseCode -> Explanation
    private final Map<String, String> explanationCache = new HashMap<>();

    public GeminiCourseDataAccessObject() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Override
    public List<Course> getRecommendations(String interests, List<String> completedCourses, String apiKey) {
        // Clear cache on new search to keep memory clean and relevant
        explanationCache.clear();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key is missing. Please set it in the settings.");
        }

        if (interests == null || interests.trim().isEmpty()) {
            throw new IllegalArgumentException("At least one interest is required");
        }

        try {
            String prompt = buildPrompt(interests, completedCourses);
            String requestBody = buildRequestBody(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_ENDPOINT + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // DEBUG: Save response to file
            try {
                Files.writeString(Paths.get("gemini_debug.json"), response.body());
            } catch (Exception ignored) {
            }

            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API Failed: " + response.statusCode() + " " + response.body());
            }

            if (response.body().contains("\"finishReason\": \"RECITATION\"")) {
                System.err.println("Gemini blocked output due to Copyright/Recitation.");
                return new ArrayList<>();
            }

            String jsonArrayString = extractModelJson(response.body());
            return parseJsonToCourseList(jsonArrayString);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error during API call", e);
        }
    }

    // New method for WhyCoursesDataAccessInterface
    @Override
    public String getRationaleForCourse(String courseCode) {
        return explanationCache.get(courseCode);
    }

    private List<Course> parseJsonToCourseList(String jsonString) {
        List<Course> courses = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();

        String[] objectStrings = jsonString.split("\\}\\s*,\\s*\\{");

        for (String objStr : objectStrings) {
            try {
                String code = extractValue(objStr, "course_code");

                if (seenCodes.contains(code) || code.equals("N/A")) {
                    continue;
                }
                seenCodes.add(code);

                String name = extractValue(objStr, "course_name");
                String desc = extractValue(objStr, "course_description");
                String explanation = extractValue(objStr, "explanation");
                String prereqs = extractValue(objStr, "prerequisite_codes");

                // Store explanation in cache
                explanationCache.put(code, explanation);

                int rank = 1;
                try {
                    String rankStr = extractValue(objStr, "course_rank");
                    rank = Integer.parseInt(rankStr);
                } catch (NumberFormatException e) { /* default */ }

                // Note: passing explanation to Course object too, as before
                Course course = new Course(code, name, desc, prereqs, rank, "", explanation);
                courses.add(course);

            } catch (Exception e) {
                System.err.println("Skipping malformed object.");
            }
        }
        return courses;
    }

    private String extractValue(String source, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\":\\s*\"(.*?)\"");
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) return matcher.group(1);

        Pattern numPattern = Pattern.compile("\"" + key + "\":\\s*(\\d+)");
        Matcher numMatcher = numPattern.matcher(source);
        if (numMatcher.find()) return numMatcher.group(1);

        return "N/A";
    }

    private String buildPrompt(String interests, List<String> completedCourses) {
        String completedText = (completedCourses == null || completedCourses.isEmpty()) ? "none" : String.join(", ", completedCourses);
        return String.format(
                "You are a course recommendation assistant for UofT.\n" +
                        "Interests: %s\n" +
                        "Completed: %s\n" +
                        "Task: Recommend 3-5 valid UofT courses.\n" +
                        "STRICT VERIFICATION: Use Google Search tool to verify course codes exist in 2024-2025 calendar.\n\n" +
                        "CRITICAL: Summarize course descriptions in your own words.\n" +
                        "DO NOT copy text verbatim from the web to avoid copyright blocks.\n\n" +
                        "Output JSON Array ONLY. Keys: course_code, course_name,\n" +
                        "course_description, prerequisite_codes, course_rank, explanation.\n" +
                        "Do NOT use Markdown formatting.",
                interests, completedText
        );
    }

    private String buildRequestBody(String prompt) {
        String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n");
        return "{"
                + "\"contents\":[{\"parts\":[{\"text\":\"" + escapedPrompt + "\"}]}],"
                + "\"tools\": [{\"google_search\": {}}]"
                + "}";
    }

    private String extractModelJson(String responseBody) {
        String marker = "\"text\":";
        int markerIndex = responseBody.indexOf(marker);
        if (markerIndex < 0) return "[]";

        int startQuote = responseBody.indexOf('"', markerIndex + marker.length());
        int endQuote = responseBody.lastIndexOf('"');
        if (endQuote <= startQuote) return "[]";

        String rawContent = responseBody.substring(startQuote + 1, endQuote);
        String unescaped = rawContent.replace("\\\"", "\"").replace("\\n", " ");

        if (unescaped.contains("```")) {
            unescaped = unescaped.replaceAll("```json", "").replaceAll("```", "");
        }

        int arrayStart = unescaped.indexOf('[');
        int arrayEnd = unescaped.lastIndexOf(']');
        if (arrayStart >= 0 && arrayEnd > arrayStart) {
            return unescaped.substring(arrayStart + 1, arrayEnd);
        }

        return unescaped.trim();
    }
}