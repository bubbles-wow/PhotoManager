package ui.panel;

import util.ui.layout.MyLayoutManager;

import javax.swing.*;
import java.awt.*;

import static util.core.GlobalResources.*;

public class PhotoComparedItem extends JPanel {
    private final Component oldItem;

    private final Component newItem;

    public PhotoComparedItem(Component oldItem, Component newItem) {
        this.oldItem = oldItem;
        this.newItem = newItem;
        this.add(oldItem);
        this.add(newItem);
        this.setBackground(BACKGROUND);
        this.setLayout(new CompareLayout());
        JLabel to = new JLabel("\ue7fd");
        to.setFont(ICON_FONT.deriveFont(Font.PLAIN, 20));
        to.setSize(20, 20);
        this.add(to);
    }

    class CompareLayout extends MyLayoutManager {
        @Override
        public void layoutContainer(Container parent) {
            int width = parent.getWidth();
            int height = parent.getHeight();
            for (Component comp : parent.getComponents()) {
                if (comp.equals(oldItem)) {
                    comp.setBounds(0, 0, itemSize.width, itemSize.height);
                }
                if (comp.equals(newItem)) {
                    comp.setBounds(width - itemSize.width, 0, itemSize.width, itemSize.height);
                }
                if (comp instanceof JLabel) {
                    comp.setBounds((width - comp.getWidth()) / 2, (height - comp.getHeight()) / 2, comp.getWidth(), comp.getHeight());
                }
            }
        }
    }
}
