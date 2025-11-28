package interface_adapter.recommend_courses;

import entity.Course;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class RecommendCoursesViewModel {
    public static final String PROPERTY_RECOMMENDATIONS = "recommendations";
    public static final String PROPERTY_ERROR = "error";

    private List<Course> recommendedCourses = new ArrayList<>();
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public List<Course> getRecommendedCourses() { return recommendedCourses; }

    public void setRecommendedCourses(List<Course> recommendedCourses) {
        List<Course> old = this.recommendedCourses;
        this.recommendedCourses = recommendedCourses;
        support.firePropertyChange(PROPERTY_RECOMMENDATIONS, old, recommendedCourses);
    }

    public void fireError(String error) {
        support.firePropertyChange(PROPERTY_ERROR, null, error);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}