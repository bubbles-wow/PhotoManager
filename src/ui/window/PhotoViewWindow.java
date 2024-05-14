package ui.window;

import javax.swing.*;

import ui.panel.PhotoViewItem;
import util.ui.component.ScrollPane;
import util.ui.component.PhotoLabel;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PhotoViewWindow {

    private final List<Component> files = new ArrayList<>();

    private int point;

    private boolean isPlaying = false;

    private Timer timer;
    
    private final JFrame window = new JFrame("图片查看器") {
        @Override
        public void dispose() {
            if (isPlaying) {
                timer.stop();
            }
            super.dispose();
        }
    };

    private final JPanel mainPanel = new JPanel();

    private PhotoLabel imageLabel;

    private final JPanel buttonPanel = new JPanel();

    public PhotoViewWindow(List<Component> fileList, int point) {
        this.files.addAll(fileList);
        this.point = point;
        this.initialWindow();
    }

    public void show() {
        this.window.setVisible(true);
        this.window.requestFocus();
        this.window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.mainPanel.setSize(this.window.getSize());
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
    }

    private void initialMainPanel() {
        if (this.files.get(this.point) instanceof PhotoViewItem item) {
            this.imageLabel = new PhotoLabel(item.getFile());
            this.imageLabel.load();
            if (imageLabel.getWidth() > window.getWidth()|| imageLabel.getHeight() > window.getHeight() - buttonPanel.getHeight()) {
                imageLabel.load(window.getWidth(), window.getHeight() - buttonPanel.getHeight() - 20);
            }
        }
        ScrollPane scrollPane = new ScrollPane(this.imageLabel);
        scrollPane.setSize(800, 600);
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
        this.mainPanel.setSize(800, 600);
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
        imageLabel.setFile(item.getFile());
        if (imageLabel.getWidth() > window.getWidth() - 20 || imageLabel.getHeight() > window.getHeight() - buttonPanel.getHeight() - 20) {
            imageLabel.load(window.getWidth() - 20, window.getHeight() - buttonPanel.getHeight() - 20);
        }
    }

    private void quitPlay() {
        if (isPlaying) {
            this.timer.stop();
            this.isPlaying = false;
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (device.isFullScreenSupported()) {
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
        this.window.setTitle("幻灯片放映 - 图片查看器");
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (device.isFullScreenSupported() && fullScreen) {
            device.setFullScreenWindow(this.window);
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
                        JOptionPane.showMessageDialog(null, "已经是最后一张图片了", "提示", JOptionPane.INFORMATION_MESSAGE);
                        this.timer.stop();
                        this.isPlaying = false;
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
                        JOptionPane.showMessageDialog(null, "已经是第一张图片了", "提示", JOptionPane.INFORMATION_MESSAGE);
                        this.timer.stop();
                        this.isPlaying = false;
                    }
                }
            }
            else if (mode == 2) {
                this.point = (int) (Math.random() * this.files.size());
            }
            if (this.files.get(this.point) instanceof PhotoViewItem item) {
                this.imageLabel.setFile(item.getFile());
                if (fit) {
                    this.imageLabel.load(this.window.getWidth(), this.window.getHeight());
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
