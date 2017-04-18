package com.aditya.filebrowser;

import android.content.Context;
import android.os.Environment;

import com.aditya.filebrowser.interfaces.OnChangeDirectoryListener;
import com.aditya.filebrowser.models.FileItem;
import com.aditya.filebrowser.utils.UIUtils;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.comparator.SizeFileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Aditya on 4/18/2017.
 */
public class NavigationHelper {

    private FileNavigator mFileNavigator;
    private ArrayList<FileItem> mFiles = new ArrayList<FileItem>();
    private Context mContext;
    private List<OnChangeDirectoryListener> mChangeDirectoryListeners;

    NavigationHelper(Context mContext) {
        this.mContext = mContext;
        this.mFileNavigator = FileNavigator.getInstance();
        this.mChangeDirectoryListeners = new ArrayList<>();
    }

    public boolean navigateBack() {

        File parent = mFileNavigator.getmCurrentNode().getParentFile();
        if(parent==null || parent.compareTo(mFileNavigator.getmCurrentNode())==0 || Constants.externalStorageRoot==null || Constants.externalStorageRoot.compareTo(mFileNavigator.getmCurrentNode())==0 || Constants.internalStorageRoot.compareTo(mFileNavigator.getmCurrentNode())==0)
            return false;
        mFileNavigator.setmCurrentNode(parent);
        updateObservers(true);
        return true;
    }

    public void navigateToInternalStorage() {
        mFileNavigator.setmCurrentNode(Constants.internalStorageRoot);
        updateObservers(true);
    }

    public void navigateToExternalStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileNavigator.setmCurrentNode(Constants.externalStorageRoot);
        } else {
            UIUtils.ShowToast("Cannot Locate External Storage",mContext);
        }
        updateObservers(true);
    }

    public void changeDirectory(File newDirectory) {
        if(newDirectory!=null && newDirectory.exists()) {
            mFileNavigator.setmCurrentNode(newDirectory);
        }
        updateObservers(true);
    }

    public void setRootDirectory(File rootDirectory) {
        if(rootDirectory!=null && rootDirectory.exists())
            mFileNavigator.setmRootNode(rootDirectory);
    }

    public ArrayList<FileItem> getFilesItemsInCurrentDirectory() {
        if (mFileNavigator.getmCurrentNode() == null) mFileNavigator.setmCurrentNode(mFileNavigator.getmRootNode());
        File[] files = mFileNavigator.getFiles();
        mFiles.clear();
        if (files != null) {
            for (int i = 0; i < files.length; i++) mFiles.add(new FileItem(files[i]));
        }
        return mFiles;
    }

    public void filter(Constants.FILTER_OPTIONS option) {
        if (mFileNavigator.getmCurrentNode() == null) mFileNavigator.setmCurrentNode(mFileNavigator.getmRootNode());
        File[] files = mFileNavigator.getmCurrentNode().listFiles();
        mFiles.clear();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                boolean addToFilter = true;
                switch(option) {
                    case FILES:
                        addToFilter = !files[i].isDirectory();
                        break;
                    case FOLDER:
                        addToFilter = files[i].isDirectory();
                        break;
                }
                if (addToFilter)
                    mFiles.add(new FileItem(files[i]));
            }
        }
        updateObservers(false);
    }

    public void sortBy(Constants.SORT_OPTIONS option) {
        if (mFileNavigator.getmCurrentNode() == null) mFileNavigator.setmCurrentNode(mFileNavigator.getmRootNode());
        File[] files = mFileNavigator.getmCurrentNode().listFiles();
        mFiles.clear();
        Comparator<File> comparator = NameFileComparator.NAME_COMPARATOR;
        switch(option) {
            case SIZE:
                comparator = SizeFileComparator.SIZE_COMPARATOR;
                break;
            case LAST_MODIFIED:
                comparator = LastModifiedFileComparator.LASTMODIFIED_COMPARATOR;
                break;
        }
        if (files != null) {
            Arrays.sort(files,comparator);
            for(int i=0;i<files.length;i++)
                mFiles.add(new FileItem(files[i]));
        }
        updateObservers(false);
    }

    public File getCurrentDirectory() {
        return mFileNavigator.getmCurrentNode();
    }

    public File getRootDirectory() {
        return mFileNavigator.getmRootNode();
    }

    private void updateObservers(boolean shouldRepopulateDirectory) {
        for(int i=0;i< mChangeDirectoryListeners.size();i++) {
            mChangeDirectoryListeners.get(i).updateUI(getCurrentDirectory(),shouldRepopulateDirectory);
        }
    }

    public void setmChangeDirectoryListener(OnChangeDirectoryListener mChangeDirectoryListener) {
        this.mChangeDirectoryListeners.add(mChangeDirectoryListener);
    }

}
