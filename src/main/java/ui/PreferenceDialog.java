package ui;

import entity.DefaultKeywordSuggester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class PreferenceDialog extends JDialog {
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);

    // --- Keywords UI: fixed-size, read-only, wrapped text ---
    private final JTextArea keywordsArea = new JTextArea(3, 40);
    private final JScrollPane keywordsScroll = new JScrollPane(keywordsArea);
    private final JPanel keywordsRow = new JPanel(new BorderLayout(8, 0));

    // We directly update this target (no Consumer method reference)
    private final JTextArea interestsTarget;

    private static final List<String> BASE_ITEMS = Arrays.asList(
            "Analyzing data and patterns",
            "Creating visual designs / art",
            "Solving complex problems",
            "Helping and teaching",
            "Building websites and applications",
            "Starting projects or businesses"
    );

    PreferenceDialog(Component parent, JTextArea interestsTarget) {
        super(
                SwingUtilities.getWindowAncestor(parent),
                "Help me choose interests",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        this.interestsTarget = interestsTarget;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        // ----- Center: orderable list -----
        BASE_ITEMS.forEach(model::addElement);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(8);
        JScrollPane listScroll = new JScrollPane(list);

        JButton upBtn = new JButton("↑ Move up");
        JButton downBtn = new JButton("↓ Move down");
        upBtn.addActionListener(e -> moveSelected(-1));
        downBtn.addActionListener(e -> moveSelected(1));

        JPanel reorderButtons = new JPanel(new GridLayout(1, 2, 8, 0));
        reorderButtons.add(upBtn);
        reorderButtons.add(downBtn);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(new JLabel("Order these by your preference:"), BorderLayout.NORTH);
        center.add(listScroll, BorderLayout.CENTER);
        center.add(reorderButtons, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // ----- Fixed-size Keywords row (always visible, initially empty) -----
        keywordsArea.setEditable(false);
        keywordsArea.setFocusable(false);
        keywordsArea.setLineWrap(true);
        keywordsArea.setWrapStyleWord(true);
        keywordsArea.setText(""); // empty until Generate
        Color inactiveBg = UIManager.getColor("TextField.inactiveBackground");
        if (inactiveBg != null) keywordsArea.setBackground(inactiveBg);

        keywordsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        keywordsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        keywordsScroll.setPreferredSize(new Dimension(520, 72));
        keywordsScroll.setMinimumSize(new Dimension(520, 72));
        keywordsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        keywordsRow.add(new JLabel("Keywords:"), BorderLayout.WEST);
        keywordsRow.add(keywordsScroll, BorderLayout.CENTER);

        // ----- Buttons (Generate on far left; right side: Cancel then Apply) -----
        JButton generateBtn = new JButton("Generate");
        JButton cancelBtn = new JButton("Cancel");
        JButton applyBtn = new JButton("Apply");

        generateBtn.addActionListener(e -> onGenerate());
        cancelBtn.addActionListener(e -> dispose());
        applyBtn.addActionListener(e -> onApply());

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftButtons.add(generateBtn);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightButtons.add(cancelBtn);
        rightButtons.add(applyBtn);

        JPanel buttonsBar = new JPanel(new BorderLayout());
        buttonsBar.add(leftButtons, BorderLayout.WEST);
        buttonsBar.add(rightButtons, BorderLayout.EAST);

        // SOUTH container: keywords row (NORTH) + buttons bar (SOUTH)
        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.add(keywordsRow, BorderLayout.NORTH);
        south.add(buttonsBar, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(560, getHeight()));
        setLocationRelativeTo(parent);
    }

    private void moveSelected(int delta) {
        int idx = list.getSelectedIndex();
        if (idx == -1) return;
        int newIdx = idx + delta;
        if (newIdx < 0 || newIdx >= model.size()) return;

        String item = model.getElementAt(idx);
        model.remove(idx);
        model.add(newIdx, item);
        list.setSelectedIndex(newIdx);
        list.ensureIndexIsVisible(newIdx);
    }

    private void onGenerate() {
        List<String> ordered = new ArrayList<>();
        for (int i = 0; i < model.size(); i++) ordered.add(model.get(i));

        // Direct call; no method reference or Function
        String keywords = DefaultKeywordSuggester.suggest(ordered);
        keywordsArea.setText(keywords);
        keywordsArea.setCaretPosition(0); // scroll to top
        // No pack() to avoid resizing
        keywordsArea.revalidate();
        keywordsArea.repaint();
    }

    private void onApply() {
        String text = keywordsArea.getText();
        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Click Generate first.", "No keywords yet", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Write directly into the interests text area
        interestsTarget.setText(text);
        dispose();
    }
}