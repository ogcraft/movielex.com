/*
AmatchMovieActivity.java:
Copyright (c) 2014, Oleg Galbert
All rights reserved.
*/

package com.mooviefish.mooviefishapp;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

public class AmatchMovieActivity extends Activity implements
        OnItemClickListener {
    public static final String MOVIE_POSITION = "MOVIE_POSITION";
    private MFApplication gs; 
    private String TAG = "Moovie";
	private String data_root_path = "";
	private String track_keys_fn = null;
	private String translation_fn = null;
    private int movie_position = -1;
    private MovieItem selectedMovie = null;
    private TextView    mv_progress_display_view;
    private SeekBar     mv_seekbar;
	private TextView    mv_title_view;
    private TextView    mv_found_display_view;
    private Button      mv_btn_start_search;
    private ListView    mv_list_view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        gs = (MFApplication) getApplication();
        TAG = gs.getTAG();

        setContentView(R.layout.movie_view);
        
        mv_seekbar = (SeekBar)findViewById(R.id.mv_seekbar);
        mv_seekbar.setClickable(false);
        
        mv_progress_display_view = (TextView)findViewById(R.id.mv_progress_display);
        mv_title_view = (TextView)findViewById(R.id.mv_title);
        mv_found_display_view = (TextView)findViewById(R.id.mv_found_display);
        mv_btn_start_search = (Button)findViewById(R.id.mv_btn_start_search);
        
        mv_list_view = (ListView) findViewById(R.id.mv_list);
            
        movie_position = getIntent().getIntExtra(MOVIE_POSITION, -1);
        Log.d(TAG,"AmatchMovieActivity.onCreate() movie_position: " + movie_position);
        
        List<String> langs = new ArrayList<String>();

        if(movie_position != -1) {
            selectedMovie = gs.movieItems.get(movie_position);
            int sz = selectedMovie.translations.size();
            for(int i=0; i<sz; i++)
            {
                langs.add(selectedMovie.translations.get(i).title);
            }
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
              android.R.layout.simple_list_item_1, android.R.id.text1, langs);
        mv_list_view.setAdapter(adapter); 
        mv_list_view.setOnItemClickListener(this);
		if(selectedMovie == null) {
            mv_title_view.setText("Movie Title");
        } else {
            mv_title_view.setText(selectedMovie.title);
        }
		mv_btn_start_search.setEnabled(false);
		mv_found_display_view.setText("");

	}

    public void onDestroy(){
    	super.onDestroy();
    	Log.d(TAG, "AmatchMovieActivity.onDestroy()");
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view,
       int position, long id) {
        int itemPosition     = position;
        String  itemValue    = (String) mv_list_view.getItemAtPosition(position); 
        Toast.makeText(getApplicationContext(),
        "\"" + itemValue + "\" not implemented yet", Toast.LENGTH_LONG).show();
    }

    public void btn_start_searchClick(View view)
    {
    	mv_found_display_view.setText("Please wait. Synchronizing...");
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
