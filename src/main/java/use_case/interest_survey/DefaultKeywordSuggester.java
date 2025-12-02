package use_case.interest_survey;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultKeywordSuggester implements KeywordGenerator {

    private static final Map<String, List<String>> MAP = new LinkedHashMap<>();
    static {
        MAP.put("Analyzing data and patterns", List.of("data analysis","statistics","machine learning"));
        MAP.put("Creating visual designs / art", List.of("design","graphics","ui","ux"));
        MAP.put("Solving complex problems",     List.of("algorithms","logic","theory"));
        MAP.put("Helping and teaching",         List.of("education","mentorship","communication"));
        MAP.put("Building websites or applications", List.of("web development","frontend","backend","software engineering"));
        MAP.put("Starting projects or business",     List.of("entrepreneurship","product","leadership","management"));
    }

    @Override
    public List<String> generate(List<String> orderedInterests) {

        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String k : orderedInterests) {
            List<String> kws = MAP.getOrDefault(k, List.of(k));
            out.addAll(kws);
        }

        return new ArrayList<>(out);
    }
}