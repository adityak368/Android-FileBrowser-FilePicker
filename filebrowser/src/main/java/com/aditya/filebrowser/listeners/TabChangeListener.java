package com.aditya.filebrowser.listeners;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.widget.RadioGroup;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.fileoperations.FileIO;
import com.aditya.filebrowser.NavigationHelper;
import com.aditya.filebrowser.fileoperations.Operations;
import com.aditya.filebrowser.R;
import com.aditya.filebrowser.adapters.CustomAdapter;
import com.aditya.filebrowser.interfaces.IContextSwitcher;
import com.aditya.filebrowser.models.FileItem;
import com.aditya.filebrowser.utils.UIUtils;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScroller;

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
    private IContextSwitcher mIContextSwitcher;
    private Constants.SELECTION_MODES selectionMode;
    private FastScrollRecyclerView mRecyclerView;

    public TabChangeListener(Activity mActivity, NavigationHelper mNavigationHelper, CustomAdapter mAdapter, FileIO io, IContextSwitcher mContextSwtcher) {
        this.mNavigationHelper = mNavigationHelper;
        this.mActivity = mActivity;
        this.mAdapter = mAdapter;
        this.io = io;
        this.mIContextSwitcher = mContextSwtcher;
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
                mNavigationHelper.triggerFileChanged();
            }
            else if(tabId==R.id.menu_filter) {
                UIUtils.showRadioButtonDialog(mActivity, mActivity.getResources().getStringArray(R.array.filter_options), mActivity.getString(R.string.filter_only), new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int position) {
                        Operations op = Operations.getInstance(mActivity);
                        if (op != null) {
                            op.setmCurrentFilterOption(Constants.FILTER_OPTIONS.values()[position]);
                        }
                        mNavigationHelper.triggerFileChanged();
                    }
                });
            }
            else if(tabId==R.id.menu_sort) {
                UIUtils.showRadioButtonDialog(mActivity, mActivity.getResources().getStringArray(R.array.sort_options), mActivity.getString(R.string.sort_by), new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int position) {
                        Operations op = Operations.getInstance(mActivity);
                        if (op != null) {
                            op.setmCurrentSortOption(Constants.SORT_OPTIONS.values()[position]);
                            if(Constants.SORT_OPTIONS.values()[position]== Constants.SORT_OPTIONS.LAST_MODIFIED || Constants.SORT_OPTIONS.values()[position]== Constants.SORT_OPTIONS.SIZE) {
                                setFastScrollVisibility(false);
                            } else {
                                setFastScrollVisibility(true);
                            }
                        }
                        mNavigationHelper.triggerFileChanged();
                    }
                });
            }
            else if(tabId==R.id.menu_delete) {
                List<FileItem> selectedItems = mAdapter.getSelectedItems();
                if (io != null) {
                    io.deleteItems(selectedItems);
                    mIContextSwitcher.switchMode(Constants.CHOICE_MODE.SINGLE_CHOICE);
                }
            }
            else if(tabId==R.id.menu_copy) {
                Operations op = Operations.getInstance(mActivity);
                if (op != null) {
                    op.setOperation(Operations.FILE_OPERATIONS.COPY);
                    op.setSelectedFiles(mAdapter.getSelectedItems());
                    mIContextSwitcher.switchMode(Constants.CHOICE_MODE.SINGLE_CHOICE);
                }
            }
            else if(tabId==R.id.menu_cut) {
                Operations op = Operations.getInstance(mActivity);
                if (op != null) {
                    op.setOperation(Operations.FILE_OPERATIONS.CUT);
                    op.setSelectedFiles(mAdapter.getSelectedItems());
                    mIContextSwitcher.switchMode(Constants.CHOICE_MODE.SINGLE_CHOICE);
                }
            }
            else if(tabId==R.id.menu_chooseitems) {
                {
                    List<FileItem> selItems = getmAdapter().getSelectedItems();
                    ArrayList<Uri> chosenItems = new ArrayList<>();
                    for (int i = 0; i < selItems.size(); i++) {
                        chosenItems.add(Uri.fromFile(selItems.get(i).getFile()));
                    }
                    mIContextSwitcher.switchMode(Constants.CHOICE_MODE.SINGLE_CHOICE);
                    if(getSelectionMode()== Constants.SELECTION_MODES.SINGLE_SELECTION) {
                        if(chosenItems.size()==1) {
                            Intent data = new Intent();
                            data.setData(chosenItems.get(0));
                            mActivity.setResult(Activity.RESULT_OK, data);
                            mActivity.finish();
                        } else {
                            UIUtils.ShowToast(mActivity.getString(R.string.selection_error_single),mActivity);
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

    private void setFastScrollVisibility(boolean visible) {
        if(getmRecyclerView()!=null) {
            if (visible) {
                getmRecyclerView().setPopupBgColor(ContextCompat.getColor(mActivity, android.R.color.black));
                getmRecyclerView().setPopupTextSize(150);
             } else {
                getmRecyclerView().setPopupBgColor(ContextCompat.getColor(mActivity, android.R.color.transparent));
                getmRecyclerView().setPopupTextSize(0);
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

    public FastScrollRecyclerView getmRecyclerView() {
        return mRecyclerView;
    }

    public void setmRecyclerView(FastScrollRecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
    }
}
