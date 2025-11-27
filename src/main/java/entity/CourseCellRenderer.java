package entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

class CourseCellRenderer extends DefaultListCellRenderer {
    private final Insets pad = new Insets(8, 12, 8, 12);

    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        c.setBorder(new EmptyBorder(pad));
        return c;
    }
}