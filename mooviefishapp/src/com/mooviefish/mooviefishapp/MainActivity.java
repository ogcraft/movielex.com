package com.mooviefish.mooviefishapp;

import android.app.Activity;
import android.app.Activity;
import android.os.Bundle;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
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
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

public class MainActivity extends Activity  implements
        OnItemClickListener 
{
	private MFApplication gs; 
	private String TAG = "Moovie";
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
    	//Log.d(TAG,"MainActivity.onCreate");
        setContentView(R.layout.main);
        Log.d(TAG, "MainActivity.onCreate(): root_path: " + gs.getRootPath());
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Downloading movies...");
        dialog.show();  
        getMovieItems(); 
  
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
        Log.d(TAG, "getMovieList(): url: " + serviceUrl);
        AjaxCallback<JSONArray> cb = new AjaxCallback<JSONArray>();        
        cb.url(serviceUrl).type(JSONArray.class).weakHandler(this, "getMovieListCallback");
        cb.header("Content-Type", "application/json");  
        gs.aq.ajax(cb);
    }

    public void getMovieListCallback(String url, JSONArray movies, AjaxStatus status){
        Log.d(TAG, "getMovieListCallback called");
        if(movies != null) {               
            Log.d(TAG, "getMovieListCallback(): Got movies: " + movies.length());
            //gs.createMovieItemsFromJson(movies);
            final JSONArray _movies = movies;
            Thread thread = new Thread() {
                    @Override
                    public void run() {
                       
                        Log.d(TAG, "getMovieListCallback(): start Resource download thread");
                        gs.createMovieItemsFromJson(_movies); 
                        Log.d(TAG, "getMovieListCallback(): Send message to resource_download_handler"); 
                        Message msg = resource_download_handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("DownloadStatus", true);
                        msg.setData(bundle);
                        resource_download_handler.sendMessage(msg);
                    }
            };
            
            thread.start();
            //try {
            //thread.join();
            //} catch (InterruptedException e) {}
                  
        } else {          
            Log.d(TAG, "getMovieListCallback failed");
        }
        
    }
    public void getMovieItems() {
        String getMoviesUrl = String.format(gs.GETMOVIES_REST, gs.BASE_URL, gs.dataLang);
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
            Log.d(TAG,"MainActivity.onItemClick() start AmatchMovieActivity");
            Intent movieActivity = new Intent(getApplicationContext(), MovieDetailsActivity.class);
            
            movieActivity.putExtra(MovieDetailsActivity.MOVIE_POSITION, position);

            startActivity(movieActivity);
        }
    }

    


}
