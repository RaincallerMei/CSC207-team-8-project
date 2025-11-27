package entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class CoursesTakenDialog extends JDialog {

    private final JTextArea coursesArea = new JTextArea();
    private boolean confirmed = false;

    public CoursesTakenDialog(Component parent, List<String> existingCourses) {
        super(
                SwingUtilities.getWindowAncestor(parent),
                "Courses I've taken",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        setSize(400, 300);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel instruction = new JLabel(
                "<html>Enter completed course codes, one per line<br>" +
                        "(e.g., CSC108, MAT135).</html>"
        );
        content.add(instruction, BorderLayout.NORTH);

        coursesArea.setLineWrap(true);
        coursesArea.setWrapStyleWord(true);

        if (existingCourses != null && !existingCourses.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String c : existingCourses) sb.append(c).append("\n");
            coursesArea.setText(sb.toString());
        }

        JScrollPane scrollPane = new JScrollPane(coursesArea);
        content.add(scrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        JButton save = new JButton("Save");

        cancel.addActionListener(e -> { confirmed = false; dispose(); });
        save.addActionListener(e -> { confirmed = true;  dispose(); });

        buttons.add(cancel);
        buttons.add(save);
        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
    }

    public boolean isConfirmed() { return confirmed; }

    public List<String> getCourses() {
        List<String> result = new ArrayList<>();
        String text = coursesArea.getText();
        if (text == null || text.isEmpty()) return result;

        String[] lines = text.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }
}