package util.core;

import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static util.core.GlobalResources.EXECUTOR;

public class FileTree {
    private final List<CurrentNodeListener> listeners = new ArrayList<>();

    private DefaultTreeModel pathTree;

    private DefaultMutableTreeNode currentNode;

    public FileTree() {
        initialFileTree();
    }

    public DefaultTreeModel getPathTree() {
        return this.pathTree;
    }

    /**
     * 初始化文件树，默认不先加载
     */
    private void initialFileTree() {
        File[] roots = File.listRoots();
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        for (File root : roots) {
            FileNode fileNode = new FileNode(root, FileSystemView.getFileSystemView().getSystemDisplayName(root));
            if (FileSystemView.getFileSystemView().getSystemDisplayName(root).isEmpty()) {
                fileNode.setName("损坏的设备");
                continue;
            }
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileNode);
//            EXECUTOR.submit(() -> addNodes(node, 1));
            addNodes(node, 0);
            rootNode.add(node);
        }
        this.pathTree = new DefaultTreeModel(rootNode);
        this.currentNode = rootNode;
    }

    /**
     * 给某个文件夹节点添加子节点，可选择是否递归添加所有子文件夹
     *
     * @param root   当前节点
     * @param deep   加载目录树的深度
     */
    public void addNodes(DefaultMutableTreeNode root, int deep) {
//        System.out.println("addNodes: " + root.toString());
        FileNode currentFolder = (FileNode) root.getUserObject();
        currentFolder.isLoaded = true;
        File[] subFiles = currentFolder.getFile().listFiles();
        if (subFiles == null) {
            return;
        }
        int folderCount = 0;
        for (File f : subFiles) {
            if (f.isHidden()) {
                continue;
            }
            if (f.isDirectory()) {
                folderCount++;
                FileNode folder = new FileNode(f, FileSystemView.getFileSystemView().getSystemDisplayName(f));
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(folder);
                // 递归调用，遍历文件夹
                if (deep > 0) {
                    EXECUTOR.submit(() -> addNodes(node, deep - 1));
                }
                root.add(node);
            }
        }
        // 添加完设置文件夹数量
        currentFolder.setFolderCount(folderCount);
    }

    /**
     * 当前节点监听器
     * @param listener 监听事件
     */
    public void addListener(CurrentNodeListener listener) {
        this.listeners.add(listener);
    }

    public void setCurrentNode(DefaultMutableTreeNode node) {
        this.currentNode = node;
        for (CurrentNodeListener listener : listeners) {
            listener.nodeChanged(node);
        }
    }

    public DefaultMutableTreeNode getCurrentNode() {
        return this.currentNode;
    }

    public DefaultMutableTreeNode searchNode(String path) {
        String[] split = path.split("\\\\");
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.pathTree.getRoot();
        for (int i = 0; i < split.length; i++) {
            if (i == 0) {
                File rootFolder = new File(split[i] + "\\");
                for (int j = 0; j < root.getChildCount(); j++) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(j);
                    FileNode fileNode = (FileNode) node.getUserObject();
                    if (fileNode.getFile().toString().equals(rootFolder.toString())) {
                        root = node;
                        break;
                    }
                }
            }
            else {
                if (root.getChildCount() == 0) {
                    addNodes(root, 1);
                }
                for (int j = 0; j < root.getChildCount(); j++) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(j);
                    FileNode fileNode = (FileNode) node.getUserObject();
                    if (fileNode.getName().equals(split[i])) {
                        root = node;
                        if (root.getChildCount() == 0) {
                            addNodes(root, 1);
                        }
                        break;
                    }
                }
            }
        }
        return root;
    }
}
