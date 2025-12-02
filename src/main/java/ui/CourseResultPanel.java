package ui;

import entity.Course;
import interface_adapter.why_courses.WhyCoursesController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt;

/**
 * One row in the "Recommended Courses" list.
 * Shows course code/name, a collapsible description,
 * and buttons for Details / Why this course.
 */
public class CourseResultPanel extends JPanel {

    private final Course course;
    private final WhyCoursesController whyController;

    private final JLabel headerLabel = new JLabel();
    private final JTextArea descriptionArea = new JTextArea();

    private final JButton detailsButton = new JButton("Details");
    private final JButton whyButton = new JButton("Why this course?");

    private boolean isExpanded = false;

    public CourseResultPanel(Course course, WhyCoursesController whyController) {
        this.course = course;
        this.whyController = whyController;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ===== Header (code + name + buttons) =====
        headerLabel.setText(course.getCourseCode() + " – " + course.getCourseName());
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 14f));

        JPanel header = new JPanel(new BorderLayout());
        header.add(headerLabel, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(detailsButton);
        buttons.add(whyButton);
        header.add(buttons, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ===== Description area (collapsed by default) =====
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setText(course.getDescription());
        descriptionArea.setBackground(UIManager.getColor("TextField.inactiveBackground"));
        descriptionArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        descriptionArea.setVisible(false);  // start collapsed

        add(descriptionArea, BorderLayout.CENTER);

        // ===== Listeners =====

        detailsButton.addActionListener(e -> {
            CourseDetailsDialog dialog = new CourseDetailsDialog(
                    this,
                    course.getCourseCode(),
                    course.getDescription()
            );
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });

        whyButton.addActionListener(e ->
                whyController.execute(course.getCourseCode())
        );

        java.awt.event.MouseAdapter toggleListener = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                toggleDescription();
            }
        };
        header.addMouseListener(toggleListener);
        headerLabel.addMouseListener(toggleListener);
    }

    private void toggleDescription() {
        isExpanded = !isExpanded;
        descriptionArea.setVisible(isExpanded);
        revalidate();
        repaint();
    }

    public Course getCourse() {
        return course;
    }
}
