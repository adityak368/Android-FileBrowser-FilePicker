package com.aditya.filebrowser.utils;

import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.aditya.filebrowser.MainActivity;
import com.aditya.filebrowser.Operations;
import com.aditya.filebrowser.R;
import com.aditya.filebrowser.adapters.CustomAdapter;
import com.aditya.filebrowser.models.FileItem;

import java.util.List;

/**
 * Created by Aditya on 4/15/2017.
 */
public class ToolbarActionMode implements ActionMode.Callback{

    CustomAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView mFilesList;
    MainActivity activity;

    public ToolbarActionMode(MainActivity activity, CustomAdapter mAdapter, RecyclerView.LayoutManager mLayoutManager, RecyclerView mFilesList) {
        this.mAdapter = mAdapter;
        this.mLayoutManager = mLayoutManager;
        this.mFilesList = mFilesList;
        this.activity = activity;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.toolbar_multiselect_menu, menu);//Inflate the menu over action mode
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mAdapter.setChoiceMode(CustomAdapter.CHOICE_MODE.MULTI_CHOICE);
        activity.changeBottomNavMenu(CustomAdapter.CHOICE_MODE.MULTI_CHOICE);
        reDrawFileList();
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mAdapter.setChoiceMode(CustomAdapter.CHOICE_MODE.SINGLE_CHOICE);
        activity.changeBottomNavMenu(CustomAdapter.CHOICE_MODE.SINGLE_CHOICE);
        reDrawFileList();
        activity.setNullToActionMode();
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        List<FileItem> selectedItems = mAdapter.getSelectedItems();;
        switch (item.getItemId()) {
            case R.id.action_properties:
                activity.getIo().getProperties(selectedItems);
                mode.finish();
                break;
            case R.id.action_share:
                activity.getIo().shareMultipleFiles(selectedItems);
                mode.finish();//Finish action mode
                break;
            case R.id.action_rename:
                if(selectedItems.size()!=1){
                    UIUtils.ShowToast("Please select a single item",activity);
                    return false;
                }
                if (!selectedItems.get(0).getFile().canWrite()) {
                    UIUtils.ShowToast("No write permission available", activity);
                    return false;
                }
                activity.getIo().renameFile(selectedItems.get(0));
                mode.finish();//Finish action mode
                break;
            case R.id.action_selectall:
                mAdapter.selectAll();
                break;

        }
        return false;
    }

    void reDrawFileList() {
        mFilesList.setLayoutManager(null);
        mFilesList.setAdapter(mAdapter);
        mFilesList.setLayoutManager(mLayoutManager);
        mAdapter.notifyDataSetChanged();
    }
}
