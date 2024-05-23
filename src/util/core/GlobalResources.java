package util.core;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;

/**
 * 全局变量
 */
public class GlobalResources {
    public static Font ICON_FONT;

    public static final FileTree FILE_TREE;

    public static final Color BACKGROUND = new Color(255, 255, 255, 255);

    public static final Color HOVER = new Color(233, 233, 233, 255);

    public static final Color SELECT = new Color(200, 200, 200, 255);

    public static final Dimension itemSize = new Dimension(150, 150);

    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(12);

    static {
        try {
            ICON_FONT = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(GlobalResources.class.getResource("/res/Segoe Fluent Icons.ttf")).openStream()).deriveFont(Font.PLAIN, 16);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(ICON_FONT);
        } catch (FontFormatException | IOException e) {
            errorDialog("无法加载图标字体，部分图标无法显示。", e);
        }
        String lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception ignored) {
        }
        try {
            Font textFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(GlobalResources.class.getResource("/res/MiSans-Regular.ttf")).openStream()).deriveFont(Font.PLAIN, 12);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(textFont);
            setUIFont(new FontUIResource(textFont));
            if (ICON_FONT == null) {
                ICON_FONT = textFont.deriveFont(Font.PLAIN, 16);
            }
        }
        catch (Exception e) {
            errorDialog("无法加载字体，部分文字可能无法显示。", e);
            ICON_FONT = Font.getFont("Dialog");
        }
        FILE_TREE = new FileTree();
    }

    /**
     * 设置全局组件的字体
     *
     * @param f 字体
     */
    public static void setUIFont(FontUIResource f) {
        UIManager.put("Button.font", f);
        UIManager.put("ToggleButton.font", f);
        UIManager.put("RadioButton.font", f);
        UIManager.put("CheckBox.font", f);
        UIManager.put("ColorChooser.font", f);
        UIManager.put("ComboBox.font", f);
        UIManager.put("Label.font", f);
        UIManager.put("List.font", f);
        UIManager.put("MenuBar.font", f);
        UIManager.put("MenuItem.font", f);
        UIManager.put("RadioButtonMenuItem.font", f);
        UIManager.put("CheckBoxMenuItem.font", f);
        UIManager.put("Menu.font", f);
        UIManager.put("PopupMenu.font", f);
        UIManager.put("OptionPane.font", f);
        UIManager.put("Panel.font", f);
        UIManager.put("ProgressBar.font", f);
        UIManager.put("ScrollPane.font", f);
        UIManager.put("Viewport.font", f);
        UIManager.put("TabbedPane.font", f);
        UIManager.put("Table.font", f);
        UIManager.put("TableHeader.font", f);
        UIManager.put("TextField.font", f);
        UIManager.put("PasswordField.font", f);
        UIManager.put("TextArea.font", f);
        UIManager.put("TextPane.font", f);
        UIManager.put("EditorPane.font", f);
        UIManager.put("TitledBorder.font", f);
        UIManager.put("ToolBar.font", f);
        UIManager.put("ToolTip.font", f);
        UIManager.put("Tree.font", f);
    }

    /**
     * 错误日志对话框
     * @param message 错误消息
     * @param e 异常对象
     */
    public static void errorDialog(String message, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String logs = sw.toString();
        new JOptionPane(message + "\n" + logs, JOptionPane.ERROR_MESSAGE).createDialog(null, "错误日志").setVisible(true);
    }
}
