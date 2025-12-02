package ui;

import storage.AppStateStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import interface_adapter.profile.ProfileController;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiKeyDialog extends JDialog {

    private final JTextField apiKeyField = new JTextField();
    private final JPasswordField passField = new JPasswordField();

    private static final OkHttpClient HTTP = new OkHttpClient();

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
                JOptionPane.showMessageDialog(this, "Enter API key.", "Missing input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                // Minimal format sanity-check for Google-style keys
                if (!apiKey.startsWith("AIza") || apiKey.length() < 30) {
                    JOptionPane.showMessageDialog(this,
                            "That doesn't look like a valid Google API key (should start with \"AIza...\").",
                            "Invalid format",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Quick online verification against Generative Language API
                if (!verifyApiKeyOnline(apiKey)) {
                    JOptionPane.showMessageDialog(this,
                            "API key appears invalid or not authorized for Generative Language API.",
                            "Verification failed",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                controller.saveApiKey(apiKey);
                JOptionPane.showMessageDialog(this, "API key saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to save API key: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        pack();
        setMinimumSize(new Dimension(520, getHeight()));
        setLocationRelativeTo(parent);
    }

    private boolean verifyApiKeyOnline(String apiKey) {
        try {
            HttpUrl url = HttpUrl.parse("https://generativelanguage.googleapis.com/v1/models")
                    .newBuilder()
                    .addQueryParameter("key", apiKey)
                    .build();
            Request req = new Request.Builder().url(url).get().build();
            try (Response res = HTTP.newCall(req).execute()) {
                // 200 OK indicates the key is valid for listing models
                return res.isSuccessful();
            }
        } catch (Exception e) {
            // Network or other transient issues — treat as failure so user can retry
            return false;
        }
    }
}