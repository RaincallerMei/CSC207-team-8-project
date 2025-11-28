package ui;

import entity.KeywordGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class PreferenceDialog extends JDialog {
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);

    // fixed-size, read-only preview
    private final JTextArea keywordsArea = new JTextArea(3, 40);
    private final JScrollPane keywordsScroll = new JScrollPane(keywordsArea);

    private final KeywordGenerator generator;
    private List<String> resultKeywords = null; // <-- dialog "returns" this

    private static final List<String> BASE_ITEMS = Arrays.asList(
            "Analyzing data and patterns",
            "Creating visual designs / art",
            "Solving complex problems",
            "Helping and teaching",
            "Building websites or applications",
            "Starting projects or business"
    );

    PreferenceDialog(Component parent, KeywordGenerator generator) {
        super(SwingUtilities.getWindowAncestor(parent), "Help me choose interests", Dialog.ModalityType.APPLICATION_MODAL);
        this.generator = generator;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        BASE_ITEMS.forEach(model::addElement);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(8);

        JButton upBtn = new JButton("↑ Move up");
        JButton downBtn = new JButton("↓ Move down");
        upBtn.addActionListener(e -> moveSelected(-1));
        downBtn.addActionListener(e -> moveSelected(1));

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(new JLabel("Order these by your preference:"), BorderLayout.NORTH);
        center.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel reorderButtons = new JPanel(new GridLayout(1, 2, 8, 0));
        reorderButtons.add(upBtn); reorderButtons.add(downBtn);
        center.add(reorderButtons, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // Keywords preview area (read-only)
        keywordsArea.setEditable(false);
        keywordsArea.setFocusable(false);
        keywordsArea.setLineWrap(true);
        keywordsArea.setWrapStyleWord(true);
        Color inactiveBg = UIManager.getColor("TextField.inactiveBackground");
        if (inactiveBg != null) keywordsArea.setBackground(inactiveBg);
        keywordsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        keywordsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        keywordsScroll.setPreferredSize(new Dimension(520, 72));

        JPanel keywordsRow = new JPanel(new BorderLayout(8, 0));
        keywordsRow.add(new JLabel("Keywords:"), BorderLayout.WEST);
        keywordsRow.add(keywordsScroll, BorderLayout.CENTER);

        JButton generateBtn = new JButton("Generate");
        JButton cancelBtn = new JButton("Cancel");
        JButton applyBtn = new JButton("Apply");

        generateBtn.addActionListener(e -> onGenerate());
        cancelBtn.addActionListener(e -> { resultKeywords = null; dispose(); });
        applyBtn.addActionListener(e -> onApply());

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftButtons.add(generateBtn);
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightButtons.add(cancelBtn);
        rightButtons.add(applyBtn);
        JPanel buttonsBar = new JPanel(new BorderLayout());
        buttonsBar.add(leftButtons, BorderLayout.WEST);
        buttonsBar.add(rightButtons, BorderLayout.EAST);

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
        int next = idx + delta;
        if (next < 0 || next >= model.size()) return;
        String item = model.getElementAt(idx);
        model.remove(idx);
        model.add(next, item);
        list.setSelectedIndex(next);
        list.ensureIndexIsVisible(next);
    }

    private void onGenerate() {
        List<String> ordered = new ArrayList<>();
        for (int i = 0; i < model.size(); i++) ordered.add(model.get(i));
        List<String> keywords = generator.generate(ordered);
        // preview only; UI formatting here is fine
        keywordsArea.setText(String.join(", ", keywords));
        keywordsArea.setCaretPosition(0);
        // keep the list to return if user hits Apply
        resultKeywords = keywords;
    }

    private void onApply() {
        if (resultKeywords == null || resultKeywords.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Click Generate first.", "No keywords yet", JOptionPane.WARNING_MESSAGE);
            return;
        }
        dispose();
    }

    /** Returns the generated keywords, or null if cancelled. */
    public List<String> getResultKeywords() {
        return resultKeywords;
    }
}