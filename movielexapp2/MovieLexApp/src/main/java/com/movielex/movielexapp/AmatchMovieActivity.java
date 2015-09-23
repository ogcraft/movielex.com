/*
AmatchMovieActivity.java:
Copyright (c) 2014, Oleg Galbert
All rights reserved.
*/

package com.movielex.movielexapp;
import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder; 
import java.nio.FloatBuffer;
import java.lang.ref.WeakReference;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import android.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class AmatchMovieActivity extends Activity {
    public static final String MOVIE_POSITION = "MOVIE_POSITION";
    public static final String MOVIE_TRANSLATION_POSITION = "MOVIE_TRANSLATION_POSITION";
    private MFApplication gs; 
    private String TAG = "Moovie";
	private String data_root_path = "";
	private String track_keys_fn = null;
	private String translation_fn = null;
    private int movie_position = -1;
    private int movie_translation_position = -1;
    private long start_sync_ms = 0;
    private long got_match_result_start_ms = 0;
    private final int MAX_SYNC_TRIES = 3;
    private int match_tries = MAX_SYNC_TRIES;
    private MovieItem selectedMovie = null;
    private MovieTranslations   selectedTranslation = null;
    private ImageView   mv_sync_img;
    //private TextView    mv_progress_display_view;
    private TextView    mv_sync_play_time_left;
    private TextView    mv_sync_play_time_right;
    private SeekBar     mv_seekbar;
	//private TextView    mv_title_view;
    //private TextView    mv_play_desc;

    private TextView    mv_found_display_view;
    private Button      mv_btn_start_search;

    private static class FoundDisplayHandler extends Handler {
        private final WeakReference<AmatchMovieActivity> mActivity;

        public FoundDisplayHandler(AmatchMovieActivity activity) {
            mActivity = new WeakReference<AmatchMovieActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            AmatchMovieActivity activity = mActivity.get();
            if (activity != null) {
                Bundle bundle = msg.getData();
                long found_ms = bundle.getLong("found_ms");
                long search_duration_ms = bundle.getLong("search_duration_ms");
                long calculated_search_duration_ms = System.currentTimeMillis() - activity.start_sync_ms;
                Log.d(activity.TAG, String.format("OGG calculated_search_duration_ms: %d search_duration_ms: %d", calculated_search_duration_ms, search_duration_ms));
                if(activity.mv_found_display_view != null) {
                    String s = null;
                    activity.match_tries--;
                    Log.d(activity.TAG,"AmatchMovieActivity.handleMessage() match_tries: " + activity.match_tries + " found_ms: " + found_ms);
                    if(!(found_ms > 0)) {
                        // not found
                        if( activity.match_tries > 0 ) {
                            s = "Not found " + activity.match_tries + ". Do second try";
                            Log.d(activity.TAG,s);
                            //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                            activity.do_sync();
                        } else {
                            s = String.format(activity.getString(R.string.amatch_not_found_fmt),
                                    calculated_search_duration_ms/1000.0);
                            activity.mv_found_display_view.setText(s);
                        }
                    } else {
                        // found
                        s = String.format(activity.getString(R.string.amatch_found_fmt),
                                found_ms/1000.0, calculated_search_duration_ms/1000.0);
                        activity.mv_found_display_view.setText(s);
                    }
                    Log.d(activity.TAG, "OGG found ms: " + System.currentTimeMillis() + String.format(activity.getString(R.string.amatch_found_fmt),
                            found_ms/1000.0, calculated_search_duration_ms/1000.0));

                }
            }
        }
    }

    private final FoundDisplayHandler mv_found_display_view_handler = new FoundDisplayHandler(this);

//
//    Handler mv_found_display_view_handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            Bundle bundle = msg.getData();
//            long found_ms = bundle.getLong("found_ms");
//            long search_duration_ms = bundle.getLong("search_duration_ms");
//            long calculated_search_duration_ms = System.currentTimeMillis() - start_sync_ms;
//            Log.d(TAG, String.format("OGG calculated_search_duration_ms: %d search_duration_ms: %d", calculated_search_duration_ms, search_duration_ms));
//            if(mv_found_display_view != null) {
//                String s = null;
//                match_tries--;
//                Log.d(TAG,"AmatchMovieActivity.handleMessage() match_tries: " + match_tries + " found_ms: " + found_ms);
//                if(!(found_ms > 0)) {
//                    // not found
//                    if( match_tries > 0 ) {
//                        s = "Not found " + match_tries + ". Do second try";
//                        Log.d(TAG,s);
//                        //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
//                        do_sync();
//                    } else {
//                        s = String.format(getString(R.string.amatch_not_found_fmt),
//                        calculated_search_duration_ms/1000.0);
//                        mv_found_display_view.setText(s);
//                    }
//                } else {
//                    // found
//                    s = String.format(getString(R.string.amatch_found_fmt),
//                    found_ms/1000.0, calculated_search_duration_ms/1000.0);
//                    mv_found_display_view.setText(s);
//                }
//                Log.d(TAG, "OGG found ms: " + System.currentTimeMillis() + String.format(getString(R.string.amatch_found_fmt),
//                        found_ms/1000.0, calculated_search_duration_ms/1000.0));
//
//            }
//        }
//    };
       
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
		if(gs.isAppValid == false) {
			Log.d(TAG,"AmatchMovieActivity Exit application");
			finish();
			Process.killProcess( Process.myPid() ); 
		}
        Log.d(TAG, "AmatchMovieActivity.onCreate(): before setContentView");
        setContentView(R.layout.movie_sync_view);
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.header_background));
        
        //mv_title_view = (TextView)findViewById(R.id.mv_sync_title);
        mv_sync_img = (ImageView)findViewById(R.id.mv_sync_img);
        //mv_play_desc = (TextView)findViewById(R.id.mv_play_desc);
        //mv_progress_display_view = (TextView)findViewById(R.id.mv_progress_display);
        mv_sync_play_time_left = (TextView)findViewById(R.id.mv_sync_play_time_left);
        mv_sync_play_time_right = (TextView)findViewById(R.id.mv_sync_play_time_right);
        
        mv_found_display_view = (TextView)findViewById(R.id.mv_found_display);
        mv_btn_start_search = (Button)findViewById(R.id.mv_btn_start_search);
		mv_btn_start_search.setEnabled(false);
        mv_seekbar = (SeekBar)findViewById(R.id.mv_sync_seekbar);
        mv_seekbar.setClickable(false);
        
		// Connect to handlers
        gs.amatch.found_display_view_handler    = mv_found_display_view_handler;
        //gs.amatch.seekbar_handler               = mv_seekbar_handler;
        //gs.amatch.progress_display_view_handler = mv_progress_display_view_handler;
            
        movie_position = getIntent().getIntExtra(MOVIE_POSITION, -1);
        movie_translation_position = getIntent().getIntExtra(MOVIE_TRANSLATION_POSITION, -1);
        Log.d(TAG,"AmatchMovieActivity.onCreate() movie_position: " + movie_position + " movie_translation_position: " + movie_translation_position);
        
        if(movie_position != -1 && gs.movieItems != null) {
            selectedMovie = gs.movieItems.get(movie_position);
            if(movie_translation_position != -1 && selectedMovie != null) {
				selectedTranslation =  selectedMovie.translations.get(movie_translation_position);
			}
        }
        
		bar.setTitle(R.string.play_view_title);
        if(selectedMovie != null && selectedTranslation != null) {
            //bar.setTitle(selectedMovie.title);
            //mv_title_view.setText(selectedMovie.title);
            RelativeLayout.LayoutParams parms = 
                    new RelativeLayout.LayoutParams(gs.width,(int)(gs.height*0.3));
                //parms.addRule(RelativeLayout.BELOW, mv_title_view.getId());
            parms.addRule(RelativeLayout.ALIGN_RIGHT);
            mv_sync_img.setLayoutParams(parms);
            
            mv_sync_img.setImageURI(selectedMovie.getImgUri());
            //if(gs.isLargScreen()) {
            //    mv_sync_img.setImageURI(selectedMovie.getImgUri());
            //} else {
            //    RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(50,50);
            //    mv_sync_img.setLayoutParams(parms);
            //    mv_sync_img.setImageURI(selectedMovie.getImgUri());
            //}
            //mv_play_desc.setText(StringUtils.abbreviate(selectedMovie.desc, 200));

            if(gs.amatch.isMediaPlayerPlaying) {
                Log.d(TAG,"AmatchMovieActivity.onCreate() already Playing do nothing");
                mv_btn_start_search.setEnabled(false);
            } else if(load_fpkeys() && load_translation_for_lang(selectedTranslation.lang)) {
                mv_found_display_view.setText("");
                mv_btn_start_search.setEnabled(true);
            } else {
                Log.d(TAG,"AmatchMovieActivity.onCreate() is Not Playing and failed to load trans and fpke files");
                //Toast.makeText(getApplicationContext(),
                //    "Error: \"" + selectedMovie.id + "\" not found", Toast.LENGTH_LONG).show();
            }
        }
		mv_found_display_view.setText("  \n  ");
	}

    public void onDestroy(){
    	super.onDestroy();
    	Log.d(TAG, "AmatchMovieActivity.onDestroy()");
    }
    
	public boolean load_translation_for_lang(String lang) {
		String fn = gs.getFileNameForUrl(selectedTranslation.file, selectedMovie.id);
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

    public void do_sync()
    {
        Log.d(TAG,"AmatchMovieActivity.do_sync() OGG ms: " + System.currentTimeMillis());
        if(!gs.amatch.isMediaPlayerReady)
        {
            Log.d(TAG,"do_sync(): MediaPlayer still not Ready.");
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

    public void btn_start_searchClick(View view)
	{
        match_tries = MAX_SYNC_TRIES;
        start_sync_ms = System.currentTimeMillis();
		do_sync();
	}

    public void btn_stopClick(View view)
    {
        gs.amatch.stop_plaing_translation();
    }
    
    static public String msToTimeFormat(long millis) {
        String s = String.format("%02d:%02d:%02d", 
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) - 
            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
            TimeUnit.MILLISECONDS.toSeconds(millis) - 
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        return s;
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
            //        String.format(getString(R.string.playing_info_fmt),    
            //                        msToTimeFormat(currentPlayingTime_ms),
            //                        msToTimeFormat(duration_ms)));
            mv_sync_play_time_left.setText(msToTimeFormat(currentPlayingTime_ms));
            mv_sync_play_time_right.setText(msToTimeFormat(duration_ms));
           
            mv_seekbar.setProgress((int)currentPlayingTime_ms);
            mv_seekbar_handler.postDelayed(this, 100);
        }
     };
}
