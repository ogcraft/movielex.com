package com.mooviefish.mooviefishapp;
import amatch_generated.amatch_interface;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import android.os.Environment;
import android.app.Application;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log; 
import com.mooviefish.mooviefishapp.Amatch;

/**
* This is a global state for MoovieFishApp
*
* // Set values
* MFApplication gs = (MFApplication) getApplication();
* gs.setTestMe("Some String");
* 
* // Get values
* MFApplication gs = (MFApplication) getApplication();
* String s = gs.getTestMe();
* 
* @author Oleg Galbert
*
*/

public class MFApplication extends Application
{
	public static final String appVersion = "0.9"; 
	private static final String TAG = "MoovieFishApp";
	private final String root_path = Environment.getExternalStorageDirectory() + "/MoovieFish/";
	public static Amatch amatch = null;

	List<MovieItem> movieItems;

	public String getTAG() {
		return TAG;
	}
	public String getRootPath() {
		return root_path;
	}

	@Override
  	public void onCreate()
  	{
    	super.onCreate();
     
    	amatch = Amatch.initInstance(this);
        createMovieItems();
  	}

	public static String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		reader.close();
		return sb.toString();
	}

	public static String getStringFromFile (String filePath) throws Exception {
		File fl = new File(filePath);
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
    //Make sure you close all streams.
		fin.close();        
		return ret;
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
                String IMG  = getRootPath() + ID + "/" + c.getString("img");
                Log.d(TAG,"JSON Parser IMG:" + IMG);
                MovieItem mi = new MovieItem(ID,TITLE,DESC,IMG);

                JSONArray trans_jsonarray = c.getJSONArray("translations");
                Log.d(TAG,"trans: " + trans_jsonarray.length());
                for(int j=0;j<trans_jsonarray.length();j++)
            	{
            		JSONObject t=trans_jsonarray.getJSONObject(j);
            		MovieTranslations mt = 
            			new MovieTranslations(
            				t.getString("id"),
            				t.getString("title"), 
            				t.getString("desc"), 
            				t.getString("img"));
            		mi.translations.add(mt);
            	}
                movieItems.add(mi);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
 	}

    public JSONArray getJSONFromUrl(String url) {
    	JSONArray jsonarr=null;
    	String json = "";
    	String json_fn = getRootPath() + "movie_list.json";
    	try{
    	// Making HTTP request
    	
    	Log.d(TAG, "json_fn: " + json_fn);
    	json = getStringFromFile(json_fn);
    	Log.d(TAG, "json: " + json);
    	} catch(Exception e) {
    		Log.d(TAG,"Failed read json: " + json_fn);
    		//return null;
            json="[{\"id\": \"test\",\"title\": \"For Developers Only\",\"desc\": \" \",\"img\": \"stopsign.png\", \"translations\": []}]";
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
