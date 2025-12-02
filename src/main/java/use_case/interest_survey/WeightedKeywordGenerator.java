package use_case.interest_survey;

import java.util.*;
import java.util.stream.Collectors;

public class WeightedKeywordGenerator implements KeywordGenerator {
    private static final Map<String, List<WeightedKeyword>> KEYWORD_WEIGHTS = Map.of(
            "Analyzing data and patterns", List.of(
                    new WeightedKeyword("data analysis", 10),
                    new WeightedKeyword("science", 9),
                    new WeightedKeyword("statistics", 8),
                    new WeightedKeyword("machine learning", 7)
            ),
            "Creating visual designs / art", List.of(
                    new WeightedKeyword("design", 10),
                    new WeightedKeyword("innovation", 9),
                    new WeightedKeyword("film", 8),
                    new WeightedKeyword("graphics", 7)
            ),
            "Solving complex problems", List.of(
                    new WeightedKeyword("algorithms", 10),
                    new WeightedKeyword("logic", 9),
                    new WeightedKeyword("math", 8),
                    new WeightedKeyword("theory", 7)
            ),
            "Helping and teaching", List.of(
                    new WeightedKeyword("education", 10),
                    new WeightedKeyword("mentorship", 9),
                    new WeightedKeyword("social work", 8),
                    new WeightedKeyword("communication", 7)
            ),
            "Building websites or applications", List.of(
                    new WeightedKeyword("web development", 10),
                    new WeightedKeyword("software engineering", 9),
                    new WeightedKeyword("frontend", 7),
                    new WeightedKeyword("backend", 7)
            ),
            "Starting projects or business", List.of(
                    new WeightedKeyword("entrepreneurship", 10),
                    new WeightedKeyword("product", 9),
                    new WeightedKeyword("leadership", 8),
                    new WeightedKeyword("management", 7)
            )
    );

    @Override
    public List<String> generate(List<String> orderedInterests) {
        Map<String, Integer> keywordScores = new HashMap<>();

        for (int i = 0; i < orderedInterests.size(); i++) {
            String interest = orderedInterests.get(i);
            int positionWeight = orderedInterests.size() - i;

            List<WeightedKeyword> weightedKeywords = KEYWORD_WEIGHTS.get(interest);
            for (WeightedKeyword wk : weightedKeywords) {
                int score = wk.baseWeight * positionWeight;
                keywordScores.merge(wk.keyword, score, Integer::sum);
            }
        }

        return keywordScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(6) // Return top 6 keywords
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private static class WeightedKeyword {
        final String keyword;
        final int baseWeight;

        WeightedKeyword(String keyword, int baseWeight) {
            this.keyword = keyword;
            this.baseWeight = baseWeight;
        }
    }
}