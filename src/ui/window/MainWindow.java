package ui.window;

import ui.panel.FileView;
import ui.panel.Tree;
import util.ui.component.IconButton;
import util.core.*;
import util.ui.layout.MyLayoutManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static util.core.GlobalResources.FILE_TREE;

public class MainWindow {
    private int point = 0;

    private final List<DefaultMutableTreeNode> openedNodes = new ArrayList<>();

    private boolean isUseBackAndForward = false;

    private JPanel main;

    private JPanel pathBar;

    private FileView fileView;

    public MainWindow(int width, int height) {
        initialMainWindow(width, height);
        // 对当前打开的节点进行监听
        FILE_TREE.addListener((newNode) -> {
            if (isUseBackAndForward) {
                fileView.setCurrentFile(newNode);
                fileView.update();
                isUseBackAndForward = false;
                return;
            }
            FileNode node = (FileNode) newNode.getUserObject();
            for (Component comp : pathBar.getComponents()) {
                if (comp instanceof JTextField) {
                    ((JTextField) comp).setText(node.getFile().toString());
                }
            }
            fileView.setCurrentFile(newNode);
            if (point < openedNodes.size() - 1) {
                point++;
                openedNodes.set(point, newNode);
                if (openedNodes.size() > point + 1) {
                    openedNodes.subList(point + 1, openedNodes.size()).clear();
                }
            }
            else {
                openedNodes.add(newNode);
                point++;
            }
            fileView.update();
        });
    }

    /**
     * 创建主窗口并配置
     * @param width 窗口宽度
     * @param height 窗口高度
     */
    private void initialMainWindow(int width, int height) {
        initialMainPanel(width, height);
        JFrame mainWindow = new JFrame("电子图片管理程序");
        mainWindow.setSize(width, height);
        mainWindow.add(this.main);
        mainWindow.setBackground(Color.WHITE);
        mainWindow.setVisible(true);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.updateComponentTreeUI(mainWindow);
    }

    /**
     * 初始化窗口面板
     * @param width 面板宽度
     * @param height 面板高度
     */
    private void initialMainPanel(int width, int height) {
        initialPathBar(width);
        this.main = new JPanel(new MainWindowLayout());
        this.main.setSize(width, height);
        JScrollPane pathTreePane = new JScrollPane(new Tree(FILE_TREE.getPathTree()));
        pathTreePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.fileView = new FileView();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                pathTreePane,
                this.fileView.getFileViewPanel());
        splitPane.setDividerLocation((int) (width * 0.3));
        splitPane.setDividerSize(2);
        splitPane.setMinimumSize(new Dimension(0, 0));
        this.main.add(splitPane);
        this.main.add(this.pathBar);
    }

    /**
     * 初始化路径栏
     * @param width 路径栏的宽度
     */
    private void initialPathBar(int width) {
        this.pathBar = new JPanel();
        this.pathBar.setSize(width, 32);
        this.pathBar.setBackground(Color.WHITE);
        this.pathBar.setLayout(null);
        JTextField path = new JTextField("");
        path.setSize(width - 120, 24);
//        path.setBounds(30, 0, width - 24, 24);
        path.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        // 后退
        IconButton iconButton1 = new IconButton("\ue72b", "后退", 24, 24) {
            @Override
            public void todo() {
                isUseBackAndForward = true;
                if (point > 0) {
                    point--;
                    FILE_TREE.setCurrentNode(openedNodes.get(point));
                    fileView.update();
                    FileNode currentNode = (FileNode) FILE_TREE.getCurrentNode().getUserObject();
                    path.setText(currentNode.getFile().toString());
                    for (Component comp : main.getComponents()) {
                        if (comp instanceof JSplitPane) {
                            comp.revalidate();
                            comp.repaint();
                        }
                    }
                }
            }
        };
        // 前进
        IconButton iconButton2 = new IconButton("\ue72a", "前进", 24, 24) {
            @Override
            public void todo() {
                isUseBackAndForward = true;
                if (point < openedNodes.size() - 1) {
                    point++;
                    FILE_TREE.setCurrentNode(openedNodes.get(point));
                    fileView.update();
                    FileNode currentNode = (FileNode) FILE_TREE.getCurrentNode().getUserObject();
                    path.setText(currentNode.getFile().toString());
                    for (Component comp : main.getComponents()) {
                        if (comp instanceof JSplitPane) {
                            comp.revalidate();
                            comp.repaint();
                        }
                    }
                }
            }
        };
        // 上一级
        IconButton iconButton3 = new IconButton("\ue74a", "上一级", 24, 24) {
            @Override
            public void todo() {
                FileNode currentNode = (FileNode) FILE_TREE.getCurrentNode().getUserObject();
                String currentPath = currentNode.getFile().toString();
                String parentPath = currentPath.substring(0, currentPath.lastIndexOf("\\"));
                FILE_TREE.setCurrentNode(FILE_TREE.searchNode(parentPath));
                path.setText(parentPath);
                for (Component comp : main.getComponents()) {
                    if (comp instanceof JSplitPane) {
                        comp.revalidate();
                        comp.repaint();
                    }
                }
            }
        };
        // 刷新
        IconButton iconButton4 = new IconButton("\ue72c", "刷新", 24, 24) {
            @Override
            public void todo() {
                fileView.update();
            }
        };
        // 前往
        IconButton iconButton5 = new IconButton("\ue751", "前往", 24, 24) {
            @Override
            public void todo() {
                File file = new File(path.getText());
                if (file.exists() && file.isDirectory()) {
                    FileNode currentNode = (FileNode) FILE_TREE.getCurrentNode().getUserObject();
                    if (path.getText().equals(currentNode.getFile().toString())) {
                        return;
                    }
                    FILE_TREE.setCurrentNode(FILE_TREE.searchNode(file.toString()));
                    path.setText(file.toString());
                }
                for (Component comp : main.getComponents()) {
                    if (comp instanceof JSplitPane) {
                        comp.revalidate();
                        comp.repaint();
                    }
                }
            }
        };
        // 设置图标
        iconButton1.setFont(GlobalResources.ICON_FONT);
        iconButton2.setFont(GlobalResources.ICON_FONT);
        iconButton3.setFont(GlobalResources.ICON_FONT);
        iconButton4.setFont(GlobalResources.ICON_FONT);
        iconButton5.setFont(GlobalResources.ICON_FONT);
        this.pathBar.add(iconButton1);
        this.pathBar.add(iconButton2);
        this.pathBar.add(iconButton3);
        this.pathBar.add(iconButton4);
        this.pathBar.add(path);
        this.pathBar.add(iconButton5);
    }

    /**
     * 主窗口的布局管理
     */
    class MainWindowLayout extends MyLayoutManager {
        @Override
        public void layoutContainer(Container parent) {
            Component[] components = parent.getComponents();
            for (Component component : components) {
                if (component.equals(pathBar)) {
                    pathBar.setSize(main.getWidth(), pathBar.getHeight());
                    Component[] pathBarComponents = pathBar.getComponents();
                    int width = 4;
                    int height = 4;
                    Component path = null;
                    for (Component comp : pathBarComponents) {
                        if (comp instanceof IconButton) {
                            if (width >= main.getWidth() && path != null) {
                                path.setSize(path.getWidth() - comp.getWidth() - 8, path.getHeight());
                                width -= comp.getWidth() + 4;
                                comp.setBounds(path.getX() + path.getWidth() + 4, height, comp.getWidth(),
                                        comp.getHeight());
                            } else {
                                comp.setBounds(width, height, comp.getWidth(), comp.getHeight());
                            }
                            width += comp.getWidth() + 4;
                        } else if (comp instanceof JTextField) {
                            path = comp;
                            comp.setBounds(width, height, main.getWidth() - width, comp.getHeight());
                            width += comp.getWidth() + 4;
                        }

                    }
                    component.setBounds(0, 0, pathBar.getWidth(), pathBar.getHeight());
                } else if (component instanceof JSplitPane) {
                    component.setBounds(0, pathBar.getHeight(), main.getWidth(),
                            main.getHeight() - pathBar.getHeight());
                }
            }
        }

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
            super.addLayoutComponent(comp, constraints);
            if (comp.equals(pathBar)) {
                comp.setBounds(0, 0, pathBar.getWidth(), pathBar.getHeight());
            }
            if (comp instanceof JSplitPane) {
                comp.setBounds(0, pathBar.getHeight(), main.getWidth(),
                        main.getHeight() - pathBar.getHeight());
            }
        }
    }
}
