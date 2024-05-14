package ui.window;

import ui.panel.PhotoComparedItem;
import ui.panel.PhotoViewItem;
import ui.panel.RenameComparedItem;
import util.ui.layout.FileViewLayout;
import util.ui.layout.MyLayoutManager;
import util.ui.component.ScrollPane;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static util.core.GlobalResources.*;

public class FileOperationWindow {
    private final List<Component> files = new ArrayList<>();

    private final List<Component> conflictingFiles = new ArrayList<>();

    private final String newPath;

    private final boolean forceReplace;

    private boolean needRename;

    private final String[] controls = {"复制", "移动", "删除", "重命名"};

    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    private final JPanel conflictPanel = new JPanel();

    private final JFrame window = new JFrame();

    private final JPanel mainPanel = new JPanel(new FileOperationWindowLayout());

    private final JLabel info = new JLabel();

    private final JProgressBar progressBar = new JProgressBar();

    public FileOperationWindow(List<Component> files, int mode, String newPath, boolean force, boolean rename) {
        this.files.addAll(files);
        this.initialWindow();
        this.newPath = newPath;
        this.forceReplace = force;
        this.needRename = rename;
        this.operate(mode);
    }

    public FileOperationWindow(List<Component> files, int mode) {
        this(files, mode, null, false, false);
    }

    public FileOperationWindow(List<Component> files, int mode, String newPath) {
        this(files, mode, newPath, false, false);
    }

    private void rename() {
        this.window.setTitle("重命名 " + this.files.size() + " 个文件...");
        for (Component comp : this.files) {
            if (comp instanceof RenameComparedItem item) {
                String newName = item.getNewName();
                File newFile = new File(Paths.get(this.newPath, newName).toString());
                if (newFile.exists() && !newFile.getName().equals(item.getOldName())) {
                    this.conflictingFiles.add(item);
                }
            }
        }
        if (!this.conflictingFiles.isEmpty()) {
            this.conflict(4);
        }
        else {
            for (Component comp : this.files) {
                if (comp instanceof RenameComparedItem item) {
                    this.info.setText("正在重命名第 " + (this.files.indexOf(item) + 1) + " 个文件...");
                    this.progressBar.setValue(this.files.indexOf(item) + 1);
                    this.window.repaint();
                    File oldFile = new File(Paths.get(this.newPath, item.getOldName()).toString());
                    File newFile = new File(Paths.get(this.newPath, item.getNewName()).toString());
                    if (oldFile.exists()) {
                        oldFile.renameTo(newFile);
                    }
                }
            }
            // 更新文件预览
            FILE_TREE.setCurrentNode(FILE_TREE.getCurrentNode());
            this.window.dispose();
        }
    }

    /**
     * 删除操作
     */
    private void delete() {
//        this.mainPanel.remove(this.info);
//        this.mainPanel.remove(this.progressBar);
        this.info.setVisible(false);
        this.progressBar.setVisible(false);
        this.mainPanel.setSize(600, 400);
        this.window.setTitle("删除 " + this.files.size() + " 个文件...");
        this.window.setSize(600, 400);
        this.window.setLocationRelativeTo(null);
        this.conflictingFiles.addAll(this.files);
        this.initialConflictPanel(3);
        this.mainPanel.add(new JLabel("以下 " + this.conflictingFiles.size() + " 个文件将被删除，请确认："));
        this.mainPanel.add(getConflictPane(3));
        JButton delete = new JButton("永久删除");
        delete.setSize(80, 30);
        JButton cancel = new JButton("取消");
        cancel.setSize(80, 30);
        JButton move = new JButton("移动至回收站");
        move.setSize(140, 30);
        this.mainPanel.add(delete);
        this.mainPanel.add(cancel);
//        this.window.add(move);
        delete.addActionListener(e -> {
            JOptionPane tip = new JOptionPane("是否要永久删除 " + conflictingFiles.size() + " 个文件？");
//            tip.setLocation(getScreenCenter(tip.getWidth(), tip.getHeight()));
            tip.setOptionType(JOptionPane.YES_NO_OPTION);
            tip.createDialog(mainPanel, "删除确认").setVisible(true);
            if (tip.getValue().equals(JOptionPane.NO_OPTION)) {
                window.dispose();
                return;
            }
            mainPanel.removeAll();
            window.setTitle("删除 " + files.size() + " 个文件...");
            initialWindow();
            mainPanel.repaint();
            for (int i = 1; i <= files.size(); i++) {
                info.setText("正在删除第 " + i + " 个文件...");
                if (conflictingFiles.get(i - 1) instanceof PhotoViewItem item) {
                    item.delete();
                }
                progressBar.setValue(i);
                window.repaint();
            }
            updateFileView();
            window.dispose();
        });
        cancel.addActionListener(e -> window.dispose());
        move.addActionListener(e -> {
//                new FileOperationWindow(conflictingFiles, 2, newPath, true, true);
            window.dispose();
        });
    }

    /**
     * 获取冲突文件预览面板
     * @return ScrollPane冲突文件预览面板
     */
    private ScrollPane getConflictPane(int mode) {
        ScrollPane pane = new ScrollPane(this.conflictPanel);
        int offsetX = 50;
        int offsetY = 160;
        if (mode == 3) {
            if (this.conflictingFiles.size() > 3) {
                offsetX = 34;
            }
            offsetY = 130;
        }
        else if (mode == 4) {
            if (conflictingFiles.size() > 10) {
                offsetX = 34;
            }
        }
        else {
            if (this.conflictingFiles.size() > 1) {
                offsetX = 34;
            }
        }
        pane.setSize(this.window.getWidth() - offsetX, this.window.getHeight() - offsetY);
        pane.remove(pane.getHorizontalScrollBar());
        return pane;
    }

    /**
     * 文件操作
     */
    public void operate(int mode) {
        if (mode == 3) {
            this.delete();
            return;
        }
        else if (mode == 4) {
            this.rename();
            return;
        }
        this.window.setTitle(controls[mode - 1] + " " + files.size() + " 个文件...");
        for (int i = 1; i <= this.files.size(); i++) {
            this.info.setText("正在" + controls[mode - 1] + "第 " + i + " 个文件...");
            if (files.get(i - 1) instanceof PhotoViewItem item) {
                // 新文件夹与文件当前所在的文件夹相同，需要添加后缀
                if (!this.needRename && item.getFile().getParent().equals(newPath)) {
                    this.needRename = true;
                }
                try {
                    if (mode == 1) {
                        if (this.needRename) {
                            // 获取新文件名
                            String newPath = Paths.get(this.newPath, this.getDifferentName(this.newPath, item.getFile().getName())).toString();
                            this.copy(item.getFile().toString(), newPath);
                        } else {
                            item.copyTo(this.newPath, this.forceReplace);
                        }
                    } else if (mode == 2) {
                        if (this.needRename) {
                            String newPath = Paths.get(this.newPath, this.getDifferentName(this.newPath, item.getFile().getName())).toString();
                            this.cut(item.getFile().toString(), newPath);
                        } else {
                            item.cutTo(this.newPath, this.forceReplace);
                        }
                    }
                } catch (FileAlreadyExistsException e) {
                    // 将文件添加到冲突列表
                    this.conflictingFiles.add(item);
                } catch (IOException e) {
                    errorDialog("进行文件操作时出现错误：" + files.get(i - 1).toString(), e);
                }
            }
            this.progressBar.setValue(i);
            this.window.repaint();
        }
        if (!conflictingFiles.isEmpty()) {
            this.conflict(mode);
        } else {
            // 文件操作完成
            this.updateFileView();
            this.window.dispose();
        }
    }

    /**
     * 处理冲突文件
     * @param mode 文件操作模式：1 复制，2 移动
     */
    private void conflict(int mode) {
        this.window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        this.mainPanel.remove(this.info);
//        this.mainPanel.remove(this.progressBar);
        this.info.setVisible(false);
        this.progressBar.setVisible(false);
        this.window.setTitle(conflictingFiles.size() + " 个文件发生冲突...");
        this.window.setSize(600, 400);
        this.window.setLocationRelativeTo(null);
        this.mainPanel.setBounds(0, 0, 600, 400);
        this.initialConflictPanel(mode);
        // 冲突文件列表
        ScrollPane pane = getConflictPane(mode);
        String oldPath = null;
        if (this.conflictingFiles.getFirst() instanceof PhotoViewItem item) {
            oldPath = item.getFile().getParent();
        }
        if (mode == 1 || mode == 2) {
            this.mainPanel.add(new JLabel(String.format("%s %d 个文件时以下 %d 个文件有冲突，请选择操作：", controls[mode - 1], this.files.size(), this.conflictingFiles.size())));
            // 说明信息
            JPanel panel = new JPanel();
            JLabel label1 = new JLabel("在路径 " + oldPath + " 中：");
            label1.setToolTipText(oldPath);
            label1.setVisible(true);
            JLabel label2 = new JLabel("在路径 " + this.newPath + " 中：");
            label2.setToolTipText(this.newPath);
            label2.setVisible(true);
            panel.setLayout(null);
            // 左边是源文件父目录
            panel.add(label1);
            label1.setBounds(0, 0, pane.getWidth() / 2, 20);
            // 右边是目标文件夹目录
            panel.add(label2);
            label2.setBounds(pane.getWidth() / 2, 0, pane.getWidth() / 2, 20);
            panel.setSize(pane.getWidth(), 20);
            // 保证JPanel要在ScrollPane之前添加，与布局管理器配合
            this.mainPanel.add(panel);
            this.mainPanel.add(pane);
            // 按钮
            JButton skip = new JButton("跳过");
            skip.setSize(60, 30);
            JButton replace = new JButton("替换全部");
            replace.setSize(80, 30);
            JButton rename = new JButton("为新文件添加后缀");
            rename.setSize(140, 30);
            this.mainPanel.add(skip);
            this.mainPanel.add(replace);
            this.mainPanel.add(rename);
            replace.addActionListener(e -> {
                new FileOperationWindow(conflictingFiles, mode, newPath, true, false);
                window.dispose();
            });
            skip.addActionListener(e -> window.dispose());
            rename.addActionListener(e -> {
                new FileOperationWindow(conflictingFiles, mode, newPath, true, true);
                window.dispose();
            });
        }
        else if (mode == 4) {
            this.mainPanel.add(new JLabel(String.format("%s %d 个文件时以下 %d 个文件有冲突，请手动调整新文件名。", controls[mode - 1], this.files.size(), this.conflictingFiles.size())));
            JPanel infoPanel2 = new JPanel();
            infoPanel2.setLayout(new GridLayout(1, 2));
            infoPanel2.setSize(pane.getWidth(), 20);
            JLabel info5 = new JLabel("旧文件名");
            info5.setSize(pane.getWidth() / 2, 20);
            JLabel info6 = new JLabel("新文件名");
            info6.setSize(pane.getWidth() / 2, 20);
            infoPanel2.add(info5);
            infoPanel2.add(info6);
            this.mainPanel.add(infoPanel2);
            this.mainPanel.add(pane);
            JButton confirm = new JButton("确认");
            confirm.setSize(80, 30);
            confirm.addActionListener(e -> this.window.dispose());
            this.mainPanel.add(confirm);
        }
    }

    /**
     * 初始化窗口
     */
    private void initialWindow() {
        this.updatePanel();
        this.window.add(this.mainPanel);
        this.window.setSize(300, 120);
        this.mainPanel.setBounds(0, 0, 300, 120);
        this.window.setLocationRelativeTo(null);
        this.window.setResizable(false);
        this.window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.window.setVisible(true);
    }

    private void updatePanel() {
        this.info.setSize(240, 20);
        this.info.setVisible(true);
        this.progressBar.setVisible(true);
        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum(files.size());
        this.progressBar.setSize(240, 20);
        this.mainPanel.setSize(300, 120);
        this.mainPanel.add(this.info);
        this.mainPanel.add(this.progressBar);
    }

    /**
     * 初始化冲突文件预览面板
     */
    private void initialConflictPanel(int mode) {
        this.conflictPanel.setSize(this.window.getWidth() - 40, this.window.getWidth() - 100);
        this.conflictPanel.setBackground(BACKGROUND);
        this.conflictPanel.setLayout(new FileViewLayout());
        for (Component component : this.conflictingFiles) {
            if (component instanceof PhotoViewItem item) {
                // 清除选中的背景色
                if (item.isSelected) {
                    item.isSelected = false;
                    item.setBackground(BACKGROUND);
                }
                if (mode == 3) {
                    PhotoViewItem oldPhotoViewItem = new PhotoViewItem(item.getFile(), item.getWidth(), item.getHeight());
                    executorService.submit(() -> oldPhotoViewItem.loadPicture());
                    this.conflictPanel.add(oldPhotoViewItem);
                }
                else {
                    String fileName = item.getFile().getName();
                    PhotoViewItem oldPhotoViewItem = new PhotoViewItem(item.getFile(), item.getWidth(), item.getHeight());
                    PhotoViewItem newPhotoViewItem = new PhotoViewItem(new File(Paths.get(this.newPath, fileName).toString()), itemSize.width, itemSize.height);
                    executorService.submit(() -> newPhotoViewItem.loadPicture());
                    executorService.submit(() -> oldPhotoViewItem.loadPicture());
                    // 左边是原文件，右边是新目录中同名的文件
                    PhotoComparedItem photoComparedItem = new PhotoComparedItem(oldPhotoViewItem, newPhotoViewItem);
                    photoComparedItem.setSize(this.conflictPanel.getWidth() - 20, itemSize.height);
                    this.conflictPanel.add(photoComparedItem);
                }
            }
            else if (component instanceof RenameComparedItem item) {
                item.setSize(this.conflictPanel.getWidth() - 20, 20);
                this.conflictPanel.add(item);
            }
        }
    }

    /**
     * 更新文件视图
     */
    private void updateFileView() {
        FILE_TREE.setCurrentNode(FILE_TREE.getCurrentNode());
    }

    /**
     * 获取新文件名
     * @param newPath 新文件夹路径
     * @param fileName 文件名
     * @return 新文件名
     */
    private String getDifferentName(String newPath, String fileName) {
        int index = fileName.lastIndexOf(".");
        String extension = null;
        if (index > 0) {
            extension = fileName.substring(index);
        }
        while (Files.exists(Paths.get(newPath, fileName))) {
            fileName = fileName.replaceFirst("[.][a-zA-Z]+$", "") + " - 副本" + extension;
        }
        return fileName;
    }

    /**
     * 复制文件
     * @param oldPath 旧文件路径
     * @param newPath 新文件路径
     * @throws IOException 文件操作异常
     */
    private void copy(String oldPath, String newPath) throws IOException {
        executorService.submit(() -> {
            try {
                Files.copy(Paths.get(oldPath), Paths.get(newPath));
            } catch (IOException e) {
                errorDialog("复制文件时出现错误：" + oldPath, e);
            }
        });
    }

    /**
     * 剪切文件
     * @param oldPath 旧文件路径
     * @param newPath 新文件路径
     * @throws IOException 文件操作异常
     */
    private void cut(String oldPath, String newPath) throws IOException {
        executorService.submit(() -> {
            try {
                Files.move(Paths.get(oldPath), Paths.get(newPath));
            } catch (IOException e) {
                errorDialog("移动文件时出现错误：" + oldPath, e);
            }
        });
    }

    /**
     * 窗口布局
     */
    static class FileOperationWindowLayout extends MyLayoutManager {
        @Override
        public void layoutContainer(Container parent) {
            int width = parent.getWidth();
            int height = parent.getHeight();
            int x = width;
            int y = 10;
            for (Component component : parent.getComponents()) {
                if (!component.isVisible()) {
                    continue;
                }
                if (component instanceof JLabel) {
                    component.setBounds(20, y, width - 40, 20);
                    y += 30;
                }
                else if (component instanceof JProgressBar) {
                    component.setBounds(20, y, width - 40, 20);
                }
                else if (component instanceof JPanel) {
                    component.setBounds(20, y, component.getWidth(), component.getHeight());
                    y += component.getHeight() + 10;
                }
                else if (component instanceof ScrollPane) {
                    component.setBounds(20, y, component.getWidth(), component.getHeight());
                }
                else if (component instanceof JButton) {
                    x -= component.getWidth() + 20;
                    component.setBounds(x, height - 40, component.getWidth(), component.getHeight());
                }
            }
        }

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
            Container parent = comp.getParent();
            this.layoutContainer(parent);
        }
    }
}
