package com.aditya.filebrowser.fileoperations;

import com.aditya.filebrowser.Constants;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Aditya on 4/17/2017.
 */
public class FileNavigator {

    private static FileNavigator mNavigator;

    private File mCurrentNode = Constants.internalStorageRoot;
    private File mRootNode = Constants.internalStorageRoot;

    public static FileNavigator getInstance() {
        if(mNavigator==null) {
            mNavigator = new FileNavigator();
        }
        return mNavigator;
    }

    public File [] getFilesInCurrentDirectory() {
        return mCurrentNode.listFiles();
    }

    private FileNavigator() {
    }

    public File getmCurrentNode() {
        return mCurrentNode;
    }

    public void setmCurrentNode(File mCurrentNode) {
        if(mCurrentNode!=null)
            this.mCurrentNode = mCurrentNode;
    }

    public File getmRootNode() {
        return mRootNode;
    }

}
