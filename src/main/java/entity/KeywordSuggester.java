package entity;

import java.util.List;

interface KeywordSuggester {
    String suggest(List<String> orderedInterests);
}