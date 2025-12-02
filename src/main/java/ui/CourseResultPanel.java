package ui;

import entity.Course;
import interface_adapter.why_courses.WhyCoursesController;
import interface_adapter.why_courses.WhyCoursesViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * One accordion item for a single recommended course.
 * Shows basic info + “Details” + “Why this course?” button.
 */
public class CourseResultPanel extends JPanel implements PropertyChangeListener {

    private final Course course;
    private final WhyCoursesController whyController;
    private final WhyCoursesViewModel whyViewModel;

    private final JButton whyButton = new JButton("Why this course?");

    public CourseResultPanel(
            Course course,
            WhyCoursesController whyController,
            WhyCoursesViewModel whyViewModel
    ) {
        this.course = course;
        this.whyController = whyController;
        this.whyViewModel = whyViewModel;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 8, 8, 8));

        // ===== Top: course code + name =====
        JLabel title = new JLabel(course.getCourseCode() + " – " + course.getCourseName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        add(title, BorderLayout.NORTH);

        // ===== Center: short description (multi-line) =====
        JTextArea descArea = new JTextArea(course.getDescription());
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setOpaque(false);
        descArea.setBorder(null);

        add(descArea, BorderLayout.CENTER);

        // ===== Bottom: buttons =====
        JButton detailsButton = new JButton("Details");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(detailsButton);
        buttons.add(whyButton);
        add(buttons, BorderLayout.SOUTH);

        // ===== Button actions =====
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CourseResultPanel extends JPanel {
    private final JTextArea descriptionArea;
    private boolean isExpanded = false;

    public CourseResultPanel(Course course) {
        // Layout: Header (North), Description (Center)
        this.setLayout(new BorderLayout());
        this.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        this.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 1. Header Panel (Clickable)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Use new fields: Code, Name, Rank
        String headerText = String.format("<html><b>%s</b>: %s <span style='color:gray;font-size:10px;'>(Rank %d)</span></html>",
                course.getCourseCode(), course.getCourseName(), course.getCourseRank());

        JLabel codeLabel = new JLabel(headerText);
        codeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JLabel arrowLabel = new JLabel("▼");

        headerPanel.add(codeLabel, BorderLayout.CENTER);
        headerPanel.add(arrowLabel, BorderLayout.EAST);

        // 2. Content Area (Hidden by default)
        // We include Description + Prerequisites + Relevance + Keywords
        String details = String.format(
                "Description: %s%n%nPrerequisites: %s%n%nRelevance: %s%n%nKeywords: %s",
                course.getDescription(),
                course.getPrerequisiteCodes(),
                course.getCourseDescription(),
                course.getCourseKeywords()
        );

        // Simple “Details” dialog (no why text yet)
        detailsButton.addActionListener(e -> {
            CourseDetailsDialog dialog = new CourseDetailsDialog(
                    this,
                    course.getCourseCode(),
                    course.getDescription()
            );
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });

        // “Why this course?” – go through the WhyCourses use case if wired,
        // otherwise fall back to the static relevance text on the Course entity.
        whyButton.addActionListener(e -> {
            if (whyController != null && whyViewModel != null) {
                // Ask the use case for a reason
                whyController.execute(course.getCourseCode());
            } else {
                // Fallback: just show the relevance string from Course
                String reason = course.getCourseRelevance();
                if (reason == null || reason.isEmpty()) {
                    reason = "This course matches your selected interests and background.";
                }
                showWhyDialog(reason);
            }
        });

        // Listen to updates from WhyCoursesViewModel (if provided)
        if (whyViewModel != null) {
            whyViewModel.addPropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (whyViewModel == null) {
            return;
        }

        switch (evt.getPropertyName()) {
            case WhyCoursesViewModel.PROPERTY_REASON -> {
                // Only react if this reason is for *this* course code
                String code = whyViewModel.getCourseCode();
                if (!course.getCourseCode().equals(code)) {
                    return;
                }
                String reason = whyViewModel.getReason();
                if (reason == null || reason.isEmpty()) {
                    reason = "This course matches your selected interests and background.";
                }
                showWhyDialog(reason);
            }
            case WhyCoursesViewModel.PROPERTY_ERROR -> {
                Object msg = evt.getNewValue();
                JOptionPane.showMessageDialog(
                        this,
                        msg == null ? "Failed to load explanation." : msg.toString(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            default -> { /* ignore other properties */ }
        }
    }

    /** Small helper to pop up the “why” explanation. */
    private void showWhyDialog(String reason) {
        // We reuse CourseDetailsDialog and put the reason into the “why” section.
        // The current CourseDetailsDialog shows placeholder text;
        // if later you extend it to accept a 'why' string, adapt here.
        JTextArea textArea = new JTextArea(reason);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(420, 160));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Why " + course.getCourseCode() + " was recommended",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
