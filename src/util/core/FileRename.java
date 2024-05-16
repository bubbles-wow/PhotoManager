package util.core;

import java.io.File;

public class FileRename {
    private final File file;

    private final String oldName;

    private String newName;

    public FileRename(File file) {
        this.file = file;
        this.oldName = file.getName();
        this.newName = file.getName();
    }

    public File getFile() {
        return this.file;
    }

    public void setNewName(String newName) {
        String ext = this.getOldName().substring(this.getOldName().lastIndexOf("."));
        this.newName = newName + ext;
    }

    public String getOldName() {
        return this.oldName;
    }

    public String getNewName() {
        return this.newName;
    }
}
