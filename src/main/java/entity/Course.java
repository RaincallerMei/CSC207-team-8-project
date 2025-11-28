package entity;

/**
 * A minimal Course entity used for displaying recommended courses.
 * You can expand this later to match the full UML specification.
 */
public class Course {

    private final String code;
    private final String title;

    public Course(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }
}
