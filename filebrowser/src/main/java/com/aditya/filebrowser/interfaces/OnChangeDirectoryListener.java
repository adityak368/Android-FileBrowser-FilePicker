package com.aditya.filebrowser.interfaces;

import java.io.File;

/**
 * Created by Aditya on 4/18/2017.
 */
public interface OnChangeDirectoryListener {
    void updateUI(File updatedDirectory,boolean shouldRePopulateCurrentDirectory);
}
