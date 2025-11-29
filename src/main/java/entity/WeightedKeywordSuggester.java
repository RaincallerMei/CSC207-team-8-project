package entity;

import java.util.*;
import java.util.stream.Collectors;

public class WeightedKeywordGenerator implements KeywordGenerator {
    private static final Map<String, List<WeightedKeyword>> KEYWORD_WEIGHTS = Map.of(
            "Analyzing data and patterns", List.of(
                    new WeightedKeyword("data analysis", 10),
                    new WeightedKeyword("statistics", 8),
                    new WeightedKeyword("science", 9),
                    new WeightedKeyword("machine learning", 9)
            ),
            "Creating visual designs / art", List.of(
                    new WeightedKeyword("design", 10),
                    new WeightedKeyword("innovation", 9),
                    new WeightedKeyword("film", 8),
                    new WeightedKeyword("graphics", 8)
            ),
            "Solving complex problems", List.of(
                    new WeightedKeyword("algorithms", 10),
                    new WeightedKeyword("logic", 9),
                    new WeightedKeyword("math", 8),
                    new WeightedKeyword("theory", 8)
            ),
            "Helping and teaching", List.of(
                    new WeightedKeyword("education", 10),
                    new WeightedKeyword("mentorship", 9),
                    new WeightedKeyword("social work", 8),
                    new WeightedKeyword("communication", 8)
            ),
            "Building websites or applications", List.of(
                    new WeightedKeyword("web development", 10),
                    new WeightedKeyword("software engineering", 9),
                    new WeightedKeyword("frontend", 8),
                    new WeightedKeyword("backend", 8)
            ),
            "Starting projects or business", List.of(
                    new WeightedKeyword("entrepreneurship", 10),
                    new WeightedKeyword("product", 9),
                    new WeightedKeyword("leadership", 8),
                    new WeightedKeyword("management", 8)
            )
    );

    private static class WeightedKeyword {
        final String keyword;
        final int baseWeight;

        WeightedKeyword(String keyword, int baseWeight) {
            this.keyword = keyword;
            this.baseWeight = baseWeight;
        }
    }