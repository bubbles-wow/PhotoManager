package util.ui.component;

import cn.hutool.core.img.gif.GifDecoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static util.core.GlobalResources.ICON_FONT;
import static util.core.GlobalResources.errorDialog;

public class PhotoLabel extends JLabel {
    private File file;

    private ImageIcon[] originIcons;

    private ImageIcon[] resizeIcons;

    private int delay;

    private Timer timer;

    private int index;

    private final double screenScale = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform().getScaleX();

    private double scale = 1;

    public PhotoLabel(File file) {
        this.file = file;
        this.setFont(ICON_FONT.deriveFont(Font.PLAIN, 64));
        this.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void initialize() {
        if (this.timer != null) {
            this.timer.stop();
        }
        this.timer = null;
        this.originIcons = null;
        this.resizeIcons = null;
        this.index = 0;
        this.scale = 1;
    }

    public void setFile(File file) {
        this.initialize();
        this.file = file;
        this.load();
    }

    public void setScale(double scaleOffset) {
        if (this.timer != null) {
            this.timer.stop();
        }
//        this.setIcon(null);
        this.scale += scaleOffset / this.screenScale;
        if (this.scale < 0.1) {
            this.scale = 0.1;
        }
        int newWidth = (int) (this.originIcons[0].getIconWidth() * this.scale);
        int newHeight = (int) (this.originIcons[0].getIconHeight() * this.scale);
        this.resizeIcon(newWidth, newHeight);
    }

    public void load() {
        if (this.file.getAbsolutePath().toLowerCase().endsWith(".gif")) {
            loadGif();
        }
        else {
            loadPicture();
        }
        System.out.println("load picture: " + this.file.toString());
    }

    public void load(int width, int height) {
        Dimension size = calculateSize(width, height);
        this.scale = (double) size.width / this.originIcons[0].getIconWidth();
        resizeIcon(size.width, size.height);
    }

    private void loadGif() {
        if (this.timer != null) {
            this.timer.stop();
        }
        this.timer = null;
        if (this.resizeIcons != null && this.resizeIcons[0] != null) {
            this.timer = new Timer(this.delay, e -> {
                setIcon(this.resizeIcons[index]);
                index = (index + 1) % resizeIcons.length;
            });
            this.timer.start();
        }
        else {
            try {
                GifDecoder decoder = new GifDecoder();
                decoder.read(this.file.getAbsolutePath());
                int frameCount = decoder.getFrameCount();
                this.originIcons = new ImageIcon[frameCount];
                this.resizeIcons = new ImageIcon[frameCount];
                for (int i = 0; i < frameCount; i++) {
                    this.originIcons[i] = new ImageIcon(decoder.getFrame(i));
                }
                this.setSize(this.originIcons[0].getIconWidth(), this.originIcons[0].getIconHeight());
                if (frameCount < 2) {
                    this.setIcon(this.originIcons[0]);
                    this.setText("");
                    return;
                }
                this.delay = decoder.getDelay(0);
                this.timer = new Timer(this.delay, e -> {
                    setIcon(this.originIcons[index]);
                    index = (index + 1) % originIcons.length;
                });
                this.timer.start();
            } catch (Exception e) {
                errorDialog("无法读取文件 " + this.file.getAbsolutePath(), e);
            }
            this.setText("");
        }
    }

    private void loadPicture() {
        if (this.resizeIcons != null && this.resizeIcons[0] != null) {
            this.setIcon(this.resizeIcons[0]);
            this.setText("");
        }
        else {
            this.originIcons = new ImageIcon[1];
            this.resizeIcons = new ImageIcon[1];
            if (this.file.getAbsolutePath().toLowerCase().endsWith(".bmp")) {
                try {
                    BufferedImage image = ImageIO.read(this.file);
                    this.originIcons[0] = new ImageIcon(image);
                } catch (IOException e) {
                    errorDialog("无法读取文件 " + this.file.toString(), e);
                }
            }
            else {
                this.originIcons[0] = new ImageIcon(this.file.getAbsolutePath());
            }
            this.setSize(this.originIcons[0].getIconWidth(), this.originIcons[0].getIconHeight());
            this.setIcon(this.originIcons[0]);
            this.setText("");
        }
    }

    private Dimension calculateSize(int width, int height) {
        int originWidth = this.originIcons[0].getIconWidth();
        int originHeight = this.originIcons[0].getIconHeight();
        if ((double) width / originWidth - (double) height / originHeight > 0.0001) {
            originWidth = (int) (height * ((double) originWidth / originHeight));
            originHeight = height;
        } else if ((double) width / originWidth - (double) height / originHeight < -0.0001) {
            originHeight = (int) (width * ((double) originHeight / originWidth));
            originWidth = width;
        } else {
            originWidth = width;
            originHeight = height;
        }
        return new Dimension(originWidth, originHeight);
    }

    public Dimension getOriginImageSize() {
//        ImageIcon image = this.resizeIcons[0] == null ? this.originIcons[0] : this.resizeIcons[0];
        return new Dimension(this.originIcons[0].getIconWidth(), this.originIcons[0].getIconHeight());
    }

    public Dimension getCurrentImageSize() {
        ImageIcon image = this.resizeIcons[0] == null ? this.originIcons[0] : this.resizeIcons[0];
        return new Dimension(image.getIconWidth(), image.getIconHeight());
    }

    private ImageIcon getResizeIcon(Image image, int width, int height) {
        ImageIcon icon = new ImageIcon(image);
        icon.setImage(icon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
        return icon;
    }

    private void resizeIcon(int width, int height) {
        if (this.originIcons == null) {
            return;
        }
        for (int i = 0; i < this.originIcons.length; i++) {
            this.resizeIcons[i] = getResizeIcon(this.originIcons[i].getImage(), width, height);
        }
        this.load();
        this.setSize(width, height);
    }
}
