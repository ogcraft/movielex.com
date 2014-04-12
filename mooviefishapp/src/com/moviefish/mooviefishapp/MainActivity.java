package com.moviefish.mooviefishapp;

import android.app.Activity;
import android.app.Activity;
import android.os.Bundle;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.moviefish.mooviefishapp.CustomListViewAdapter;
import com.moviefish.mooviefishapp.MFApplication;
import com.moviefish.mooviefishapp.R;
import com.moviefish.mooviefishapp.RowItem;
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

	public static final String[] titles = new String[] { "Monsters University (2013)",
            "The Smurfs (2011)", "For Developers Only"};
 
    public static final String[] descriptions = new String[] {
            "A look at the relationship between Mike and Sulley during their days at Monsters University -- when they weren't necessarily the best of friends.",
            "When the evil wizard Gargamel chases the tiny blue Smurfs out of their village, they tumble from their magical world into New York City.",
            "..."};

    public static final Integer[] images = { 
    	R.drawable.ic_mfish,
        R.drawable.ic_mfish, 
        R.drawable.ic_mfish };

    ListView listView;
    List<RowItem> rowItems;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	Log.d(TAG,"onCreate");
    	gs = (MFApplication) getApplication();
    	TAG = gs.getTAG();
        setContentView(R.layout.main);
        Log.d(TAG, "onCreate(): root_path: " + gs.getRootPath());
        
        createRowItems();
        
        listView = (ListView) findViewById(R.id.list);
        CustomListViewAdapter adapter = new CustomListViewAdapter(this,
                R.layout.list_item, rowItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
  
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Toast toast = Toast.makeText(getApplicationContext(),
            "Item " + (position + 1) + ": " + rowItems.get(position),
            Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public void createRowItems() {
    	JSONArray jsonarray = getJSONFromUrl("");

        rowItems = new ArrayList<RowItem>();
        
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
                rowItems.add(new RowItem(ID,TITLE,DESC,IMG));
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
    		return null;
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
