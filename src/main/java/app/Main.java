package app;

import entity.CourseExplorerPanel;

import javax.swing.*;

public class Main {

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("UofT Course Explorer & Planner");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Our UI is now a panel:
        frame.add(new CourseExplorerPanel());

        frame.pack();
        frame.setLocationRelativeTo(null); // center
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }
}