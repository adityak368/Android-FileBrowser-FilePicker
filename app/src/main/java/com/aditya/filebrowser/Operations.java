package com.aditya.filebrowser;

import java.util.ArrayList;

/**
 * Created by adik on 9/27/2015.
 */
public class Operations {

    static ArrayList<String> files = new ArrayList<>();

    public static void resetFiles() {
        files.clear();
    }

    public static String getAction() {
        return Action;
    }

    public static void setAction(String action) {
        Action = action;
    }

    static String Action;

    public static ArrayList<String> getFiles() {
        return files;
    }

    public static void addFile(String path) {
        files.add(path);
    }

    private static Operations op;

    private Operations()
    {

    }
    public static Operations getInstance()
    {
        if(op==null)
        {
            op = new Operations();
        }
        return op;
    }
}
