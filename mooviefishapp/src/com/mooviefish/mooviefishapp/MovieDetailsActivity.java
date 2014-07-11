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
import android.widget.ProgressBar;
import android.app.ProgressDialog;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

public class MovieDetailsActivity extends Activity implements OnClickListener {
    public static final String MOVIE_POSITION = "MOVIE_POSITION";
    public enum TransState { UNKNOWN, DOWNLOADING, DOWNLOADED};
    private MFApplication gs; 
    private String TAG = "Moovie";
	private String      data_root_path = "";
	private String      track_keys_fn = null;
	private String      translation_fn = null;
    private int         movie_position = -1;
    private MovieItem   selectedMovie = null;
	private TextView    mv_title_view;
    private ImageView   mv_details_img;
    private TextView    mv_details_desc;

    private Button      mv_details_trans_get1;
    private TransState  trans_state1 = TransState.UNKNOWN;
    private ProgressBar mv_details_trans_progress1;
    private ProgressDialog fpkey_download_dialog = null;
    private ProgressDialog trans_download_dialog = null;
       
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        gs = (MFApplication) getApplication();
        TAG = gs.getTAG();

        setContentView(R.layout.movie_details_view);
        
        
        mv_details_img = (ImageView)findViewById(R.id.mv_details_img);
        mv_title_view = (TextView)findViewById(R.id.mv_title);
        mv_details_desc = (TextView)findViewById(R.id.mv_details_desc);
        mv_details_trans_get1 = (Button)findViewById(R.id.mv_details_trans_get1);
        mv_details_trans_get1.setText("Загрузить");
        mv_details_trans_get1.setOnClickListener(this);
        mv_details_trans_progress1 = (ProgressBar) findViewById(R.id.mv_details_trans_progress1);
        mv_details_trans_progress1.setVisibility(View.INVISIBLE);

        
        movie_position = getIntent().getIntExtra(MOVIE_POSITION, -1);
        Log.d(TAG,"MovieDetailsActivity.onCreate() movie_position: " + movie_position);
        
        List<String> langs = new ArrayList<String>();

        if(movie_position != -1 || gs.movieItems == null) {
            selectedMovie = gs.movieItems.get(movie_position);
            int sz = selectedMovie.translations.size();
            Log.d(TAG, "MovieDetailsActivity.onCreate() Movie has trans: " + sz);
            //for(int i=0; i<sz; i++)
            //{
            //    langs.add(selectedMovie.translations.get(i).title);
            //}
        }
        
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        //      android.R.layout.simple_list_item_1, android.R.id.text1, langs);
        //mv_list_view.setAdapter(adapter); 
        //mv_list_view.setOnItemClickListener(this);
		if(selectedMovie == null) {
            mv_title_view.setText("Movie Title");
        } else {
            mv_title_view.setText(selectedMovie.title);
            mv_details_img.setImageURI(selectedMovie.getImgUri());
            mv_details_desc.setText(selectedMovie.desc);
        }
	}

    public void onDestroy(){
    	super.onDestroy();
    	Log.d(TAG, "MovieDetailsActivity.onDestroy()");
    }

	public boolean load_translation_for_lang(String lang) {
		if(selectedMovie == null) {
            Log.d(TAG,"MovieDetailsActivity.load_translation_for_lang() Fails: selectedMovie is null");
            return false;
        }
        final String url = selectedMovie.getTranslationFileName(lang);
        Log.d(TAG, "MovieDetailsActivity.load_translation_for_lang: url: " + url);
        mv_details_trans_get1.setText("Загрузка...");
        String fn = gs.getFileNameForUrl(url, selectedMovie.id);

        Log.d(TAG,"MovieDetailsActivity.load_translation_for_lang() fn: " + fn);
        
        File target = new File(fn);
        if(!target.isFile()) {
            mv_details_trans_progress1.setVisibility(View.INVISIBLE);
        

                trans_download_dialog = new ProgressDialog(this);

                //trans_download_dialog.setIndeterminate(true);
                trans_download_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                trans_download_dialog.setCancelable(true);
                trans_download_dialog.setInverseBackgroundForced(false);
                trans_download_dialog.setCanceledOnTouchOutside(true);
                trans_download_dialog.setTitle("Downloading Translation");
            
            //gs.aq.progress(R.id.mv_details_trans_progress1).download(url, target, new AjaxCallback<File>(){
            gs.aq.progress(trans_download_dialog).download(url, target, new AjaxCallback<File>(){

                public void callback(String url, File file, AjaxStatus status) {

                    if(file != null){
                        Log.d(TAG, " Downloaded File:" + file.length() + ":" + file);
                        if(trans_download_dialog != null) {
                            trans_download_dialog.dismiss(); 
                        }
                        mv_details_trans_get1.setText("Слушать");
                    }else{
                        Log.d(TAG, "Failed download: "  + url);
                        mv_details_trans_get1.setText("Загрузить");
                        if(trans_download_dialog != null) {
                            trans_download_dialog.dismiss(); 
                        }
                    }
                }

            });
        } else {
            if(trans_download_dialog != null) {
                trans_download_dialog.dismiss();
            }
            mv_details_trans_get1.setText("Слушать");
            Log.d(TAG, "File " + fn + " exist");
        } 
		return true;
	}

    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mv_details_trans_get1:
            mv_details_trans_get1_onClick(v);
            break;
        }
    }

    public void mv_details_trans_get1_onClick(View v) {
        Log.d(TAG,"MovieDetailsActivity.mv_details_trans_get1_onClick():");
        load_fpkeys();
    }

	public boolean load_fpkeys() {
		if(selectedMovie == null) {
            Log.d(TAG,"MovieDetailsActivity.load_fpkeys() Fails: selectedMovie is null");
			return false;
		}
        mv_details_trans_get1.setText("Загрузка...");
		String fn = gs.getFileNameForUrl(selectedMovie.fpkeys_file, selectedMovie.id);

        Log.d(TAG,"MovieDetailsActivity.load_fpkeys() fn: " + fn);
        
        File target = new File(fn);
        if(!target.isFile()) {
            mv_details_trans_progress1.setVisibility(View.INVISIBLE);

                fpkey_download_dialog = new ProgressDialog(this);

                //fpkey_download_dialog.setIndeterminate(true);
                fpkey_download_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                fpkey_download_dialog.setCancelable(true);
                fpkey_download_dialog.setInverseBackgroundForced(false);
                fpkey_download_dialog.setCanceledOnTouchOutside(true);
                fpkey_download_dialog.setTitle("Downloading Index");
        
            gs.aq.progress(fpkey_download_dialog).download(selectedMovie.fpkeys_file, target, new AjaxCallback<File>(){

                public void callback(String url, File file, AjaxStatus status) {

                    if(file != null){
                        Log.d(TAG, " Downloaded File:" + file.length() + ":" + file);
                        mv_details_trans_get1.setText("Слушать");
                        if(fpkey_download_dialog != null) {
                            fpkey_download_dialog.dismiss();   
                        }
                        load_translation_for_lang("ru");
                    }else{
                        Log.d(TAG, "Failed download: "  + selectedMovie.fpkeys_file);
                        mv_details_trans_get1.setText("Загрузить");
                        if(fpkey_download_dialog != null) {
                            fpkey_download_dialog.dismiss();   
                        }
                    }
                }

            });
        } else {
            Log.d(TAG, "File " + fn + " exist");
            load_translation_for_lang("ru");
        }

/*
        long keys = gs.amatch.load_fpkeys(fn);
        //String short_fn = fn.substring(data_root_path.length()+1);
		boolean ret = gs.amatch.isFpkeysLoaded;
        if(ret) {
            Log.d(TAG,"MovieDetailsActivity.load_fpkeys() Load: " + fn);
        } else {
            Log.d(TAG,"MovieDetailsActivity.load_fpkeys() Failed to Load: " + fn);
        }      
		return ret;
*/
        return true;
	}
   
}
