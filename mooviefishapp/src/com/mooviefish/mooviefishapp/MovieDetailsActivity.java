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
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
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
import android.app.ActionBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import org.apache.commons.lang3.StringUtils;
import android.net.Uri;

public class MovieDetailsActivity extends Activity implements OnClickListener {
    public static final String MOVIE_POSITION = "MOVIE_POSITION";
    public enum TransState { UNKNOWN, DOWNLOADING, DOWNLOADED};
    private MFApplication gs; 
    private String TAG = "Moovie";
	private String      data_root_path = "";
	private String      transLang = "";
    private int         movie_position = -1;
    private MovieItem   selectedMovie = null;
	//private TextView    mv_title_view;
    private ImageView   mv_details_img;
    private TextView    mv_details_desc;

    private TextView    mv_details_trans_title1;
    private Button      mv_details_trans_get1;
    private TransState  trans_state1 = TransState.UNKNOWN;
    private ProgressDialog fpkey_download_dialog = null;
    private ProgressDialog trans_download_dialog = null;
       
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        gs = (MFApplication) getApplication();
        TAG = gs.getTAG();
        transLang = gs.getTransLang();
        setContentView(R.layout.movie_details_view);
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.header_background));
        
        //mv_title_view = (TextView)findViewById(R.id.mv_detail_title);
        mv_details_img = (ImageView)findViewById(R.id.mv_details_img);
        mv_details_desc = (TextView)findViewById(R.id.mv_details_desc);
        mv_details_trans_title1 = (TextView)findViewById(R.id.mv_details_trans_title1);
        mv_details_trans_title1.setText(R.string.ru_lang_name);
        mv_details_trans_get1 = (Button)findViewById(R.id.mv_details_trans_get1);
        mv_details_trans_get1.setText(R.string.download);
        mv_details_trans_get1.setOnClickListener(this);

        
        movie_position = getIntent().getIntExtra(MOVIE_POSITION, -1);
        Log.d(TAG,"MovieDetailsActivity.onCreate() movie_position: " + movie_position);
        
        List<String> langs = new ArrayList<String>();

        if(movie_position != -1 || gs.movieItems == null) {
            selectedMovie = gs.movieItems.get(movie_position);
        }
        
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        //      android.R.layout.simple_list_item_1, android.R.id.text1, langs);
        //mv_list_view.setAdapter(adapter); 
        //mv_list_view.setOnItemClickListener(this);
        bar.setTitle(R.string.details_view_title);
		if(selectedMovie == null) {
            //mv_title_view.setText("   ");
        } else {
            //mv_title_view.setText(selectedMovie.title);
            if(mv_details_desc != null && gs.isLargScreen()) {
                //mv_details_desc.setText(
                //    StringUtils.abbreviate(selectedMovie.desc, 
                //    MFApplication.MAX_CHAR_ALLOWED_MOVIE_DETAILS_DESC_VIEW));
                RelativeLayout.LayoutParams parms = 
                    new RelativeLayout.LayoutParams(gs.width,(int)(gs.height*0.6));
                //parms.addRule(RelativeLayout.BELOW, mv_title_view.getId());
                parms.addRule(RelativeLayout.ALIGN_RIGHT);
                mv_details_img.setLayoutParams(parms);
                mv_details_img.setClickable(true);
                mv_details_img.setOnClickListener(this); 
            }

            if( isFpkesExist() && isTranslationExist(transLang)) {
                trans_state1 = TransState.DOWNLOADED;
                mv_details_trans_get1.setText(R.string.play); 
            }

            mv_details_img.setImageURI(selectedMovie.getImgUri());
        }
	}

    public void onDestroy(){
    	super.onDestroy();
    	Log.d(TAG, "MovieDetailsActivity.onDestroy()");
    }

    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mv_details_trans_get1:
            mv_details_trans_get1_onClick(v);
            break;
            case R.id.mv_details_img:
            //Toast.makeText(getApplicationContext(),
            //    "The movie details would appear on clicking this icon",
            //    Toast.LENGTH_LONG).show();

            if(selectedMovie != null) {
                Log.d(TAG, "MovieDetailsActivity open " + selectedMovie.src_url);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                    Uri.parse(selectedMovie.src_url));
                startActivity(browserIntent);
            }
            break;
        }
    }

//   @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.layout.menu, menu);
//        return true;
//    }

    public void mv_details_trans_get1_onClick(View v) {
        Log.d(TAG,"MovieDetailsActivity.mv_details_trans_get1_onClick():");
    	acquirePermissionForMovie("11");
    }

    public boolean isFpkesExist() {
        final String fn = gs.getFileNameForUrl(selectedMovie.fpkeys_file, selectedMovie.id);
        final File f = new File(fn);
        return f.isFile() && f.length() > 100;
    }
    
    public boolean isTranslationExist(String lang) {
        final String url = selectedMovie.getTranslationFileName(lang);
        final String fn = gs.getFileNameForUrl(url, selectedMovie.id);
        File f = new File(fn);
        return f.isFile() && f.length() > 100;
    }

    public boolean acquirePermissionForMovie(String did) {
		if(selectedMovie == null) {
            Log.d(TAG,"MovieDetailsActivity.acquirePermissionForMovie() Fails: selectedMovie is null");
			return false;
		}
        String url = String.format(
				gs.GETACQUIRE_PERMISSION_REST, gs.BASE_URL, did, selectedMovie.id);
        Log.d(TAG, "acquirePermissionForMovie(): url: " + url);

		gs.aq.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {
			@Override
			public void callback(String url, JSONObject json, AjaxStatus status) {
				if(json != null){
					Log.d(TAG,"MovieDetailsActivity.acquirePermissionForMovie() got result: " + json);	
					boolean permission = false; 
					try {
						permission = json.getBoolean("permission");
					} catch (JSONException e) {
						Log.d(TAG, "acquirePermissionForMovie(): Json parsing failed");
						e.printStackTrace();
					}
					if(permission) {
						Log.d(TAG, "MovieDetailsActivity.acquirePermissionForMovie(): got permission true");
						switch(trans_state1) {
							case UNKNOWN:
								load_fpkeys();
								break;
							case DOWNLOADED:
								if( isFpkesExist() && isTranslationExist(transLang)) { 
									Log.d(TAG,"MainActivity.onItemClick() start AmatchMovieActivity");
									Intent movieActivity = new Intent(getApplicationContext(), AmatchMovieActivity.class);
									movieActivity.putExtra(AmatchMovieActivity.MOVIE_POSITION, movie_position);
									startActivity(movieActivity);
								} else {
									trans_state1 = TransState.UNKNOWN;
									mv_details_trans_get1.setText(R.string.download); 
								}
								break;
							case DOWNLOADING:
								break;
						}
					} else {
						Log.d(TAG, "MovieDetailsActivity.acquirePermissionForMovie(): got permission false");
            			Toast.makeText(getApplicationContext(),
            			"You have no permission to play this movie",
            			Toast.LENGTH_LONG).show();
					}
				}else{
					Log.d(TAG,"MovieDetailsActivity.acquirePermissionForMovie() failed");	
				}
			}
		});
		return false;
	}

    public boolean load_fpkeys() {
		if(selectedMovie == null) {
            Log.d(TAG,"MovieDetailsActivity.load_fpkeys() Fails: selectedMovie is null");
			return false;
		}
        trans_state1 = TransState.DOWNLOADING;
        mv_details_trans_get1.setText(R.string.downloading);
		String fn = gs.getFileNameForUrl(selectedMovie.fpkeys_file, selectedMovie.id);

        Log.d(TAG,"MovieDetailsActivity.load_fpkeys() fn: " + fn);
        
        File target = new File(fn);
        if( !target.isFile() || target.length() < 10) {

            fpkey_download_dialog = new ProgressDialog(this);

                //fpkey_download_dialog.setIndeterminate(true);
            fpkey_download_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            fpkey_download_dialog.setCancelable(true);
            fpkey_download_dialog.setInverseBackgroundForced(false);
            fpkey_download_dialog.setCanceledOnTouchOutside(true);
            fpkey_download_dialog.setTitle(R.string.downloading_index);
        
            gs.aq.progress(fpkey_download_dialog).download(selectedMovie.fpkeys_file, target, new AjaxCallback<File>(){

                public void callback(String url, File file, AjaxStatus status) {

                    if(file != null){
                        Log.d(TAG, " Downloaded File:" + file.length() + ":" + file);
                        trans_state1 = TransState.DOWNLOADED;
                        mv_details_trans_get1.setText(R.string.play);
                        if(fpkey_download_dialog != null) {
                            fpkey_download_dialog.dismiss();   
                        }
                        load_translation_for_lang(transLang);
                    }else{
                        Log.d(TAG, "Failed download: "  + selectedMovie.fpkeys_file);
                        trans_state1 = TransState.UNKNOWN;
                        mv_details_trans_get1.setText(R.string.download);
                        if(fpkey_download_dialog != null) {
                            fpkey_download_dialog.dismiss();   
                        }
                    }
                }

            });
        } else {
            Log.d(TAG, "File " + fn + " exist");
            load_translation_for_lang(transLang);
        }
        return true;
	}

    public boolean load_translation_for_lang(String lang) {
        if(selectedMovie == null) {
            Log.d(TAG,"MovieDetailsActivity.load_translation_for_lang() Fails: selectedMovie is null");
            trans_state1 = TransState.UNKNOWN;
            return false;
        }
        final String url = selectedMovie.getTranslationFileName(lang);
        Log.d(TAG, "MovieDetailsActivity.load_translation_for_lang: url: " + url);
        trans_state1 = TransState.DOWNLOADING;
        mv_details_trans_get1.setText(R.string.downloading);
        String fn = gs.getFileNameForUrl(url, selectedMovie.id);

        Log.d(TAG,"MovieDetailsActivity.load_translation_for_lang() fn: " + fn);
        
        File target = new File(fn);
        if( !target.isFile() || target.length() < 10) {
        
                trans_download_dialog = new ProgressDialog(this);

                //trans_download_dialog.setIndeterminate(true);
                trans_download_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                trans_download_dialog.setCancelable(true);
                trans_download_dialog.setInverseBackgroundForced(false);
                trans_download_dialog.setCanceledOnTouchOutside(true);
                trans_download_dialog.setTitle(R.string.downloading_translation);
            
                gs.aq.progress(trans_download_dialog).download(url, target, new AjaxCallback<File>(){

                public void callback(String url, File file, AjaxStatus status) {

                    if(file != null){
                        Log.d(TAG, " Downloaded File:" + file.length() + ":" + file);
                        if(trans_download_dialog != null) {
                            trans_download_dialog.dismiss(); 
                        }
                        trans_state1 = TransState.DOWNLOADED;
                        mv_details_trans_get1.setText(R.string.play);
                    }else{
                        Log.d(TAG, "Failed download: "  + url);
                        trans_state1 = TransState.UNKNOWN;
                        mv_details_trans_get1.setText(R.string.download);
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
            trans_state1 = TransState.DOWNLOADED;
            mv_details_trans_get1.setText(R.string.play);
            Log.d(TAG, "File " + fn + " exist");
        } 
        return true;
    }

}
