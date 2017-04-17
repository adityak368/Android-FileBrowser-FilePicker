package com.aditya.filebrowser;

import android.app.Activity;
import android.app.ProgressDialog;

import com.aditya.filebrowser.utils.UIUtils;
import com.beardedhen.androidbootstrap.BootstrapProgressBar;

/**
 * Created by Aditya on 4/16/2017.
 */
public class UIHelper {

    MainActivity mActivity;

    UIHelper(MainActivity activity) {
        this.mActivity = activity;
    }

    Runnable updateRunner() {
        return new Runnable() {
            @Override
            public void run() {
                mActivity.update();
            }
        };
    }

    Runnable errorRunner(final String msg) {
        return new Runnable() {
            @Override
            public void run() {
                UIUtils.ShowToast(msg,mActivity);
                mActivity.update();
            }
        };
    }

    Runnable progressUpdater(final ProgressDialog progressDialog, final int progress, final String msg) {
        return new Runnable() {
            @Override
            public void run() {
                progressDialog.setProgress(progress);
                progressDialog.setMessage(msg);
            }
        };
    }

    Runnable toggleProgressBarVisibility(final ProgressDialog progressDialog) {
        return new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        };
    }
}
