package entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CourseExplorerPanel extends JPanel {

    // ==== dependencies ====
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

    /**
     * Convenience constructor that uses a dummy recommender.
     * Good for quick testing or when wiring in Main.
     */
    public CourseExplorerPanel() {
        this(new RecommendCoursesUseCase(
                new RecommendCoursesUseCase.DummyCourseRecommender()
        ));
    }

    /**
     * Main constructor: UI depends only on the use case.
     */
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

        interestsArea.setLineWrap(true);
        interestsArea.setWrapStyleWord(true);
        JScrollPane interestsScroll = new JScrollPane(interestsArea);
        interestsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        interestsScroll.setPreferredSize(new Dimension(200, 200));
        interestsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        JButton submitButton = new JButton("Submit");
        submitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        submitButton.addActionListener(e -> handleSubmit());

        panel.add(title);
        panel.add(Box.createVerticalStrut(25));
        panel.add(coursesButton);
        panel.add(Box.createVerticalStrut(25));
        panel.add(interestsLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(interestsScroll);
        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalStrut(20));
        panel.add(submitButton);

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

        // Placeholder card
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.add(placeholderLabel, BorderLayout.CENTER);

        // List card
        courseList.setVisibleRowCount(8);
        JScrollPane listScroll = new JScrollPane(courseList);
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(listScroll, BorderLayout.CENTER);

        // Card layout: either placeholder or list
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

    // ===========================================
    // Simple dialog for "Courses I've taken"
    // ===========================================
    private static class CoursesTakenDialog extends JDialog {

        private final JTextArea coursesArea = new JTextArea();
        private boolean confirmed = false;

        public CoursesTakenDialog(Component parent, List<String> existingCourses) {
            super(
                    SwingUtilities.getWindowAncestor(parent),
                    "Courses I've taken",
                    Dialog.ModalityType.APPLICATION_MODAL
            );

            setSize(400, 300);
            setLayout(new BorderLayout());
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            JPanel content = new JPanel(new BorderLayout());
            content.setBorder(new EmptyBorder(15, 15, 15, 15));

            JLabel instruction = new JLabel(
                    "<html>Enter completed course codes, one per line<br>" +
                            "(e.g., CSC108, MAT135).</html>"
            );
            content.add(instruction, BorderLayout.NORTH);

            coursesArea.setLineWrap(true);
            coursesArea.setWrapStyleWord(true);

            if (existingCourses != null && !existingCourses.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String c : existingCourses) {
                    sb.append(c).append("\n");
                }
                coursesArea.setText(sb.toString());
            }

            JScrollPane scrollPane = new JScrollPane(coursesArea);
            content.add(scrollPane, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Cancel");
            JButton save = new JButton("Save");

            cancel.addActionListener(e -> {
                confirmed = false;
                dispose();
            });

            save.addActionListener(e -> {
                confirmed = true;
                dispose();
            });

            buttons.add(cancel);
            buttons.add(save);
            content.add(buttons, BorderLayout.SOUTH);

            setContentPane(content);
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public List<String> getCourses() {
            List<String> result = new ArrayList<>();
            String text = coursesArea.getText();
            if (text == null) return result;

            String[] lines = text.split("\\R");
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
            return result;
        }
    }
}