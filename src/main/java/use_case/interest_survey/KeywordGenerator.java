package use_case.interest_survey;

import java.util.List;

public interface KeywordGenerator {
    /** Given user-ordered interests, return discrete keywords (no formatting). */
    List<String> generate(List<String> orderedInterests);
}
