package com.aditya.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.FileBrowser;
import com.aditya.filebrowser.FileBrowserWithCustomHandler;
import com.aditya.filebrowser.FileChooser;
import com.aditya.filebrowser.FolderChooser;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    static int PICK_FOLDER_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i = new Intent(this, FolderChooser.class);
        i.putExtra(Constants.INITIAL_DIRECTORY, new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"Movies").getAbsolutePath());
        i.putExtra(Constants.ALLOWED_FILE_EXTENSIONS, "mkv;mp4");
        i.putExtra(Constants.SELECTION_MODE, Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal());
        startActivityForResult(i, PICK_FOLDER_REQUEST);
        //startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FOLDER_REQUEST && data!=null) {
            if (resultCode == RESULT_OK) {
                Uri file = data.getData();
                Toast.makeText(this, file.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
