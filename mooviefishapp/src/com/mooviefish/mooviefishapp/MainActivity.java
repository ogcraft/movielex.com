package com.mooviefish.mooviefishapp;

import android.app.Activity;
import android.app.Activity;
import android.os.Bundle;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.ProgressDialog;
import com.mooviefish.mooviefishapp.CustomListViewAdapter;
import com.mooviefish.mooviefishapp.MFApplication;
import com.mooviefish.mooviefishapp.R;
import com.mooviefish.mooviefishapp.MovieItem;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.http.Header;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import android.view.ViewGroup.LayoutParams;
import android.os.Process;
import android.app.ActionBar;
import java.util.Timer;  
import java.util.TimerTask; 

public class MainActivity extends Activity  implements
        OnItemClickListener 
{
	private MFApplication gs; 
	private String TAG = "MoovieFishApp";
    private final String test_id = "test";
    private ProgressDialog dialog;
    ListView listView;
    CustomListViewAdapter adapter;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	
    	gs = (MFApplication) getApplication();
    	TAG = gs.getTAG();
        gs.width = MFApplication.getWidth(getApplicationContext());
        gs.height = MFApplication.getHeight(getApplicationContext());
        Log.d(TAG, "MainActivity.onCreate h: " + gs.height + " w: " + gs.width);
        //Log.d(TAG,"MainActivity.onCreate");
        setContentView(R.layout.main);
        Log.d(TAG, "MainActivity.onCreate(): root_path: " + gs.getRootPath());
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.header_background));
        bar.setTitle(R.string.main_view_title);
        
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Downloading movies...");
        dialog.show();
        Toast.makeText(getApplicationContext(),
         String.format("%dx%d Path: %s", gs.height, gs.width, gs.getRootPath()), Toast.LENGTH_SHORT).show();

        getMovieItems(); 
        
    }

    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "MainActivity.onDestroy()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Log.d(TAG, "going to SetPrefsActivity");
                Intent prefActivity = new Intent(getApplicationContext(), SetPrefsActivity.class);
                startActivity(prefActivity);
                return true;
            case R.id.menu_clear_cache:
                gs.cleanDownloadedData();
                return true;
            case R.id.menu_about:
                AboutDialog about = new AboutDialog(this);
				about.gs = gs; 
                about.setTitle("About this app");
                about.show();
                //about.getWindow().setLayout(300, 300);
                return true;
            case R.id.menu_exit:
                //super.onBackPressed();
                this.finish();
                Process.killProcess( Process.myPid() ); 
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showMovies() {
        listView = (ListView) findViewById(R.id.list);
        CustomListViewAdapter adapter = new CustomListViewAdapter(this,
                                                    R.layout.list_item, gs.movieItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this); 
    }

    Handler resource_download_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {              
                Bundle bundle = msg.getData();
                boolean status = bundle.getBoolean("DownloadStatus");
                Log.d(TAG, "MainActivity: all Resources collected: status: " + status);
                dialog.dismiss();  
                showMovies(); 
        }
    }; 

    public void getMovieList(String serviceUrl) {
        File target = new File(gs.getRootPath()+"movies.json");
        Log.d(TAG, "getMovieList(): url: " + serviceUrl + " -> " + target.getPath());
        AjaxCallback<File> cb = new AjaxCallback<File>();        
        cb.url(serviceUrl).type(File.class).weakHandler(this, "getMovieListCallback").targetFile(target);
        cb.header("Content-Type", "application/json");  
        int DOWNLOAD_TO_MS = 30*1000; //in ms  
		cb.setTimeout(DOWNLOAD_TO_MS);
        gs.aq.ajax(cb);
    }

    public void getMovieListCallback(String url, File file, AjaxStatus status){
        Log.d(TAG, "getMovieListCallback called");
		File json_file = null;
        if(file != null && file.length() > 10) {               
			json_file = file;
        } else {
        	json_file = new File(gs.getRootPath(),"movies.json");
            if(!(json_file != null && json_file.length() > 10)) {               
                Log.d(TAG,"getMovieListCallback() failed exit");
                return;
            }
			//Log.d(TAG,"getMovieListCallback() failed fetch JSON. Using stored " + json_file.getPath());
		}
		Log.d(TAG, "getMovieListCallback() Parsing file "+json_file.getPath()+" size: "+json_file.length());
        
        final JSONArray _movies = gs.getJSONFromFile(json_file);

        if(_movies != null) {
			Log.d(TAG, "getMovieListCallback(): Got movies: " + _movies.length());
			Thread thread = new Thread() {
				@Override
				public void run() {

					Log.d(TAG, "getMovieListCallback(): start Resource download thread");
					gs.movieItems = gs.createMovieItemsFromJson(_movies); 
					Log.d(TAG, "getMovieListCallback(): Send message to resource_download_handler"); 
					Message msg = resource_download_handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putBoolean("DownloadStatus", true);
					msg.setData(bundle);
					resource_download_handler.sendMessage(msg);
				}
			};
			thread.start();
		} else {
            Log.d(TAG,"getMovieListCallback() _movies == null");
        }
    }

    public void getMovieItems() {
        String getMoviesUrl = String.format(gs.GETMOVIES_REST, gs.BASE_URL, gs.getDataLang());
        Log.d(TAG, "getMovieItems(): getMoviesUrl: " + getMoviesUrl);
        getMovieList(getMoviesUrl);
        Log.d(TAG, "getMovieItems(): Exit");
    }   

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
    	MovieItem r = gs.movieItems.get(position);
        Log.d(TAG,"MainActivity.onItemClick() position: " + position + " id: " + r.id);
    	if( test_id.equals(r.id) ) {
            Log.d(TAG,"MainActivity.onItemClick() test case");
    		Intent testActivity = new Intent(getApplicationContext(), AmatchTestActivity.class);
        	startActivity(testActivity);
		} else {
            Log.d(TAG,"MainActivity.onItemClick() start MovieDetailsActivity");
            Intent movieActivity = new Intent(getApplicationContext(), MovieDetailsActivity.class);
            
            movieActivity.putExtra(MovieDetailsActivity.MOVIE_POSITION, position);

            startActivity(movieActivity);
        }
    }

    


}
