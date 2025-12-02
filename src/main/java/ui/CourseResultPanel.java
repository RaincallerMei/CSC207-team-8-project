package ui;

import entity.Course;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CourseResultPanel extends JPanel {

    // We now toggle the entire content panel, not just a text area
    private final JPanel contentPanel;
    private boolean isExpanded = false;

    public CourseResultPanel(Course course) {
        // Layout: Header (North), Content (Center)
        this.setLayout(new BorderLayout());
        this.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        this.setAlignmentX(Component.LEFT_ALIGNMENT);

        // =================================================================
        // 1. Header Panel (Always Visible)
        // =================================================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String headerText = String.format("<html><b>%s</b>: %s <span style='color:gray;font-size:10px;'>(Rank %d)</span></html>",
                course.getCourseCode(), course.getCourseName(), course.getCourseRank());

        JLabel codeLabel = new JLabel(headerText);
        codeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JLabel arrowLabel = new JLabel("▼");

        headerPanel.add(codeLabel, BorderLayout.CENTER);
        headerPanel.add(arrowLabel, BorderLayout.EAST);

        // =================================================================
        // 2. Content Container (Hidden by default)
        // =================================================================
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setVisible(false);

        // --- Part A: Main Description (Description, Prereqs, Keywords) ---
        // Note: We removed 'Relevance' from here
        String mainDetails = String.format(
                "Description: %s%n%nPrerequisites: %s%n%nKeywords: %s",
                course.getDescription(),
                course.getPrerequisiteCodes(),
                course.getCourseKeywords()
        );

        JTextArea descriptionArea = new JTextArea(mainDetails);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(true);
        descriptionArea.setBackground(Color.WHITE);
        descriptionArea.setBorder(new EmptyBorder(10, 15, 5, 15));
        descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 12));

        contentPanel.add(descriptionArea);

        // --- Part B: The "Why This?" Panel (The Button + Hidden Text) ---
        JPanel relevancePanel = new JPanel(new BorderLayout());
        relevancePanel.setBackground(Color.WHITE);
        relevancePanel.setBorder(new EmptyBorder(0, 15, 15, 15)); // Padding

        JButton whyButton = new JButton("Why this?");
        whyButton.setFocusable(false);

        // The actual explanation text (Hidden initially)
        // Note: course.getCourseDescription() holds the AI rationale in your Entity
        JTextArea rationaleArea = new JTextArea("AI Rationale: " + course.getCourseDescription());
        rationaleArea.setWrapStyleWord(true);
        rationaleArea.setLineWrap(true);
        rationaleArea.setEditable(false);
        rationaleArea.setBackground(new Color(230, 240, 255)); // Light blue bg to stand out
        rationaleArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        rationaleArea.setFont(new Font("SansSerif", Font.ITALIC, 12));
        rationaleArea.setVisible(false);

        // Button Action: Toggle the rationale text
        whyButton.addActionListener(e -> {
            boolean visible = !rationaleArea.isVisible();
            rationaleArea.setVisible(visible);
            whyButton.setText(visible ? "Hide Rationale" : "Why this?");

            // Critical: Force the list to redraw with new height
            this.revalidate();
            this.repaint();
        });

        // Wrapper to keep button aligned left and not stretched
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        buttonWrapper.setBackground(Color.WHITE);
        buttonWrapper.add(whyButton);

        relevancePanel.add(buttonWrapper, BorderLayout.NORTH);
        relevancePanel.add(rationaleArea, BorderLayout.CENTER);

        contentPanel.add(relevancePanel);

        // =================================================================
        // 3. Assemble and Listen
        // =================================================================
        this.add(headerPanel, BorderLayout.NORTH);
        this.add(contentPanel, BorderLayout.CENTER);

        // Click listener for the main accordion expansion
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggle();
            }
        });
    }

    private void toggle() {
        isExpanded = !isExpanded;
        contentPanel.setVisible(isExpanded);
        this.revalidate();
        this.repaint();
    }
}