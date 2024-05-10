package ui.window;

import ui.panel.CompareItemView;
import ui.panel.ItemView;
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

    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    private final JPanel conflictPanel = new JPanel();

    private final JFrame window = new JFrame();

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

    /**
     * 删除操作
     */
    public void delete() {
        this.window.remove(this.info);
        this.window.remove(this.progressBar);
        this.window.setTitle("删除 " + this.files.size() + " 个文件...");
        this.window.setSize(600, 400);
        this.window.setLocation(this.getScreenCenter(this.window.getWidth(), this.window.getHeight()));
        this.conflictingFiles.addAll(this.files);
        this.initialConflictPanel(3);
        this.window.add(new JLabel("以下 " + this.conflictingFiles.size() + " 个文件将被删除，请确认："));
        this.window.add(getConflictPane(3));
        JButton delete = new JButton("永久删除");
        delete.setSize(80, 30);
        JButton cancel = new JButton("取消");
        cancel.setSize(80, 30);
        JButton move = new JButton("移动至回收站");
        move.setSize(140, 30);
        this.window.add(delete);
        this.window.add(cancel);
//        this.window.add(move);
        delete.addActionListener(e -> {
            JOptionPane tip = new JOptionPane("是否要永久删除 " + conflictingFiles.size() + " 个文件？");
            tip.setLocation(getScreenCenter(tip.getWidth(), tip.getHeight()));
            tip.setOptionType(JOptionPane.YES_NO_OPTION);
            tip.createDialog(delete, "删除确认").setVisible(true);
            if (tip.getValue().equals(JOptionPane.NO_OPTION)) {
                window.dispose();
                return;
            }
            window.removeAll();
            window.setTitle("删除 " + files.size() + " 个文件...");
            initialWindow();
            window.add(info);
            window.add(progressBar);
            for (int i = 1; i <= files.size(); i++) {
                info.setText("正在删除第 " + i + " 个文件...");
                if (conflictingFiles.get(i - 1) instanceof ItemView item) {
                    item.delete();
                }
                progressBar.setValue(i);
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
                offsetX = 36;
            }
            offsetY = 130;
        }
        else {
            if (this.conflictingFiles.size() > 1) {
                offsetX = 36;
            }
        }
        pane.setSize(this.window.getWidth() - offsetX, this.window.getHeight() - offsetY);
        pane.remove(pane.getHorizontalScrollBar());
        return pane;
    }

    /**
     * 文件操作
     * @param mode 操作模式：1 复制，2 移动
     */
    public void operate(int mode) {
        if (mode == 3) {
            this.delete();
            return;
        }
        String control = null;
        if (mode == 1) {
            control = "复制";
        } else if (mode == 2) {
            control = "移动";
        }
        this.window.setTitle(control + " " + files.size() + " 个文件...");
        for (int i = 1; i <= this.files.size(); i++) {
            this.info.setText("正在" + control + "第 " + i + " 个文件...");
            if (files.get(i - 1) instanceof ItemView item) {
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
                this.progressBar.setValue(i);
            }
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
    public void conflict(int mode) {
        this.window.remove(this.info);
        this.window.remove(this.progressBar);
        this.window.setTitle(conflictingFiles.size() + " 个文件发生冲突...");
        this.window.setSize(600, 400);
        this.window.setLocation(this.getScreenCenter(this.window.getWidth(), this.window.getHeight()));
        this.initialConflictPanel(mode);
        if (mode == 1) {
            this.window.add(new JLabel("复制 " + this.files.size() + " 个文件时以下 " + this.conflictingFiles.size() + " 个文件有冲突，请选择操作："));
        } else if (mode == 2) {
            this.window.add(new JLabel("移动 " + this.files.size() + " 个文件时以下 " + this.conflictingFiles.size() + " 个文件有冲突，请选择操作："));
        }
        // 冲突文件列表
        ScrollPane pane = getConflictPane(mode);
        String oldPath = null;
        if (this.conflictingFiles.getFirst() instanceof ItemView item) {
            oldPath = item.getFile().getParent();
        }
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
        this.window.add(panel);
        this.window.add(pane);
        // 按钮
        JButton skip = new JButton("跳过");
        skip.setSize(60, 30);
        JButton replace = new JButton("替换全部");
        replace.setSize(80, 30);
        JButton rename = new JButton("为新文件添加后缀");
        rename.setSize(140, 30);
        this.window.add(skip);
        this.window.add(replace);
        this.window.add(rename);
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

    /**
     * 初始化窗口
     */
    public void initialWindow() {
        this.info.setVisible(true);
        this.progressBar.setBorderPainted(false);
        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum(files.size());
        this.progressBar.setVisible(true);
        this.window.setLayout(new FileOperationWindowLayout());
        this.window.add(this.info);
        this.window.add(this.progressBar);
        this.window.setSize(300, 120);
        this.window.setLocation(this.getScreenCenter(this.window.getWidth(), this.window.getHeight()));
        this.window.setResizable(false);
        this.window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.window.setBackground(Color.WHITE);
        this.window.setVisible(true);
    }

    /**
     * 初始化冲突文件预览面板
     */
    public void initialConflictPanel(int mode) {
        this.conflictPanel.setSize(this.window.getWidth() - 40, this.window.getWidth() - 100);
        this.conflictPanel.setBackground(BACKGROUND);
        this.conflictPanel.setLayout(new FileViewLayout());
        for (Component component : this.conflictingFiles) {
            if (component instanceof ItemView item) {
                // 清除选中的背景色
                if (item.isSelected) {
                    item.isSelected = false;
                    item.setBackground(BACKGROUND);
                }
                if (mode == 3) {
                    if (!item.isLoad) {
                        executorService.submit(() -> item.loadPicture());
                    }
                    item.isSelected = false;
                    item.setBackground(BACKGROUND);
                    this.conflictPanel.add(item);
                }
                else {
                    String fileName = item.getFile().getName();
                    ItemView newItemView = new ItemView(new File(Paths.get(this.newPath, fileName).toString()), itemSize.width, itemSize.height);
                    executorService.submit(() -> newItemView.loadPicture());
                    if (!item.isLoad) {
                        executorService.submit(() -> item.loadPicture());
                    }
                    // 左边是原文件，右边是新目录中同名的文件
                    CompareItemView compareItemView = new CompareItemView(item, newItemView);
                    compareItemView.setSize(this.conflictPanel.getWidth() - 20, itemSize.height);
                    this.conflictPanel.add(compareItemView);
                }
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
     * 获取窗口显示在屏幕中央的位置
     * @return Point对象的位置
     */
    public Point getScreenCenter(int width, int height) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Point((screenSize.width - width) / 2, (screenSize.height - height) / 2);
    }

    /**
     * 获取新文件名
     * @param newPath 新文件夹路径
     * @param fileName 文件名
     * @return 新文件名
     */
    public String getDifferentName(String newPath, String fileName) {
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
    public void copy(String oldPath, String newPath) throws IOException {
        Files.copy(Paths.get(oldPath), Paths.get(newPath));
    }

    /**
     * 剪切文件
     * @param oldPath 旧文件路径
     * @param newPath 新文件路径
     * @throws IOException 文件操作异常
     */
    public void cut(String oldPath, String newPath) throws IOException {
        Files.move(Paths.get(oldPath), Paths.get(newPath));
    }

    /**
     * 窗口布局
     */
    static class FileOperationWindowLayout extends MyLayoutManager {
        @Override
        public void layoutContainer(Container parent) {
            int width = parent.getWidth();
            int height = parent.getHeight();
            int x = 20;
            int y = 10;
            for (Component component : parent.getComponents()) {
                if (component instanceof JLabel) {
                    component.setBounds(x, y, width - 40, 20);
                    y += 30;
                }
                if (component instanceof JProgressBar) {
                    component.setBounds(x, y, width - 40, 20);
                }
                if (component instanceof JPanel) {
                    component.setBounds(x, y, component.getWidth(), component.getHeight());
                    y += component.getHeight() + 10;
                }
                if (component instanceof ScrollPane) {
                    component.setBounds(x, y, component.getWidth(), component.getHeight());
                }
                if (component instanceof JButton) {
                    component.setBounds(x, height - 40, component.getWidth(), component.getHeight());
                    x += component.getWidth() + 20;
                }
            }
        }
    }
}
