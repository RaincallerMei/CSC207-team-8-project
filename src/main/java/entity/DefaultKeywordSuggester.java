package entity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

class DefaultKeywordSuggester implements KeywordSuggester {

    private static final LinkedHashMap<String, String> MAP = new LinkedHashMap<>();
    static {
        MAP.put("Analyzing data and patterns", "data analysis, statistics, machine learning");
        MAP.put("Creating visual designs / art", "design, graphics, UI/UX");
        MAP.put("Solving complex problems", "algorithms, logic, theory");
        MAP.put("Helping and teaching", "education, mentorship, communication");
        MAP.put("Building websites or applications", "web development, software engineering, frontend, backend");
        MAP.put("Starting projects or business", "entrepreneurship, product, leadership, management");
    }

    @Override
    public String suggest(List<String> orderedInterests) {
        return orderedInterests.stream()
                .map(k -> MAP.getOrDefault(k, k))
                .collect(Collectors.joining(", "));
    }
}