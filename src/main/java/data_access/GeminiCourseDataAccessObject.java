package data_access;

import entity.Course;
import use_case.recommend_courses.RecommendCoursesDataAccessInterface;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiCourseDataAccessObject implements RecommendCoursesDataAccessInterface {

    // VERIFY THIS URL IS EXACTLY AS SHOWN:
    private static final String GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private final String apiKey;
    private final HttpClient httpClient;

    public GeminiCourseDataAccessObject(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Override
    public List<Course> getRecommendations(String interests, List<String> completedCourses) {
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
            try { Files.writeString(Paths.get("gemini_debug.json"), response.body()); } catch (Exception ignored) {}

            if (response.statusCode() != 200) {
                // This is where your 404 is coming from.
                // If it prints 404 here, the URL above is incorrect or the model name is wrong.
                throw new RuntimeException("Gemini API Failed: " + response.statusCode() + " " + response.body());
            }

            // Check if blocked by copyright (Recitation error)
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

    private List<Course> parseJsonToCourseList(String jsonString) {
        List<Course> courses = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();

        String[] objectStrings = jsonString.split("\\}\\s*,\\s*\\{");

        for (String objStr : objectStrings) {
            try {
                String code = extractValue(objStr, "course_code");

                // Prevent Duplicates
                if (seenCodes.contains(code) || code.equals("N/A")) {
                    continue;
                }
                seenCodes.add(code);

                String name = extractValue(objStr, "course_name");
                String desc = extractValue(objStr, "course_description");
                String explanation = extractValue(objStr, "explanation");
                String prereqs = extractValue(objStr, "prerequisite_codes");

                int rank = 1;
                try {
                    String rankStr = extractValue(objStr, "course_rank");
                    rank = Integer.parseInt(rankStr);
                } catch (NumberFormatException e) { /* default */ }

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
        return """
            You are a course recommendation assistant for UofT.
            Interests: %s
            Completed: %s
            Task: Recommend 3-5 valid UofT courses. 
            STRICT VERIFICATION: Use Google Search tool to verify course codes exist in 2024-2025 calendar.
            
            CRITICAL: Summarize course descriptions in your own words. 
            DO NOT copy text verbatim from the web to avoid copyright blocks.
            
            Output JSON Array ONLY. Keys: course_code, course_name, 
            course_description, prerequisite_codes, course_rank, explanation.
            Do NOT use Markdown formatting.
            """.formatted(interests, completedText);
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

        // Remove markdown wrappers if present
        if (unescaped.contains("```")) {
            unescaped = unescaped.replaceAll("```json", "").replaceAll("```", "");
        }

        // Isolate the array brackets
        int arrayStart = unescaped.indexOf('[');
        int arrayEnd = unescaped.lastIndexOf(']');
        if (arrayStart >= 0 && arrayEnd > arrayStart) {
            return unescaped.substring(arrayStart + 1, arrayEnd);
        }

        return unescaped.trim();
    }
}