package com.aditya.filebrowser;

import android.Manifest;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by adik on 10/18/2015.
 */
public class Constants {

    public static final String APP_PREMISSION_KEY = "APP_PERMISSIONS";
    public static final String [] APP_PREMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final String INTERNALSTORAGE = "Internal Storage";
    public static final String EXTERNALSTORAGE = "External Storage";
    public static File internalStorageRoot = Environment.getExternalStorageDirectory();
    public static File externalStorageRoot;
    static {

        try {
            List<GetRemovableDevice.StorageInfo> infos = GetRemovableDevice.getStorageList();
            for(int i=0;i<infos.size();i++) {
                if(infos.get(i).getDisplayName().contains(Constants.EXTERNALSTORAGE)) {
                    File detectedDirectory =  new File(infos.get(i).path).getCanonicalFile();
                    if(detectedDirectory.getTotalSpace()>0)
                        externalStorageRoot = detectedDirectory;
                    else
                        externalStorageRoot = new File("/");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            externalStorageRoot = new File("/");
        }
    }
    //GetRemovableDevice.getDirectories().length>0?new File(GetRemovableDevice.getDirectories()[0]):Environment.getExternalStorageDirectory();
    public static final String SHOW_FOLDER_SIZE = "false";
    public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
}
