package com.aditya.filebrowser;

import android.Manifest;
import android.os.Environment;

import java.io.File;

/**
 * Created by adik on 10/18/2015.
 */
public class Constants {

    public static final String APP_PREMISSION_KEY = "APP_PERMISSIONS";
    public static final String [] APP_PREMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final File internalStorageRoot = Environment.getDataDirectory();
    public static final File externalStorageRoot = Environment.getExternalStorageDirectory();
    public static final String SHOW_FOLDER_SIZE = "false";
}
