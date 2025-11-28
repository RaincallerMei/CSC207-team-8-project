package ui;

import entity.*;
import storage.AppStateStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CourseExplorerPanel extends JPanel {

    private final RecommendCoursesUseCase recommendCoursesUseCase;

    // Local "DB"
    private final AppStateStore store = new AppStateStore();

    // ==== survey state ====
    private final JTextArea interestsArea = new JTextArea();
    private List<String> completedCourses = new ArrayList<>();

    // ==== recommended courses UI ====
    private final DefaultListModel<String> courseListModel = new DefaultListModel<>();
    private final JList<String> courseList = new JList<>(courseListModel);
    private final JLabel placeholderLabel = new JLabel(
            "<html><i>Once you complete the interest survey, courses will appear here!</i></html>",
            SwingConstants.CENTER
    );
    private final CardLayout recommendedCardLayout = new CardLayout();
    private final JPanel recommendedCardPanel = new JPanel(recommendedCardLayout);

    private static final String CARD_PLACEHOLDER = "placeholder";
    private static final String CARD_LIST = "list";

    private JButton saveButton;
    private JButton submitButton;

    public CourseExplorerPanel() {
        this(new RecommendCoursesUseCase(new RecommendCoursesUseCase.DummyCourseRecommender()));
    }

    public CourseExplorerPanel(RecommendCoursesUseCase recommendCoursesUseCase) {
        this.recommendCoursesUseCase = recommendCoursesUseCase;

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createSurveyPanel(),
                createRecommendedPanel()
        );
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerSize(4);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);

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

    private JPanel createSurveyPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Interest Survey");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton coursesButton = new JButton("Courses I've taken");
        coursesButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        coursesButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // expands with width
        coursesButton.addActionListener(e -> openCoursesTakenDialog());

        JLabel interestsLabel = new JLabel("What are your interests?");
        interestsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton notSureButton = new JButton("Not sure…");
        notSureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        notSureButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); // ← expand like courses button
        notSureButton.addActionListener(e -> openPreferenceAssistant());

        // Set API Key button
        JButton apiKeyButton = new JButton("Set API Key");
        apiKeyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        apiKeyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));   // ← expand like courses button
        apiKeyButton.addActionListener(e -> openApiKeyDialog());

        interestsArea.setLineWrap(true);
        interestsArea.setWrapStyleWord(true);
        JScrollPane interestsScroll = new JScrollPane(interestsArea);
        interestsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        interestsScroll.setPreferredSize(new Dimension(200, 200));
        interestsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> handleSave());

        submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> handleSubmit());

        JPanel actionsRow = new JPanel(new GridLayout(1, 2, 10, 0));
        actionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsRow.add(saveButton);
        actionsRow.add(submitButton);
        actionsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(coursesButton);

        panel.add(Box.createVerticalStrut(15));
        // (removed "API & Interests" label)
        panel.add(apiKeyButton);                // now full-width
        panel.add(Box.createVerticalStrut(10));

        panel.add(interestsLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(notSureButton);               // now full-width
        panel.add(Box.createVerticalStrut(8));
        panel.add(interestsScroll);

        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalStrut(16));
        panel.add(actionsRow);

        return panel;
    }

    private void openApiKeyDialog() {
        ApiKeyDialog d = new ApiKeyDialog(this, store);
        d.setLocationRelativeTo(this);
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
        PreferenceDialog dialog = new PreferenceDialog(this, interestsArea);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void handleSave() {
        store.saveCoursesAndInterests(completedCourses, interestsArea.getText());
        JOptionPane.showMessageDialog(this, "Saved locally.", "Save", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleSubmit() {
        String interests = interestsArea.getText().trim();
        if (interests.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please type your interests before submitting.",
                    "Missing interests",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Submit does not save.
        List<String> recommended = recommendCoursesUseCase.execute(interests, completedCourses);
        showRecommendedCourses(recommended);
    }

    private JPanel createRecommendedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Recommended Courses");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        panel.add(title, BorderLayout.NORTH);

        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.add(placeholderLabel, BorderLayout.CENTER);

        courseList.setVisibleRowCount(8);
        courseList.setFont(courseList.getFont().deriveFont(Font.BOLD, 15f));
        courseList.setFixedCellHeight(44);
        courseList.setCellRenderer(new CourseCellRenderer());
        courseList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = courseList.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        String item = courseListModel.getElementAt(idx);
                        openCourseDetails(item);
                    }
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(courseList);
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(listScroll, BorderLayout.CENTER);

        recommendedCardPanel.add(placeholderPanel, CARD_PLACEHOLDER);
        recommendedCardPanel.add(listPanel, CARD_LIST);
        recommendedCardLayout.show(recommendedCardPanel, CARD_PLACEHOLDER);

        panel.add(recommendedCardPanel, BorderLayout.CENTER);
        return panel;
    }

    private void showRecommendedCourses(List<String> courses) {
        courseListModel.clear();
        if (courses == null || courses.isEmpty()) {
            recommendedCardLayout.show(recommendedCardPanel, CARD_PLACEHOLDER);
            return;
        }
        for (String c : courses) courseListModel.addElement(c);
        recommendedCardLayout.show(recommendedCardPanel, CARD_LIST);
    }

    private void openCourseDetails(String courseText) {
        String[] parsed = parseCourse(courseText);
        String code = parsed[0];
        String description = parsed[1];

        CourseDetailsDialog dialog = new CourseDetailsDialog(this, code, description);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String[] parseCourse(String text) {
        if (text == null || text.isEmpty())
            return new String[]{"Unknown", "Description coming soon."};
        String[] parts = text.split("\\s+[-–—]\\s+", 2);
        if (parts.length == 2) return new String[]{parts[0].trim(), parts[1].trim()};
        String[] tokens = text.trim().split("\\s+", 2);
        String code = tokens[0];
        String desc = (tokens.length > 1) ? tokens[1] : "Description coming soon.";
        return new String[]{code, desc};
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
}