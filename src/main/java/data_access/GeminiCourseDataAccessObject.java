package data_access;

import entity.Course;
import use_case.recommend_courses.RecommendCoursesDataAccessInterface;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GeminiCourseDataAccessObject implements RecommendCoursesDataAccessInterface {

    private static final String GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private final String apiKey;
    private final HttpClient httpClient;

    public GeminiCourseDataAccessObject(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60)) // 60s timeout for Search Grounding
                .build();
    }

    @Override
    public List<Course> getRecommendedCourses(List<String> interests, List<String> completedCourses) {
        // 1. Validation
        if (interests.isEmpty()) {
            throw new IllegalArgumentException("At least one interest is required");
        }

        try {
            // 2. Prepare Request
            String prompt = buildPrompt(interests, completedCourses);
            String requestBody = buildRequestBody(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_ENDPOINT + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 3. Send Request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API Failed: " + response.statusCode() + " " + response.body());
            }

            // 4. Extract and Parse
            String jsonArrayString = extractModelJson(response.body());
            return parseJsonToCourseList(jsonArrayString);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error during API call", e);
        }
    }

    // --- 5. The PARSING Logic (Refactored from placeholder) ---
    // This converts the JSON String into a List of Course objects.
    private List<Course> parseJsonToCourseList(String jsonString) {
        List<Course> courses = new ArrayList<>();

        // This regex looks for objects inside the JSON array.
        // It's a simplified parser. For production, use a library like Jackson.
        // We split the string by the closing/opening braces "}, {" to separate objects.
        String[] objectStrings = jsonString.split("\\},\\s*\\{");

        for (String objStr : objectStrings) {
            try {
                // We use helper method extractValue to find specific keys
                String code = extractValue(objStr, "course_code");
                String name = extractValue(objStr, "course_name");
                String desc = extractValue(objStr, "course_description");
                String prereqs = extractValue(objStr, "prerequisite_codes");
                String explanation = extractValue(objStr, "explanation");

                // Parse Rank (default to 1 if failing)
                int rank = 1;
                try {
                    String rankStr = extractValue(objStr, "course_rank");
                    rank = Integer.parseInt(rankStr);
                } catch (NumberFormatException e) { /* keep default */ }

                // Create the Entity
                Course course = new Course(
                        code,
                        name,
                        desc,
                        prereqs,
                        rank,
                        "", // keywords (not strictly returned by current prompt)
                        explanation
                );
                courses.add(course);
            } catch (Exception e) {
                System.err.println("Skipping malformed course object: " + e.getMessage());
            }
        }
        return courses;
    }

    // Helper to find "key": "value" in a string using Regex
    private String extractValue(String source, String key) {
        // Regex explanation: Look for "key" followed by colon, whitespace, quote, (Group 1), quote
        Pattern pattern = Pattern.compile("\"" + key + "\":\\s*\"(.*?)\"");
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Fallback for numbers (no quotes)
        Pattern numberPattern = Pattern.compile("\"" + key + "\":\\s*(\\d+)");
        Matcher numMatcher = numberPattern.matcher(source);
        if (numMatcher.find()) {
            return numMatcher.group(1);
        }
        return "N/A";
    }

    // --- Existing Helper Methods (Copied from LLMs.java) ---

    private String buildPrompt(List<String> interests, List<String> completedCourses) {
        String interestsText = String.join(", ", interests);
        String completedText = completedCourses.isEmpty() ? "none" : String.join(", ", completedCourses);

        return """
            You are a course recommendation assistant for UofT.
            Interests: %s
            Completed: %s
            Task: Recommend 3-5 valid UofT courses. 
            STRICT VERIFICATION: Use Google Search tool to verify course codes exist in 2024-2025 calendar.
            Output JSON Array ONLY. Keys: course_code, course_name, course_description, prerequisite_codes, course_rank, explanation.
            """.formatted(interestsText, completedText);
    }

    private String buildRequestBody(String prompt) {
        String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n");
        return "{"
                + "\"contents\":[{\"parts\":[{\"text\":\"" + escapedPrompt + "\"}]}],"
                + "\"tools\": [{\"google_search\": {}}],"
                + "\"generationConfig\":{\"response_mime_type\":\"application/json\"}"
                + "}";
    }

    private String extractModelJson(String responseBody) {
        String marker = "\"text\":";
        int markerIndex = responseBody.indexOf(marker);
        if (markerIndex < 0) return responseBody;
        int firstQuote = responseBody.indexOf('"', markerIndex + marker.length());

        // Find the actual content
        int start = firstQuote + 1;
        int end = responseBody.lastIndexOf('"');
        if (end <= start) return responseBody;

        String rawContent = responseBody.substring(start, end);

        // Unescape the JSON string provided by Gemini
        return rawContent.replace("\\\"", "\"").replace("\\n", " ");
    }
}