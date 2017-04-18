package com.aditya.filebrowser;

/**
 * Created by Aditya on 4/15/2017.
 */

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aditya.filebrowser.adapters.CustomAdapter;
import com.aditya.filebrowser.adapters.CustomAdapterItemClickListener;
import com.aditya.filebrowser.interfaces.ContextSwitcher;
import com.aditya.filebrowser.interfaces.FuncPtr;
import com.aditya.filebrowser.interfaces.OnChangeDirectoryListener;
import com.aditya.filebrowser.listeners.TabChangeListener;
import com.aditya.filebrowser.models.FileItem;
import com.aditya.filebrowser.utils.AssortedUtils;
import com.aditya.filebrowser.utils.Permissions;
import com.aditya.filebrowser.utils.ToolbarActionMode;
import com.aditya.filebrowser.utils.UIUtils;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.comparator.SizeFileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class FileBrowser extends AppCompatActivity implements OnChangeDirectoryListener,ContextSwitcher {

    private Context mContext;
    private Toolbar toolbar;

    private CustomAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mFilesList;

    private BottomBar mBottomView;
    private BottomBar mPathChange;
    private TabChangeListener mTabChangeListener;

    TextView mCurrentPath;
    private NavigationHelper mNavigationHelper;
    private Operations op;
    private FileIO io;

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
        mNavigationHelper = new NavigationHelper(mContext);
        if (savedInstanceState != null) {
            mNavigationHelper.changeDirectory((File)savedInstanceState.getSerializable("current_node"));
            mNavigationHelper.setRootDirectory((File)savedInstanceState.getSerializable("root_node"));
        }
        io = new FileIO(this,this);
        op = Operations.getInstance(mContext);
        mTabChangeListener = new TabChangeListener(this,mNavigationHelper,mAdapter,io,op,this,this);
        mNavigationHelper.setmChangeDirectoryListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("root_node", mNavigationHelper.getRootDirectory());
        outState.putSerializable("current_node", mNavigationHelper.getCurrentDirectory());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {

        if(mAdapter.getChoiceMode()==Constants.CHOICE_MODE.MULTI_CHOICE) {
            switchMode(Constants.CHOICE_MODE.SINGLE_CHOICE);
            return;
        }

        if(!mNavigationHelper.navigateBack()) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_default_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_showfoldersizes: {
                if (AssortedUtils.GetPrefs(Constants.SHOW_FOLDER_SIZE, mContext).equalsIgnoreCase("true"))
                    AssortedUtils.SavePrefs(Constants.SHOW_FOLDER_SIZE, "false", mContext);
                else
                    AssortedUtils.SavePrefs(Constants.SHOW_FOLDER_SIZE, "true", mContext);
                updateUI(null,false);
            }
            break;
            case R.id.action_newfolder:  {
                UIUtils.showEditTextDialog(this, "Folder Name", "" , new FuncPtr(){
                    @Override
                    public void execute(final String val) {
                        if(mNavigationHelper.getCurrentDirectory().canWrite()) {
                            io.createDirectory(new File(mNavigationHelper.getCurrentDirectory(),val.trim()));
                        } else {
                            UIUtils.ShowToast("No Write Permission Granted",mContext);
                        }
                    }
                });
                updateUI(null,true);
            }
            break;
            case R.id.action_paste: {
                if (op.getOperation() == Operations.FILE_OPERATIONS.NONE) {
                    UIUtils.ShowToast("No operation selected", mContext);
                    return false;
                }
                if(op.getSelectedFiles()==null) {
                    UIUtils.ShowToast("No files selected to paste", mContext);
                    return false;
                }
                if(mNavigationHelper.getCurrentDirectory().canWrite())
                    io.pasteFiles(mNavigationHelper.getCurrentDirectory());
                else
                    UIUtils.ShowToast("No Write permissions for the paste directory",mContext);
                updateUI(null,true);
            }
            break;
        }
        return false;
    }

    @Override
    public void updateUI(File updatedDirectory,boolean shouldRePopulateCurrentDirectory) {
        if(updatedDirectory==null)
            updatedDirectory = mNavigationHelper.getCurrentDirectory();
        if(shouldRePopulateCurrentDirectory)
            mNavigationHelper.getFilesItemsInCurrentDirectory();
        mCurrentPath.setText(updatedDirectory.getAbsolutePath());
        mAdapter.notifyDataSetChanged();
        mPathChange.getTabWithId(R.id.menu_internal_storage).setTitle(FileUtils.byteCountToDisplaySize(Constants.internalStorageRoot.getUsableSpace()) + "/" +  FileUtils.byteCountToDisplaySize(Constants.internalStorageRoot.getTotalSpace()) );
        if(Constants.externalStorageRoot!=null)
            mPathChange.getTabWithId(R.id.menu_external_storage).setTitle(FileUtils.byteCountToDisplaySize(Constants.externalStorageRoot.getUsableSpace()) + "/" +  FileUtils.byteCountToDisplaySize(Constants.externalStorageRoot.getTotalSpace()));

    }

    private void loadUi() {
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        mCurrentPath = (TextView) findViewById(R.id.currentPath);

        mFilesList = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new CustomAdapter(mNavigationHelper.getFilesItemsInCurrentDirectory(),mContext);
        mFilesList.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(mContext);
        mFilesList.setLayoutManager(mLayoutManager);
        mFilesList.addOnItemTouchListener(
                new CustomAdapterItemClickListener(mContext, mFilesList, new CustomAdapterItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // TODO Handle item click
                        if (mAdapter.getChoiceMode()== Constants.CHOICE_MODE.SINGLE_CHOICE) {
                            File f = mAdapter.getItemAt(position).getFile();
                            if (f.isDirectory()) {
                                mNavigationHelper.changeDirectory(f);
                            } else {
                                MimeTypeMap mimeMap = MimeTypeMap.getSingleton();
                                Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                                String mimeType = mimeMap.getMimeTypeFromExtension(FilenameUtils.getExtension(f.getName()));
                                openFileIntent.setDataAndType(Uri.fromFile(f),mimeType);
                                openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                try {
                                    mContext.startActivity(openFileIntent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(mContext, "No app found to handle this type of file.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        switchMode(Constants.CHOICE_MODE.MULTI_CHOICE);
                        mAdapter.selectItem(position);
                        mFilesList.scrollToPosition(position);
                    }
                }));

        mBottomView = (BottomBar) findViewById(R.id.bottom_navigation);
        mPathChange = (BottomBar) findViewById(R.id.currPath_Nav);

        mBottomView.setOnTabSelectListener(mTabChangeListener);
        mBottomView.setOnTabReselectListener(mTabChangeListener);
        mPathChange.getTabWithId(R.id.menu_internal_storage).setTitle( FileUtils.byteCountToDisplaySize(Constants.internalStorageRoot.getUsableSpace()) + "/" +  FileUtils.byteCountToDisplaySize(Constants.internalStorageRoot.getTotalSpace()) );
        if(Constants.externalStorageRoot!=null)
            mPathChange.getTabWithId(R.id.menu_external_storage).setTitle( FileUtils.byteCountToDisplaySize(Constants.externalStorageRoot.getUsableSpace()) + "/" +  FileUtils.byteCountToDisplaySize(Constants.externalStorageRoot.getTotalSpace()));
        mPathChange.setOnTabSelectListener(mTabChangeListener);
        mPathChange.setOnTabReselectListener(mTabChangeListener);

        mBottomView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        mPathChange.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        updateUI(null,true);
    }

    public void switchMode(Constants.CHOICE_MODE mode) {
        if(mode== Constants.CHOICE_MODE.SINGLE_CHOICE) {
            if(mActionMode!=null)
                mActionMode.finish();
        } else {
            if(mActionMode==null) {
                mActionMode = startSupportActionMode(new ToolbarActionMode(mContext,this,mAdapter,Constants.APP_MODE.FILE_BROWSER,io));
                mActionMode.setTitle("Select Multiple Files");
            }
        }
    }

    public void changeBottomNavMenu(Constants.CHOICE_MODE multiChoice) {
        if(multiChoice== Constants.CHOICE_MODE.SINGLE_CHOICE) {
            mBottomView.setItems(R.xml.bottom_nav_items);
            mBottomView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
            mPathChange.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        } else {
            mBottomView.setItems(R.xml.bottom_nav_items_multiselect);
            mBottomView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
            mPathChange.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        }
    }

    @Override
    public void setNullToActionMode() {
        if (mActionMode != null)
            mActionMode = null;
    }

    @Override
    public void reDrawFileList() {
        mFilesList.setLayoutManager(null);
        mFilesList.setAdapter(mAdapter);
        mFilesList.setLayoutManager(mLayoutManager);
        mTabChangeListener.setmAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    public FileIO getIo() {
        return io;
    }

    public void setIo(FileIO io) {
        this.io = io;
    }
}
