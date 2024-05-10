package util.ui.layout;

import javax.swing.*;
import java.awt.*;

import static util.core.GlobalResources.itemSize;

/**
     * 图片预览布局管理
     */
public class FileViewLayout extends MyLayoutManager {
        private int currentX = 4;
        private int currentY = 4;

        public void addLayoutComponent(Component comp, Object constraints) {
            Component parent = comp.getParent();
            int width = comp.getWidth();
            int height = comp.getHeight();
            if (comp instanceof JPanel) {
                comp.setBounds(currentX, currentY, width, height);
                currentX += width + 4;
                if (currentX + width > parent.getWidth()) {
                    currentX = 4;
                    currentY += height + 4;
                }
                parent.setSize(parent.getWidth(), currentY + 4);
            } else if (comp instanceof JLabel) {
                comp.setBounds((parent.getWidth() - width) / 2, (parent.getHeight() - height) / 2, width, height);
                parent.setSize(parent.getWidth(), parent.getHeight());
            }
        }

        @Override
        public void layoutContainer(Container parent) {
            Component parentComponent = parent.getParent();
            int scrollPaneWidth = parentComponent.getWidth();
            int scrollPaneHeight = parentComponent.getHeight();
            int itemWidth = itemSize.width;
            int itemHeight = itemSize.height;
            int parentWidth = parent.getWidth() - 20;
            int rowCount = parentWidth / itemWidth;
            int gap = parentWidth - (itemWidth * rowCount);
            int newGap = gap / (rowCount + 1);
            if (rowCount != 0) {
                if (newGap < 4) {
                    newGap = (gap + itemWidth + newGap) / rowCount;
                } else if (parent.getComponents().length <= rowCount) {
                    newGap = 4;
                }
            } else {
                newGap = 4;
            }
            currentX = newGap;
            currentY = 4;
            for (Component comp : parent.getComponents()) {
                if (comp instanceof JPanel) {
                    comp.setBounds(currentX, currentY, comp.getWidth(), comp.getHeight());
                    currentX += comp.getWidth() + newGap;
                    if (currentX + comp.getWidth() + newGap >= parent.getWidth()) {
                        currentX = newGap;
                        currentY += comp.getHeight() + 4;
                    }
                } else if (comp instanceof JLabel) {
                    comp.setBounds((parent.getWidth() - comp.getWidth()) / 2,
                            (parent.getHeight() - comp.getHeight()) / 2, comp.getWidth(), comp.getHeight());
                    return;
                }
            }
            if (currentX == newGap) {
                parent.setSize(scrollPaneWidth, Math.max(currentY, scrollPaneHeight));
            } else {
                parent.setSize(scrollPaneWidth, Math.max(currentY + itemHeight + 4, scrollPaneHeight));
            }
            parent.repaint();
        }
    }
