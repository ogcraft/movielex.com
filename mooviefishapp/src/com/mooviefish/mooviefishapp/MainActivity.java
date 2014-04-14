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
import com.mooviefish.mooviefishapp.CustomListViewAdapter;
import com.mooviefish.mooviefishapp.MFApplication;
import com.mooviefish.mooviefishapp.R;
import com.mooviefish.mooviefishapp.RowItem;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class MainActivity extends Activity  implements
        OnItemClickListener 
{
	private MFApplication gs; 
	private String TAG = "Moovie";
    private final String test_id = "test";

    ListView listView;
    List<MovieItem> movieItems;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	
    	gs = (MFApplication) getApplication();
    	TAG = gs.getTAG();
    	Log.d(TAG,"MainActivity.onCreate");
        setContentView(R.layout.main);
        Log.d(TAG, "onCreate(): root_path: " + gs.getRootPath());
        
        createMovieItems();
        
        listView = (ListView) findViewById(R.id.list);
        CustomListViewAdapter adapter = new CustomListViewAdapter(this,
                R.layout.list_item, movieItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
  
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
    	MovieItem r = movieItems.get(position);
        Log.d(TAG,"MainActivity.onItemClick() position: " + position + " id: " + r.id);
    	if( test_id.equals(r.id) ) {
            Log.d(TAG,"MainActivity.onItemClick() test case");
    		Intent testActivity = new Intent(getApplicationContext(), AmatchTestActivity.class);
        	startActivity(testActivity);
		} else {
            Log.d(TAG,"MainActivity.onItemClick() start AmatchMovieActivity");
            Intent movieActivity = new Intent(getApplicationContext(), AmatchMovieActivity.class);
            
            movieActivity.putExtra(AmatchMovieActivity.MOVIE_POSITION, position);

            startActivity(movieActivity);
        }
    }

    public void createMovieItems() {
    	JSONArray jsonarray = getJSONFromUrl("");

        movieItems = new ArrayList<MovieItem>();
        
        try {
            for(int i=0;i<jsonarray.length();i++)
            {
                JSONObject c=jsonarray.getJSONObject(i);// Used JSON Object from Android
                //Storing each Json in a string variable
                String ID 	=c.getString("id");
                String TITLE=c.getString("title");
                String DESC =c.getString("desc");
                String IMG  = gs.getRootPath() + ID + "/" + c.getString("img");
                Log.d(TAG,"IMG:" + IMG);
                movieItems.add(new MovieItem(ID,TITLE,DESC,IMG));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
 	}

    public JSONArray getJSONFromUrl(String url) {
    	JSONArray jsonarr=null;
    	String json = "";
    	String json_fn = gs.getRootPath() + "movie_list.json";
    	try{
    	// Making HTTP request
    	
    	Log.d(TAG, "json_fn: " + json_fn);
    	json = gs.getStringFromFile(json_fn);
    	Log.d(TAG, "json: " + json);
    	} catch(Exception e) {
    		Log.d(TAG,"Failed read json: " + json_fn);
    		//return null;
            json="[{\"id\": \"test\",\"title\": \"For Developers Only\",\"desc\": \" \",\"img\": \"stopsign.png\"}]";
    	} 
    	// try parse the string to a JSONArray
    	try {
    		jsonarr = new JSONArray(json);
    	} catch (JSONException e) {
    		Log.d(TAG,"Failed parse json: " + json_fn);
    	}

    // return JSON String
    return jsonarr;  //<<<< return JSONArray instead of JSONObject
}
}
