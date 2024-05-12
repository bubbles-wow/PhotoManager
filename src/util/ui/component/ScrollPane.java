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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.dispose();
    }
}
