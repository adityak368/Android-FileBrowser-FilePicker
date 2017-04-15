package com.aditya.filebrowser;

/**
 * Created by Aditya on 4/15/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aditya.filebrowser.Interfaces.UpdatableItem;
import com.aditya.filebrowser.adapters.CustomAdapter;
import com.aditya.filebrowser.adapters.CustomAdapterItemClickListener;
import com.aditya.filebrowser.models.FileItem;
import com.aditya.filebrowser.utils.Permissions;
import com.aditya.filebrowser.utils.ToolbarActionMode;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnTabSelectListener,OnTabReselectListener,UpdatableItem {

    private File mCurrentNode = Constants.internalStorageRoot;
    private File mRootNode = Constants.internalStorageRoot;
    Context mContext;
    Toolbar toolbar;
    private ArrayList<FileItem> mFiles = new ArrayList<FileItem>();
    private CustomAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView mFilesList;
    BottomBar mBottomView;
    BottomBar mPathChange;
    TextView mCurrentPath;


    //Action Mode for toolbar
    private static ActionMode mActionMode;
    private static final int APP_PERMISSION_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        Intent in = new Intent(this, Permissions.class);
        Bundle b=new Bundle();
        b.putStringArray(Constants.APP_PREMISSION_KEY, Constants.APP_PREMISSIONS);
        in.putExtras(b);
        startActivityForResult(in,APP_PERMISSION_REQUEST);
        if (savedInstanceState != null) {
            mRootNode = (File) savedInstanceState.getSerializable("root_node");
            mCurrentNode = (File) savedInstanceState.getSerializable("current_node");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("root_node", mRootNode);
        outState.putSerializable("current_node", mCurrentNode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {

        if(mAdapter.getChoiceMode()==CustomAdapter.CHOICE_MODE.MULTI_CHOICE) {
            switchMode(CustomAdapter.CHOICE_MODE.SINGLE_CHOICE);
            return;
        }

        if(!navigateBack()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==APP_PERMISSION_REQUEST ) {
            if(resultCode != Activity.RESULT_OK)
                Toast.makeText(mContext,"Some permissions not granted!. App may not work properly!. Please grant the required permissions!",Toast.LENGTH_LONG).show();
            loadUi();
        }
    }

    private void loadUi() {
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        mCurrentPath = (TextView) findViewById(R.id.currentPath);
        mBottomView = (BottomBar) findViewById(R.id.bottom_navigation);
        mPathChange = (BottomBar) findViewById(R.id.currPath_Nav);

        mBottomView.setOnTabSelectListener(this);
        mBottomView.setOnTabReselectListener(this);
        mPathChange.getTabWithId(R.id.menu_internal_storage).setTitle(getResources().getString(R.string.internal_storage) + " : " + FileUtils.byteCountToDisplaySize(Environment.getDataDirectory().getUsableSpace()) + "/" +  FileUtils.byteCountToDisplaySize(Environment.getDataDirectory().getTotalSpace()) );
        mPathChange.getTabWithId(R.id.menu_external_storage).setTitle(getResources().getString(R.string.external_storage) + " : " + FileUtils.byteCountToDisplaySize(Environment.getExternalStorageDirectory().getUsableSpace()) + "/" +  FileUtils.byteCountToDisplaySize(Environment.getExternalStorageDirectory().getTotalSpace()));
        mPathChange.setOnTabSelectListener(this);
        mPathChange.setOnTabReselectListener(this);

        mFilesList = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new CustomAdapter(mFiles,mContext);
        mFilesList.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(mContext);
        mFilesList.setLayoutManager(mLayoutManager);
        mFilesList.addOnItemTouchListener(
                new CustomAdapterItemClickListener(mContext, mFilesList, new CustomAdapterItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // TODO Handle item click
                        if (mAdapter.getChoiceMode()== CustomAdapter.CHOICE_MODE.SINGLE_CHOICE) {
                            File f = mFiles.get(position).getFile();
                            if (f.isDirectory()) {
                                mCurrentNode = f;
                                update();
                            } else {

                            }
                        } else {

                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        switchMode(CustomAdapter.CHOICE_MODE.MULTI_CHOICE);
                    }
                }));

        update();

    }

    private void switchMode(CustomAdapter.CHOICE_MODE mode) {
        if(mode== CustomAdapter.CHOICE_MODE.SINGLE_CHOICE) {
            if(mActionMode!=null)
                mActionMode.finish();
        } else {
            if(mActionMode==null) {
                mActionMode = startSupportActionMode(new ToolbarActionMode(this,mAdapter,mLayoutManager,mFilesList));
                mActionMode.setTitle("Select Multiple Files");
            }
        }
    }

    public void changeBottomNavMenu(CustomAdapter.CHOICE_MODE multiChoice) {
        if(multiChoice== CustomAdapter.CHOICE_MODE.SINGLE_CHOICE) {
            mBottomView.setItems(R.xml.bottom_nav_items);
        } else {
            mBottomView.setItems(R.xml.bottom_nav_items_multiselect);
        }
    }

    @Override
    public void onTabSelected(@IdRes int tabId) {
        handleTabChange(tabId);
    }

    @Override
    public void onTabReSelected(@IdRes int tabId) {
        handleTabChange(tabId);
    }

    private void handleDelete() {
        List<FileItem> selectedItems = mAdapter.getSelectedItems();
        AlertDialog confirmDialog = new AlertDialog.Builder(mContext)
                .setTitle("Delete Files")
                .setMessage("Are you sure you want to delete "+ selectedItems.size() + " files?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private boolean navigateBack() {

        File parent = mCurrentNode.getParentFile();
        if(parent==null || parent.compareTo(mCurrentNode)==0 || Constants.externalStorageRoot.compareTo(mCurrentNode)==0 || Constants.internalStorageRoot.compareTo(mCurrentNode)==0)
            return false;
        mCurrentNode = parent;
        update();
        return true;
    }

    private void navigateToInternalStorage() {
        mCurrentNode = Environment.getDataDirectory();
        update();
    }

    private void navigateToExternalStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mCurrentNode = Environment.getExternalStorageDirectory();
            update();
        } else {
            Toast.makeText(mContext,"Cannot Locate External Storage",Toast.LENGTH_SHORT).show();
        }
    }

    private void handleTabChange(int tabId) {
        switch (tabId) {
            case R.id.menu_back:
                navigateBack();
                break;
            case R.id.menu_internal_storage:
                navigateToInternalStorage();
                break;
            case R.id.menu_external_storage:
                navigateToExternalStorage();
                break;
            case R.id.menu_refresh:
                update();
                break;
            case R.id.menu_delete:
                break;
            default:
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_default_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_newfolder:
                break;
            case R.id.action_showfoldersize:
                break;
        }
        return false;
    }

    @Override
    public void update() {
        if (mCurrentNode == null) mCurrentNode = mRootNode;
        File[] files = mCurrentNode.listFiles();
        mFiles.clear();
        if (files != null) {
            for (int i = 0; i < files.length; i++) mFiles.add(new FileItem(files[i]));
        }
        mCurrentPath.setText(mCurrentNode.getAbsolutePath());
        mAdapter.notifyDataSetChanged();
    }

    //Set action mode null after use
    public static void setNullToActionMode() {
        if (mActionMode != null)
            mActionMode = null;
    }
}
