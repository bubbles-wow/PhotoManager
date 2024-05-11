package util.core;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class FileNode {
    private Icon icon;

    private String name;

    private File file;

    private long lastModifiedTime;

    private int folderCount = 0;

    public boolean isLoaded = false;

    public FileNode(File file, String name) {
        load(file, name);
    }

    private void load(File file, String name) {
        Icon icon1;
        this.file = file;
        this.lastModifiedTime = file.lastModified();
        this.name = name;
        icon1 = FileSystemView.getFileSystemView().getSystemIcon(file);
        if (icon1 == null) {
            icon1 = UIManager.getIcon("FileView.fileIcon");
        }
        this.icon = icon1;
    }

    public long getLastModifiedTime() {
        return this.lastModifiedTime;
    }

    public int getFolderCount() {
        return this.folderCount;
    }

    public void setFolderCount(int folderCount) {
        this.folderCount = folderCount;
    }

    public void reload() {
        String path = this.file.getAbsolutePath();
        File newFile = new File(path);
        if (newFile.lastModified() != this.lastModifiedTime) {
            load(newFile, FileSystemView.getFileSystemView().getSystemDisplayName(newFile));
        }
    }

    public File getFile() {
        return this.file;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public String toString() {
        return this.name;
    }
}
