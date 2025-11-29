package data_access;

import entity.Course;
import use_case.recommend_courses.RecommendCoursesDataAccessInterface;
import java.util.Arrays;
import java.util.List;

public class InMemoryCourseDataAccessObject{

    public List<Course> getRecommendations(String interests, List<String> completedCourses, String apiKey) {
        // Dummy data populated with the new 7-field constructor
        return Arrays.asList(
                new Course(
                        "CSC207",
                        "Software Design",
                        "An introduction to software design and development concepts, emphasizing object-oriented programming, design patterns, and Clean Architecture.",
                        "CSC148, CSC165",
                        1,
                        "software, design, architecture, patterns",
                        "High relevance: Matches your interest in 'backend systems'."
                ),
                new Course(
                        "CSC165",
                        "Mathematical Expression",
                        "Introduction to abstraction and rigour. Informal introduction to logical notation and reasoning.",
                        "None",
                        2,
                        "math, logic, proofs",
                        "Medium relevance: Fundamental for CS theory."
                ),
                new Course(
                        "CSC240",
                        "Enriched Theory of Comp",
                        "A theoretical course covering regular languages, automata, and computability theory in depth.",
                        "CSC165",
                        3,
                        "theory, automata, computation",
                        "Low relevance: Very theoretical compared to your practical interests."
                ),
                new Course(
                        "CSC301",
                        "Intro to Software Eng",
                        "Introduction to software engineering principles, agile development, and working in large teams.",
                        "CSC207",
                        4,
                        "engineering, agile, project",
                        "High relevance: Directly builds on CSC207."
                )
        );
    }
}