package entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

class PreferenceDialog extends JDialog {
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);

    // --- Keywords UI: fixed-size, read-only, wrapped text ---
    private final JTextArea keywordsArea = new JTextArea(3, 40);
    private final JScrollPane keywordsScroll = new JScrollPane(keywordsArea);
    private final JPanel keywordsRow = new JPanel(new BorderLayout(8, 0));

    private final KeywordSuggester suggester;
    private final Consumer<String> applyCallback;

    private static final List<String> BASE_ITEMS = Arrays.asList(
            "Analyzing data and patterns",
            "Creating visual designs / art",
            "Solving complex problems",
            "Helping and teaching",
            "Building websites or applications",
            "Starting projects or business"
    );

    PreferenceDialog(Component parent, KeywordSuggester suggester, Consumer<String> applyCallback) {
        super(
                SwingUtilities.getWindowAncestor(parent),
                "Help me choose interests",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        this.suggester = suggester;
        this.applyCallback = applyCallback;

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
        // keep colors consistent with L&F
        Color inactiveBg = UIManager.getColor("TextField.inactiveBackground");
        if (inactiveBg != null) keywordsArea.setBackground(inactiveBg);

        keywordsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        keywordsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        // Fixed size to prevent dialog widening on long text
        keywordsScroll.setPreferredSize(new Dimension(520, 72));
        keywordsScroll.setMinimumSize(new Dimension(520, 72));
        keywordsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        keywordsRow.add(new JLabel("Keywords:"), BorderLayout.WEST);
        keywordsRow.add(keywordsScroll, BorderLayout.CENTER);

        // ----- Buttons -----
        JButton generateBtn = new JButton("Generate");
        JButton applyBtn = new JButton("Apply");
        JButton cancelBtn = new JButton("Cancel");

        generateBtn.addActionListener(e -> onGenerate());
        applyBtn.addActionListener(e -> onApply());
        cancelBtn.addActionListener(e -> dispose());

        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomButtons.add(generateBtn);
        bottomButtons.add(applyBtn);
        bottomButtons.add(cancelBtn);

        // SOUTH container: keywords row (NORTH) + buttons (SOUTH)
        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.add(keywordsRow, BorderLayout.NORTH);
        south.add(bottomButtons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        pack();
        // Set a reasonable minimum width so layout stays stable
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

        String keywords = suggester.suggest(ordered);
        keywordsArea.setText(keywords);
        keywordsArea.setCaretPosition(0); // scroll to top
        // No pack() here — avoids horizontal stretching
        keywordsArea.revalidate();
        keywordsArea.repaint();
    }

    private void onApply() {
        String text = keywordsArea.getText();
        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Click Generate first.", "No keywords yet", JOptionPane.WARNING_MESSAGE);
            return;
        }
        applyCallback.accept(text);
        dispose();
    }
}