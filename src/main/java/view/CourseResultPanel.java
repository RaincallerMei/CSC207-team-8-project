package view;

import entity.Course;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
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
                course.getCourseRelevance(),
                course.getCourseKeywords()
        );

        descriptionArea = new JTextArea(details);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(true);
        descriptionArea.setBackground(Color.WHITE);
        descriptionArea.setBorder(new EmptyBorder(10, 15, 10, 15));
        descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descriptionArea.setVisible(false);

        this.add(headerPanel, BorderLayout.NORTH);
        this.add(descriptionArea, BorderLayout.CENTER);

        // 3. Click Listener
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggle();
            }
        });
    }

    private void toggle() {
        isExpanded = !isExpanded;
        descriptionArea.setVisible(isExpanded);
        this.revalidate();
        this.repaint();
    }
}