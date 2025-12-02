package interface_adapter.why_courses;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class WhyCoursesViewModel {
    public static final String PROPERTY_RATIONALE = "rationale";
    public static final String PROPERTY_ERROR = "error";

    // State: map or single string?
    // Since we usually click one button at a time, holding the "current rationale" is sufficient.
    private String currentRationale = "";
    private String currentCourseCode = "";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void setRationale(String courseCode, String rationale) {
        this.currentCourseCode = courseCode;
        this.currentRationale = rationale;
        support.firePropertyChange(PROPERTY_RATIONALE, null, rationale);
    }

    public void fireError(String error) {
        support.firePropertyChange(PROPERTY_ERROR, null, error);
    }

    public String getCurrentRationale() { return currentRationale; }
    public String getCurrentCourseCode() { return currentCourseCode; }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}