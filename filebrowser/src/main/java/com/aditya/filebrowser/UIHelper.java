package com.aditya.filebrowser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.aditya.filebrowser.interfaces.OnChangeDirectoryListener;
import com.aditya.filebrowser.utils.UIUtils;

/**
 * Created by Aditya on 4/16/2017.
 */
public class UIHelper {

    OnChangeDirectoryListener mOnChangeDirectoryListener;
    Context mContext;

    UIHelper(Context mContext,OnChangeDirectoryListener mOnChangeDirectoryListener) {
        this.mOnChangeDirectoryListener = mOnChangeDirectoryListener;
        this.mContext = mContext;
    }

    Runnable updateRunner() {
        return new Runnable() {
            @Override
            public void run() {
                if(mOnChangeDirectoryListener!=null) {
                    mOnChangeDirectoryListener.updateUI(null, true);
                }
            }
        };
    }

    Runnable errorRunner(final String msg) {
        return new Runnable() {
            @Override
            public void run() {
                if(mOnChangeDirectoryListener!=null) {
                    UIUtils.ShowToast(msg, mContext);
                    mOnChangeDirectoryListener.updateUI(null, true);
                }
            }
        };
    }

    Runnable progressUpdater(final ProgressDialog progressDialog, final int progress, final String msg) {
        return new Runnable() {
            @Override
            public void run() {
                if(progressDialog!=null) {
                    progressDialog.setProgress(progress);
                    progressDialog.setMessage(msg);
                }
            }
        };
    }

    Runnable toggleProgressBarVisibility(final ProgressDialog progressDialog) {
        return new Runnable() {
            @Override
            public void run() {
                if(progressDialog!=null) {
                    progressDialog.dismiss();
                }
            }
        };
    }
}
