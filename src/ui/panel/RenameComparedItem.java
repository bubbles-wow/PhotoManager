package ui.panel;

import util.ui.layout.MyLayoutManager;

import javax.swing.*;
import java.awt.*;

import static util.core.GlobalResources.BACKGROUND;
import static util.core.GlobalResources.ICON_FONT;

public class RenameComparedItem extends JPanel {
    private final JLabel oldLabel = new JLabel();

    private final JLabel newLabel = new JLabel();

    public RenameComparedItem(String oldName, String newName) {
        this.oldLabel.setText(oldName);
        this.oldLabel.setToolTipText(oldName);
        this.newLabel.setText(newName);
        this.newLabel.setToolTipText(newName);
        JLabel icon = new JLabel("\ue7fd");
        icon.setFont(ICON_FONT);
        icon.setSize(20, 20);
        icon.setHorizontalTextPosition(SwingConstants.CENTER);
        this.setBackground(BACKGROUND);
        this.setLayout(new MyLayoutManager() {
            @Override
            public void layoutContainer(Container target) {
                int width = target.getWidth();
                int height = target.getHeight();
                for (Component comp : target.getComponents()) {
                    if (comp.equals(oldLabel)) {
                        comp.setBounds(0, 0, width / 2 - 16, height);
                    }
                    else if (comp.equals(newLabel)) {
                        comp.setBounds(width / 2 + 16, 0, width / 2 - 8, height);
                    }
                    else {
                        comp.setBounds(width / 2 - 12, 0, comp.getWidth(), comp.getHeight());
                    }
                }
            }
        });
        this.add(this.oldLabel);
        this.add(icon);
        this.add(this.newLabel);
    }

    public void setNewName(String newName) {
        this.newLabel.setText(newName);
        this.newLabel.setToolTipText(newName);
    }

    public String getNewName() {
        return this.newLabel.getText();
    }

    public String getOldName() {
        return this.oldLabel.getText();
    }
}
