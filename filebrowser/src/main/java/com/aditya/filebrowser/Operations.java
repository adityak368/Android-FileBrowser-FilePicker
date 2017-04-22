package com.aditya.filebrowser;

import android.app.Activity;
import android.content.Context;

import com.aditya.filebrowser.models.FileItem;
import com.aditya.filebrowser.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adik on 9/27/2015.
 */
public class Operations {

    public enum FILE_OPERATIONS {
        CUT,
        COPY,
        NONE
    }

    private List<FileItem> selectedFiles;

    public void resetOperation() {
        if(selectedFiles!=null)
            selectedFiles.clear();
        selectedFiles  = null;
        setOperation(FILE_OPERATIONS.NONE);
    }

    public FILE_OPERATIONS getOperation() {
        return currOperation;
    }

    public void setOperation(FILE_OPERATIONS operationn) {
        this.currOperation = operationn;
    }

    private FILE_OPERATIONS currOperation;

    public List<FileItem> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFiles(List<FileItem> selectedItems) {
        this.selectedFiles = selectedItems;
        UIUtils.ShowToast("Selected "+selectedItems.size()+" items",mContext);
    }

    private static Operations op;

    private Context mContext;

    private Operations(Context mContext)
    {
        this.mContext = mContext;
        this.currOperation = FILE_OPERATIONS.NONE;
    }

    public static Operations getInstance(Context mContext)
    {
        if(op==null)
        {
            op = new Operations(mContext);
        }
        return op;
    }
}
