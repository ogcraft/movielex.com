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
import android.os.Process;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class AmatchMovieActivity extends Activity {
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
    private TextView    mv_play_desc;

    private TextView    mv_found_display_view;
    private Button      mv_btn_start_search;
    
	Handler mv_found_display_view_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {              
            Bundle bundle = msg.getData();
            long found_ms = bundle.getLong("found_ms");
            long search_duration_ms = bundle.getLong("search_duration_ms");
            if(mv_found_display_view != null) {
                String s = null;
                if(found_ms > 0) {
                   s = String.format(getString(R.string.amatch_found_fmt), 
                    found_ms/1000.0, search_duration_ms/1000.0);     
                } else {
                   s = String.format(getString(R.string.amatch_not_found_fmt),
                    search_duration_ms/1000.0);     
                }
                mv_found_display_view.setText(s);
            }
        }
    };
       
    Handler mv_seekbar_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {              
        }
    };     

    Handler mv_progress_display_view_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {              
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        gs = (MFApplication) getApplication();
        TAG = gs.getTAG();
        Log.d(TAG, "AmatchMovieActivity.onCreate(): before setContentView");
        setContentView(R.layout.movie_view);
        
        mv_title_view = (TextView)findViewById(R.id.mv_title);
        mv_play_desc = (TextView)findViewById(R.id.mv_play_desc);
        mv_progress_display_view = (TextView)findViewById(R.id.mv_progress_display);
        
        mv_found_display_view = (TextView)findViewById(R.id.mv_found_display);
        mv_btn_start_search = (Button)findViewById(R.id.mv_btn_start_search);
		mv_btn_start_search.setEnabled(false);
        mv_seekbar = (SeekBar)findViewById(R.id.mv_seekbar);
        mv_seekbar.setClickable(false);
        
		// Connect to handlers
        gs.amatch.found_display_view_handler    = mv_found_display_view_handler;
        //gs.amatch.seekbar_handler               = mv_seekbar_handler;
        //gs.amatch.progress_display_view_handler = mv_progress_display_view_handler;
            
        movie_position = getIntent().getIntExtra(MOVIE_POSITION, -1);
        Log.d(TAG,"AmatchMovieActivity.onCreate() movie_position: " + movie_position);
        
        if(movie_position != -1 && gs.movieItems != null) {
            selectedMovie = gs.movieItems.get(movie_position);
        }
        
        if(selectedMovie == null) {
            mv_title_view.setText("   ");
        } else {
            mv_title_view.setText(selectedMovie.title);
            mv_play_desc.setText(StringUtils.abbreviate(selectedMovie.desc, 200));
            //mv_details_img.setImageURI(selectedMovie.getImgUri());
            //mv_details_desc.setText(selectedMovie.desc);
            if(load_fpkeys() && load_translation_for_lang("ru")) {
                mv_found_display_view.setText("");
                mv_btn_start_search.setEnabled(true);
            } else {
                //Toast.makeText(getApplicationContext(),
                //    "Error: \"" + selectedMovie.id + "\" not found", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Error: \"" + selectedMovie.id + "\" not found");
            }
        }
		mv_found_display_view.setText("  \n  ");
	}

    public void onDestroy(){
    	super.onDestroy();
    	Log.d(TAG, "AmatchMovieActivity.onDestroy()");
    }
    
	public boolean load_translation_for_lang(String lang) {
		String fn = selectedMovie.getTranslationFileName(lang);
        Log.d(TAG, "AmatchMovieActivity: translation fn: " + fn);
		gs.amatch.createMediaPlayerForTranslation(fn);
		return true;
	}

	public boolean load_fpkeys() {
		if(selectedMovie == null) {
            Log.d(TAG,"AmatchMovieActivity.load_fpkeys() Fails: selectedMovie is null");
			return false;
		}
		String fn = gs.getFileNameForUrl(selectedMovie.fpkeys_file, selectedMovie.id);
        long keys = gs.amatch.load_fpkeys(fn);
		boolean ret = gs.amatch.isFpkeysLoaded;
        if(ret) {
            Log.d(TAG,"AmatchMovieActivity.load_fpkeys() Load: " + fn);
        } else {
            Log.d(TAG,"AmatchMovieActivity.load_fpkeys() Failed to Load: " + fn);
        }
		return ret;
	}

    public void btn_start_searchClick(View view)
	{
		Log.d(TAG,"AmatchMovieActivity.btn_start_searchClick()");
		if(!gs.amatch.isMediaPlayerReady)
		{
			Log.d(TAG,"btn_start_searchClick(): MediaPlayer still not Ready.");
			Toast.makeText(getApplicationContext(), R.string.mediaplayer_not_ready, Toast.LENGTH_SHORT).show();
			return;
		}
		if(gs.amatch.isMatching)
		{
			Toast.makeText(getApplicationContext(), R.string.still_in_progress_wait, Toast.LENGTH_SHORT).show();
			return;
		}
		mv_found_display_view.setText(R.string.wait_synchronizing);
		
		mv_seekbar.setProgress(0);
		mv_seekbar_handler.postDelayed(UpdateTranslationTime,100);
		gs.amatch.start_recording_thread();
	}

    public void btn_stopClick(View view)
    {
        gs.amatch.stop_plaing_translation();
    }
    
	private Runnable UpdateTranslationTime = new Runnable() {
        public void run() {
            mv_seekbar.setMax((int)gs.amatch.getTranslationMaxDuration());
            long currentPlayingTime_ms = gs.amatch.getCurrentTranslationPosition();
            
            long hour = TimeUnit.MILLISECONDS.toHours((long) currentPlayingTime_ms);
            long min = TimeUnit.MILLISECONDS.toMinutes((long) currentPlayingTime_ms)- 
                   TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours((long) currentPlayingTime_ms));
            long sec = TimeUnit.MILLISECONDS.toSeconds((long) currentPlayingTime_ms) - 
                   TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) currentPlayingTime_ms));
            
            long duration_ms = gs.amatch.getTranslationMaxDuration();

            long duration_hour = TimeUnit.MILLISECONDS.toHours((long) duration_ms);
            long duration_min = TimeUnit.MILLISECONDS.toMinutes((long) duration_ms)- 
                   TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours((long) duration_ms));
            long duration_sec = TimeUnit.MILLISECONDS.toSeconds((long) duration_ms) - 
                   TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) duration_ms));
            
           
            //mv_progress_display_view.setText(
            //         String.format("Playing %.2f ( %2d:%02d:%02d ) sec from %.2f sec", 
            //                         currentPlayingTime_ms / 1000.0,
            //                         hour, min, sec,
            //                         gs.amatch.getTranslationMaxDuration() / 1000.0
            //        ));
            mv_progress_display_view.setText(
                    String.format(getString(R.string.playing_info_fmt),    
                                    hour, min, sec,
                                    duration_hour, duration_min, duration_sec));
           
            mv_seekbar.setProgress((int)currentPlayingTime_ms);
            mv_seekbar_handler.postDelayed(this, 100);
        }
     };
}
