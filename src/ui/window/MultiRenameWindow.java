package ui.window;

import ui.panel.PhotoViewItem;
import ui.panel.RenameComparedItem;
import util.core.FileRename;
import util.ui.component.ScrollPane;
import util.ui.layout.MyLayoutManager;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static util.core.GlobalResources.BACKGROUND;

public class MultiRenameWindow {
    private final List<Component> files = new ArrayList<>();

    private final List<FileRename> fileList = new ArrayList<>();

    private final JFrame window = new JFrame("批量重命名");

    private final JPanel fileViewPanel = new JPanel();

    private final JTextField name = new JTextField();

    private final JTextField start = new JTextField();

    private final JTextField number = new JTextField();

    public MultiRenameWindow(List<Component> files) {
        this.files.addAll(files);
        for (Component file : files) {
            if (file instanceof PhotoViewItem item) {
                fileList.add(new FileRename(item.getFile()));
            }
        }
        window.setLayout(new MyLayoutManager() {
            @Override
            public void layoutContainer(Container target) {
                int y = 10;
                for (Component comp : target.getComponents()) {
                    comp.setBounds(20, y, comp.getWidth(), comp.getHeight());
                    y += comp.getHeight() + 4;
                }
            }
        });
        window.setSize(600, 400);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.setVisible(true);
        this.initialInfo();
        this.initialize();
        this.initialTextField();
        this.initialButton();
    }

    private void initialButton() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setSize(this.window.getWidth() - 50, 30);
        buttonPanel.setLayout(new MyLayoutManager() {
            @Override
            public void layoutContainer(Container target) {
                int x = 0;
                for (Component comp : target.getComponents()) {
                    comp.setBounds(x, 0, comp.getWidth(), comp.getHeight());
                    x += comp.getWidth() + 20;
                }
            }
        });
        JButton confirm = new JButton("确定");
        confirm.setSize(80, 30);
        confirm.addActionListener(e -> {
            new FileOperationWindow(List.of(this.fileViewPanel.getComponents()), 4, ((PhotoViewItem) this.files.getFirst()).getFile().getParent());
            window.dispose();
        });
        buttonPanel.add(confirm);
        this.window.add(buttonPanel);
    }

    private void initialize() {
        fileViewPanel.setSize(this.window.getWidth() - 50, 240);
        fileViewPanel.setBackground(BACKGROUND);
        fileViewPanel.setLayout(new MyLayoutManager() {
            @Override
            public void layoutContainer(Container target) {
                int width = target.getWidth();
                int height = target.getHeight();
                int y = 4;
                for (Component comp : target.getComponents()) {
                    comp.setBounds(4, y, comp.getWidth(), comp.getHeight());
                    y += comp.getHeight() + 4;
                }
                if (y > height) {
                    target.setPreferredSize(new Dimension(width, y));
                }
            }
        });
        int fileCount = 0;
        for (FileRename file : fileList) {
            RenameComparedItem item = new RenameComparedItem(file.getOldName(), file.getNewName());
            item.setSize(this.fileViewPanel.getWidth() - 8, 20);
            fileViewPanel.add(item);
            fileCount++;
        }
        ScrollPane scrollPane = new ScrollPane(fileViewPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        if (fileCount > 9) {
            scrollPane.setSize(this.window.getWidth() - 34, 240);
        } else {
            scrollPane.setSize(this.window.getWidth() - 50, 240);
        }
        window.add(scrollPane);
    }

    private void initialInfo() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new MyLayoutManager() {
            @Override
            public void layoutContainer(Container target) {
                int x = 0;
                for (Component comp : target.getComponents()) {
                    comp.setBounds(x, 0, comp.getWidth(), comp.getHeight());
                    x += comp.getWidth() + 4;
                }
            }

        });
        JLabel info1 = new JLabel("新文件名：");
        info1.setSize(60, 20);
        infoPanel.add(info1);
        name.setSize(116, 20);
        infoPanel.add(name);
        JLabel info2 = new JLabel("起始序号：");
        info2.setSize(60, 20);
        infoPanel.add(info2);
        start.setSize(116, 20);
        infoPanel.add(start);
        JLabel info3 = new JLabel("序号位数：");
        info3.setSize(60, 20);
        infoPanel.add(info3);
        number.setSize(116, 20);
        infoPanel.add(number);
        infoPanel.setSize(this.window.getWidth() - 40, 20);
        JLabel info4 = new JLabel("重命名预览：在路径 " + ((PhotoViewItem) this.files.getFirst()).getFile().getParent() + " 中");
        info4.setSize(this.window.getWidth() - 40, 20);
        JPanel infoPanel2 = new JPanel();
        infoPanel2.setLayout(new GridLayout(1, 2));
        JLabel info5 = new JLabel("旧文件名");
        JLabel info6 = new JLabel("新文件名");
        infoPanel2.add(info5);
        infoPanel2.add(info6);
        infoPanel2.setSize(this.window.getWidth() - 40, 20);
        window.add(infoPanel);
        window.add(info4);
        window.add(infoPanel2);
    }

    private void initialTextField() {
        name.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateFileView();
            }
        });
        start.addKeyListener(new OnlyNumberKeyListener());
        number.addKeyListener(new OnlyNumberKeyListener());
        ((AbstractDocument) start.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches("\\d*")) {
                    int proposedValue = Integer.parseInt(start.getText() + string);
                    if (proposedValue >= -2147483647 && proposedValue <= 2147483647 - fileList.size()) {
                        super.insertString(fb, offset, string, attr);
                    }
                    updateFileView();
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("\\d*")) {
                    try {
                        Integer.parseInt(start.getText().substring(0, offset) + text + start.getText().substring(offset + length));
                    } catch (NumberFormatException e) {
                        return;
                    }
                    int proposedValue = Integer.parseInt(start.getText().substring(0, offset) + text + start.getText().substring(offset + length));
                    if (proposedValue >= -2147483647 && proposedValue <= 2147483647 - fileList.size()) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                    updateFileView();
                }
            }
        });
        ((AbstractDocument) number.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches("\\d*")) {
                    int proposedValue = Integer.parseInt(number.getText() + string);
                    if (proposedValue >= 0 && proposedValue <= 200) {
                        super.insertString(fb, offset, string, attr);
                    }
                    updateFileView();
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("\\d*")) {
                    int proposedValue = Integer.parseInt(number.getText().substring(0, offset) + text + number.getText().substring(offset + length));
                    if (proposedValue >= 0 && proposedValue <= 200) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                    updateFileView();
                }
            }
        });
    }

    private void updateFileView() {
        String newName = name.getText();
        try {
            Integer.parseInt(start.getText());
            Integer.parseInt(number.getText());
        }
        catch (NumberFormatException e) {
            return;
        }
        int startNumber = start.getText().isEmpty() ? 0 : Integer.parseInt(start.getText());
        int numberLength = number.getText().isEmpty() ? 0 : Integer.parseInt(number.getText());
        for (int i = 0; i < fileList.size(); i++) {
            FileRename file = fileList.get(i);
            RenameComparedItem item = (RenameComparedItem) fileViewPanel.getComponent(i);
            if (numberLength == 0) {
                file.setNewName(newName);
            }
            else {
                file.setNewName(newName + String.format("%0" + numberLength + "d", startNumber + i));
            }
            item.setNewName(file.getNewName());
        }
        fileViewPanel.repaint();
    }

    class OnlyNumberKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() < KeyEvent.VK_0 || e.getKeyCode() > KeyEvent.VK_9) {
                if (e.getKeyCode() != KeyEvent.VK_BACK_SPACE && e.getKeyCode() != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() < KeyEvent.VK_0 || e.getKeyCode() > KeyEvent.VK_9) {
                if (e.getKeyCode() != KeyEvent.VK_BACK_SPACE && e.getKeyCode() != KeyEvent.VK_DELETE) {
                        e.consume();
                }
            }
            updateFileView();
        }
    }
}
