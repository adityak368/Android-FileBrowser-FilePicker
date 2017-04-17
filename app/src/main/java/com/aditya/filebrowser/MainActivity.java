package com.aditya.filebrowser;

/**
 * Created by Aditya on 4/15/2017.
 */

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.webkit.MimeTypeMap;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aditya.filebrowser.interfaces.FuncPtr;
import com.aditya.filebrowser.interfaces.UpdatableItem;
import com.aditya.filebrowser.adapters.CustomAdapter;
import com.aditya.filebrowser.adapters.CustomAdapterItemClickListener;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity implements OnTabSelectListener,OnTabReselectListener,UpdatableItem {

    private enum FILTER_OPTIONS {
        FILES,
        FOLDER,
        ALL
    }

    private enum SORT_OPTIONS {
        NAME,
        SIZE,
        LAST_MODIFIED
    }

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
    Operations op;
    FileIO io;

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
        io = new FileIO(this);
        op = Operations.getInstance(mContext);
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

    @Override
    public void onTabSelected(@IdRes int tabId) {
        handleTabChange(tabId);
    }

    @Override
    public void onTabReSelected(@IdRes int tabId) {
        handleTabChange(tabId);
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
                update();
            }
            break;
            case R.id.action_newfolder:  {
                UIUtils.showEditTextDialog(this, "Folder Name", "" , new FuncPtr(){
                    @Override
                    public void execute(final String val) {
                        if(mCurrentNode.canWrite()) {
                            io.createDirectory(new File(mCurrentNode,val.trim()));
                        } else {
                            UIUtils.ShowToast("No Write Permission Granted",mContext);
                        }
                    }
                });
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
                if(mCurrentNode.canWrite())
                    io.pasteFiles(mCurrentNode);
                else
                    UIUtils.ShowToast("No Write permissions for the paste directory",mContext);
            }
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
                        switchMode(CustomAdapter.CHOICE_MODE.MULTI_CHOICE);
                        mAdapter.selectItem(position);
                        mFilesList.scrollToPosition(position);
                    }
                }));

        mBottomView = (BottomBar) findViewById(R.id.bottom_navigation);
        mPathChange = (BottomBar) findViewById(R.id.currPath_Nav);

        mBottomView.setOnTabSelectListener(this);
        mBottomView.setOnTabReselectListener(this);
        mPathChange.getTabWithId(R.id.menu_internal_storage).setTitle( FileUtils.byteCountToDisplaySize(Constants.internalStorageRoot.getUsableSpace()) + "/" +  FileUtils.byteCountToDisplaySize(Constants.internalStorageRoot.getTotalSpace()) );
        if(Constants.externalStorageRoot!=null)
            mPathChange.getTabWithId(R.id.menu_external_storage).setTitle( FileUtils.byteCountToDisplaySize(Constants.externalStorageRoot.getUsableSpace()) + "/" +  FileUtils.byteCountToDisplaySize(Constants.externalStorageRoot.getTotalSpace()));
        mPathChange.setOnTabSelectListener(this);
        mPathChange.setOnTabReselectListener(this);

        mBottomView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        mPathChange.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        update();

    }

    public void switchMode(CustomAdapter.CHOICE_MODE mode) {
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
            mBottomView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
            mPathChange.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        } else {
            mBottomView.setItems(R.xml.bottom_nav_items_multiselect);
            mBottomView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
            mPathChange.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        }
    }

    private boolean navigateBack() {

        File parent = mCurrentNode.getParentFile();
        if(parent==null || parent.compareTo(mCurrentNode)==0 || Constants.externalStorageRoot==null || Constants.externalStorageRoot.compareTo(mCurrentNode)==0 || Constants.internalStorageRoot.compareTo(mCurrentNode)==0)
            return false;
        mCurrentNode = parent;
        update();
        return true;
    }

    private void navigateToInternalStorage() {
        mCurrentNode = Constants.internalStorageRoot;
        update();
    }

    private void navigateToExternalStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mCurrentNode = Constants.externalStorageRoot;
            update();
        } else {
            UIUtils.ShowToast("Cannot Locate External Storage",mContext);
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
                List<FileItem> selectedItems = mAdapter.getSelectedItems();
                io.deleteItems(selectedItems);
                break;
            case R.id.menu_filter:
                UIUtils.showRadioButtonDialog(this, getResources().getStringArray(R.array.filter_options), "Filter Only", new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int position) {
                        filter(FILTER_OPTIONS.values()[position]);
                    }
                });
                break;
            case R.id.menu_sort:
                UIUtils.showRadioButtonDialog(this, getResources().getStringArray(R.array.sort_options), "Sort By", new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int position) {
                        sortBy(SORT_OPTIONS.values()[position]);
                    }
                });
                break;
            case R.id.menu_copy:
                op.setOperation(Operations.FILE_OPERATIONS.COPY);
                op.setSelectedFiles(mAdapter.getSelectedItems());
                switchMode(CustomAdapter.CHOICE_MODE.SINGLE_CHOICE);
                break;
            case R.id.menu_cut:
                op.setOperation(Operations.FILE_OPERATIONS.CUT);
                op.setSelectedFiles(mAdapter.getSelectedItems());
                switchMode(CustomAdapter.CHOICE_MODE.SINGLE_CHOICE);
                break;
            default:
        }
    }

    private void filter(FILTER_OPTIONS option) {
        if (mCurrentNode == null) mCurrentNode = mRootNode;
        File[] files = mCurrentNode.listFiles();
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
        mCurrentPath.setText(mCurrentNode.getAbsolutePath());
        mAdapter.notifyDataSetChanged();
    }

    private void sortBy(SORT_OPTIONS option) {
        if (mCurrentNode == null) mCurrentNode = mRootNode;
        File[] files = mCurrentNode.listFiles();
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
        mCurrentPath.setText(mCurrentNode.getAbsolutePath());
        mAdapter.notifyDataSetChanged();
    }

    //Set action mode null after use
    public static void setNullToActionMode() {
        if (mActionMode != null)
            mActionMode = null;
    }

    public FileIO getIo() {
        return io;
    }

    public void setIo(FileIO io) {
        this.io = io;
    }

    void reDrawFileList() {
        mFilesList.setLayoutManager(null);
        mFilesList.setAdapter(mAdapter);
        mFilesList.setLayoutManager(mLayoutManager);
        mAdapter.notifyDataSetChanged();
    }
}
