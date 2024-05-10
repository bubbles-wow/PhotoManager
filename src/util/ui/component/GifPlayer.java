package util.ui.component;

import cn.hutool.core.img.gif.GifDecoder;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static util.core.GlobalResources.ICON_FONT;
import static util.core.GlobalResources.errorDialog;

public class GifPlayer extends JLabel {
    private ImageIcon[] icons;

    private Timer timer;

    private int index;

    public GifPlayer(String path, int width, int height) {
        setFont(ICON_FONT.deriveFont(Font.PLAIN, 64));
        setHorizontalAlignment(SwingConstants.CENTER);
        setSize(width, height);
        initialize();
    }

    public GifPlayer(String path) {
        initialize();
    }

    private ImageIcon getResizeIcon(BufferedImage image, int width, int height) {
        ImageIcon icon = new ImageIcon(image);
        icon.setImage(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        return icon;
    }

    public void initialize() {
        if (this.timer != null) {
            this.timer.stop();
        }
        this.timer = null;
        this.icons = null;
        this.index = 0;
    }

    public void load(String path, int width, int height) {
        setText("");
        setSize(width, height);
        try {
            GifDecoder decoder = new GifDecoder();
            decoder.read(path);
            int frameCount = decoder.getFrameCount();
            if (frameCount == 1) {
                setIcon(getResizeIcon(decoder.getFrame(0), width, height));
                return;
            }
            this.icons = new ImageIcon[frameCount];
            for (int i = 0; i < frameCount; i++) {
                this.icons[i] = getResizeIcon(decoder.getFrame(i), width, height);
            }
            int delay = decoder.getDelay(0);
            this.timer = new Timer(delay, e -> {
                setIcon(this.icons[index]);
                index = (index + 1) % icons.length;
            });
            this.timer.start();
        } catch (Exception e) {
            errorDialog("无法读取文件 " + path, e);
        }
    }

    public void start() {
        this.timer.start();
    }

    public void stop() {
        this.timer.stop();
    }
}
