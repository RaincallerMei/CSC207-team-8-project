package data_access;

import use_case.why_courses.WhyCoursesDataAccessInterface;

public class WhyCoursesDataAccessObject implements WhyCoursesDataAccessInterface {

    @Override
    public String getReasonForCourse(String courseCode) {
        // TODO: later you can make this smarter or dynamic.
        // For now, we just hard-code explanations.

        return switch (courseCode) {
            case "CSC207" -> "You showed interest in software design and backend development, " +
                    "and CSC207 focuses on object-oriented design and Clean Architecture.";

            case "CSC165" -> "You like logical thinking and problem solving, " +
                    "and CSC165 builds your foundations in proofs and reasoning.";

            case "CSC240" -> "You mentioned interest in theory / math-heavy CS, " +
                    "and CSC240 goes deep into automata and computability.";

            case "CSC301" -> "You are interested in real-world projects and teamwork. " +
                    "CSC301 is a software engineering course with group projects and agile methods.";

            default -> "This course matches your selected interests and background.";
        };
    }
}
