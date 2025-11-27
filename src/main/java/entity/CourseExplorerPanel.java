package entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CourseExplorerPanel extends JPanel {

    // ==== dependency (use case) ====
    private final RecommendCoursesUseCase recommendCoursesUseCase;

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

    // Buttons we want to keep references to (so we can set default button)
    private JButton saveButton;
    private JButton submitButton;

    /** Convenience ctor for quick wiring. */
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
    }

    // Ensure the root pane exists, then set Submit as the default button (Enter activates it)
    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null && submitButton != null) {
            root.setDefaultButton(submitButton);
        }
    }

    // =======================
    // LEFT: Interest Survey
    // =======================
    private JPanel createSurveyPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Interest Survey");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton coursesButton = new JButton("Courses I've taken");
        coursesButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        coursesButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        coursesButton.addActionListener(e -> openCoursesTakenDialog());

        JLabel interestsLabel = new JLabel("What are your interests?");
        interestsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton notSureButton = new JButton("Not sure…");
        notSureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        notSureButton.setMaximumSize(new Dimension(200, 36));
        notSureButton.addActionListener(e -> openPreferenceAssistant());

        interestsArea.setLineWrap(true);
        interestsArea.setWrapStyleWord(true);
        JScrollPane interestsScroll = new JScrollPane(interestsArea);
        interestsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        interestsScroll.setPreferredSize(new Dimension(200, 200));
        interestsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        // Default Look & Feel buttons (no custom colors/borders)
        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> handleSave());

        submitButton = new JButton("Submit"); // will be set as default button
        submitButton.addActionListener(e -> handleSubmit());

        // Equal-size row
        JPanel actionsRow = new JPanel(new GridLayout(1, 2, 10, 0));
        actionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsRow.add(saveButton);
        actionsRow.add(submitButton);
        actionsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        panel.add(title);
        panel.add(Box.createVerticalStrut(25));
        panel.add(coursesButton);
        panel.add(Box.createVerticalStrut(25));
        panel.add(interestsLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(notSureButton);
        panel.add(Box.createVerticalStrut(8));
        panel.add(interestsScroll);
        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalStrut(20));
        panel.add(actionsRow);

        return panel;
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
        PreferenceDialog dialog = new PreferenceDialog(
                this,
                new DefaultKeywordSuggester(),
                interestsArea::setText // apply callback
        );
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void handleSave() {
        JOptionPane.showMessageDialog(this, "Saved (UI stub).", "Save", JOptionPane.INFORMATION_MESSAGE);
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
        List<String> recommended = recommendCoursesUseCase.execute(interests, completedCourses);
        showRecommendedCourses(recommended);
    }

    // ===========================
    // RIGHT: Recommended Courses
    // ===========================
    private JPanel createRecommendedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Recommended Courses");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        panel.add(title, BorderLayout.NORTH);

        // Placeholder
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.add(placeholderLabel, BorderLayout.CENTER);

        // List (reduced size, padded)
        courseList.setVisibleRowCount(8);
        courseList.setFont(courseList.getFont().deriveFont(Font.BOLD, 15f));
        courseList.setFixedCellHeight(44);
        courseList.setCellRenderer(new CourseCellRenderer());

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

        for (String c : courses) {
            courseListModel.addElement(c);
        }
        recommendedCardLayout.show(recommendedCardPanel, CARD_LIST);
    }
}