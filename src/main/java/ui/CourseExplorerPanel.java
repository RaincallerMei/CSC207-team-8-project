package ui;

import entity.Course;
import entity.DefaultKeywordSuggester;
import entity.KeywordGenerator;
import interface_adapter.recommend_courses.RecommendCoursesController;
import interface_adapter.recommend_courses.RecommendCoursesViewModel;
import storage.AppStateStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class CourseExplorerPanel extends JPanel implements PropertyChangeListener {

    // ==== Clean Architecture Dependencies ====
    private final RecommendCoursesController controller;
    private final RecommendCoursesViewModel viewModel;
    private final KeywordGenerator keywordGenerator;

    // ==== Local Storage / DB ====
    private final AppStateStore store = new AppStateStore();

    // ==== Survey State ====
    private final JTextArea interestsArea = new JTextArea();
    private List<String> completedCourses = new ArrayList<>();

    // ==== Recommended Courses UI (Accordion) ====
    private final JPanel coursesContainer = new JPanel();
    private final CardLayout recommendedCardLayout = new CardLayout();
    private final JPanel recommendedCardPanel = new JPanel(recommendedCardLayout);
    private final JLabel placeholderLabel = new JLabel(
            "<html><div style='text-align: center;'><i>Once you complete the interest survey,<br>recommended courses will appear here!</i></div></html>",
            SwingConstants.CENTER
    );

    private static final String CARD_PLACEHOLDER = "placeholder";
    private static final String CARD_RESULTS = "results";

    // Default button
    private JButton submitButton;

    public CourseExplorerPanel(RecommendCoursesController controller,
                               RecommendCoursesViewModel viewModel) {
        this(controller, viewModel, new DefaultKeywordSuggester());
    }

    public CourseExplorerPanel(RecommendCoursesController controller,
                               RecommendCoursesViewModel viewModel,
                               KeywordGenerator keywordGenerator) {
        this.controller = controller;
        this.viewModel = viewModel;
        this.keywordGenerator = keywordGenerator;

        this.viewModel.addPropertyChangeListener(this);
        setLayout(new BorderLayout());

        // Build split pane with a slimmer, stable left panel
        JPanel leftPanel = createSurveyPanel();
        JPanel rightPanel = createRecommendedPanel();

        // Keep the left from collapsing; don't set a 0 height
        leftPanel.setMinimumSize(new Dimension(220, 1));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        // Extra space goes to the RIGHT so the left stays slim
        splitPane.setResizeWeight(1.0);
        splitPane.setDividerSize(4);

        add(splitPane, BorderLayout.CENTER);

        // Set initial left width in pixels after layout so it never collapses
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(280));

        // Load saved state (courses + last interests)
        loadSavedStateIntoUI();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null && submitButton != null) {
            root.setDefaultButton(submitButton);
        }
    }

    // =======================
    // LEFT: Survey & Inputs
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
        coursesButton.addActionListener(e -> openCoursesTakenDialog());

        JButton apiKeyButton = new JButton("Set API Key");
        apiKeyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        apiKeyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        apiKeyButton.addActionListener(e -> openApiKeyDialog());

        JLabel interestsLabel = new JLabel("What are your interests?");
        interestsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton notSureButton = new JButton("Not sure…");
        notSureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        notSureButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        notSureButton.addActionListener(e -> openPreferenceAssistant());

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
        panel.add(Box.createVerticalStrut(16));
        panel.add(coursesButton);
        panel.add(Box.createVerticalStrut(8));
        panel.add(apiKeyButton);
        panel.add(Box.createVerticalStrut(16));
        panel.add(interestsLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(notSureButton);
        panel.add(Box.createVerticalStrut(6));
        panel.add(interestsScroll);
        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalStrut(12));
        panel.add(actionsRow);

        return panel;
    }

    // =======================
    // RIGHT: Results
    // =======================
    private JPanel createRecommendedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Recommended Courses");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

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

    // =======================
    // ViewModel Observer
    // =======================
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RecommendCoursesViewModel.PROPERTY_RECOMMENDATIONS)) {
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
            for (Course c : courses) {
                CourseResultPanel itemPanel = new CourseResultPanel(c);
                coursesContainer.add(itemPanel);
            }
            coursesContainer.revalidate();
            coursesContainer.repaint();
            recommendedCardLayout.show(recommendedCardPanel, CARD_RESULTS);
        }
    }

    // =======================
    // Controller & Helpers
    // =======================
    private void handleSubmit() {
        String interests = interestsArea.getText().trim();
        if (interests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter some interests first!", "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
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

    // =======================
    // Dialogs
    // =======================
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
    }
}