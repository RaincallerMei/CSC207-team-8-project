package ui;

import entity.Course;
import entity.DefaultKeywordSuggester;
import entity.KeywordGenerator;
import interface_adapter.profile.ProfileController;
import interface_adapter.recommend_courses.RecommendCoursesController;
import interface_adapter.recommend_courses.RecommendCoursesViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class CourseExplorerPanel extends JPanel implements PropertyChangeListener {

    // ==== Dependencies (Clean Architecture: Controllers & ViewModel) ====
    private final RecommendCoursesController recommendController;
    private final ProfileController profileController;
    private final RecommendCoursesViewModel viewModel;
    private final KeywordGenerator keywordGenerator;

    // ==== View State ====
    private final JTextArea interestsArea = new JTextArea();
    private List<String> completedCourses = new ArrayList<>();

    // ==== UI Components ====
    private final JPanel coursesContainer = new JPanel();
    private final CardLayout recommendedCardLayout = new CardLayout();
    private final JPanel recommendedCardPanel = new JPanel(recommendedCardLayout);
    private JButton submitButton; // Tracked for default button setting

    private static final String CARD_PLACEHOLDER = "placeholder";
    private static final String CARD_RESULTS = "results";

    /**
     * Primary Constructor with Dependency Injection
     */
    public CourseExplorerPanel(RecommendCoursesController recommendController,
                               ProfileController profileController,
                               RecommendCoursesViewModel viewModel) {
        this.recommendController = recommendController;
        this.profileController = profileController;
        this.viewModel = viewModel;
        this.keywordGenerator = new DefaultKeywordSuggester(); // Defaulting for simplicity

        // 1. Observe the ViewModel (Reactive UI)
        this.viewModel.addPropertyChangeListener(this);

        // 2. Build UI (slim left panel)
        setLayout(new BorderLayout());

        JPanel leftPanel = createSurveyPanel();
        JPanel rightPanel = createRecommendedPanel();

        // Keep left slim and stable
        leftPanel.setPreferredSize(new Dimension(300, 1)); // ~300px wide initially
        leftPanel.setMinimumSize(new Dimension(240, 1));   // don't let it collapse

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel
        );
        splitPane.setDividerSize(4);
        // Extra space goes to the RIGHT when resizing (left stays slim)
        splitPane.setResizeWeight(1.0);

        add(splitPane, BorderLayout.CENTER);

        // Set the initial divider after layout to ensure a slim left pane
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(300));

        // 3. Trigger Initial Data Load via Controller
        profileController.loadProfile();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null && submitButton != null) root.setDefaultButton(submitButton);
    }

    // =======================
    // VIEW LOGIC: Reacting to State Changes
    // =======================
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();

        if (RecommendCoursesViewModel.PROPERTY_RECOMMENDATIONS.equals(prop)) {
            // Update the Accordion List
            @SuppressWarnings("unchecked")
            List<Course> courses = (List<Course>) evt.getNewValue();
            updateResultsView(courses);
        }
        else if (RecommendCoursesViewModel.PROPERTY_PROFILE_LOADED.equals(prop)) {
            // Update Inputs from Loaded State
            this.completedCourses = viewModel.getCompletedCoursesState();
            this.interestsArea.setText(viewModel.getInterestsState());
        }
        else if (RecommendCoursesViewModel.PROPERTY_ERROR.equals(prop)) {
            JOptionPane.showMessageDialog(this, evt.getNewValue(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateResultsView(List<Course> courses) {
        coursesContainer.removeAll();
        if (courses == null || courses.isEmpty()) {
            recommendedCardLayout.show(recommendedCardPanel, CARD_PLACEHOLDER);
        } else {
            for (Course c : courses) {
                coursesContainer.add(new CourseResultPanel(c));
            }
            coursesContainer.revalidate();
            coursesContainer.repaint();
            recommendedCardLayout.show(recommendedCardPanel, CARD_RESULTS);
        }
    }

    // =======================
    // USER ACTIONS (Forward to Controllers)
    // =======================
    private void handleSubmit() {
        String interests = interestsArea.getText().trim();
        if (interests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter some interests first!", "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        recommendController.execute(interests, completedCourses);
    }

    private void handleSave() {
        profileController.saveProfile(completedCourses, interestsArea.getText());
        JOptionPane.showMessageDialog(this, "Saved locally.", "Save", JOptionPane.INFORMATION_MESSAGE);
    }

    // =======================
    // UI CONSTRUCTION HELPERS
    // =======================
    private JPanel createSurveyPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Interest Survey");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton coursesButton = new JButton("Courses I've taken");
        coursesButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        coursesButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        coursesButton.addActionListener(e -> {
            CoursesTakenDialog d = new CoursesTakenDialog(this, completedCourses);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
            if (d.isConfirmed()) completedCourses = d.getCourses();
        });

        JButton apiKeyButton = new JButton("Set API Key");
        apiKeyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        apiKeyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        apiKeyButton.addActionListener(e -> {
            // ApiKeyDialog hooked to ProfileController
            ApiKeyDialog d = new ApiKeyDialog(this, profileController);
            d.setVisible(true);
        });

        JLabel interestsLabel = new JLabel("What are your interests?");
        interestsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton notSureButton = new JButton("Not sure…");
        notSureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        notSureButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        notSureButton.addActionListener(e -> {
            PreferenceDialog d = new PreferenceDialog(this, keywordGenerator);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
            List<String> kws = d.getResultKeywords();
            if (kws != null && !kws.isEmpty()) interestsArea.setText(String.join(", ", kws));
        });

        interestsArea.setLineWrap(true);
        interestsArea.setWrapStyleWord(true);
        JScrollPane interestsScroll = new JScrollPane(interestsArea);
        interestsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        interestsScroll.setPreferredSize(new Dimension(200, 200));
        interestsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        JButton saveButton = new JButton("Save State");
        saveButton.addActionListener(e -> handleSave());

        submitButton = new JButton("Get Recommendations");
        submitButton.addActionListener(e -> handleSubmit());

        JPanel actionsRow = new JPanel(new GridLayout(1, 2, 10, 0));
        actionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsRow.add(saveButton);
        actionsRow.add(submitButton);
        actionsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(coursesButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(apiKeyButton);
        panel.add(Box.createVerticalStrut(20));
        panel.add(interestsLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(notSureButton);
        panel.add(Box.createVerticalStrut(8));
        panel.add(interestsScroll);
        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalStrut(16));
        panel.add(actionsRow);

        return panel;
    }

    private JPanel createRecommendedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Recommended Courses");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        JLabel placeholderLabel = new JLabel(
                "<html><div style='text-align: center;'><i>Once you complete the interest survey,<br>recommended courses will appear here!</i></div></html>",
                SwingConstants.CENTER
        );
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.add(placeholderLabel, BorderLayout.CENTER);

        coursesContainer.setLayout(new BoxLayout(coursesContainer, BoxLayout.Y_AXIS));
        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.add(coursesContainer, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(scrollWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        recommendedCardPanel.add(placeholderPanel, CARD_PLACEHOLDER);
        recommendedCardPanel.add(scrollPane, CARD_RESULTS);
        recommendedCardLayout.show(recommendedCardPanel, CARD_PLACEHOLDER);

        panel.add(recommendedCardPanel, BorderLayout.CENTER);
        return panel;
    }
}