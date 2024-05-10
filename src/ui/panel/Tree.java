package ui.panel;

import util.core.FileNode;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

import static util.core.GlobalResources.*;

public class Tree extends JTree {

    public Tree(DefaultTreeModel tree) {
        super(tree);
        setRootVisible(false);
        setShowsRootHandles(true);
        setCellRenderer(new TreeDisplayRenderer());
        this.addMouseListener(new TreeMouseListener());
    }

    /**
     * 目录树的点击监听器
     */
    class TreeMouseListener implements MouseListener {
        private boolean firstClicked = false;

        @Override
        public void mouseClicked(MouseEvent evt) {
            if (evt.getButton() != MouseEvent.BUTTON1) {
                return;
            }
            boolean isDoubleClicked = false;
            if (evt.getClickCount() == 2) {
                if (!firstClicked) {
                    firstClicked = true;
                    Timer timer = new Timer(300, e -> firstClicked = false);
                    timer.setRepeats(false);
                    timer.start();
                }
                else {
                    isDoubleClicked = true;
                }
            }
            boolean hasModified = false;
            TreePath path = getSelectionPath();
            if (path == null) {
                return;
            }
            if (isExpanded(path)) {
                if (isDoubleClicked) {
                    setExpandedState(path, false);
                }
            } else {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
                FileNode fileNode = (FileNode) node.getUserObject();
                File file = fileNode.getFile();
                File newFile = new File(file.getAbsolutePath());
                if (FILE_TREE.getCurrentNode().getUserObject() == null || !FILE_TREE.getCurrentNode().getUserObject().toString().equals(fileNode.toString())) {
                    FILE_TREE.setCurrentNode(node);
                }
                if (fileNode.getLastModifiedTime() != newFile.lastModified() || !fileNode.isLoaded) {
                    hasModified = true;
                    node.removeAllChildren();
                    if (evt.getClickCount() == 2) {
                        EXECUTOR.submit(() -> FILE_TREE.addNodes(node, 1));
                    }
                    else if (evt.getClickCount() == 1) {
                        EXECUTOR.submit(() -> FILE_TREE.addNodes(node, 0));
                    }
                    fileNode.setLastModifiedTime(newFile.lastModified());
                }
                else {
                    for (int i = 0; i < node.getChildCount(); i++) {
                        DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                        FileNode childNode = (FileNode) child.getUserObject();
                        File childFile = childNode.getFile();
                        File newChildFile = new File(childFile.getAbsolutePath());
                        if (!childFile.exists()) {
                            hasModified = true;
                            node.remove(i);
                            i--;
                            continue;
                        }
                        if (childNode.getLastModifiedTime() != newChildFile.lastModified() || !childNode.isLoaded) {
                            hasModified = true;
                            EXECUTOR.submit(() -> FILE_TREE.addNodes(child, 1));
                        }

                    }
                }
                if (hasModified) {
                    try {
                        EXECUTOR.awaitTermination(500, java.util.concurrent.TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        errorDialog("等待目录树加载线程结束时出现错误", e);
                    }
                    FILE_TREE.getPathTree().reload();
                    setExpandedState(path.getParentPath(), true);
//                    setExpandedState(path, true);
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent evt) {
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
        }

        @Override
        public void mouseEntered(MouseEvent evt) {
        }

        @Override
        public void mouseExited(MouseEvent evt) {
        }
    }

    static class TreeDisplayRenderer extends DefaultTreeCellRenderer {
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
    }
}