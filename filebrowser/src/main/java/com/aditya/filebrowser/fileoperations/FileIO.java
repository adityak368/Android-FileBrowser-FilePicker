package com.aditya.filebrowser.fileoperations;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.NavigationHelper;
import com.aditya.filebrowser.R;
import com.aditya.filebrowser.utils.UIUpdateHelper;
import com.aditya.filebrowser.interfaces.IFuncPtr;
import com.aditya.filebrowser.models.FileItem;
import com.aditya.filebrowser.utils.UIUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Aditya on 4/15/2017.
 */
public class FileIO {

    ExecutorService executor;
    Handler mUIUpdateHandler;
    Context mContext;
    UIUpdateHelper mHelper;
    NavigationHelper mNavigationHelper;

    public FileIO(NavigationHelper mNavigationHelper,Handler mUIUpdateHandler, Context mContext) {
        this.mUIUpdateHandler = mUIUpdateHandler;
        this.mContext = mContext;
        this.mNavigationHelper = mNavigationHelper;
        mHelper = new UIUpdateHelper(mNavigationHelper, mContext);
        executor  = Executors.newFixedThreadPool(1);
    }

    public void createDirectory(final File path) {
        if(path.getParentFile()!=null && path.getParentFile().canWrite()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileUtils.forceMkdir(path);
                        mUIUpdateHandler.post(mHelper.updateRunner());
                    } catch (IOException e) {
                        e.printStackTrace();
                        mUIUpdateHandler.post(mHelper.errorRunner("An error occurred while creating a new folder"));
                    }
                }
            });
        } else {
            UIUtils.ShowToast("No Write Permission Granted",mContext);
        }
    }

    public void deleteItems(final List<FileItem> selectedItems) {
        if(selectedItems!=null && selectedItems.size()>0) {
            AlertDialog confirmDialog = new AlertDialog.Builder(mContext)
                    .setTitle("Delete Files")
                    .setMessage("Are you sure you want to delete " + selectedItems.size() + " items?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            final ProgressDialog progressDialog = new ProgressDialog(mContext);
                            progressDialog.setTitle("Deleting Please Wait... ");
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setCancelable(false);
                            progressDialog.setProgress(0);
                            progressDialog.setMessage("");
                            progressDialog.show();

                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    int i = 0;
                                    float TOTAL_ITEMS = selectedItems.size();
                                    try {
                                        for (; i < selectedItems.size(); i++) {
                                            mUIUpdateHandler.post(mHelper.progressUpdater(progressDialog, (int)((i/TOTAL_ITEMS)*100), "File: "+selectedItems.get(i).getFile().getName()));
                                            if (selectedItems.get(i).getFile().isDirectory()) {
                                                FileUtils.deleteDirectory(selectedItems.get(i).getFile());
                                            } else {
                                                FileUtils.forceDelete(selectedItems.get(i).getFile());
                                            }
                                        }
                                        mUIUpdateHandler.post(mHelper.toggleProgressBarVisibility(progressDialog));
                                        mUIUpdateHandler.post(mHelper.updateRunner());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        mUIUpdateHandler.post(mHelper.toggleProgressBarVisibility(progressDialog));
                                        mUIUpdateHandler.post(mHelper.errorRunner("An error occurred while deleting "));
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            UIUtils.ShowToast("No Items Selected!",mContext);
        }
    }

    public void pasteFiles(final File destination) {

        final Operations op = Operations.getInstance(mContext);
        final List<FileItem> selectedItems = op.getSelectedFiles();
        final Operations.FILE_OPERATIONS operation = op.getOperation();
        if(destination.canWrite()) {
            if (selectedItems != null && selectedItems.size() > 0) {
                final ProgressDialog progressDialog = new ProgressDialog(mContext);
                String title = "Please Wait... ";
                progressDialog.setTitle(title);
                if (operation == Operations.FILE_OPERATIONS.COPY)
                    progressDialog.setTitle("Copying " + title);
                else if (operation == Operations.FILE_OPERATIONS.CUT)
                    progressDialog.setTitle("Moving " + title);

                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("");
                progressDialog.setProgress(0);
                progressDialog.show();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        float TOTAL_ITEMS = selectedItems.size();
                        try {
                            for (; i < selectedItems.size(); i++) {
                                mUIUpdateHandler.post(mHelper.progressUpdater(progressDialog, (int) ((i / TOTAL_ITEMS) * 100), "File: " + selectedItems.get(i).getFile().getName()));
                                if (selectedItems.get(i).getFile().isDirectory()) {
                                    if (operation == Operations.FILE_OPERATIONS.CUT)
                                        FileUtils.moveDirectory(selectedItems.get(i).getFile(), new File(destination, selectedItems.get(i).getFile().getName()));
                                    else if (operation == Operations.FILE_OPERATIONS.COPY)
                                        FileUtils.copyDirectory(selectedItems.get(i).getFile(), new File(destination, selectedItems.get(i).getFile().getName()));
                                } else {
                                    if (operation == Operations.FILE_OPERATIONS.CUT)
                                        FileUtils.moveFile(selectedItems.get(i).getFile(), new File(destination, selectedItems.get(i).getFile().getName()));
                                    else if (operation == Operations.FILE_OPERATIONS.COPY)
                                        FileUtils.copyFile(selectedItems.get(i).getFile(), new File(destination, selectedItems.get(i).getFile().getName()));
                                }
                            }
                            mUIUpdateHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    op.resetOperation();
                                }
                            });
                            mUIUpdateHandler.post(mHelper.toggleProgressBarVisibility(progressDialog));
                            mUIUpdateHandler.post(mHelper.updateRunner());
                        } catch (IOException e) {
                            e.printStackTrace();
                            mUIUpdateHandler.post(mHelper.toggleProgressBarVisibility(progressDialog));
                            mUIUpdateHandler.post(mHelper.errorRunner("An error occurred while pasting "));
                        }
                    }
                });
            } else {
                UIUtils.ShowToast("No Items Selected!", mContext);
            }
        } else {
            UIUtils.ShowToast("No Write permissions for the paste directory",mContext);
        }
    }

    public void renameFile(final FileItem fileItem) {
        UIUtils.showEditTextDialog(mContext, "Rename", fileItem.getFile().getName() ,new IFuncPtr() {
            @Override
            public void execute(final String val) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(fileItem.getFile().isDirectory())
                                FileUtils.moveDirectory(fileItem.getFile(),new File(fileItem.getFile().getParentFile(), val.trim()));
                            else
                                FileUtils.moveFile(fileItem.getFile(),new File(fileItem.getFile().getParentFile(), val.trim()));
                            mUIUpdateHandler.post(mHelper.updateRunner());
                        } catch (Exception e) {
                            e.printStackTrace();
                            mUIUpdateHandler.post(mHelper.errorRunner("An error occurred while renaming "));
                        }
                    }
                });
            }
        });
    }

    public void getProperties(List<FileItem> selectedItems) {

        StringBuilder msg = new StringBuilder();
        if(selectedItems.size()==1) {
            boolean isDirectory = (selectedItems.get(0).getFile().isDirectory());
            String type = isDirectory?"Directory":"File";
            String size = FileUtils.byteCountToDisplaySize(isDirectory?FileUtils.sizeOfDirectory(selectedItems.get(0).getFile()):FileUtils.sizeOf(selectedItems.get(0).getFile()));
            String lastModified = new SimpleDateFormat(Constants.DATE_FORMAT).format(selectedItems.get(0).getFile().lastModified());
            msg.append("Type : " + type + "\n\n");
            msg.append("Size : " + size + "\n\n");
            msg.append("Last Modified : " + lastModified + "\n\n");
            msg.append("Path : "+selectedItems.get(0).getFile().getAbsolutePath());
        } else {
            long totalSize = 0;
            for(int i=0;i<selectedItems.size();i++) {
                boolean isDirectory = (selectedItems.get(i).getFile().isDirectory());
                totalSize += isDirectory?FileUtils.sizeOfDirectory(selectedItems.get(i).getFile()):FileUtils.sizeOf(selectedItems.get(i).getFile());
            }
            msg.append("Type : " + "Multiple Files" + "\n\n");
            msg.append("Size : " + FileUtils.byteCountToDisplaySize(totalSize) + "\n\n");
        }
        UIUtils.ShowMsg(msg.toString(),"Properties",mContext);
    }

    public void shareMultipleFiles(List<FileItem> filesToBeShared){

        ArrayList<Uri> uris = new ArrayList<>();
        for(FileItem file: filesToBeShared){
            uris.add(Uri.fromFile(file.getFile()));
        }
        final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        PackageManager manager = mContext.getPackageManager();
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
        if (infos.size() > 0) {
            mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share)));
        } else {
            UIUtils.ShowToast("No app found to handle sharing",mContext);
        }
    }
}
