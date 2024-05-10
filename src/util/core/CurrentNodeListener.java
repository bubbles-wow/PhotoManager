package util.core;

import javax.swing.tree.DefaultMutableTreeNode;

public interface CurrentNodeListener {
    void nodeChanged(DefaultMutableTreeNode newNode);
}
