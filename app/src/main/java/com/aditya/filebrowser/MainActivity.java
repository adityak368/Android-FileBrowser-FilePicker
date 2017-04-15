package com.aditya.filebrowser;

/**
 * Created by Aditya on 4/15/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aditya.filebrowser.adapters.CustomAdapter;
import com.aditya.filebrowser.adapters.CustomAdapterItemClickListener;
import com.aditya.filebrowser.utils.Permissions;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    private File mCurrentNode = null;
    private File mLastNode = null;
    private File mRootNode = null;
    Context mContext;
    Toolbar toolbar;
    private ArrayList<File> mFiles = new ArrayList<File>();
    private CustomAdapter mAdapter;
    RecyclerView mFilesList;
    BottomNavigationView mBottomView;
    BottomNavigationView mPathChange;
    TextView mCurrentPath;

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
            mLastNode = (File) savedInstanceState.getSerializable("last_node");
            mCurrentNode = (File) savedInstanceState.getSerializable("current_node");
        }

    }


    private void refreshFileList() {
        if (mRootNode == null)
            mRootNode = new File(Environment.getExternalStorageDirectory().toString());
        if (mCurrentNode == null) mCurrentNode = mRootNode;
        mLastNode = mCurrentNode;
        File[] files = mCurrentNode.listFiles();
        mFiles.clear();
        if (files != null) {
            for (int i = 0; i < files.length; i++) mFiles.add(files[i]);
        }
        mCurrentPath.setText(mCurrentNode.getAbsolutePath());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putSerializable("root_node", mRootNode);
        outState.putSerializable("current_node", mCurrentNode);
        outState.putSerializable("last_node", mLastNode);
        super.onSaveInstanceState(outState);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_back:
                navigateBack();
                break;
            case R.id.menu_internal_storage:
                navigateToInternalStorage();
                break;
            case R.id.menu_external_storage:
                navigateToExternalStorage();
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        if(!navigateBack()) {
            super.onBackPressed();
        }
    }

    private boolean navigateBack() {

        if (mCurrentNode.compareTo(mRootNode) != 0) {
            File parent = mCurrentNode.getParentFile();
            if(parent==null)
                return false;
            mCurrentNode = parent;
            refreshFileList();
        }
        return true;
    }

    private void navigateToInternalStorage() {
        mCurrentNode = Environment.getDataDirectory();
        refreshFileList();
    }

    private void navigateToExternalStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mCurrentNode = Environment.getExternalStorageDirectory();
            refreshFileList();
        } else {
            Toast.makeText(mContext,"Cannot Locate External Storage",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==APP_PERMISSION_REQUEST ) {
            if(resultCode != Activity.RESULT_OK)
                Toast.makeText(mContext,"Permissions not granted!. App may not work properly!. Please grant the required permissions!",Toast.LENGTH_LONG).show();
            loadUi();
        }
    }

    private void loadUi() {
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        mCurrentPath = (TextView) findViewById(R.id.currentPath);
        mBottomView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mPathChange = (BottomNavigationView) findViewById(R.id.currPath_Nav);
        mBottomView.setOnNavigationItemSelectedListener(this);
        mPathChange.getMenu().getItem(1).setTitle(getResources().getString(R.string.internal_storage) + " : " + FileUtils.byteCountToDisplaySize(Environment.getDataDirectory().getFreeSpace()) + "/" +  FileUtils.byteCountToDisplaySize(Environment.getDataDirectory().getTotalSpace()) );
        mPathChange.getMenu().getItem(2).setTitle(getResources().getString(R.string.external_storage) + " : " + FileUtils.byteCountToDisplaySize(Environment.getExternalStorageDirectory().getFreeSpace()) + "/" +  FileUtils.byteCountToDisplaySize(Environment.getExternalStorageDirectory().getTotalSpace()));
        mPathChange.setOnNavigationItemSelectedListener(this);

        mFilesList = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new CustomAdapter(mFiles);
        mFilesList.setAdapter(mAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mFilesList.setLayoutManager(mLayoutManager);
        mFilesList.addOnItemTouchListener(
                new CustomAdapterItemClickListener(mContext, new CustomAdapterItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // TODO Handle item click
                        File f = mFiles.get(position);

                        if (f.isDirectory()) {
                            mCurrentNode = f;
                            refreshFileList();
                        } else {

                        }
                    }
                }));

        refreshFileList();

    }
}
