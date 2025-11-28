package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CourseDetailsDialog extends JDialog {

    public CourseDetailsDialog(Component parent, String courseCode, String courseDescription) {
        super(
                SwingUtilities.getWindowAncestor(parent),
                "Course Details",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel codeLabel = new JLabel("Course: " + courseCode);
        codeLabel.setFont(codeLabel.getFont().deriveFont(Font.BOLD, 16f));
        codeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("Description:");
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea descArea = new JTextArea(courseDescription);
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(UIManager.getColor("TextField.inactiveBackground"));
        descArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setPreferredSize(new Dimension(520, 90));

        JLabel whyLabel = new JLabel("Why this course was recommended:");
        whyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea whyArea = new JTextArea("(placeholder)");
        whyArea.setEditable(false);
        whyArea.setFocusable(false);
        whyArea.setLineWrap(true);
        whyArea.setWrapStyleWord(true);
        whyArea.setBackground(UIManager.getColor("TextField.inactiveBackground"));
        whyArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JScrollPane whyScroll = new JScrollPane(whyArea);
        whyScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        whyScroll.setPreferredSize(new Dimension(520, 70));

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(closeBtn);

        content.add(codeLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(descLabel);
        content.add(descScroll);
        content.add(Box.createVerticalStrut(10));
        content.add(whyLabel);
        content.add(whyScroll);

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(560, getHeight()));
    }
}