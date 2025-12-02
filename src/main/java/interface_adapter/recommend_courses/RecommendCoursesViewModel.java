package interface_adapter.recommend_courses;

import entity.Course;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class RecommendCoursesViewModel {
    public static final String PROPERTY_RECOMMENDATIONS = "recommendations";
    public static final String PROPERTY_ERROR = "error";
    public static final String PROPERTY_PROFILE_LOADED = "profileLoaded"; // New Property

    private List<Course> recommendedCourses = new ArrayList<>();

    // ==== NEW STATE ====
    private List<String> completedCoursesState = new ArrayList<>();
    private String interestsState = "";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    // Getters
    public List<Course> getRecommendedCourses() { return recommendedCourses; }
    public List<String> getCompletedCoursesState() { return completedCoursesState; }
    public String getInterestsState() { return interestsState; }

    // Setters
    public void setRecommendedCourses(List<Course> recommendedCourses) {
        List<Course> old = this.recommendedCourses;
        this.recommendedCourses = recommendedCourses;
        support.firePropertyChange(PROPERTY_RECOMMENDATIONS, old, recommendedCourses);
    }

    public void setProfileState(List<String> completedCourses, String interests) {
        this.completedCoursesState = completedCourses;
        this.interestsState = interests;
        // Notify View that profile data has changed
        support.firePropertyChange(PROPERTY_PROFILE_LOADED, null, null);
    }

    public void fireError(String error) {
        support.firePropertyChange(PROPERTY_ERROR, null, error);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}