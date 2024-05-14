package ui.panel;

import ui.window.FileOperationWindow;
import ui.window.MultiRenameWindow;
import ui.window.PhotoViewWindow;
import ui.window.PlayOptionWindow;
import util.ui.component.IconButton;
import util.ui.component.IconTextButton;
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

    private long currentFileSize;

    private int currentSelectedFileCount;

    private long currentSelectedFileSize;

    private boolean isLoading = false;

    private boolean isSelecting = false;

    private final List<Component> lastSelected = new ArrayList<>();

    private final List<Component> selected = new ArrayList<>();

    private final List<PhotoViewItem> lastLoaded = new ArrayList<>();

    // ==0: undo
    // ==1: copy
    // ==2: cut
    // ==3: delete
    private int selectedMode = 0;

    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    private final FileViewPopupMenu popupMenu = new FileViewPopupMenu();

    private final JPanel mainPanel = new JPanel();

    private final JPanel fileView = new JPanel();

    private final JPanel toolBar = new JPanel();

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
                            if (component instanceof PhotoViewItem photoViewItem) {
                                // 当前选中的文件不取消
                                // if (current.equals(itemView)) {
                                // continue;
                                // }
                                // 取消重命名状态
                                if (photoViewItem.isRenaming) {
                                    photoViewItem.isRenaming = false;
                                    photoViewItem.quitRename();
                                }
                                photoViewItem.isSelected = false;
                                currentSelectedFileSize -= photoViewItem.getFile().length();
                                updateInfo(currentFileCount, --currentSelectedFileCount);
                                photoViewItem.setBackground(BACKGROUND);
                            }
                        }
                        lastSelected.clear();
                        // if (current instanceof ItemView item) {
                        // item.isSelected = false;
                        // if (currentSelectedFileCount > 0) {
                        // updateInfo(currentFileCount, --currentSelectedFileCount);
                        // }
                        // }
                    }
                    if (current instanceof PhotoViewItem item) {
                        // view.openFile();
                        // if (e.getClickCount() == 2) {
                        //
                        // }
                        if (e.getClickCount() == 1) {
                            // 再次单击取消选中
                            if (item.isSelected) {
                                item.isSelected = false;
                                currentSelectedFileSize -= item.getFile().length();
                                updateInfo(currentFileCount, --currentSelectedFileCount);
                                item.setBackground(BACKGROUND);
                                lastSelected.remove(item);
                            }
                            // 选中
                            else {
                                item.isSelected = true;
                                currentSelectedFileSize += item.getFile().length();
                                updateInfo(currentFileCount, ++currentSelectedFileCount);
                                item.setBackground(SELECT);
                                lastSelected.add(item);
                                if (lastSelected.size() > 1) {
                                    PhotoViewItem rename = null;
                                    for (Component component : lastSelected) {
                                        if (component instanceof PhotoViewItem photoViewItem) {
                                            if (photoViewItem.isRenaming) {
                                                rename = photoViewItem;
                                                break;
                                            }
                                        }
                                    }
                                    if (rename != null) {
                                        for (Component component : lastSelected) {
                                            if (component instanceof PhotoViewItem photoViewItem) {
                                                if (!photoViewItem.isRenaming && photoViewItem.equals(rename)) {
                                                    photoViewItem.isSelected = false;
                                                    currentSelectedFileSize -= photoViewItem.getFile().length();
                                                    updateInfo(currentFileCount, --currentSelectedFileCount);
                                                    lastSelected.remove(photoViewItem);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else if (e.getClickCount() == 2) {
                            item.isSelected = true;
                            item.setBackground(SELECT);
                            lastSelected.clear();
                            lastSelected.add(current);
                            open();
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
                                    if (component instanceof PhotoViewItem photoViewItem) {
                                        if (photoViewItem.isRenaming) {
                                            photoViewItem.isRenaming = false;
                                            photoViewItem.quitRename();
                                        }
                                        photoViewItem.isSelected = false;
                                        photoViewItem.setBackground(BACKGROUND);
                                        currentSelectedFileSize -= photoViewItem.getFile().length();
                                        updateInfo(currentFileCount, --currentSelectedFileCount);
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
                            popupMenu.multiRename.setVisible(false);
                            popupMenu.separator3.setVisible(false);
                            popupMenu.delete.setVisible(false);
                            if (!selected.isEmpty()) {
                                popupMenu.paste.setVisible(true);
                                if (currentFileCount > 1) {
                                    popupMenu.separator2.setVisible(true);
                                    popupMenu.multiRename.setVisible(true);
                                }
                            }
                            else if (currentFileCount > 1) {
                                popupMenu.multiRename.setVisible(true);
                            }
                        } else {
                            if (comp instanceof PhotoViewItem item) {
                                popupMenu.open.setVisible(true);
                                popupMenu.separator1.setVisible(true);
                                popupMenu.copy.setVisible(true);
                                popupMenu.copyTo.setVisible(true);
                                popupMenu.cut.setVisible(true);
                                popupMenu.cutTo.setVisible(true);
                                popupMenu.paste.setVisible(false);
                                popupMenu.separator2.setVisible(true);
                                popupMenu.rename.setVisible(true);
                                popupMenu.multiRename.setVisible(false);
                                popupMenu.separator3.setVisible(true);
                                popupMenu.delete.setVisible(true);
                                // 右键选中文件
                                if (!item.isSelected) {
                                    // 已有其他文件被选中，清除选中状态
                                    if (!lastSelected.isEmpty()) {
                                        for (Component component : lastSelected) {
                                            if (component instanceof PhotoViewItem photoViewItem) {
                                                if (photoViewItem.isRenaming) {
                                                    photoViewItem.isRenaming = false;
                                                    photoViewItem.quitRename();
                                                }
                                                photoViewItem.isSelected = false;
                                                photoViewItem.setBackground(BACKGROUND);
                                                currentSelectedFileSize -= photoViewItem.getFile().length();
                                                updateInfo(currentFileCount, --currentSelectedFileCount);
                                            }
                                        }
                                        lastSelected.clear();
                                    }
                                    // 添加当前选中文件
                                    item.isSelected = true;
                                    item.setBackground(SELECT);
                                    lastSelected.add(item);
                                    currentSelectedFileSize += item.getFile().length();
                                    updateInfo(currentFileCount, ++currentSelectedFileCount);
                                } else {
                                    // 右键选中的文件在选中列表中
                                    if (lastSelected.size() > 1) {
                                        popupMenu.open.setVisible(false);
                                        popupMenu.separator1.setVisible(false);
                                        popupMenu.rename.setVisible(false);
                                        popupMenu.multiRename.setVisible(true);
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
                                    currentFileSize -= item.getFile().length();
                                    currentSelectedFileSize -= item.getFile().length();
                                    updateInfo(currentFileCount, --currentSelectedFileCount);
                                    int currentY = fileViewScrollPane.getVerticalScrollBar().getValue();
                                    update();
                                    fileViewScrollPane.getVerticalScrollBar().setValue(currentY);
                                    return;
                                }
                            }
                        }
                        // 非空文件夹默认显示
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
        this.currentSelectedFileSize = 0;
        this.currentFileSize = 0;
        initialMainPanel();
    }

    private void initialToolBar() {
        this.toolBar.setBackground(BACKGROUND);
        this.toolBar.setLayout(new FlowLayout() {
            @Override
            public void layoutContainer(Container target) {
                int leftX = 10;
                int rightX = target.getWidth() - 10;
                for(Component comp : target.getComponents()) {
                    if(comp instanceof IconButton) {
                        int width = comp.getWidth();
                        int height = comp.getHeight();
                        comp.setBounds(leftX, 4, width, height);
                        leftX += width + 10;
                    }
                    else if (comp instanceof IconTextButton) {
                        int width = comp.getWidth();
                        int height = comp.getHeight();
                        comp.setBounds(rightX - width, 4, width, height);
                        rightX -= width + 10;
                    }
                }
            }
        });
        this.toolBar.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));
        this.toolBar.setPreferredSize(new Dimension(this.mainPanel.getWidth(), 32));
        IconTextButton open = new IconTextButton("\uee4a", "幻灯片放映", 96, 24) {
            @Override
            public void todo() {
                play();
            }

        };
        IconButton copy = new IconButton("\ue8c8", "复制", 24, 24) {
            @Override
            public void todo() {
                copy();
            }
        };
        IconButton cut = new IconButton("\ue8c6", "剪切", 24, 24) {
            @Override
            public void todo() {
                cut();
            }

        };
        IconButton paste = new IconButton("\ue77f", "粘贴", 24, 24) {
            @Override
            public void todo() {
                paste();
            }

        };
        IconButton rename = new IconButton("\ue8ac", "重命名", 24, 24) {
            @Override
            public void todo() {
                rename();
            }
        };
        IconButton delete = new IconButton("\ue74d", "删除", 24, 24) {
            @Override
            public void todo() {
                delete();
            }
        };
        this.toolBar.add(open);
        this.toolBar.add(copy);
        this.toolBar.add(cut);
        this.toolBar.add(paste);
        this.toolBar.add(rename);
        this.toolBar.add(delete);
        this.updateToolBar(0, 0);
    }

    /**
     * 更新工具栏的按钮状态
     * @param totalCount 当前总文件数
     * @param selectedCount 当前选中的文件数
     */
    private void updateToolBar(int totalCount, int selectedCount) {
        if (this.toolBar.getComponents().length < 6) {
            return;
        }
        int pasteCount = this.selected.size();
        this.toolBar.getComponent(0).disable();
        this.toolBar.getComponent(1).disable();
        this.toolBar.getComponent(2).disable();
        this.toolBar.getComponent(3).disable();
        this.toolBar.getComponent(4).disable();
        this.toolBar.getComponent(5).disable();
        if (pasteCount != 0) {
            this.toolBar.getComponent(3).enable();
        }
        if (selectedCount != 0) {
            this.toolBar.getComponent(1).enable();
            this.toolBar.getComponent(2).enable();
            this.toolBar.getComponent(4).enable();
            this.toolBar.getComponent(5).enable();
        }
        if (totalCount != 0) {
            this.toolBar.getComponent(0).enable();
        }
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
        this.updateInfo(0, 0);
    }

    private void initialPopupMenu() {
        this.popupMenu.open.addActionListener(e -> this.open());
        this.popupMenu.copy.addActionListener(e -> this.copy());
//        this.popupMenu.copyTo.addActionListener(e -> {
//            for (Component component : selected) {
//                if (component instanceof ItemView itemView) {
//                    // itemView.copyTo();
//                }
//            }
//        });
        this.popupMenu.cut.addActionListener(e -> this.cut());
//        this.popupMenu.cutTo.addActionListener(e -> {
//            for (Component component : selected) {
//                if (component instanceof ItemView itemView) {
//                    // itemView.cutTo();
//                }
//            }
//        });
        this.popupMenu.paste.addActionListener(e -> this.paste());
        this.popupMenu.rename.addActionListener(e -> this.rename());
        this.popupMenu.multiRename.addActionListener(e -> this.multiRename());
        this.popupMenu.delete.addActionListener(e -> this.delete());
    }

    /**
     * 初始化图片预览面板
     */
    private void initialMainPanel() {
        this.initialFileViewScrollPane();
        this.initialInfoBar();
        this.initialPopupMenu();
        this.initialToolBar();
        this.mainPanel.setLayout(new BorderLayout());
        this.mainPanel.add(this.fileViewScrollPane, BorderLayout.CENTER);
        this.mainPanel.add(this.infoBar, BorderLayout.SOUTH);
        this.mainPanel.add(this.toolBar, BorderLayout.NORTH);
    }

    /**
     * 文件打开
     */
    private void open() {
        if (this.lastSelected.isEmpty()) {
            return;
        }
        List<Component> files = List.of(this.fileView.getComponents());
        int index = files.indexOf(this.lastSelected.getLast());
        PhotoViewWindow photo = new PhotoViewWindow(files, index);
        photo.show();
    }

    /**
     * 文件打开
     */
    private void play() {
        List<Component> files = List.of(this.fileView.getComponents());
        int index = this.lastSelected.isEmpty() ? 0 : files.indexOf(this.lastSelected.getLast());
        new PlayOptionWindow(new PhotoViewWindow(files, index));
    }

    /**
     * 文件复制
     */
    private void copy() {
        if (!this.selected.isEmpty()) {
            this.selected.clear();
        }
        this.selected.addAll(this.lastSelected);
        this.selectedMode = 1;
        this.updateToolBar(this.currentFileCount, this.currentSelectedFileCount);
    }

    /**
     * 文件移动
     */
    private void cut() {
        if (!this.selected.isEmpty()) {
            this.selected.clear();
        }
        this.selected.addAll(this.lastSelected);
        this.selectedMode = 2;
        this.updateToolBar(this.currentFileCount, this.currentSelectedFileCount);
    }

    /**
     * 文件粘贴
     */
    private void paste() {
        if (this.selected.isEmpty()) {
            return;
        }
        new FileOperationWindow(this.selected, this.selectedMode, this.currentFile.getAbsolutePath(), false, false);
        this.selected.clear();
        this.currentSelectedFileCount = 0;
        this.currentSelectedFileSize = 0;
        this.updateInfo(this.currentFileCount, 0);
    }

    /**
     * 文件重命名
     */
    private void rename() {
        // 使用批量重命名
        if (this.lastSelected.size() > 1) {
            new MultiRenameWindow(this.lastSelected);
        }
        ((PhotoViewItem) this.lastSelected.getFirst()).rename();
    }

    /**
     * 批量重命名
     */
    private void multiRename() {
        if (this.lastSelected.size() < 2) {
            new MultiRenameWindow(List.of(this.fileView.getComponents()));
        }
        else {
            new MultiRenameWindow(this.lastSelected);
        }
    }

    /**
     * 文件删除操作
     */
    private void delete() {
        if (this.lastSelected.isEmpty()) {
            return;
        }
        new FileOperationWindow(this.lastSelected, 3);
        this.lastSelected.clear();
        this.currentSelectedFileCount = 0;
        this.currentSelectedFileSize = 0;
        this.updateInfo(this.currentFileCount, 0);
    }

    /**
     * 更改信息栏的信息
     *
     * @param totalCount    当前文件夹中的图片文件总数
     * @param selectedCount 已选中的文件数
     */
    public void updateInfo(int totalCount, int selectedCount) {
        String[] signs = new String[]{"B", "K", "M", "G", "T"};
        int index = 0;
        double size = this.currentFileSize;
        double selectedSize = this.currentSelectedFileSize;
        while (size > 1024) {
            size /= 1024;
            index++;
            if (index == 4) {
                break;
            }
        }
        int selectedIndex = 0;
        while (selectedSize > 1024) {
            selectedSize /= 1024;
            selectedIndex++;
            if (index == 4) {
                break;
            }
        }
        this.infoBar.setText(String.format(" %d 张图片 (%.2f %s) | %d 张已选中 (%.2f %s)", totalCount, size, signs[index], selectedCount, selectedSize, signs[selectedIndex]));
        this.updateToolBar(totalCount, selectedCount);
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
            if (!this.lastLoaded.isEmpty()) {
                this.lastLoaded.clear();
            }
            System.gc();
            this.isLoading = true;
            this.currentFileCount = 0;
            this.currentFileSize = 0;
            this.currentSelectedFileCount = 0;
            this.currentSelectedFileSize = 0;
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
                        this.currentFileSize += file.length();
                    }
                }
                this.isLoading = false;
            }
            if (this.currentFileCount == 0) {
                JLabel label = new JLabel("没有图片");
                label.setSize(100, 20);
                label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                this.fileView.add(label);
                this.isLoading = false;
            }
            this.executorService.submit(() -> reloadViewImage(0));
        }
        this.updateInfo(this.currentFileCount, 0);
    }

    /**
     * 给图片预览面板添加图片预览显示
     *
     * @param file 图片文件
     */
    private void addImage(File file) {
//        SwingWorker<Void, Void> worker = new SwingWorker<>() {
//            @Override
//            protected Void doInBackground() {
//                ItemView view = new ItemView(file, itemSize.width, itemSize.height);
//                fileView.add(view);
//                return null;
//            }
//        };
//        worker.execute();
        PhotoViewItem view = new PhotoViewItem(file, itemSize.width, itemSize.height);
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
        // 预加载不在可视范围的图片
        start -= lineCount;
        if (start < 0) {
            start = 0;
        }
        end += lineCount;
//        while (this.currentFileCount != 0 && this.fileView.getComponents().length == 0) {
//            continue;
//        }
//        for (int i = 0; i < this.fileView.getComponents().length; i++) {
//            if (i >= this.fileView.getComponents().length) {
//                break;
//            }
//            if (this.fileView.getComponent(i) instanceof ItemView view) {
//                if (i >= start && i < end) {
//                    if (!view.isLoad) {
//                        executorService.submit(() -> view.loadPicture());
//                        view.setVisible(true);
//                    }
//                } else if (view.isLoad) {
//                    executorService.submit(() -> view.loadIcon());
//                    view.setVisible(false);
//                }
//            }
//        }
        // 取消不在当前显示范围的图片的加载
        for (int i = 0; i < this.lastLoaded.size(); i++) {
            int index = List.of(this.fileView.getComponents()).indexOf(this.lastLoaded.get(i));
            if (index == -1) {
                this.lastLoaded.remove(i);
                i--;
                continue;
            }
            if (index < start || index > end) {
                PhotoViewItem view = (PhotoViewItem) this.fileView.getComponent(index);
                if (view.isLoad) {
                    executorService.submit(() -> view.loadIcon());
                }
            }
        }
        // 加载当前范围的图片
        for (int i = start; i < end + 1; i++) {
            if (i >= this.fileView.getComponents().length) {
                break;
            }
            if (this.fileView.getComponent(i) instanceof PhotoViewItem view) {
                if (!view.isLoad) {
                    view.isLoad = true;
                    this.executorService.submit(() -> view.loadPicture());
                    this.lastLoaded.add(view);
                }
            }
        }
        System.gc();
    }
}
