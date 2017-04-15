package com.aditya.filebrowser;

import android.Manifest;
import android.os.Environment;

/**
 * Created by adik on 10/18/2015.
 */
public class Constants {

    public static final String APP_PREMISSION_KEY = "APP_PERMISSIONS";
    public static final String [] APP_PREMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ,Manifest.permission.INTERNET
//            ,Manifest.permission.ACCESS_NETWORK_STATE
            ,Manifest.permission.READ_CONTACTS,
            Manifest.permission.CAMERA};
}
