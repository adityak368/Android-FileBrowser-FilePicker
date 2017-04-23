package com.aditya.filebrowser.listeners;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.widget.RadioGroup;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.FileIO;
import com.aditya.filebrowser.NavigationHelper;
import com.aditya.filebrowser.Operations;
import com.aditya.filebrowser.R;
import com.aditya.filebrowser.adapters.CustomAdapter;
import com.aditya.filebrowser.interfaces.ContextSwitcher;
import com.aditya.filebrowser.interfaces.OnChangeDirectoryListener;
import com.aditya.filebrowser.models.FileItem;
import com.aditya.filebrowser.utils.UIUtils;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aditya on 4/18/2017.
 */
public class TabChangeListener implements OnTabSelectListener,OnTabReselectListener {

    private NavigationHelper mNavigationHelper;
    private CustomAdapter mAdapter;
    private Activity mActivity;
    private FileIO io;
    private Operations op;
    private ContextSwitcher mContextSwitcher;
    private OnChangeDirectoryListener mOnChangeDirectoryListener;
    private Constants.SELECTION_MODES selectionMode;

    public TabChangeListener(Activity mActivity, NavigationHelper mNavigationHelper, CustomAdapter mAdapter, FileIO io, Operations op, ContextSwitcher mContextSwtcher,OnChangeDirectoryListener mOnChangeDirectoryListener) {
        this.mNavigationHelper = mNavigationHelper;
        this.mActivity = mActivity;
        this.mAdapter = mAdapter;
        this.io = io;
        this.op = op;
        this.mContextSwitcher = mContextSwtcher;
        this.mOnChangeDirectoryListener = mOnChangeDirectoryListener;
        this.selectionMode = Constants.SELECTION_MODES.SINGLE_SELECTION;
    }

    @Override
    public void onTabSelected(@IdRes int tabId) {
        handleTabChange(tabId);
    }

    @Override
    public void onTabReSelected(@IdRes int tabId) {
        handleTabChange(tabId);
    }

    private void handleTabChange(int tabId) {

            if(tabId==R.id.menu_back) {
                mNavigationHelper.navigateBack();
            }
            else if(tabId==R.id.menu_internal_storage) {
                mNavigationHelper.navigateToInternalStorage();
            }
            else if(tabId==R.id.menu_external_storage) {
                mNavigationHelper.navigateToExternalStorage();
            }
            else if(tabId==R.id.menu_refresh) {
                mOnChangeDirectoryListener.updateUI(null, true);
            }
            else if(tabId==R.id.menu_filter) {
                UIUtils.showRadioButtonDialog(mActivity, mActivity.getResources().getStringArray(R.array.filter_options), "Filter Only", new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int position) {
                        mNavigationHelper.filter(Constants.FILTER_OPTIONS.values()[position]);
                    }
                });
            }
            else if(tabId==R.id.menu_sort) {
                UIUtils.showRadioButtonDialog(mActivity, mActivity.getResources().getStringArray(R.array.sort_options), "Sort By", new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int position) {
                        mNavigationHelper.sortBy(Constants.SORT_OPTIONS.values()[position]);
                    }
                });
            }
            else if(tabId==R.id.menu_delete) {
                List<FileItem> selectedItems = mAdapter.getSelectedItems();
                if (io != null) {
                    io.deleteItems(selectedItems);
                    mContextSwitcher.switchMode(Constants.CHOICE_MODE.SINGLE_CHOICE);
                }
            }
            else if(tabId==R.id.menu_copy) {
                if (op != null) {
                    op.setOperation(Operations.FILE_OPERATIONS.COPY);
                    op.setSelectedFiles(mAdapter.getSelectedItems());
                    mContextSwitcher.switchMode(Constants.CHOICE_MODE.SINGLE_CHOICE);
                }
            }
            else if(tabId==R.id.menu_cut) {
                if (op != null) {
                    op.setOperation(Operations.FILE_OPERATIONS.CUT);
                    op.setSelectedFiles(mAdapter.getSelectedItems());
                    mContextSwitcher.switchMode(Constants.CHOICE_MODE.SINGLE_CHOICE);
                }
            }
            else if(tabId==R.id.menu_chooseitems) {
                {
                    List<FileItem> selItems = getmAdapter().getSelectedItems();
                    ArrayList<Uri> chosenItems = new ArrayList<>();
                    for (int i = 0; i < selItems.size(); i++) {
                        chosenItems.add(Uri.fromFile(selItems.get(i).getFile()));
                    }
                    mContextSwitcher.switchMode(Constants.CHOICE_MODE.SINGLE_CHOICE);
                    if(getSelectionMode()== Constants.SELECTION_MODES.SINGLE_SELECTION) {
                        if(chosenItems.size()==1) {
                            Intent data = new Intent();
                            data.setData(chosenItems.get(0));
                            mActivity.setResult(Activity.RESULT_OK, data);
                            mActivity.finish();
                        } else {
                            UIUtils.ShowToast("Please select only 1 item",mActivity);
                        }
                    } else {
                        Intent data = new Intent();
                        data.putParcelableArrayListExtra(Constants.SELECTED_ITEMS, chosenItems);
                        mActivity.setResult(Activity.RESULT_OK, data);
                        mActivity.finish();
                    }
                }
            }
    }

    public CustomAdapter getmAdapter() {
        return mAdapter;
    }

    public void setmAdapter(CustomAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    public Constants.SELECTION_MODES getSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(Constants.SELECTION_MODES selectionMode) {
        this.selectionMode = selectionMode;
    }
}
