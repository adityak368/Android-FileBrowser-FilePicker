package com.aditya.filebrowser;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.aditya.filebrowser.adapters.CustomAdapter;
import com.aditya.filebrowser.adapters.CustomAdapterItemClickListener;
import com.aditya.filebrowser.fileoperations.FileIO;
import com.aditya.filebrowser.fileoperations.Operations;
import com.aditya.filebrowser.interfaces.IContextSwitcher;
import com.aditya.filebrowser.interfaces.IFuncPtr;
import com.aditya.filebrowser.listeners.OnFileChangedListener;
import com.aditya.filebrowser.listeners.TabChangeListener;
import com.aditya.filebrowser.models.FileItem;
import com.aditya.filebrowser.utils.UIUtils;
import com.roughike.bottombar.BottomBar;
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileBrowserFragment extends Fragment implements OnFileChangedListener, IContextSwitcher {
    private Context mContext;
    private CustomAdapter mAdapter;
    private FastScrollRecyclerView.LayoutManager mLayoutManager;
    private FastScrollRecyclerView mFilesListView;
    private BottomBar mBottomView;
    private BottomBar mTopStorageView;
    private TabChangeListener mTabChangeListener;

    private TextView mCurrentPath;
    private NavigationHelper mNavigationHelper;
    private Operations op;
    private FileIO io;

    //Action Mode for filebrowser_toolbar
    private static ActionMode mActionMode;

    private List<FileItem> mFileList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.filebrowser_activity_frag, container, false);
        mContext = getActivity();
        setHasOptionsMenu(true);

        //TODO: let user create permissions check on main

        // Initialize Stuff
        mNavigationHelper = new NavigationHelper(mContext);
        mNavigationHelper.setmChangeDirectoryListener(this);
        io = new FileIO(mNavigationHelper, new Handler(Looper.getMainLooper()), mContext);
        op = Operations.getInstance(mContext);

        //set file filter (i.e display files with the given extension)
        String filterFilesWithExtension = getActivity().getIntent().getStringExtra(Constants.ALLOWED_FILE_EXTENSIONS);
        if (filterFilesWithExtension != null && !filterFilesWithExtension.isEmpty()) {
            Set<String> allowedFileExtensions = new HashSet<String>(Arrays.asList(filterFilesWithExtension.split(";")));
            mNavigationHelper.setAllowedFileExtensionFilter(allowedFileExtensions);
        }

        mFileList = mNavigationHelper.getFilesItemsInCurrentDirectory();

        loadUi(root);
        return root;
    }


    @Override
    public void changeBottomNavMenu(Constants.CHOICE_MODE multiChoice) {
        if (multiChoice == Constants.CHOICE_MODE.SINGLE_CHOICE) {
            mBottomView.setItems(R.xml.bottom_nav_items);
            mBottomView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
            mTopStorageView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        } else {
            mBottomView.setItems(R.xml.bottom_nav_items_multiselect);
            mBottomView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
            mTopStorageView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        }
    }

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
    public void switchMode(Constants.CHOICE_MODE mode) {
        if (mode == Constants.CHOICE_MODE.SINGLE_CHOICE) {
            if (mActionMode != null)
                mActionMode.finish();
        } else {
            if(mActionMode == null) {
                ToolbarActionMode newToolBar = new ToolbarActionMode(getActivity(),
                        this, mAdapter, Constants.APP_MODE.FILE_BROWSER, io);

                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(newToolBar);
                mActionMode.setTitle(mContext.getString(R.string.select_multiple));
            }
        }
    }

    @Override
    public void onFileChanged(File updatedDirectory) {
        if (updatedDirectory != null && updatedDirectory.exists() && updatedDirectory.isDirectory()) {
            mFileList = mNavigationHelper.getFilesItemsInCurrentDirectory();
            mCurrentPath.setText(updatedDirectory.getAbsolutePath());
            mAdapter.notifyDataSetChanged();
            mTopStorageView.getTabWithId(R.id.menu_internal_storage).setTitle(FileUtils.byteCountToDisplaySize(Constants.internalStorageRoot.getUsableSpace()) + "/" + FileUtils.byteCountToDisplaySize(Constants.internalStorageRoot.getTotalSpace()));
            if (Constants.externalStorageRoot != null)
                mTopStorageView.getTabWithId(R.id.menu_external_storage).setTitle(FileUtils.byteCountToDisplaySize(Constants.externalStorageRoot.getUsableSpace()) + "/" + FileUtils.byteCountToDisplaySize(Constants.externalStorageRoot.getTotalSpace()));
        }
    }

    private void loadUi(View v) {
        getActivity().setContentView(R.layout.filebrowser_activity_main);

        v.findViewById(R.id.menu_newfolder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.showEditTextDialog(getActivity(),
                        getString(R.string.new_folder), "", new IFuncPtr(){
                    @Override
                    public void execute(final String val) {
                        io.createDirectory(new File(mNavigationHelper.getCurrentDirectory(),val.trim()));
                    }
                });
            }
        });

        v.findViewById(R.id.menu_new_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.showEditTextDialog(getActivity(),
                        getString(R.string.new_file), "", new IFuncPtr(){
                            @Override
                            public void execute(final String val) {
                                io.createNewFile(new File(mNavigationHelper.getCurrentDirectory(),val.trim()));
                            }
                        });
            }
        });


        mCurrentPath = v.findViewById(R.id.currentPathFB);
        mFilesListView = v.findViewById(R.id.recycler_view);
        mAdapter = new CustomAdapter(mFileList,mContext);
        mFilesListView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(mContext);
        mFilesListView.setLayoutManager(mLayoutManager);
        final CustomAdapterItemClickListener onItemClickListener =
                new CustomAdapterItemClickListener(mContext, mFilesListView,
                new CustomAdapterItemClickListener.OnItemClickListener() {
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
                        Uri uri = FileProvider.getUriForFile(mContext, mContext.getString(R.string.filebrowser_provider), f);
                        openFileIntent.setDataAndType(uri,mimeType);
                        openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        openFileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        try {
                            mContext.startActivity(openFileIntent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(mContext, mContext.getString(R.string.no_app_to_handle), Toast.LENGTH_LONG).show();
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

        Toolbar toolbar = v.findViewById(R.id.filebrowser_tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        mBottomView = v.findViewById(R.id.bottom_navigation);
        mTopStorageView = v.findViewById(R.id.currPath_Nav);

        mTabChangeListener = new TabChangeListener(getActivity(),
                mNavigationHelper, mAdapter, io,this);

        mBottomView.setOnTabSelectListener(mTabChangeListener);
        mBottomView.setOnTabReselectListener(mTabChangeListener);

        mTopStorageView.setOnTabSelectListener(mTabChangeListener);
        mTopStorageView.setOnTabReselectListener(mTabChangeListener);

        mBottomView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);
        mTopStorageView.getTabWithId(R.id.menu_none).setVisibility(View.GONE);

        onFileChanged(mNavigationHelper.getCurrentDirectory());

        //switch to initial directory if given
        String initialDirectory = getActivity().getIntent().getStringExtra(Constants.INITIAL_DIRECTORY);
        if (initialDirectory != null && !initialDirectory.isEmpty() ) {
            File initDir = new File(initialDirectory);
            if (initDir.exists())
                mNavigationHelper.changeDirectory(initDir);
        }

        final TextView editSearchFrag = v.findViewById(R.id.editFBFragSearch);

        editSearchFrag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        v.findViewById(R.id.btnFBFragSearch)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSearchOpen = mCurrentPath.getVisibility() == View.GONE;
                editSearchFrag.setText("");
                editSearchFrag.setVisibility(isSearchOpen ? View.GONE : View.VISIBLE);
                mCurrentPath.setVisibility(isSearchOpen ? View.VISIBLE : View.GONE);
                mAdapter.getFilter().filter("");
                v.setBackgroundResource(isSearchOpen
                        ? R.drawable.ic_search_black_24
                        :R.drawable.ic_round_close_24_black);
            }
        });
    }

}