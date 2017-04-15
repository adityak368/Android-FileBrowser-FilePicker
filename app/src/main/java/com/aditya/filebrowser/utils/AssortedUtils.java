package com.aditya.filebrowser.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AssortedUtils {


	public static void SavePrefs(String key, String value, Context context)
	{
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = SP.edit();
		editor.putString(key,value);
		editor.commit();
	}

	public static String GetPrefs(String key,Context context)
	{
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
		return SP.getString(key, "");
	}
}