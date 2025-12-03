package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog showing detailed information about a recommended course.
 */
public class CourseDetailsDialog extends JDialog {

    public CourseDetailsDialog(Component parent,
                               String courseCode,
                               String courseName,
                               String courseDescription) {

        super(SwingUtilities.getWindowAncestor(parent),
                courseCode + " – " + courseName,
                Dialog.ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Main content panel with vertical BoxLayout
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Course title (big and bold)
        JLabel titleLabel = new JLabel(courseCode + " – " + courseName);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Description label
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(descLabel.getFont().deriveFont(Font.BOLD, 14f));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Description text area (read-only)
        JTextArea descArea = new JTextArea(courseDescription);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(UIManager.getColor("Panel.background"));
        descArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setPreferredSize(new Dimension(600, 160));

        // Why-this-course section (placeholder for now)
        JLabel whyLabel = new JLabel("Why this course was recommended:");
        whyLabel.setFont(whyLabel.getFont().deriveFont(Font.BOLD, 14f));
        whyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea whyArea = new JTextArea(
                "This section will display the reasoning once the \"Why this course?\" feature is fully connected.");
        whyArea.setEditable(false);
        whyArea.setLineWrap(true);
        whyArea.setWrapStyleWord(true);
        whyArea.setBackground(UIManager.getColor("Panel.background"));
        whyArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane whyScroll = new JScrollPane(whyArea);
        whyScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        whyScroll.setPreferredSize(new Dimension(600, 120));

        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);

        // Assemble everything
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(15));
        content.add(descLabel);
        content.add(Box.createVerticalStrut(5));
        content.add(descScroll);
        content.add(Box.createVerticalStrut(15));
        content.add(whyLabel);
        content.add(Box.createVerticalStrut(5));
        content.add(whyScroll);

        add(content, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(700, 560));
        setLocationRelativeTo(parent);
    }
}