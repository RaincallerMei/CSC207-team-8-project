package ui;

import entity.Course;
import entity.KeywordGenerator;
import entity.WeightedKeywordGenerator;
import interface_adapter.profile.ProfileController;
import interface_adapter.recommend_courses.RecommendCoursesController;
import interface_adapter.recommend_courses.RecommendCoursesViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Left = survey/actions, right = results (placeholder/loading).
 * Minimal guard: if user clicks "Get Recommendations" and we can't find an API key
 * in env / .env, we warn and stop. No disabled/grey button.
 */
public class CourseExplorerPanel extends JPanel implements PropertyChangeListener {

    // ==== Dependencies ====
    private final RecommendCoursesController recommendController;
    private final ProfileController profileController;
    private final RecommendCoursesViewModel viewModel;
    private final KeywordGenerator keywordGenerator;

    // ==== View State ====
    private final JTextArea interestsArea = new JTextArea();
    private List<String> completedCourses = new ArrayList<>();

    // ==== UI Components ====
    private final JPanel coursesContainer = new JPanel();
    private final CardLayout recommendedCardLayout = new CardLayout();
    private final JPanel recommendedCardPanel = new JPanel(recommendedCardLayout);

    private JButton submitButton; // tracked for default + loading state

    private static final String CARD_PLACEHOLDER = "placeholder";
    private static final String CARD_RESULTS = "results";
    private static final String CARD_LOADING = "loading";

    // Common env var names used for Gemini/Google GenAI keys
    private static final Set<String> KEY_NAMES = new HashSet<>();
    static {
        KEY_NAMES.add("API_KEY");
        KEY_NAMES.add("GOOGLE_API_KEY");
        KEY_NAMES.add("GOOGLE_GENAI_API_KEY");
        KEY_NAMES.add("GEMINI_API_KEY");
        KEY_NAMES.add("GENAI_API_KEY");
    }

    public CourseExplorerPanel(RecommendCoursesController recommendController,
                               ProfileController profileController,
                               RecommendCoursesViewModel viewModel) {
        this(recommendController, profileController, viewModel, new WeightedKeywordGenerator());
    }

    public CourseExplorerPanel(RecommendCoursesController controller,
                               ProfileController profileController,
                               RecommendCoursesViewModel viewModel,
                               KeywordGenerator keywordGenerator) {
        this.recommendController = controller;
        this.profileController = profileController;
        this.viewModel = viewModel;
        this.keywordGenerator = keywordGenerator;

        this.viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        JPanel leftPanel = createSurveyPanel();
        JPanel rightPanel = createRecommendedPanel();

        leftPanel.setPreferredSize(new Dimension(300, 1));
        leftPanel.setMinimumSize(new Dimension(240, 1));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel
        );
        splitPane.setDividerSize(4);
        splitPane.setResizeWeight(1.0);
        add(splitPane, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(300));

        profileController.loadProfile();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null && submitButton != null) root.setDefaultButton(submitButton);
    }

    // =======================
    // VIEW LOGIC: React to VM
    // =======================
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();

        if (RecommendCoursesViewModel.PROPERTY_RECOMMENDATIONS.equals(prop)) {
            @SuppressWarnings("unchecked")
            List<Course> courses = (List<Course>) evt.getNewValue();
            restoreIdle();
            updateResultsView(courses);
        } else if (RecommendCoursesViewModel.PROPERTY_PROFILE_LOADED.equals(prop)) {
            this.completedCourses = viewModel.getCompletedCoursesState();
            this.interestsArea.setText(viewModel.getInterestsState());
        } else if (RecommendCoursesViewModel.PROPERTY_ERROR.equals(prop)) {
            restoreIdle();
            JOptionPane.showMessageDialog(this, evt.getNewValue(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateResultsView(List<Course> courses) {
        coursesContainer.removeAll();
        if (courses == null || courses.isEmpty()) {
            recommendedCardLayout.show(recommendedCardPanel, CARD_PLACEHOLDER);
        } else {
            for (Course c : courses) {
                coursesContainer.add(new CourseResultPanel(c));
            }
            coursesContainer.revalidate();
            coursesContainer.repaint();
            recommendedCardLayout.show(recommendedCardPanel, CARD_RESULTS);
        }
    }

    // =======================
    // USER ACTIONS
    // =======================
    private void handleSubmit() {
        // Guard: require API key present (env or .env)
        if (!hasStoredApiKey()) {
            JOptionPane.showMessageDialog(
                    this,
                    "API key not found in environment/.env.\nClick \"Set API Key\" to add it before getting recommendations.",
                    "API Key required",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String interests = interestsArea.getText().trim();
        if (interests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter some interests first!", "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        showLoading();
        recommendController.execute(interests, completedCourses);
    }

    private void handleSave() {
        profileController.saveProfile(completedCourses, interestsArea.getText());
        JOptionPane.showMessageDialog(this, "Saved locally.", "Save", JOptionPane.INFORMATION_MESSAGE);
    }

    // =======================
    // UI BUILDERS
    // =======================
    private JPanel createSurveyPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Interest Survey");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton coursesButton = new JButton("Courses I've taken");
        coursesButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        coursesButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        coursesButton.addActionListener(e -> {
            CoursesTakenDialog d = new CoursesTakenDialog(this, completedCourses);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
            if (d.isConfirmed()) completedCourses = d.getCourses();
        });

        JButton apiKeyButton = new JButton("Set API Key");
        apiKeyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        apiKeyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        apiKeyButton.addActionListener(e -> {
            ApiKeyDialog d = new ApiKeyDialog(this, profileController);
            d.setVisible(true);
        });

        JLabel interestsLabel = new JLabel("What are your interests?");
        interestsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton notSureButton = new JButton("Not sure…");
        notSureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        notSureButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        notSureButton.addActionListener(e -> {
            PreferenceDialog d = new PreferenceDialog(this, keywordGenerator);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
            List<String> kws = d.getResultKeywords();
            if (kws != null && !kws.isEmpty()) interestsArea.setText(String.join(", ", kws));
        });

        interestsArea.setLineWrap(true);
        interestsArea.setWrapStyleWord(true);
        JScrollPane interestsScroll = new JScrollPane(interestsArea);
        interestsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        interestsScroll.setPreferredSize(new Dimension(200, 200));
        interestsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        JButton saveButton = new JButton("Save State");
        saveButton.addActionListener(e -> handleSave());

        submitButton = new JButton("Get Recommendations");
        submitButton.addActionListener(e -> handleSubmit());

        JPanel actionsRow = new JPanel(new GridLayout(1, 2, 10, 0));
        actionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsRow.add(saveButton);
        actionsRow.add(submitButton);
        actionsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(coursesButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(apiKeyButton);
        panel.add(Box.createVerticalStrut(20));
        panel.add(interestsLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(notSureButton);
        panel.add(Box.createVerticalStrut(8));
        panel.add(interestsScroll);
        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalStrut(16));
        panel.add(actionsRow);

        return panel;
    }

    private JPanel createRecommendedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Recommended Courses");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        // Placeholder
        JLabel placeholderLabel = new JLabel(
                "<html><div style='text-align: center;'><i>Once you complete the interest survey,<br>recommended courses will appear here!</i></div></html>",
                SwingConstants.CENTER
        );
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.add(placeholderLabel, BorderLayout.CENTER);

        // Results container
        coursesContainer.setLayout(new BoxLayout(coursesContainer, BoxLayout.Y_AXIS));
        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.add(coursesContainer, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(scrollWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Loading card
        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        loadingPanel.setBorder(new EmptyBorder(40, 20, 40, 20));
        JLabel loadingText = new JLabel("Loading recommendations…", SwingConstants.CENTER);
        loadingText.setAlignmentX(Component.CENTER_ALIGNMENT);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingPanel.add(loadingText);
        loadingPanel.add(Box.createVerticalStrut(12));
        loadingPanel.add(bar);

        recommendedCardPanel.add(placeholderPanel, CARD_PLACEHOLDER);
        recommendedCardPanel.add(scrollPane, CARD_RESULTS);
        recommendedCardPanel.add(loadingPanel, CARD_LOADING);
        recommendedCardLayout.show(recommendedCardPanel, CARD_PLACEHOLDER);

        panel.add(recommendedCardPanel, BorderLayout.CENTER);
        return panel;
    }

    // =======================
    // Loading helpers
    // =======================
    private void showLoading() {
        if (submitButton != null) submitButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        recommendedCardLayout.show(recommendedCardPanel, CARD_LOADING);
        recommendedCardPanel.revalidate();
        recommendedCardPanel.repaint();
    }

    private void restoreIdle() {
        setCursor(Cursor.getDefaultCursor());
        if (submitButton != null) submitButton.setEnabled(true);
    }

    // =======================
    // API key presence (env / .env)
    // =======================
    private boolean hasStoredApiKey() {
        try {
            // 1) Dotenv (uses working dir .env by default)
            try {
                Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
                for (String name : KEY_NAMES) {
                    String v = dotenv.get(name);
                    if (v != null && !v.isBlank()) return true;
                }
            } catch (Throwable ignore) { /* Dotenv not available or failed */ }

            // 2) System environment variables
            for (String name : KEY_NAMES) {
                String v = System.getenv(name);
                if (v != null && !v.isBlank()) return true;
            }

            // 3) Fallback: parse ~/.course_explorer/.env and ./ .env manually
            Path homeEnv = Paths.get(System.getProperty("user.home"), ".course_explorer", ".env");
            if (Files.exists(homeEnv) && fileHasAnyKey(homeEnv)) return true;

            Path localEnv = Paths.get(".env");
            return Files.exists(localEnv) && fileHasAnyKey(localEnv);

        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean fileHasAnyKey(Path envPath) {
        try {
            for (String line : Files.readAllLines(envPath, StandardCharsets.UTF_8)) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;
                int eq = t.indexOf('=');
                if (eq <= 0) continue;
                String key = t.substring(0, eq).trim();
                String val = t.substring(eq + 1).trim();
                if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                    val = val.substring(1, val.length() - 1);
                }
                if (KEY_NAMES.contains(key) && !val.isBlank()) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}