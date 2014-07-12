package com.mooviefish.mooviefishapp;

import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SetPrefsActivity extends PreferenceActivity {
	private MFApplication gs; 
	private String TAG = "MoovieFishApp";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gs = (MFApplication) getApplication();
    	TAG = gs.getTAG();
    	Log.d(TAG, "SetPrefsActivity.onCreate()");
		addPreferencesFromResource(R.layout.preferences);

	}

}