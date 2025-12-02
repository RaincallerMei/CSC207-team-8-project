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
