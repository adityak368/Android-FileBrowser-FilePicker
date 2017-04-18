package com.aditya.filebrowser;

import com.aditya.filebrowser.models.FileItem;

import java.io.File;
import java.util.ArrayList;

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

    public File [] getFiles() {
        return mCurrentNode.listFiles();
    }

    private FileNavigator() {
    }

    public File getmCurrentNode() {
        return mCurrentNode;
    }

    public void setmCurrentNode(File mCurrentNode) {
        this.mCurrentNode = mCurrentNode;
    }

    public File getmRootNode() {
        return mRootNode;
    }

    public void setmRootNode(File mRootNode) {
        this.mRootNode = mRootNode;
    }

}
