package com.aditya.filebrowser;

import android.app.Activity;
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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aditya.filebrowser.adapters.CustomAdapter;
import com.aditya.filebrowser.adapters.CustomAdapterItemClickListener;
import com.aditya.filebrowser.interfaces.ContextSwitcher;
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
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.comparator.SizeFileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Aditya on 4/17/2017.
 */
public class FileChooser extends AppCompatActivity implements OnChangeDirectoryListener,ContextSwitcher {

    private Context mContext;
    private Toolbar toolbar;

    private CustomAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mFilesList;

    private BottomBar mBottomView;
    private BottomBar mPathChange;
    private TabChangeListener mTabChangeListener;

    private TextView mCurrentPath;
    private NavigationHelper mNavigationHelper;

    private FileIO io;
    private int mSelectionMode;

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
        mSelectionMode = getIntent().getIntExtra(Constants.SELECTION_MODE,Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal());
        mNavigationHelper = new NavigationHelper(mContext);
        if (savedInstanceState != null) {
            mNavigationHelper.changeDirectory((File)savedInstanceState.getSerializable("current_node"));
            mNavigationHelper.setRootDirectory((File)savedInstanceState.getSerializable("root_node"));
        }
        io = new FileIO(this,this);
        mTabChangeListener = new TabChangeListener(this,mNavigationHelper,mAdapter,null,null,this,this);
        mTabChangeListener.setSelectionMode(Constants.SELECTION_MODES.values()[mSelectionMode]);
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

        if(mAdapter.getChoiceMode()== Constants.CHOICE_MODE.MULTI_CHOICE) {
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
        getMenuInflater().inflate(R.menu.toolbar_default_menu_filechooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_showfoldersizes) {
                if (AssortedUtils.GetPrefs(Constants.SHOW_FOLDER_SIZE, mContext).equalsIgnoreCase("true"))
                    AssortedUtils.SavePrefs(Constants.SHOW_FOLDER_SIZE, "false", mContext);
                else
                    AssortedUtils.SavePrefs(Constants.SHOW_FOLDER_SIZE, "true", mContext);
                updateUI(null,false);
        }
        return false;
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
                                if(mSelectionMode==Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal()) {
                                    Uri fileUri = Uri.fromFile(f);
                                    Intent data = new Intent();
                                    data.setData(fileUri);
                                    setResult(RESULT_OK, data);
                                    finish();
                                } else {
                                    ArrayList<Uri> chosenItems = new ArrayList<>();
                                    chosenItems.add(Uri.fromFile(f));
                                    Intent data = new Intent();
                                    data.putParcelableArrayListExtra(Constants.SELECTED_ITEMS, chosenItems);
                                    setResult(Activity.RESULT_OK, data);
                                    finish();
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

    @Override
    public void switchMode(Constants.CHOICE_MODE mode) {
        if(mode== Constants.CHOICE_MODE.SINGLE_CHOICE) {
            if(mActionMode!=null)
                mActionMode.finish();
        } else {
            if(mActionMode==null) {
                mActionMode = startSupportActionMode(new ToolbarActionMode(mContext,this,mAdapter,Constants.APP_MODE.FILE_CHOOSER,io));
                mActionMode.setTitle("Select Multiple Files");
            }
        }
    }

    @Override
    public void changeBottomNavMenu(Constants.CHOICE_MODE multiChoice) {
        if(multiChoice== Constants.CHOICE_MODE.SINGLE_CHOICE) {
            mBottomView.setItems(R.xml.bottom_nav_items);
            mBottomView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
            mPathChange.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        } else {
            mBottomView.setItems(R.xml.bottom_nav_items_multiselect_filechooser);
            mBottomView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
            mBottomView.getTabWithId(R.id.menu_none1).setVisibility(View.GONE);
            mBottomView.getTabWithId(R.id.menu_none2).setVisibility(View.GONE);
            mPathChange.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        }
    }

    //Set action mode null after use
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

    @Override
    public void updateUI(File updatedDirectory,boolean shouldRePopulateDirectory) {
        if(updatedDirectory==null)
            updatedDirectory = mNavigationHelper.getCurrentDirectory();
        mCurrentPath.setText(updatedDirectory.getAbsolutePath());
        if(shouldRePopulateDirectory)
            mNavigationHelper.getFilesItemsInCurrentDirectory();
        mAdapter.notifyDataSetChanged();
        mPathChange.getTabWithId(R.id.menu_internal_storage).setTitle(FileUtils.byteCountToDisplaySize(Constants.internalStorageRoot.getUsableSpace()) + "/" +  FileUtils.byteCountToDisplaySize(Constants.internalStorageRoot.getTotalSpace()) );
        if(Constants.externalStorageRoot!=null)
            mPathChange.getTabWithId(R.id.menu_external_storage).setTitle(FileUtils.byteCountToDisplaySize(Constants.externalStorageRoot.getUsableSpace()) + "/" +  FileUtils.byteCountToDisplaySize(Constants.externalStorageRoot.getTotalSpace()));

    }
}
