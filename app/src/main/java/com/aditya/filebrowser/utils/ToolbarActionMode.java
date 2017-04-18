package com.aditya.filebrowser.utils;

import android.content.Context;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.FileIO;
import com.aditya.filebrowser.R;
import com.aditya.filebrowser.adapters.CustomAdapter;
import com.aditya.filebrowser.interfaces.ContextSwitcher;
import com.aditya.filebrowser.models.FileItem;

import java.util.List;

/**
 * Created by Aditya on 4/15/2017.
 */
public class ToolbarActionMode implements ActionMode.Callback{

    CustomAdapter mAdapter;
    ContextSwitcher mContextSwitcher;
    Constants.APP_MODE appMode;
    Context mContext;
    FileIO io;

    public ToolbarActionMode(Context mContext, ContextSwitcher mContextSwitcher, CustomAdapter mAdapter, Constants.APP_MODE mode, FileIO io) {
        this.mAdapter = mAdapter;
        this.mContextSwitcher = mContextSwitcher;
        this.appMode = mode;
        this.mContext = mContext;
        this.io = io;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if(this.appMode== Constants.APP_MODE.FILE_BROWSER)
            mode.getMenuInflater().inflate(R.menu.toolbar_multiselect_menu, menu);//Inflate the menu over action mode
        else if(this.appMode== Constants.APP_MODE.FILE_CHOOSER)
            mode.getMenuInflater().inflate(R.menu.toolbar_multiselect_menu_filechooser, menu);//Inflate the menu over action mode
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mAdapter.setChoiceMode(Constants.CHOICE_MODE.MULTI_CHOICE);
        mContextSwitcher.changeBottomNavMenu(Constants.CHOICE_MODE.MULTI_CHOICE);
        mContextSwitcher.reDrawFileList();
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mAdapter.setChoiceMode(Constants.CHOICE_MODE.SINGLE_CHOICE);
        mContextSwitcher.changeBottomNavMenu(Constants.CHOICE_MODE.SINGLE_CHOICE);
        mContextSwitcher.reDrawFileList();
        mContextSwitcher.setNullToActionMode();
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        List<FileItem> selectedItems = mAdapter.getSelectedItems();;
        switch (item.getItemId()) {
            case R.id.action_properties:
                if(io!=null)
                    io.getProperties(selectedItems);
                mode.finish();
                break;
            case R.id.action_share:
                if(io!=null)
                    io.shareMultipleFiles(selectedItems);
                mode.finish();//Finish action mode
                break;
            case R.id.action_rename:
                if(selectedItems.size()!=1){
                    UIUtils.ShowToast("Please select a single item",mContext);
                    return false;
                }
                if (!selectedItems.get(0).getFile().canWrite()) {
                    UIUtils.ShowToast("No write permission available", mContext);
                    return false;
                }
                io.renameFile(selectedItems.get(0));
                mode.finish();//Finish action mode
                break;
            case R.id.action_selectall:
                mAdapter.selectAll();
                break;

        }
        return false;
    }
}
