package com.moviefish.mooviefishapp;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import android.os.Environment;
import android.app.Application;
 
/**
* This is a global state for MoovieFishApp
*
* // Set values
* MFApplication gs = (MFApplication) getApplication();
* gs.setTestMe("Some String");
* 
* // Get values
* MFApplication gs = (MFApplication) getApplication();
* String s = gs.getTestMe();
* 
* @author Oleg Galbert
*
*/

public class MFApplication extends Application
{
	private static final String TAG = "MoovieFishApp";
	private final String root_path = Environment.getExternalStorageDirectory() + "/MoovieFish/";
	public String getTAG() {
		return TAG;
	}
	public String getRootPath() {
		return root_path;
	}

	public static String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		reader.close();
		return sb.toString();
	}

	public static String getStringFromFile (String filePath) throws Exception {
		File fl = new File(filePath);
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
    //Make sure you close all streams.
		fin.close();        
		return ret;
	}

}
