package interface_adapter.why_courses;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class WhyCoursesViewModel {

    public static final String PROPERTY_REASON = "reason";
    public static final String PROPERTY_ERROR = "error";

    private String courseCode;
    private String reason;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void setReason(String courseCode, String reason) {
        this.courseCode = courseCode;
        this.reason = reason;
        support.firePropertyChange(PROPERTY_REASON, null, reason);
    }

    public void setError(String error) {
        support.firePropertyChange(PROPERTY_ERROR, null, error);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getReason() {
        return reason;
    }
}
