package ui.panel;

import util.ui.component.PopupMenuItem;

import javax.swing.*;
import java.awt.*;

public class FileViewPopupMenu extends JPopupMenu {
    public final PopupMenuItem open = new PopupMenuItem("\ue8e5", "打开");

    public final Separator separator1 = new Separator();

    public final PopupMenuItem copy = new PopupMenuItem("\ue8c8", "复制");

    public final PopupMenuItem copyTo = new PopupMenuItem("\uf413", "复制到...");

    public final PopupMenuItem cut = new PopupMenuItem("\ue8c6", "剪切");

    public final PopupMenuItem cutTo = new PopupMenuItem("\ue8de", "剪切到...");

    public final PopupMenuItem paste = new PopupMenuItem("\ue77f", "粘贴");

    public final Separator separator2 = new Separator();

    public final PopupMenuItem rename = new PopupMenuItem("\ue8ac", "重命名");

    public final PopupMenuItem multiRename = new PopupMenuItem("\ue762", "批量重命名...");

    public final Separator separator3 = new Separator();

    public final PopupMenuItem delete = new PopupMenuItem("\ue74d", "删除");

    public FileViewPopupMenu() {
        add(open);
        add(separator1);
        add(copy);
//        add(copyTo);
        add(cut);
//        add(cutTo);
        add(paste);
        add(separator2);
        add(rename);
        add(multiRename);
        add(separator3);
        add(delete);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (needPaint()) {
            super.paintComponent(g);
        }
    }

    private boolean needPaint() {
        for (Component comp : getComponents()) {
            if (comp instanceof PopupMenuItem) {
                if (comp.isVisible()) {
                    setVisible(true);
                    return true;
                }
            }
        }
        setVisible(false);
        return false;
    }
}
