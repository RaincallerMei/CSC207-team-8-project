package ui;

import entity.Course;
import entity.KeywordGenerator;
import entity.WeightedKeywordGenerator;
import interface_adapter.profile.ProfileController;
import interface_adapter.recommend_courses.RecommendCoursesController;
import interface_adapter.recommend_courses.RecommendCoursesViewModel;
import interface_adapter.why_courses.WhyCoursesController;
import interface_adapter.why_courses.WhyCoursesViewModel;
import storage.AppStateStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Main UI panel: left = interest survey, right = recommended courses (accordion).
 * Listens to RecommendCoursesViewModel. “Why this course?” is handled in CourseResultPanel.
 */
public class CourseExplorerPanel extends JPanel implements PropertyChangeListener {

    // ==== Dependencies (Clean Architecture: Controllers & ViewModel) ====
    private final RecommendCoursesController recommendController;
    private final ProfileController profileController;
    private final RecommendCoursesViewModel viewModel;
    private final KeywordGenerator keywordGenerator;

    // NEW: Why use case wiring
    private final WhyCoursesController whyController;
    private final WhyCoursesViewModel whyViewModel;

    // ==== Local Storage / DB ====
    private final AppStateStore store = new AppStateStore();

    // ==== Survey State ====
    // ==== View State ====
    private final JTextArea interestsArea = new JTextArea();
    private List<String> completedCourses = new ArrayList<>();

    // ==== UI Components ====
    private final JPanel coursesContainer = new JPanel();
    private final CardLayout recommendedCardLayout = new CardLayout();
    private final JPanel recommendedCardPanel = new JPanel(recommendedCardLayout);
    private final JLabel placeholderLabel = new JLabel(
            "<html><div style='text-align: center;'><i>Once you complete the interest survey,<br>" +
                    "recommended courses will appear here!</i></div></html>",
            SwingConstants.CENTER
    );

    // Track submit for enabling/disabling
    private JButton submitButton;

    private static final String CARD_PLACEHOLDER = "placeholder";
    private static final String CARD_RESULTS = "results";
    private static final String CARD_LOADING = "loading"; // NEW

    // =======================
    // Constructors
    // =======================

    // Old 2-arg constructor (for tests or simple wiring)
    public CourseExplorerPanel(RecommendCoursesController controller,
                               RecommendCoursesViewModel viewModel) {
        this(controller, viewModel, new DefaultKeywordSuggester(), null, null);
    /**
     * Primary Constructor with Dependency Injection
     */
    public CourseExplorerPanel(RecommendCoursesController recommendController,
                               ProfileController profileController,
                               RecommendCoursesViewModel viewModel) {
        this(recommendController, profileController, viewModel, new WeightedKeywordGenerator());
    }

    // Old 3-arg constructor (if someone wants custom KeywordGenerator)
    public CourseExplorerPanel(RecommendCoursesController controller,
                               ProfileController profileController,
                               RecommendCoursesViewModel viewModel,
                               KeywordGenerator keywordGenerator) {
        this(controller, viewModel, keywordGenerator, null, null);
    }

    // NEW: full injection including Why use case
    public CourseExplorerPanel(RecommendCoursesController controller,
                               RecommendCoursesViewModel viewModel,
                               WhyCoursesController whyController,
                               WhyCoursesViewModel whyViewModel) {
        this(controller, viewModel, new DefaultKeywordSuggester(), whyController, whyViewModel);
    }

    // Canonical constructor
    private CourseExplorerPanel(RecommendCoursesController controller,
                                RecommendCoursesViewModel viewModel,
                                KeywordGenerator keywordGenerator,
                                WhyCoursesController whyController,
                                WhyCoursesViewModel whyViewModel) {
        this.controller = controller;
        this.recommendController = controller;
        this.profileController = profileController;
        this.viewModel = viewModel;
        this.keywordGenerator = keywordGenerator;
        this.whyController = whyController;
        this.whyViewModel = whyViewModel;

        // Observe the ViewModel
        this.viewModel.addPropertyChangeListener(this);

        // Build UI (slim left panel)
        setLayout(new BorderLayout());

        JPanel leftPanel = createSurveyPanel();
        JPanel rightPanel = createRecommendedPanel();

        leftPanel.setPreferredSize(new Dimension(300, 1));
        leftPanel.setMinimumSize(new Dimension(240, 1));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel
        );
        splitPane.setDividerSize(4);
        splitPane.setResizeWeight(1.0);
        add(splitPane, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(300));

        // Initial data load
        profileController.loadProfile();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null && submitButton != null) root.setDefaultButton(submitButton);
    }

    // =======================
    // VIEW LOGIC: React to VM
    // =======================
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();

        if (RecommendCoursesViewModel.PROPERTY_RECOMMENDATIONS.equals(prop)) {
            @SuppressWarnings("unchecked")
            List<Course> courses = (List<Course>) evt.getNewValue();
            restoreIdle(); // NEW: stop loading
            updateResultsView(courses);
        } else if (RecommendCoursesViewModel.PROPERTY_PROFILE_LOADED.equals(prop)) {
            this.completedCourses = viewModel.getCompletedCoursesState();
            this.interestsArea.setText(viewModel.getInterestsState());
        } else if (RecommendCoursesViewModel.PROPERTY_ERROR.equals(prop)) {
            restoreIdle(); // NEW: stop loading on error too
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

    // ==========================================================
    // LEFT PANEL: Survey & User Inputs
    // ==========================================================
    // =======================
    // USER ACTIONS
    // =======================
    private void handleSubmit() {
        String interests = interestsArea.getText().trim();
        if (interests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter some interests first!", "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showLoading(); // NEW: show loader immediately
        recommendController.execute(interests, completedCourses);
    }

    private void handleSave() {
        profileController.saveProfile(completedCourses, interestsArea.getText());
        JOptionPane.showMessageDialog(this, "Saved locally.", "Save", JOptionPane.INFORMATION_MESSAGE);
    }

    // =======================
    // UI BUILDERS
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

    // ==========================================================
    // RIGHT PANEL: Results (Accordion)
    // ==========================================================
    private JPanel createRecommendedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Recommended Courses");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        // Placeholder
        JLabel placeholderLabel = new JLabel(
                "<html><div style='text-align: center;'><i>Once you complete the interest survey,<br>recommended courses will appear here!</i></div></html>",
                SwingConstants.CENTER
        );
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.add(placeholderLabel, BorderLayout.CENTER);

        // Results container
        coursesContainer.setLayout(new BoxLayout(coursesContainer, BoxLayout.Y_AXIS));
        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.add(coursesContainer, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(scrollWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // NEW: Loading card
        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        loadingPanel.setBorder(new EmptyBorder(40, 20, 40, 20));
        JLabel loadingText = new JLabel("Loading recommendations…", SwingConstants.CENTER);
        loadingText.setAlignmentX(Component.CENTER_ALIGNMENT);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingPanel.add(loadingText);
        loadingPanel.add(Box.createVerticalStrut(12));
        loadingPanel.add(bar);

        recommendedCardPanel.add(placeholderPanel, CARD_PLACEHOLDER);
        recommendedCardPanel.add(scrollPane, CARD_RESULTS);
        recommendedCardPanel.add(loadingPanel, CARD_LOADING); // NEW
        recommendedCardLayout.show(recommendedCardPanel, CARD_PLACEHOLDER);

        panel.add(recommendedCardPanel, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================================
    // CLEAN ARCHITECTURE OBSERVER LOGIC
    // ==========================================================
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RecommendCoursesViewModel.PROPERTY_RECOMMENDATIONS)) {
            // Update the UI with the list of Course entities
            @SuppressWarnings("unchecked")
            List<Course> courses = (List<Course>) evt.getNewValue();
            updateResultsView(courses);
        } else if (evt.getPropertyName().equals(RecommendCoursesViewModel.PROPERTY_ERROR)) {
            JOptionPane.showMessageDialog(this, evt.getNewValue(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateResultsView(List<Course> courses) {
        coursesContainer.removeAll();

        if (courses == null || courses.isEmpty()) {
            recommendedCardLayout.show(recommendedCardPanel, CARD_PLACEHOLDER);
        } else {
            // Create accordion panels
            for (Course c : courses) {
                CourseResultPanel itemPanel =
                        new CourseResultPanel(c, whyController, whyViewModel);
                coursesContainer.add(itemPanel);
            }
            // Refresh layout
            coursesContainer.revalidate();
            coursesContainer.repaint();
            recommendedCardLayout.show(recommendedCardPanel, CARD_RESULTS);
        }
    }

    // ==========================================================
    // CONTROLLER & HELPER LOGIC
    // ==========================================================

    private void handleSubmit() {
        String interests = interestsArea.getText().trim();
        if (interests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter some interests first!", "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // CLEAN ARCHITECTURE: Call Controller, not UseCase directly
        controller.execute(interests, completedCourses);
    }

    private void handleSave() {
        store.saveCoursesAndInterests(completedCourses, interestsArea.getText());
        JOptionPane.showMessageDialog(this, "Saved locally.", "Save", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadSavedStateIntoUI() {
        try {
            this.completedCourses = store.loadCoursesTaken();
            String last = store.loadLastInterests();
            interestsArea.setText(last);
        } catch (Exception e) {
            System.err.println("Failed to load saved state: " + e.getMessage());
        }
    }

    // ==========================================================
    // DIALOG HELPERS
    // ==========================================================

    private void openApiKeyDialog() {
        ApiKeyDialog d = new ApiKeyDialog(this, store);
        d.setVisible(true);
    }

    private void openCoursesTakenDialog() {
        CoursesTakenDialog dialog = new CoursesTakenDialog(this, completedCourses);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            completedCourses = dialog.getCourses();
        }
    }

    private void openPreferenceAssistant() {
        PreferenceDialog dialog = new PreferenceDialog(this, keywordGenerator);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        List<String> kws = dialog.getResultKeywords();
        if (kws != null && !kws.isEmpty()) {
            interestsArea.setText(String.join(", ", kws));
        }
    // =======================
    // Loading helpers (NEW)
    // =======================
    private void showLoading() {
        if (submitButton != null) submitButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        recommendedCardLayout.show(recommendedCardPanel, CARD_LOADING);
        recommendedCardPanel.revalidate();
        recommendedCardPanel.repaint();
    }

    private void restoreIdle() {
        setCursor(Cursor.getDefaultCursor());
        if (submitButton != null) submitButton.setEnabled(true);
    }
}
