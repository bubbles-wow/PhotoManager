package ui.panel;

import ui.window.FileOperationWindow;
import util.ui.component.ScrollPane;
import util.core.FileNode;
import util.ui.layout.FileViewLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

import static util.core.GlobalResources.*;

public class FileView {
    private File currentFile;

    private int currentFileCount;

    private int currentSelectedFileCount;

    private boolean isLoading = false;

    private boolean isSelecting = false;

    private final List<Component> lastSelected = new ArrayList<>();

    private final List<Component> selected = new ArrayList<>();

    // ==0: undo
    // ==1: copy
    // ==2: cut
    private int selectedMode = 0;

    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    private final FileViewPopupMenu popupMenu = new FileViewPopupMenu();

    private final JPanel mainPanel = new JPanel();

    private final JPanel fileView = new JPanel();

    private final ScrollPane fileViewScrollPane = new ScrollPane(this.fileView);

    private final JLabel infoBar = new JLabel();

    public FileView() {
        this.fileView.setBackground(BACKGROUND);
        this.fileView.setLayout(new FileViewLayout());
        // 按住Ctrl键多选
        this.fileView.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    isSelecting = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    isSelecting = false;
                }
            }
        });
        this.fileView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println(e.getPoint());
                fileView.requestFocus();
                // 鼠标左键点击触发选中功能
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Component current = fileView.getComponentAt(e.getPoint());
                    // 取消选中列表所有文件的选中状态
                    if (!lastSelected.isEmpty() && e.getClickCount() == 1 && !isSelecting) {
                        for (Component component : lastSelected) {
                            if (component instanceof ItemView itemView) {
                                // 当前选中的文件不取消
                                // if (current.equals(itemView)) {
                                // continue;
                                // }
                                // 取消重命名状态
                                if (itemView.isRenaming) {
                                    itemView.isRenaming = false;
                                    itemView.quitRename();
                                }
                                itemView.isSelected = false;
                                setInfoBar(currentFileCount, --currentSelectedFileCount);
                                itemView.setBackground(BACKGROUND);
                            }
                        }
                        lastSelected.clear();
                        // if (current instanceof ItemView item) {
                        // item.isSelected = false;
                        // if (currentSelectedFileCount > 0) {
                        // setInfoBar(currentFileCount, --currentSelectedFileCount);
                        // }
                        // }
                    }
                    if (current instanceof ItemView item) {
                        // view.openFile();
                        // if (e.getClickCount() == 2) {
                        //
                        // }
                        if (e.getClickCount() == 1) {
                            // 再次单击取消选中
                            if (item.isSelected) {
                                item.isSelected = false;
                                setInfoBar(currentFileCount, --currentSelectedFileCount);
                                item.setBackground(BACKGROUND);
                                lastSelected.remove(item);
                            }
                            // 选中
                            else {
                                item.isSelected = true;
                                setInfoBar(currentFileCount, ++currentSelectedFileCount);
                                item.setBackground(SELECT);
                                lastSelected.add(item);
                                if (lastSelected.size() > 1) {
                                    ItemView rename = null;
                                    for (Component component : lastSelected) {
                                        if (component instanceof ItemView itemView) {
                                            if (itemView.isRenaming) {
                                                rename = itemView;
                                                break;
                                            }
                                        }
                                    }
                                    if (rename != null) {
                                        for (Component component : lastSelected) {
                                            if (component instanceof ItemView itemView) {
                                                if (!itemView.isRenaming && itemView.equals(rename)) {
                                                    itemView.isSelected = false;
                                                    setInfoBar(currentFileCount, --currentSelectedFileCount);
                                                    lastSelected.remove(itemView);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // 右键事件处理
                else if (e.getButton() == MouseEvent.BUTTON3) {
                    // 弹出右键菜单时禁止使用Ctrl键选中多个文件
                    isSelecting = false;
                    if (fileView.getComponents().length == 0) {
                        return;
                    }
                    if (currentFileCount == 0) {
                        popupMenu.open.setVisible(false);
                        popupMenu.separator1.setVisible(false);
                        popupMenu.copy.setVisible(false);
                        popupMenu.copyTo.setVisible(false);
                        popupMenu.cut.setVisible(false);
                        popupMenu.cutTo.setVisible(false);
                        popupMenu.paste.setVisible(false);
                        popupMenu.separator2.setVisible(false);
                        popupMenu.rename.setVisible(false);
                        popupMenu.multiRename.setVisible(false);
                        popupMenu.separator3.setVisible(false);
                        popupMenu.delete.setVisible(false);
                        if (!selected.isEmpty()) {
                            popupMenu.paste.setVisible(true);
                        }
                    } else {
                        Component comp = fileView.getComponentAt(e.getPoint());
                        // 当前右键位置无图片
                        if (comp.equals(fileView)) {
                            // 清除已选中文件列表
                            if (!lastSelected.isEmpty()) {
                                for (Component component : lastSelected) {
                                    if (component instanceof ItemView itemView) {
                                        if (itemView.isRenaming) {
                                            itemView.isRenaming = false;
                                            itemView.quitRename();
                                        }
                                        itemView.isSelected = false;
                                        itemView.setBackground(BACKGROUND);
                                        setInfoBar(currentFileCount, --currentSelectedFileCount);
                                    }
                                }
                                lastSelected.clear();
                            }
                            popupMenu.open.setVisible(false);
                            popupMenu.separator1.setVisible(false);
                            popupMenu.copy.setVisible(false);
                            popupMenu.copyTo.setVisible(false);
                            popupMenu.cut.setVisible(false);
                            popupMenu.cutTo.setVisible(false);
                            popupMenu.paste.setVisible(false);
                            popupMenu.separator2.setVisible(false);
                            popupMenu.rename.setVisible(false);
                            popupMenu.separator3.setVisible(false);
                            popupMenu.delete.setVisible(false);
                            if (!selected.isEmpty()) {
                                popupMenu.paste.setVisible(true);
                                popupMenu.separator2.setVisible(true);
                            }
                        } else {
                            if (comp instanceof ItemView item) {
                                popupMenu.open.setVisible(true);
                                popupMenu.separator1.setVisible(true);
                                popupMenu.copy.setVisible(true);
                                popupMenu.copyTo.setVisible(true);
                                popupMenu.cut.setVisible(true);
                                popupMenu.cutTo.setVisible(true);
                                popupMenu.paste.setVisible(false);
                                popupMenu.separator2.setVisible(true);
                                popupMenu.rename.setVisible(true);
                                popupMenu.separator3.setVisible(true);
                                popupMenu.delete.setVisible(true);
                                // 右键选中文件
                                if (!item.isSelected) {
                                    // 已有其他文件被选中，清除选中状态
                                    if (!lastSelected.isEmpty()) {
                                        for (Component component : lastSelected) {
                                            if (component instanceof ItemView itemView) {
                                                if (itemView.isRenaming) {
                                                    itemView.isRenaming = false;
                                                    itemView.quitRename();
                                                }
                                                itemView.isSelected = false;
                                                itemView.setBackground(BACKGROUND);
                                                setInfoBar(currentFileCount, --currentSelectedFileCount);
                                            }
                                        }
                                        lastSelected.clear();
                                    }
                                    // 添加当前选中文件
                                    item.isSelected = true;
                                    item.setBackground(SELECT);
                                    lastSelected.add(item);
                                    setInfoBar(currentFileCount, ++currentSelectedFileCount);
                                } else {
                                    // 右键选中的文件在选中列表中
                                    if (lastSelected.size() > 1) {
                                        popupMenu.open.setVisible(false);
                                        popupMenu.separator1.setVisible(false);
                                        popupMenu.rename.setVisible(false);
                                    } else if (lastSelected.size() == 1) {
                                        // 切换为当前选中文件
                                        if (lastSelected.getFirst().equals(item)) {
                                            popupMenu.open.setVisible(true);
                                            popupMenu.separator1.setVisible(true);
                                        }
                                    }
                                }
                                if (!item.getFile().exists()) {
                                    lastSelected.remove(item);
                                    currentFileCount--;
                                    setInfoBar(currentFileCount, --currentSelectedFileCount);
                                    int currentY = fileViewScrollPane.getVerticalScrollBar().getValue();
                                    update();
                                    fileViewScrollPane.getVerticalScrollBar().setValue(currentY);
                                    return;
                                }
                            }
                        }
                        // 非空文件夹默认显示
                        popupMenu.multiRename.setVisible(true);
                    }
                    popupMenu.show(fileView, e.getX(), e.getY());
                }
            }
        });
        FileNode currentFolder = (FileNode) (FILE_TREE.getCurrentNode().getUserObject());
        if (currentFolder != null) {
            this.currentFile = currentFolder.getFile();
        }
        this.currentSelectedFileCount = 0;
        this.currentFileCount = 0;
        initialMainPanel();
    }

    /**
     * 初始化图片预览滚动面板
     */
    private void initialFileViewScrollPane() {
        this.fileViewScrollPane.setBackground(BACKGROUND);
        this.fileViewScrollPane.setMinimumSize(new Dimension(0, 0));
        this.fileViewScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (!this.isLoading && this.fileViewScrollPane.getHeight() <= this.fileView.getHeight()) {
                this.reloadViewImage(this.fileViewScrollPane.getViewport().getViewPosition().y);
            }
        });
    }

    /**
     * 初始化信息栏
     */
    private void initialInfoBar() {
        this.infoBar.setBackground(BACKGROUND);
        this.infoBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        this.infoBar.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY));
        this.setInfoBar(0, 0);
    }

    private void initialPopupMenu() {
        this.popupMenu.open.addActionListener(e -> {
            if (lastSelected.isEmpty()) {
                return;
            }
            // ((ItemView) lastSelected.getFirst()).open();
        });
        this.popupMenu.copy.addActionListener(e -> {
            if (!selected.isEmpty()) {
                selected.clear();
            }
            selected.addAll(lastSelected);
            selectedMode = 1;
        });
        this.popupMenu.copyTo.addActionListener(e -> {
            for (Component component : selected) {
                if (component instanceof ItemView itemView) {
                    // itemView.copyTo();
                }
            }
        });
        this.popupMenu.cut.addActionListener(e -> {
            if (!selected.isEmpty()) {
                selected.clear();
            }
            selected.addAll(lastSelected);
            selectedMode = 2;
        });
        this.popupMenu.cutTo.addActionListener(e -> {
            for (Component component : selected) {
                if (component instanceof ItemView itemView) {
                    // itemView.cutTo();
                }
            }
        });
        this.popupMenu.paste.addActionListener(e -> {
            if (selected.isEmpty()) {
                return;
            }
            new FileOperationWindow(selected, selectedMode, currentFile.getAbsolutePath(), false, false);
            selected.clear();
        });
        this.popupMenu.rename.addActionListener(e -> {
            if (lastSelected.size() > 1) {
                return;
            }
            ((ItemView) lastSelected.getFirst()).rename();
        });
        this.popupMenu.multiRename.addActionListener(e -> {
            ;
        });
        this.popupMenu.delete.addActionListener(e -> {
            if (lastSelected.isEmpty()) {
                return;
            }
            new FileOperationWindow(lastSelected, 3);
            lastSelected.clear();
            this.currentSelectedFileCount = 0;
            setInfoBar(currentFileCount, 0);
        });
    }

    /**
     * 初始化图片预览面板
     */
    private void initialMainPanel() {
        this.initialFileViewScrollPane();
        this.initialInfoBar();
        this.initialPopupMenu();
        this.mainPanel.setLayout(new BorderLayout());
        this.mainPanel.add(this.fileViewScrollPane, BorderLayout.CENTER);
        this.mainPanel.add(this.infoBar, BorderLayout.SOUTH);
    }

    /**
     * 更改信息栏的信息
     *
     * @param totalCount    当前文件夹中的图片文件总数
     * @param selectedCount 已选中的文件数
     */
    public void setInfoBar(int totalCount, int selectedCount) {
        this.infoBar.setText(" " + totalCount + " 张图片 | " + selectedCount + " 张已选中");
    }

    /**
     * 设置当前显示的文件夹节点
     *
     * @param node 当前文件夹
     */
    public void setCurrentFile(DefaultMutableTreeNode node) {
        FileNode currentFolder = (FileNode) (node.getUserObject());
        this.currentFile = currentFolder.getFile();
    }

    /**
     * 初始化图片预览布局
     */
    private void initialFileView() {
        if (this.currentFile == null || this.currentFile.isFile()) {
            return;
        } else {
            this.fileView.removeAll();
            if (!this.lastSelected.isEmpty()) {
                this.lastSelected.clear();
            }
            System.gc();
            this.isLoading = true;
            this.currentFileCount = 0;
            File[] folder = this.currentFile.listFiles();
            if (folder != null) {
                for (File file : folder) {
                    if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg")
                            || file.getName().endsWith(".png") || file.getName().endsWith(".gif")
                            || file.getName().endsWith(".bmp"))) {
                        // this.executorService.submit(() -> addImage(file));
                        // 保证排序正确，不使用多线程
                        addImage(file);
                        this.currentFileCount++;
                    }
                }
                this.executorService.submit(() -> this.isLoading = false);
            }
            if (this.currentFileCount == 0) {
                JLabel label = new JLabel("没有图片");
                label.setSize(100, 20);
                label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                this.fileView.add(label);
                this.isLoading = false;
            } else {
                this.executorService.submit(() -> reloadViewImage(0));
            }
        }
        this.setInfoBar(this.currentFileCount, 0);
    }

    /**
     * 给图片预览面板添加图片预览显示
     *
     * @param file 图片文件
     */
    private void addImage(File file) {
        ItemView view = new ItemView(file, itemSize.width, itemSize.height);
        this.fileView.add(view);
    }

    /**
     * 取出面板
     *
     * @return 面板
     */
    public JPanel getFileViewPanel() {
        return this.mainPanel;
    }

    /**
     * 更新当前显示的文件夹
     */
    public void update() {
        this.initialFileView();
        this.fileViewScrollPane.getVerticalScrollBar().setValue(0);
        if (fileView.getHeight() < fileViewScrollPane.getHeight()) {
            this.fileView.setSize(fileView.getWidth(), fileViewScrollPane.getHeight());
        }
        this.fileView.repaint();
    }

    /**
     * 加载指定范围的图片缩略图
     *
     * @param y 当前滚动到的高度
     */
    private void reloadViewImage(int y) {
        int height = this.fileViewScrollPane.getHeight();
        int lineCount = this.fileView.getWidth() / (itemSize.width + 4);
        if (lineCount == 0) {
            lineCount = 1;
        }
        int start = y / (itemSize.height + 4) * lineCount;
        int end = ((y + height) / (itemSize.height + 4) + 1) * lineCount;
        JScrollBar scrollBar = this.fileViewScrollPane.getVerticalScrollBar();
        int panelNewHeight = (this.fileView.getComponents().length / lineCount) * (itemSize.height + 4) + 4;
        if (this.fileView.getComponents().length % lineCount != 0) {
            panelNewHeight += itemSize.height + 4;
        }
        if (end > this.fileView.getComponents().length + lineCount) {
            int oldValue = scrollBar.getValue();
            this.fileView.setSize(this.fileView.getWidth(), panelNewHeight);
            this.fileViewScrollPane.getVerticalScrollBar().setValue(oldValue);
        }
        start -= lineCount;
        end += lineCount;
        while (this.currentFileCount != 0 && this.fileView.getComponents().length == 0) {
            continue;
        }
        for (int i = 0; i < this.fileView.getComponents().length; i++) {
            if (i >= this.fileView.getComponents().length) {
                break;
            }
            if (this.fileView.getComponent(i) instanceof ItemView view) {
                if (i >= start && i < end) {
                    if (!view.isLoad) {
                        executorService.submit(() -> view.loadPicture());
                        view.isLoad = true;
                    }
                } else if (view.isLoad) {
                    executorService.submit(() -> view.loadIcon());
                }
            }
        }
        System.gc();
    }
}
