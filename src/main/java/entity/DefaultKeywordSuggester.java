package entity;

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
        // Simple, deterministic: concatenate mapped lists in the given order, de-dup while preserving order.
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String k : orderedInterests) {
            List<String> kws = MAP.getOrDefault(k, List.of(k));
            out.addAll(kws);
        }
        // Return a plain list; formatting (commas, etc.) is a UI concern.
        return new ArrayList<>(out);
    }
}