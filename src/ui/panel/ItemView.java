package ui.panel;

import util.ui.component.GifPlayer;
import util.ui.layout.MyLayoutManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static util.core.GlobalResources.*;

public class ItemView extends JPanel {

    private File file;

    private JLabel label;

    private JPanel namePanel;

    public boolean isLoad = false;

    public boolean isSelected = false;

    public boolean isRenaming = false;

    private boolean isHover = false;

    public ItemView(File file, int width, int height) {
        initialItem(file, width, height);
        this.setBackground(BACKGROUND);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHover = true;
                if (isSelected) {
                    setBackground(SELECT);
                } else {
                    setBackground(HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHover = false;
                if (isSelected) {
                    setBackground(SELECT);
                } else {
                    setBackground(BACKGROUND);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    // 选中状态改由父组件管理
                    if (isRenaming) {
                        isRenaming = false;
                        quitRename();
                    }
                    if (isSelected) {
                        setBackground(SELECT);
                    } else {
                        if (isHover) {
                            setBackground(HOVER);
                        } else {
                            setBackground(BACKGROUND);
                        }
                    }
                }
                // 鼠标事件转发给面板
                Point parentPoint = SwingUtilities.convertPoint(ItemView.this, e.getPoint(), getParent());
                MouseEvent parent = new MouseEvent(getParent(), e.getID(), e.getWhen(), e.getModifiersEx(),
                        parentPoint.x,
                        parentPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
                getParent().dispatchEvent(parent);
            }
        });
    }

    /**
     * 初始化图片项
     *
     * @param file   图片文件
     * @param width  宽度
     * @param height 高度
     */
    private void initialItem(File file, int width, int height) {
        this.setLayout(new ItemViewLayout());
        this.setSize(width, height);
        this.file = file;
        if (file.getAbsolutePath().toLowerCase().endsWith(".gif")) {
            this.label = new GifPlayer(file.getAbsolutePath());
        }
        else {
            this.label = new JLabel();
        }
        this.label.setFont(ICON_FONT.deriveFont(Font.PLAIN, 64));
        this.label.setHorizontalAlignment(SwingConstants.CENTER);
        loadIcon();
        this.namePanel = new JPanel();
        this.namePanel.setSize(width, height / 5);
        this.namePanel.setBackground(new Color(0, 0, 0, 0));
        this.namePanel.setLayout(new BorderLayout());

        // 文件名显示
        JLabel name = new JLabel(file.getName());
        name.setHorizontalAlignment(SwingConstants.CENTER);
        name.setSize(width, height / 5);
        name.setVisible(true);
        // 文件名过长时可以通过提示来查看完整文件名
        name.setToolTipText(file.getName());
        // 转发鼠标事件，使得鼠标事件不会被提示条占用
        name.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                Point parentPoint = SwingUtilities.convertPoint(name, e.getPoint(), namePanel);
                MouseEvent parent = new MouseEvent(getParent(), e.getID(), e.getWhen(), e.getModifiersEx(),
                        parentPoint.x,
                        parentPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
                namePanel.getParent().dispatchEvent(parent);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                Point parentPoint = SwingUtilities.convertPoint(name, e.getPoint(), namePanel);
                MouseEvent parent = new MouseEvent(getParent(), e.getID(), e.getWhen(), e.getModifiersEx(),
                        parentPoint.x,
                        parentPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
                namePanel.getParent().dispatchEvent(parent);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (isSelected && !isRenaming && e.getButton() == MouseEvent.BUTTON1) {
                    isRenaming = true;
                    for (Component comp : namePanel.getComponents()) {
                        if (comp instanceof JTextField) {
                            comp.setVisible(true);
                            comp.requestFocus();
                        }
                        if (comp instanceof JLabel) {
                            comp.setVisible(false);
                        }
                    }
                    repaint();
                    return;
                }
                Point parentPoint = SwingUtilities.convertPoint(name, e.getPoint(), namePanel);
                MouseEvent parent = new MouseEvent(getParent(), e.getID(), e.getWhen(), e.getModifiersEx(),
                        parentPoint.x,
                        parentPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
                namePanel.getParent().dispatchEvent(parent);
            }
        });
        // 重命名框
        // 使用正则表达式去除文件后缀名
        JTextField renameBar = initialRenameBar(file.getName().replaceFirst("[.][a-zA-Z]+$", ""), width, height);
        this.namePanel.add(name, BorderLayout.CENTER);
        this.namePanel.add(renameBar, BorderLayout.CENTER);

        this.add(this.label);
        this.add(this.namePanel);
    }

    /**
     * 初始化重命名框
     * 
     * @param name   文本框初始内容
     * @param width  宽度
     * @param height 高度
     * @return 文本框对象
     */
    private JTextField initialRenameBar(String name, int width, int height) {
        JTextField renameBar = new JTextField(name);
        renameBar.setSize(width, height / 5);
        renameBar.setVisible(false);
        renameBar.setHorizontalAlignment(SwingConstants.CENTER);
        // 检测到输入回车结束重命名
        renameBar.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    quitRename();
                }
            }
        });
        return renameBar;
    }

    private Dimension calculateSize(int iconWidth, int iconHeight) {
        int width = this.getWidth();
        int height = this.getHeight();
        if ((double) iconWidth / width > iconHeight / (height * 0.8)) {
            iconHeight = (width - 2) * iconHeight / iconWidth;
            iconWidth = width - 2;
        } else if ((double) iconWidth / width < iconHeight / (height * 0.8)) {
            iconWidth = (int) (height * 0.8 - 2) * iconWidth / iconHeight;
            iconHeight = (int) (height * 0.8 - 2);
        } else {
            iconWidth = width - 2;
            iconHeight = (int) (height * 0.8 - 2);
        }
        return new Dimension(iconWidth, iconHeight);

    }

    /**
     * 加载指定图标并显示
     *
     * @param icon 指定图标
     */
    private void loadIcon(ImageIcon icon) {
        Dimension size = calculateSize(icon.getIconWidth(), icon.getIconHeight());
        icon.setImage(icon.getImage().getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH));
        this.label.setIcon(icon);
        this.label.setSize(size.width, size.height);
        this.label.setText("");
    }

    private void loadGif(String path) {
        ImageIcon icon = new ImageIcon(path);
        Dimension size = calculateSize(icon.getIconWidth(), icon.getIconHeight());
        ((GifPlayer) this.label).load(path, size.width, size.height);
    }

    /**
     * 加载默认图标
     */
    public void loadIcon() {
        if (this.label instanceof GifPlayer gif) {
            gif.initialize();
        }
        this.label.setIcon(null);
        this.label.setSize(118, 118);
        this.label.setText("\ue91b");
        this.isLoad = false;
    }

    /**
     * 加载图片缩略图并显示
     */
    public void loadPicture() {
        this.isLoad = true;
        if (this.file.getAbsolutePath().toLowerCase().endsWith(".bmp")) {
            try {
                BufferedImage image = ImageIO.read(this.file);
                loadIcon(new ImageIcon(image));
            } catch (IOException e) {
                errorDialog("无法读取文件 " + this.file.toString(), e);
            }
        }
        else if (this.file.getAbsolutePath().toLowerCase().endsWith(".gif")) {
            loadGif(this.file.getAbsolutePath());
        }
        else {
            loadIcon(new ImageIcon(this.file.getAbsolutePath()));
        }
        System.out.println("load picture: " + this.file.toString());
    }

    public File getFile() {
        return this.file;
    }

    public void copyTo(String path, boolean force) throws FileAlreadyExistsException {
        Path target = Paths.get(path, this.file.getName());
        if (Files.exists(target) && !force) {
            throw new FileAlreadyExistsException(target.toString());
        }
        try {
            Files.copy(this.file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            new Thread(() -> errorDialog("无法复制文件 " + this.file.toString() + " 到" + path, e)).start();
        }
    }

    public void cutTo(String path, boolean force) throws FileAlreadyExistsException {
        Path target = Paths.get(path, this.file.getName());
        if (Files.exists(target) && !force) {
            throw new FileAlreadyExistsException(target.toString());
        }
        try {
            Files.move(this.file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            new Thread(() -> errorDialog("无法移动文件 " + this.file.toString() + " 到" + path, e)).start();
        }
    }

    public void delete() {
        try {
            Files.delete(this.file.toPath());
        } catch (IOException e) {
            new Thread(() -> errorDialog("无法删除文件 " + this.file.toString(), e)).start();
        }
    }

    public void rename() {
        this.isRenaming = true;
        for (Component comp : namePanel.getComponents()) {
            if (comp instanceof JTextField) {
                comp.setVisible(true);
                continue;
            }
            if (comp instanceof JLabel) {
                comp.setVisible(false);
                continue;
            }
        }
    }

    public void quitRename() {
        String newName = ((JTextField) this.namePanel.getComponent(1)).getText()
                + this.file.getName().substring(this.file.getName().lastIndexOf("."));
        this.namePanel.getComponent(1).setVisible(false);
        this.namePanel.getComponent(0).setVisible(true);
        File newFile = new File(this.file.getParent() + File.separator + newName);
        if (newName.equals(this.file.getName()) || newName.isEmpty()) {
            return;
        }
        if (newFile.exists()) {
            ((JTextField) this.namePanel.getComponent(1))
                    .setText(this.file.getName().replaceFirst("[.][a-zA-Z]+$", ""));
            new JOptionPane("文件已存在", JOptionPane.WARNING_MESSAGE).createDialog("操作已取消").setVisible(true);
        } else {
            boolean status = this.file.renameTo(newFile);
            if (!status) {
                new JOptionPane("重命名失败", JOptionPane.WARNING_MESSAGE).createDialog("操作已取消").setVisible(true);
                ((JTextField) this.namePanel.getComponent(1))
                        .setText(this.file.getName().replaceFirst("[.][a-zA-Z]+$", ""));
            } else {
                this.file = newFile;
                ((JLabel) this.namePanel.getComponent(0)).setToolTipText(newName);
                ((JLabel) this.namePanel.getComponent(0)).setText(newName);
            }

        }
    }

    /**
     * 图片项布局管理
     */
    static class ItemViewLayout extends MyLayoutManager {
        public void addLayoutComponent(Component comp, Object constraints) {
            Component parent = comp.getParent();
            int width = parent.getWidth();
            int height = parent.getHeight();
            int x = 1;
            int y = 1;
            if (comp instanceof JLabel) {
                if (comp.getWidth() == width - 2) {
                    y += ((int) (height * 0.8) - 2 - comp.getHeight()) / 2;
                } else if (comp.getHeight() == (int) (height * 0.8) - 2) {
                    x += (width - 2 - comp.getWidth()) / 2;
                }
                comp.setBounds(x, y, comp.getWidth(), comp.getHeight());
            } else if (comp instanceof JPanel) {
                comp.setSize(width, height / 5);
                comp.setBounds(1, height - comp.getHeight() + 1, comp.getWidth() - 2, comp.getHeight() - 2);
            }
        }

        @Override
        public void layoutContainer(Container parent) {
            int width = parent.getWidth();
            int height = parent.getHeight();
            int x = 1;
            int y = 1;
            for (Component comp : parent.getComponents()) {
                if (comp instanceof JLabel) {
                    if (comp.getWidth() == width - 2) {
                        y += ((int) (height * 0.8) - 2 - comp.getHeight()) / 2;
                    } else if (comp.getHeight() == (int) (height * 0.8) - 2) {
                        x += (width - 2 - comp.getWidth()) / 2;
                    }
                    comp.setBounds(x, y, comp.getWidth(), comp.getHeight());
                }
            }
        }
    }
}
