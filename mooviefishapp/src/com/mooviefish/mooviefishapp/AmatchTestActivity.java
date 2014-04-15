/*
AmatchTestActivity.java:
Copyright (c) 2014, Oleg Galbert
All rights reserved.
*/

package com.mooviefish.mooviefishapp;
import amatch_generated.amatch_interface;
import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder; 
import java.nio.FloatBuffer; 
import java.util.concurrent.TimeUnit;

import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AmatchTestActivity extends Activity {
    private MFApplication gs; 
    private String TAG = "Moovie";
	private String data_root_path = "";
	private String track_keys_fn = null;
	private String translation_fn = null;
	int file_selecting_button_id = R.id.btn_load_fpkeys;

    private TextView progress_display_view;
    private SeekBar seekbar;
	private Button btnLoadFpkeys;
    private Button btnLoadTranslation;
    private TextView fpkeys_fn_view;
    private TextView found_display_view;
    private Button btn_start_search;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        gs = (MFApplication) getApplication();
        TAG = gs.getTAG();

        setContentView(R.layout.test_view);

        File f = Environment.getExternalStorageDirectory();
        data_root_path = f.getAbsolutePath();
        if(track_keys_fn == null) {
            track_keys_fn = data_root_path;
        }
        if(translation_fn == null) {
            translation_fn = data_root_path;
        }
        Log.d(TAG,"AmatchTestActivity.onCreate()");
        
        seekbar = (SeekBar)findViewById(R.id.seekbar);
        seekbar.setClickable(false);
      
        progress_display_view = (TextView)findViewById(R.id.progress_display);
        btnLoadFpkeys = (Button)findViewById(R.id.btn_load_fpkeys);
        btnLoadTranslation = (Button)findViewById(R.id.btn_load_translation);
        fpkeys_fn_view = (TextView)findViewById(R.id.fpkeys_fn);
        found_display_view = (TextView)findViewById(R.id.found_display);
        btn_start_search = (Button)findViewById(R.id.btn_start_search);
       
        btnLoadFpkeys.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
            	file_selecting_button_id = R.id.btn_load_fpkeys;
            	Log.d(TAG,"onClick() btnLoadFpkeys");
                final Intent intent = new Intent(AmatchTestActivity.this.getBaseContext(), FileDialog.class);
				intent.putExtra(FileDialog.START_PATH, data_root_path);
                intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
                // set file filter
                intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "fpkeys" });
                intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
                AmatchTestActivity.this.startActivityForResult(intent, FileAction.LOAD.value);
            }
        });
        
        btnLoadTranslation.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
            	file_selecting_button_id = R.id.btn_load_translation;
            	Log.d(TAG,"onClick() btnLoadTranslation");
                final Intent intent = new Intent(AmatchTestActivity.this.getBaseContext(), FileDialog.class);
				intent.putExtra(FileDialog.START_PATH, data_root_path);
                intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
                // set file filter
                intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "mp3","ogg","wav" });
                intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
                AmatchTestActivity.this.startActivityForResult(intent, FileAction.LOAD.value);
            }
        });

		fpkeys_fn_view.setText("App ver: " + gs.appVersion + ", Amatch: " + amatch_interface.AMATCH_VER);
		btn_start_search.setEnabled(false);
		found_display_view.setText("");

	}

    public void onDestroy(){
    	super.onDestroy();
    	Log.d(TAG, "onDestroy()");
    }

    @Override
    public synchronized void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            final String filename = data.getStringExtra(FileDialog.RESULT_PATH);
            switch (FileAction.fromValue(requestCode))
            {
                case LOAD:
                	File f = new File(filename);
                	data_root_path = f.getParent();
                    Log.d(TAG, "onActivity(): filename: " + filename + " data_root_path: " + data_root_path);
                    if(file_selecting_button_id == R.id.btn_load_fpkeys) {
                    	track_keys_fn = filename;
                    	//load_track_fpkeys(track_keys_fn);        	
                    	
                    } else if(file_selecting_button_id == R.id.btn_load_translation) {
                    	Button b = (Button)findViewById(R.id.btn_load_translation);   	
                    	btnLoadTranslation.setEnabled(false);
                    	translation_fn = filename;
                    	Log.d(TAG, "translation_fn: " + translation_fn);
                    	btnLoadTranslation.setText("Translation: " + translation_fn.substring(data_root_path.length()+1));
                    }
                    break;
                case SAVE:
                    break;
                case KEY:
                    break;
            }
           
        }
        else if (resultCode == Activity.RESULT_CANCELED)
            ; // do nothing
    }
    
    public void btn_start_searchClick(View view)
    {
    	btnLoadFpkeys.setEnabled(false);
    	found_display_view.setText("Please wait. Synchronizing...");
		// start search
    }
    public void btn_load_fpkeysClick(View view)
    {

    }
    
/*    
    private Runnable UpdateTranslationTime = new Runnable() {
        public void run() {

           currentPlayingTime_ms = mp.getCurrentPosition();
           long min = TimeUnit.MILLISECONDS.toMinutes((long) currentPlayingTime_ms);
           long sec = TimeUnit.MILLISECONDS.toSeconds((long) currentPlayingTime_ms) - 
                   TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) currentPlayingTime_ms));
           
           progress_display_view.setText(
        		   String.format("Playing %.2f ( %3d:%02d ) sec from %.2f sec",
                		   			currentPlayingTime_ms / 1000.0,
                		   			min, sec,
                		   			finalTime_ms / 1000.0
        		   ));
           
           seekbar.setProgress((int)currentPlayingTime_ms);
           seekbar_handler.postDelayed(this, 100);
        }
     };
*/
}
