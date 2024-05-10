package util.ui.component;

import javax.swing.*;
import java.awt.*;

public class ScrollPane extends JScrollPane {
    private final Component comp;
    public ScrollPane(Component comp) {
        super(comp);
        this.comp = comp;
        this.getVerticalScrollBar().setUnitIncrement(16);
        this.setBorder(null);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        this.comp.setSize(width - 20, comp.getHeight());
    }
}
