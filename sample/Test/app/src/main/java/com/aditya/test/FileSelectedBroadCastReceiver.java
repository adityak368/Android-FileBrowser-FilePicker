package com.aditya.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * Created by Aditya on 7/15/2017.
 */

public class FileSelectedBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Uri filePath = intent.getParcelableExtra(com.aditya.filebrowser.Constants.BROADCAST_SELECTED_FILE);
        Toast.makeText(context,filePath.toString(),Toast.LENGTH_LONG).show();
    }
}