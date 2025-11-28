package view;

import entity.Course;
import interface_adapter.recommend_courses.RecommendCoursesController;
import interface_adapter.recommend_courses.RecommendCoursesViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame implements PropertyChangeListener {

    // ==== Dependencies ====
    private final RecommendCoursesController controller;
    private final RecommendCoursesViewModel viewModel;

    // ==== Survey State ====
    private final JTextArea interestsArea = new JTextArea();
    private List<String> completedCourses = new ArrayList<>();

    // ==== Recommended Courses UI ====
    private final JPanel coursesContainer = new JPanel();
    private final CardLayout recommendedCardLayout = new CardLayout();
    private final JPanel recommendedCardPanel = new JPanel(recommendedCardLayout);

    private static final String CARD_PLACEHOLDER = "placeholder";
    private static final String CARD_RESULTS = "results";

    public MainFrame(RecommendCoursesController controller, RecommendCoursesViewModel viewModel) {
        super("UofT Course Explorer & Planner");

        this.controller = controller;
        this.viewModel = viewModel;
        this.viewModel.addPropertyChangeListener(this); // Listen for updates

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null); // Center on screen

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createSurveyPanel(),
                createRecommendedPanel()
        );
        splitPane.setResizeWeight(0.35); // Left side takes 35% width
        splitPane.setDividerSize(5);

        setContentPane(splitPane);
    }

    // =======================
    // LEFT: Interest Survey
    // =======================
    private JPanel createSurveyPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Interest Survey");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel prompt = new JLabel("What are your academic interests?");
        prompt.setAlignmentX(Component.LEFT_ALIGNMENT);

        interestsArea.setLineWrap(true);
        interestsArea.setWrapStyleWord(true);
        JScrollPane interestsScroll = new JScrollPane(interestsArea);
        interestsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        interestsScroll.setPreferredSize(new Dimension(200, 150));
        interestsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JButton submitButton = new JButton("Get Recommendations");
        submitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSubmit();
            }
        });

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(prompt);
        panel.add(Box.createVerticalStrut(10));
        panel.add(interestsScroll);
        panel.add(Box.createVerticalStrut(20));
        panel.add(submitButton);

        return panel;
    }

    private void handleSubmit() {
        String interests = interestsArea.getText().trim();
        if (interests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter some interests first!");
            return;
        }
        // Trigger the Clean Architecture Flow
        controller.execute(interests, completedCourses);
    }

    // ===========================
    // RIGHT: Recommended Courses
    // ===========================
    private JPanel createRecommendedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Recommended Courses");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        // 1. Placeholder View (Empty State)
        JLabel placeholderLabel = new JLabel(
                "<html><div style='text-align: center;'>Enter your interests on the left<br>to see course recommendations here.</div></html>",
                SwingConstants.CENTER
        );
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.add(placeholderLabel, BorderLayout.CENTER);

        // 2. Results View (Scrollable List)
        coursesContainer.setLayout(new BoxLayout(coursesContainer, BoxLayout.Y_AXIS));

        // Wrap container in a panel formatted for scrolling (aligns to top)
        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.add(coursesContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(scrollWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Faster scrolling

        recommendedCardPanel.add(placeholderPanel, CARD_PLACEHOLDER);
        recommendedCardPanel.add(scrollPane, CARD_RESULTS);
        recommendedCardLayout.show(recommendedCardPanel, CARD_PLACEHOLDER);

        panel.add(recommendedCardPanel, BorderLayout.CENTER);
        return panel;
    }

    // ==========================
    // OBSERVER: React to Changes
    // ==========================
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RecommendCoursesViewModel.PROPERTY_RECOMMENDATIONS)) {
            // 1. Get new data
            List<Course> courses = (List<Course>) evt.getNewValue();

            // 2. Clear old results
            coursesContainer.removeAll();

            if (courses.isEmpty()) {
                recommendedCardLayout.show(recommendedCardPanel, CARD_PLACEHOLDER);
            } else {
                // 3. Create accordion panels for each course
                for (Course c : courses) {
                    CourseResultPanel itemPanel = new CourseResultPanel(c);
                    coursesContainer.add(itemPanel);
                }

                // 4. Refresh Layout
                coursesContainer.revalidate();
                coursesContainer.repaint();
                recommendedCardLayout.show(recommendedCardPanel, CARD_RESULTS);
            }
        } else if (evt.getPropertyName().equals(RecommendCoursesViewModel.PROPERTY_ERROR)) {
            JOptionPane.showMessageDialog(this, evt.getNewValue());
        }
    }
}