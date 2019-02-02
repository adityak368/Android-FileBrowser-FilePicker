package com.aditya.filebrowser;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aditya.filebrowser.adapters.CustomAdapter;
import com.aditya.filebrowser.adapters.CustomAdapterItemClickListener;
import com.aditya.filebrowser.fileoperations.FileIO;
import com.aditya.filebrowser.fileoperations.Operations;
import com.aditya.filebrowser.interfaces.IContextSwitcher;
import com.aditya.filebrowser.listeners.OnFileChangedListener;
import com.aditya.filebrowser.listeners.SearchViewListener;
import com.aditya.filebrowser.listeners.TabChangeListener;
import com.aditya.filebrowser.models.FileItem;
import com.aditya.filebrowser.utils.AssortedUtils;
import com.aditya.filebrowser.utils.Permissions;
import com.roughike.bottombar.BottomBar;
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Aditya on 4/17/2017.
 */
public class FileChooser extends AppCompatActivity implements OnFileChangedListener,IContextSwitcher,SearchView.OnQueryTextListener {

    private Context mContext;
    private Toolbar toolbar;

    private CustomAdapter mAdapter;
    private FastScrollRecyclerView.LayoutManager mLayoutManager;
    private FastScrollRecyclerView mFilesListView;

    private BottomBar mBottomView;
    private BottomBar mPathChange;
    private TabChangeListener mTabChangeListener;

    private TextView mCurrentPath;
    private NavigationHelper mNavigationHelper;

    private FileIO io;
    private Operations op;
    private int mSelectionMode;

    //Action Mode for filebrowser_toolbar
    private static ActionMode mActionMode;
    private static final int APP_PERMISSION_REQUEST = 0;

    private SearchView mSearchView;
    private MenuItem mSearchMenuItem;
    private SearchViewListener mSearchViewListener;
    private Handler mUIUpdateHandler;
    private List<FileItem> mFileList = new ArrayList<>();

    private String mInitialDirectory;
    private String mFilterFilesWithExtension;

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
        mNavigationHelper.setmChangeDirectoryListener(this);
        mUIUpdateHandler = new Handler(Looper.getMainLooper());
        io = new FileIO(mNavigationHelper,mUIUpdateHandler,mContext);
        op = Operations.getInstance(mContext);

        //set file filter (i.e display files with the given extension)
        mFilterFilesWithExtension = getIntent().getStringExtra(Constants.ALLOWED_FILE_EXTENSIONS);
        if(mFilterFilesWithExtension!=null && !mFilterFilesWithExtension.isEmpty()) {
            String allowedFileExtension[] = mFilterFilesWithExtension.split(";");
            Set<String> allowedFilesFilter = new HashSet<String>(Arrays.asList(allowedFileExtension));
            mNavigationHelper.setAllowedFileExtensionFilter(allowedFilesFilter);
        }

        mFileList = mNavigationHelper.getFilesItemsInCurrentDirectory();
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
                Toast.makeText(mContext,mContext.getString(R.string.permission_error),Toast.LENGTH_LONG).show();
            loadUi();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_default_menu_filechooser, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView)mSearchMenuItem.getActionView();
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setSubmitButtonEnabled(true);
        mSearchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_showfoldersizes) {
                if (AssortedUtils.GetPrefs(Constants.SHOW_FOLDER_SIZE, mContext).equalsIgnoreCase("true"))
                    AssortedUtils.SavePrefs(Constants.SHOW_FOLDER_SIZE, "false", mContext);
                else
                    AssortedUtils.SavePrefs(Constants.SHOW_FOLDER_SIZE, "true", mContext);
                onFileChanged(mNavigationHelper.getCurrentDirectory());
        }
        return false;
    }

    private void loadUi() {
        setContentView(R.layout.filebrowser_activity_main);
        mCurrentPath = (TextView) findViewById(R.id.currentPath);

        mFilesListView = (FastScrollRecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new CustomAdapter(mFileList,mContext);
        mFilesListView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(mContext);
        mFilesListView.setLayoutManager(mLayoutManager);
        final CustomAdapterItemClickListener onItemClickListener = new CustomAdapterItemClickListener(mContext, mFilesListView, new CustomAdapterItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // TODO Handle item click
                if (mAdapter.getChoiceMode()== Constants.CHOICE_MODE.SINGLE_CHOICE) {
                    File f = mAdapter.getItemAt(position).getFile();
                    if (f.isDirectory()) {
                        closeSearchView();
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
                mFilesListView.scrollToPosition(position);
            }
        });
        mFilesListView.addOnItemTouchListener(onItemClickListener);

        mFilesListView.setOnFastScrollStateChangeListener(new OnFastScrollStateChangeListener() {

            @Override
            public void onFastScrollStart() {
                onItemClickListener.setmFastScrolling(true);
            }

            @Override
            public void onFastScrollStop() {
                onItemClickListener.setmFastScrolling(false);
            }
        });

        mSearchViewListener = new SearchViewListener(mAdapter);

        toolbar = (Toolbar) findViewById(R.id.filebrowser_tool_bar);
        setSupportActionBar(toolbar);

        mBottomView = (BottomBar) findViewById(R.id.bottom_navigation);
        mPathChange = (BottomBar) findViewById(R.id.currPath_Nav);

        mTabChangeListener = new TabChangeListener(this,mNavigationHelper,mAdapter,io,this);
        mTabChangeListener.setSelectionMode(Constants.SELECTION_MODES.values()[mSelectionMode]);
        mTabChangeListener.setmRecyclerView(mFilesListView);

        mBottomView.setOnTabSelectListener(mTabChangeListener);
        mBottomView.setOnTabReselectListener(mTabChangeListener);

        mPathChange.setOnTabSelectListener(mTabChangeListener);
        mPathChange.setOnTabReselectListener(mTabChangeListener);

        mBottomView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        mPathChange.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        onFileChanged(mNavigationHelper.getCurrentDirectory());

        //switch to initial directory if given
        mInitialDirectory = getIntent().getStringExtra(Constants.INITIAL_DIRECTORY);
        if(mInitialDirectory != null && !mInitialDirectory.isEmpty() ) {
            File initDir = new File(mInitialDirectory);
            if (initDir.exists())
                mNavigationHelper.changeDirectory(initDir);
        }
    }

    @Override
    public void switchMode(Constants.CHOICE_MODE mode) {
        if(mode== Constants.CHOICE_MODE.SINGLE_CHOICE) {
            if(mActionMode!=null)
                mActionMode.finish();
        } else {
            if(mActionMode==null) {
                closeSearchView();
                ToolbarActionMode newToolBar = new ToolbarActionMode(this,this,mAdapter,Constants.APP_MODE.FILE_CHOOSER,io);
                mActionMode = startSupportActionMode(newToolBar);
                mActionMode.setTitle(mContext.getString(R.string.select_multiple));
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
        mFilesListView.setLayoutManager(null);
        mFilesListView.setAdapter(mAdapter);
        mFilesListView.setLayoutManager(mLayoutManager);
        mTabChangeListener.setmAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFileChanged(File updatedDirectory) {
        if(updatedDirectory!=null && updatedDirectory.exists() && updatedDirectory.isDirectory()) {
            mFileList = mNavigationHelper.getFilesItemsInCurrentDirectory();
            mCurrentPath.setText(updatedDirectory.getAbsolutePath());
            mAdapter.notifyDataSetChanged();
            mPathChange.getTabWithId(R.id.menu_internal_storage).setTitle(FileUtils.byteCountToDisplaySize(Constants.internalStorageRoot.getUsableSpace()) + "/" + FileUtils.byteCountToDisplaySize(Constants.internalStorageRoot.getTotalSpace()));
            if (Constants.externalStorageRoot != null)
                mPathChange.getTabWithId(R.id.menu_external_storage).setTitle(FileUtils.byteCountToDisplaySize(Constants.externalStorageRoot.getUsableSpace()) + "/" + FileUtils.byteCountToDisplaySize(Constants.externalStorageRoot.getTotalSpace()));
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAdapter.getFilter().filter(newText);
        return false;
    }

    private void closeSearchView() {
        if (mSearchView.isShown()) {
            mSearchView.setQuery("", false);
            mSearchMenuItem.collapseActionView();
            mSearchView.setIconified(true);
        }
    }
}
