package ui;

import storage.AppStateStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import interface_adapter.profile.ProfileController;

public class ApiKeyDialog extends JDialog {

    private final JTextField apiKeyField = new JTextField();
    private final JPasswordField passField = new JPasswordField();

    public ApiKeyDialog(Component parent, ProfileController controller) {
        super(SwingUtilities.getWindowAncestor(parent), "Set API Key", Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel apiLabel = new JLabel("API Key:");
        apiLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        apiKeyField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        content.add(apiLabel);
        content.add(apiKeyField);
        content.add(Box.createVerticalStrut(10));

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> {
            String apiKey = apiKeyField.getText().trim();
            if (apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter API key and passphrase.", "Missing input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                controller.saveApiKey(apiKey);
                JOptionPane.showMessageDialog(this, "API key saved securely.", "Saved", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to save API key: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        pack();
        setMinimumSize(new Dimension(520, getHeight()));
        setLocationRelativeTo(parent);
    }
}