package ui.panel;

import util.core.FileNode;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Objects;

import static util.core.GlobalResources.*;

public class DirectoryTree extends JTree {

    public DirectoryTree(DefaultTreeModel tree) {
        super(tree);
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
        // 设置项的外观
        this.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                          boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

                FileNode fileNode = (FileNode) node.getUserObject();
                if (fileNode == null) {
                    return label;
                }
                if (fileNode.getFile().isFile()) {
                    setText("");
                    setBackground(new Color(0, 0, 0, 0));
                    setSize(0, 0);
                }
                label.setText(fileNode.getName());
                label.setIcon(fileNode.getIcon());

                return label;
            }
        });
        // 单击时更新当前预览的文件夹
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
                FileNode fileNode = (FileNode) node.getUserObject();
                if (FILE_TREE.getCurrentNode().getUserObject() == null || !FILE_TREE.getCurrentNode().getUserObject().toString().equals(fileNode.toString())) {
                    FILE_TREE.setCurrentNode(node);
                }
            }
        });
        // 展开时加载子节点
        this.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                FileNode fileNode = (FileNode) node.getUserObject();
                File file = fileNode.getFile();
                File newFile = new File(file.getAbsolutePath());
                if (fileNode.getLastModifiedTime() != newFile.lastModified() || Objects.requireNonNull(file.listFiles()).length != Objects.requireNonNull(newFile.listFiles()).length || !fileNode.isLoaded) {
                    fileNode.reload();
                    node.removeAllChildren();
                    EXECUTOR.submit(() -> FILE_TREE.addNodes(node, 1));
                }
                else {
                    for (int i = 0; i < node.getChildCount(); i++) {
                        DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                        FileNode childNode = (FileNode) child.getUserObject();
                        File childFile = childNode.getFile();
                        File newChildFile = new File(childFile.getAbsolutePath());
                        if (!childFile.exists()) {
                            node.remove(i);
                            i--;
                            continue;
                        }
                        if (childNode.getLastModifiedTime() != newChildFile.lastModified() || Objects.requireNonNull(file.listFiles()).length != Objects.requireNonNull(newFile.listFiles()).length || !childNode.isLoaded) {
                            childNode.reload();
                            child.removeAllChildren();
                            EXECUTOR.submit(() -> FILE_TREE.addNodes(child, 0));
                        }
                    }
                    SwingUtilities.invokeLater(() -> updateUI());
//                    EXECUTOR.submit(() -> updateUI());
                }
                try {
                    int sleepTime = fileNode.getFolderCount() * 20;
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    errorDialog("等待目录树加载线程结束时出现错误", e);
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {

            }
        });
    }
}