# FileBrowser

A FileBrowser / FileChooser for Android that you can integrate to your app to browse/select files from internal/external storage.

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android--FileBrowser--FilePicker-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/5636)

# Using Maven
``` xml
<dependency>
  <groupId>com.adityak</groupId>
  <artifactId>browsemyfiles</artifactId>
  <version>1.7</version>
  <type>pom</type>
</dependency>
```
# Or Using Gradle
```
compile 'com.adityak:browsemyfiles:1.7'
```


<img src="https://cloud.githubusercontent.com/assets/19688735/25305189/670232ec-2794-11e7-819f-b92f487b3075.png" width="300">   <img src="https://cloud.githubusercontent.com/assets/19688735/25305190/670328a0-2794-11e7-86ac-62b69af7b577.png" width="300">
<img src="https://cloud.githubusercontent.com/assets/19688735/25305188/6701de1e-2794-11e7-981f-7d6d0124b2b2.png" width="300">   <img src="https://cloud.githubusercontent.com/assets/19688735/25305187/6701b74a-2794-11e7-8057-c5677db858b0.png" width="300">
<img src="https://cloud.githubusercontent.com/assets/19688735/25305186/6701b33a-2794-11e7-8c58-d5da64e40768.png" width="300">   <img src="https://cloud.githubusercontent.com/assets/19688735/25305191/67038f8e-2794-11e7-8777-4bb0db870c31.png" width="300">
<img src="https://cloud.githubusercontent.com/assets/19688735/25305192/6730cec2-2794-11e7-8ab0-9b696822520f.png" width="300">   <img src="https://cloud.githubusercontent.com/assets/19688735/25563142/0f8568ca-2db3-11e7-8782-f22bcce53fd1.png" width="300">

It easily integrates with your app's color scheme. You can change the color scheme using the following in your styles.xml

``` xml
<item name="colorPrimary">@color/colorPrimary</item>
<item name="colorPrimaryDark">@color/colorPrimaryDark</item>
<item name="colorAccent">@color/colorAccent</item>
<item name="android:actionModeBackground">@color/actionModeToolbar</item>
```

There are 3 main classes to use the library.

1. FileBrowser - Used to just Browse files in storage (has all file IO features)
2. FileChooser - Used to select single/multiple files in storage (has some IO features)
3. FileBrowserWithCustomHandler - Used to run custom code when files are selected (has all IO features)

# 1. FileBrowser
Use following Intent to start the FileBrowser

``` java
Intent i4 = new Intent(getApplicationContext(), FileBrowser.class);
startActivity(i4);
```

# 2. FileChooser

Use following Intent to start the FileChooser
  - For Single Selection

``` java
Intent i2 = new Intent(getApplicationContext(), FileChooser.class);
i2.putExtra(Constants.SELECTION_MODE,Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal());
startActivityForResult(i2,PICK_FILE_REQUEST);
```


To get the selected file, In your calling activity's onActivityResult method, use the following

```java

if (requestCode == PICK_FILE_REQUEST && data!=null) {
      if (resultCode == RESULT_OK) {
          Uri file = data.getData();
      }
}
        
```


  - For Multiple Selection
``` java
Intent i2 = new Intent(getApplicationContext(), FileChooser.class);
i2.putExtra(Constants.SELECTION_MODE,Constants.SELECTION_MODES.MULTIPLE_SELECTION.ordinal());
startActivityForResult(i2,PICK_FILE_REQUEST);
```

To get the selected file, In your calling activity's onActivityResult method, use the following

```java

if (requestCode == PICK_FILE_REQUEST && data!=null) {
      if (resultCode == RESULT_OK) {
          ArrayList<Uri> selectedFiles  = data.getParcelableArrayListExtra(Constants.SELECTED_ITEMS);
      }
}
        
```

# 3. FileBrowserWithCustomHandler

Register a Broadcast receiver and handle the onReceive method like below

```java

public class FileSelectedBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
         Uri filePath = intent.getParcelableExtra(com.aditya.filebrowser.Constants.BROADCAST_SELECTED_FILE);
    }
}

```

Register the receiver like the following snippet 

``` xml

        <receiver android:name=".FileSelectedBroadCastReceiver"
            android:exported="false"
            android:enabled="true">
            <intent-filter>
                <action android:name="com.adityak.filebrowser.FILE_SELECTED_BROADCAST" />
            </intent-filter>
        </receiver>
        
```

If you also need some other parameters to be sent with the broadcast use the following when creating the activity
``` java

Intent i = new Intent(mContext, FileBrowserWithCustomHandler.class);
Bundle ib = new Bundle();
//add extras
i.putExtras(ib);
startActivity(i);

```
and in the broadcast receiver use the following to get the extras

``` java
Bundle b = intent.getExtras()
```

### To load a particular directory instead of the normal root directory
Add the following in the intent
``` java
Intent i = new Intent(this, FileBrowser.class); //works for all 3 main classes (i.e FileBrowser, FileChooser, FileBrowserWithCustomHandler)
i.putExtra(Constants.INITIAL_DIRECTORY, new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"Movies").getAbsolutePath());
```


### To list only the files with a particular extension
Add the following in the intent
``` java
Intent i = new Intent(this, FileBrowser.class); //works for all 3 main classes (i.e FileBrowser, FileChooser, FileBrowserWithCustomHandler)
i.putExtra(Constants.ALLOWED_FILE_EXTENSIONS, "mkv;mp4;avi");
```

Use file extensions delimited by semicolon

### Known Issues
Currently folder size is calculated using Java's Api getTotalSpace() which gives the size of the partition of the path which may not always give the desired result.
To get the exact size, properties can be used which gives the exact result. If Exact size is to be known, then UI would lag as it would take some time to calculate size - Fix for this is postponed


If you have any questions/queries/Bugs/Hugs please mail @
adityak368@gmail.com
