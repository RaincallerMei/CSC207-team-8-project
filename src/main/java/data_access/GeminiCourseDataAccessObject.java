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

                // --- NEW CODE START ---
                String keywords = extractValue(objStr, "course_keywords");
                // Fallback if extraction fails
                if (keywords.equals("N/A")) {
                    keywords = "General Interest";
                }
                // --- NEW CODE END ---

                // Store explanation in cache
                explanationCache.put(code, explanation);

                int rank = 1;
                try {
                    String rankStr = extractValue(objStr, "course_rank");
                    rank = Integer.parseInt(rankStr);
                } catch (NumberFormatException e) { /* default */ }

                // UPDATED CONSTRUCTOR CALL: Pass 'keywords' instead of ""
                Course course = new Course(code, name, desc, prereqs, rank, keywords, explanation);
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

                        // --- UPDATED INSTRUCTIONS START ---
                        "STRICT DATA REQUIREMENT:\n" +
                        "1. Use Google Search to find the OFFICIAL 2024-2025 UofT Academic Calendar entry for each course.\n" +
                        "2. Extract the 'Prerequisite' field VERBATIM (word-for-word).\n" +
                        "3. DO NOT approximate or substitute equivalent courses (e.g., do not swap APS105 for CSC108).\n" +
                        "4. If you cannot find the exact prerequisite string in the search results, output 'Check Academic Calendar' for that field. DO NOT GUESS.\n" +
                        // --- UPDATED INSTRUCTIONS END ---

                        "CRITICAL: Summarize course descriptions in your own words to avoid copyright.\n" +
                        "Output JSON Array ONLY. Keys:\n" +
                        "1. course_code (string)\n" +
                        "2. course_name (string)\n" +
                        "3. course_description (string)\n" +
                        "4. prerequisite_codes (String: the exact string found, or 'Check Academic Calendar')\n" +
                        "5. course_rank (integer)\n" +
                        "6. course_keywords (String: comma-separated topic keywords)\n" +
                        "7. explanation (string)\n" +
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