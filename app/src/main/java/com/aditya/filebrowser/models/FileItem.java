package com.aditya.filebrowser.models;

import com.aditya.filebrowser.interfaces.ITrackSelection;

import java.io.File;

/**
 * Created by Aditya on 4/15/2017.
 */
public class FileItem implements ITrackSelection {
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    File file;
    boolean isSelected;

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public FileItem(File file) {
        this.file = file;
    }
}
