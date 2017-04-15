package com.aditya.filebrowser.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

public class UIUtils {
		
	public static void ShowError(String msg, Context context)
	{
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);                      
	    dlgAlert.setMessage(msg);
	    dlgAlert.setTitle("Error");
	    dlgAlert.setIcon(android.R.drawable.ic_dialog_alert);
	    dlgAlert.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface popup, int arg1) {
				// TODO Auto-generated method stub
				popup.dismiss();
			}
		});
	    dlgAlert.setCancelable(false);
	    dlgAlert.show();
	}
	
	public static void ShowMsg(String msg, String title,Context context)
	{
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);                      
	    dlgAlert.setMessage(msg);
	    dlgAlert.setTitle(title);
	    dlgAlert.setIcon(android.R.drawable.ic_dialog_alert);
	    dlgAlert.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface popup, int arg1) {
				// TODO Auto-generated method stub
				popup.dismiss();
			}
		});
	    dlgAlert.setCancelable(false);
	    dlgAlert.show();
	}
	
	public static void ShowToast(String msg,Context context)
	{
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
}
