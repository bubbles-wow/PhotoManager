package ui.window;

import util.ui.layout.MyLayoutManager;

import javax.swing.*;
import java.awt.*;

public class PlayOptionWindow {
    private final JFrame window = new JFrame("幻灯片放映 - 选项");

    private final JPanel mainPanel = new JPanel();

    private final PhotoViewWindow playWindow;

    public PlayOptionWindow(PhotoViewWindow playWindow) {
        this.playWindow = playWindow;
        this.initialWindow();
    }

    public void initialWindow() {
        initialMainPanel();
        this.window.setSize(320, 240);
        this.window.setLocationRelativeTo(null);
        this.window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.window.setResizable(false);
        this.window.add(this.mainPanel);
        this.window.setVisible(true);
    }

    private void initialMainPanel() {
        this.mainPanel.setLayout(new MyLayoutManager() {
            @Override
            public void layoutContainer(Container parent) {
                int x = 20;
                int y = 10;
                int width = parent.getWidth() - 40;
                for (Component comp : parent.getComponents()) {
                    comp.setBounds(x, y, width, 30);
                    y += 40;
                }
            }
        });
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel label1 = new JLabel("播放间隔（秒）：");
        JTextField textField1 = new JTextField(10);
        panel1.add(label1);
        panel1.add(textField1);
        this.mainPanel.add(panel1);
        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel label2 = new JLabel("播放顺序：");
        JComboBox<String> comboBox = new JComboBox<>(new String[]{"顺序播放", "倒序播放", "随机播放"});
        panel2.add(label2);
        panel2.add(comboBox);
        this.mainPanel.add(panel2);
        JPanel panel3 = new JPanel();
        panel3.setLayout(new FlowLayout(FlowLayout.LEFT));
        JCheckBox checkBox = new JCheckBox("循环播放");
        JCheckBox checkBox1 = new JCheckBox("图片自适应");
        JCheckBox checkBox2 = new JCheckBox("全屏");
        panel3.add(checkBox);
        panel3.add(checkBox1);
        panel3.add(checkBox2);
        this.mainPanel.add(panel3);
        this.mainPanel.add(new JLabel("提示：双击图片或者按ESC键退出播放。"));
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton button1 = new JButton("确定");
        JButton button2 = new JButton("取消");
        panel4.add(button1);
        panel4.add(button2);
        this.mainPanel.add(panel4);
        button1.addActionListener((e) -> {
            try {
                Integer.parseInt(textField1.getText());
            }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this.window, "请输入正确的播放间隔！\n（大于 0 的数字，单位为秒）", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.playWindow.play(Integer.parseInt(textField1.getText()) * 1000, comboBox.getSelectedIndex(), checkBox.isSelected(), checkBox1.isSelected(), checkBox2.isSelected());
            this.window.dispose();
        });
        button2.addActionListener((e) -> this.window.dispose());
    }
}
