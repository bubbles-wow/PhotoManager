package util.ui.component;

import javax.swing.*;
import java.awt.*;

import static util.core.GlobalResources.ICON_FONT;

public class PopupMenuItem extends JMenuItem {

    private final String icon;

    public PopupMenuItem(String icon, String text) {
        super(text);
        this.icon = icon;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);
        g2d.setFont(ICON_FONT.deriveFont(Font.PLAIN, 16));
        g2d.drawString(icon, 6, 21);
    }
}
