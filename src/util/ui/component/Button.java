package util.ui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class Button extends JButton {
    private static final Color BUTTON_COLOR = new Color(0, 0, 0, 0);

    private static final Color BUTTON_HOVER = new Color(222, 222, 222, 96);

    private static final Color BUTTON_PRESS= new Color(222, 222, 222, 160);

    private static final int BUTTON_RADIUS = 10;

    public Button(String text, String info, int width, int height) {
        super(text);
        setToolTipText(info);
        setForeground(new Color(0,0,0,0));
        setFont(new Font("微软雅黑", Font.PLAIN, 10));
        setBackground(BUTTON_COLOR);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(true);
        setSize(width, height);
        //setCursor(new Cursor(Cursor.HAND_CURSOR));
        repaint();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(BUTTON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(BUTTON_COLOR);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(BUTTON_PRESS);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setBackground(BUTTON_HOVER);
            }

            public void mouseClicked(MouseEvent e) {
                todo();
            }
        });
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.clearRect(0, 0, getWidth(), getHeight());
        // 绘制字
        FontMetrics fontMetrics = g2d.getFontMetrics(this.getFont());
        int textWidth = fontMetrics.stringWidth(this.getText());
        int textHeight = fontMetrics.getHeight();
        int x = (getWidth() - textWidth) / 2;
        int y = (getHeight() - textHeight) / 2 + fontMetrics.getAscent();
        g2d.setFont(getFont());
        g2d.setColor(Color.BLACK);
        g2d.drawString(this.getText(), x, y);
        g2d.setColor(getBackground());
        Shape shape = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), BUTTON_RADIUS, BUTTON_RADIUS);
        g2d.fill(shape);
        //super.paintComponent(g);
    }

    protected void paintBorder(Graphics g) {

    }

    public void todo() {
        System.out.println(getText());
    }
}
