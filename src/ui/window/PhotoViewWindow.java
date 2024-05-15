package ui.window;

import javax.swing.*;

import ui.panel.PhotoViewItem;
import util.ui.component.ScrollPane;
import util.ui.component.PhotoLabel;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class PhotoViewWindow {

    private final List<Component> files = new ArrayList<>();

    private int point;

    private Timer timer;

    private boolean isPlaying = false;

    private boolean isFullScreen = false;

    private boolean isFitMode = false;

    private final JPanel mainPanel = new JPanel();

    private PhotoLabel imageLabel;

    private final JPanel buttonPanel = new JPanel();
    
    private final JFrame window = new JFrame("图片查看器") {
        @Override
        public void dispose() {
            if (isPlaying) {
                timer.stop();
            }
            super.dispose();
        }
    };

    public PhotoViewWindow(List<Component> fileList, int point) {
        this.files.addAll(fileList);
        this.point = point;
        this.initialWindow();
    }

    public void show() {
        this.window.requestFocus();
        this.window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.window.setVisible(true);
    }

    private void initialWindow() {
        this.window.setSize(800, 600);
        this.window.setLocationRelativeTo(null);
        this.window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.initialMainPanel();
        this.window.add(this.mainPanel);
        this.window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    quitPlay();
                }
            }
        });
        this.window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if (isPlaying) {
                    timer.stop();
                }
            }
        });
        this.window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension originImageSize = imageLabel.getOriginImageSize();
                ScrollPane scrollPane = null;
                for (Component comp : mainPanel.getComponents()) {
                    if (comp instanceof ScrollPane) {
                        scrollPane = (ScrollPane) comp;
                        break;
                    }
                }
                if (scrollPane != null) {
                    scrollPane.getHorizontalScrollBar().setEnabled(false);
                }
                if (isPlaying && isFitMode) {
                    if (isFullScreen) {
                        imageLabel.load(window.getWidth(), window.getHeight());
                    }
                    else {
                        imageLabel.load(window.getWidth() - 12, window.getHeight() - 36);
                    }
                }
                else if (originImageSize.width > window.getWidth() || originImageSize.height > window.getHeight() - buttonPanel.getHeight() - 36) {
                    imageLabel.load(window.getWidth() - 12, window.getHeight() - buttonPanel.getHeight() - 36);
                }
                else {
                    imageLabel.load(originImageSize.width, originImageSize.height);
                }
                if (scrollPane != null) {
                    scrollPane.getHorizontalScrollBar().setEnabled(true);
                }
            }
        });
    }

    private void initialMainPanel() {
        if (this.files.get(this.point) instanceof PhotoViewItem item) {
            this.imageLabel = new PhotoLabel(item.getFile());
            this.imageLabel.load();
        }
        ScrollPane scrollPane = new ScrollPane(this.imageLabel);
        scrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    quitPlay();
                }
            }
        });
        this.initialButtonPanel();
        this.mainPanel.setLayout(new BorderLayout());
        this.mainPanel.add(scrollPane, BorderLayout.CENTER);
        this.mainPanel.add(this.buttonPanel, BorderLayout.SOUTH);
    }

    private void initialButtonPanel() {
        JButton previous = new JButton("上一张");
        JButton next = new JButton("下一张");
        JButton play = new JButton("播放");
        JButton zoomIn = new JButton("放大");
        JButton zoomOut = new JButton("缩小");
        this.buttonPanel.add(previous);
        this.buttonPanel.add(next);
        this.buttonPanel.add(play);
        this.buttonPanel.add(zoomIn);
        this.buttonPanel.add(zoomOut);
        previous.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (point > 0) {
                    point--;
                    if (files.get(point) instanceof PhotoViewItem item) {
                        reload(item);
                    }
                }
                else {
                    JOptionPane.showMessageDialog(null, "已经是第一张图片了", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        next.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (point < files.size() - 1) {
                    point++;
                    if (files.get(point) instanceof PhotoViewItem item) {
                        reload(item);
                    }
                }
                else {
                    JOptionPane.showMessageDialog(null, "已经是最后一张图片了", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        play.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new PlayOptionWindow(PhotoViewWindow.this);
            }
        });
        zoomIn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                imageLabel.setScale(0.1);
            }
        });
        zoomOut.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                imageLabel.setScale(-0.1);
            }
        });
    }

    private void reload(PhotoViewItem item) {
        this.imageLabel.setFile(item.getFile());
        Dimension imageSize = this.imageLabel.getOriginImageSize();
        if (imageSize.width > this.window.getWidth() || imageSize.height > this.window.getHeight() - this.buttonPanel.getHeight() - 36) {
            if (!isFullScreen) {
                this.imageLabel.load(this.window.getWidth() - 12, this.window.getHeight() - this.buttonPanel.getHeight() - 36);
            }
            else {
                this.imageLabel.load(this.window.getWidth(), this.window.getHeight() - this.buttonPanel.getHeight() - 36);
            }
        }
    }

    private void quitPlay() {
        if (isPlaying) {
            this.timer.stop();
            this.isPlaying = false;
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (device.isFullScreenSupported()) {
                this.isFullScreen = false;
                device.setFullScreenWindow(null);
            }
            this.window.setTitle("图片查看器");
            this.buttonPanel.setVisible(true);
        }
        if (files.get(point) instanceof PhotoViewItem item) {
            reload(item);
        }
    }

    /**
     * 幻灯片放映
     * @param delay 播放间隔
     * @param mode 播放模式：0 顺序播放 1 倒序播放 2 随机播放
     * @param loop 是否循环播放
     * @param fit 是否图片自适应
     */
    public void play(int delay, int mode, boolean loop, boolean fit, boolean fullScreen) {
        if (this.timer != null) {
            this.timer.stop();
        }
        this.isPlaying = true;
        this.isFitMode = fit;
        this.window.setTitle("幻灯片放映 - 图片查看器");
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (device.isFullScreenSupported() && fullScreen) {
            device.setFullScreenWindow(this.window);
            this.isFullScreen = true;
        }
        if (fit) {
            this.imageLabel.load(this.window.getWidth(), this.window.getHeight());
        }
        this.timer = new Timer(delay, e -> {
            if (mode == 0) {
                if (this.point < this.files.size() - 1) {
                    this.point++;
                }
                else {
                    if (loop) {
                        this.point = 0;
                    }
                    else {
                        if (device.isFullScreenSupported()) {
                            device.setFullScreenWindow(null);
                        }
                        JOptionPane.showMessageDialog(null, "已经是最后一张图片了", "提示", JOptionPane.INFORMATION_MESSAGE);
                        this.quitPlay();
                    }
                }
            }
            else if (mode == 1) {
                if (this.point > 0) {
                    this.point--;
                }
                else {
                    if (loop) {
                        this.point = this.files.size() - 1;
                    }
                    else {
                        if (device.isFullScreenSupported()) {
                            device.setFullScreenWindow(null);
                        }
                        JOptionPane.showMessageDialog(null, "已经是第一张图片了", "提示", JOptionPane.INFORMATION_MESSAGE);
                        this.quitPlay();
                    }
                }
            }
            else if (mode == 2) {
                this.point = (int) (Math.random() * this.files.size());
            }
            if (this.files.get(this.point) instanceof PhotoViewItem item) {
                this.imageLabel.setFile(item.getFile());
                Dimension imageSize = this.imageLabel.getOriginImageSize();
                if (fit || imageSize.width > this.window.getWidth() || imageSize.height > this.window.getHeight() - 36) {
                    if (isFullScreen) {
                        this.imageLabel.load(this.window.getWidth(), this.window.getHeight());
                    }
                    else {
                        this.imageLabel.load(this.window.getWidth() - 12, this.window.getHeight() - 36);
                    }
                }
            }
            System.out.println("play picture: " + this.point);
        });
        if (!this.window.isVisible()) {
            this.show();
        }
        this.buttonPanel.setVisible(false);
        this.timer.start();
    }
}
