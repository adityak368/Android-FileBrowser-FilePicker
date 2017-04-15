package com.aditya.filebrowser;


public class Filename {
    private String fullPath;
    private char pathSeparator, extensionSeparator;
    int indication = 0;

    public Filename(String str, char sep, char ext) {
        fullPath = str;
        pathSeparator = sep;
        extensionSeparator = ext;
    }

    public String getName()
    {
        if(fullPath.lastIndexOf(".")==-1)
            return fullPath;
        else
            return fullPath.substring(0,fullPath.lastIndexOf("."));
    }

    public String extension() {
        if (!fullPath.contains(".")) {
            indication = 1;
        }
        if (indication == 0) {
            int dot = fullPath.lastIndexOf(extensionSeparator);
            return fullPath.substring(dot + 1);
        } else {
            return "noext";
        }
    }


}