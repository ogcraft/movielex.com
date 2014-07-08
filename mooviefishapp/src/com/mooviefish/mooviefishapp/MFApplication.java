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
import java.util.*;
import java.util.regex.*;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import android.text.TextUtils;
import android.os.Build;
import com.mooviefish.mooviefishapp.Amatch;
import java.net.URL;
import android.os.StrictMode;
import android.os.Build;

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
	public static final String appVersion = "1.3s"; 
	private static final String TAG = "MoovieFishApp";
	private String root_path = Environment.getExternalStorageDirectory() + "/MoovieFish/";
	public static Amatch amatch = null;
    private static final Pattern DIR_SEPARATOR = Pattern.compile("/");
    static int CONNECTION_TIMEOUT = 0; //msec
    static int DATARETRIEVAL_TIMEOUT = 0; //msec
    static String moovifishSite = "http://mooviefish.com";
    //static String moovifishSite = "http://192.168.10.109:3000";
    static String dataLang = "en";
    static String GETMOVIES_REST = "%s/api/movies/%s";
    static String GETMOVIEDETAIL_REST = "%s/api/movie/%s/%d";

	List<MovieItem> movieItems;

	public String getTAG() {
		return TAG;
	}
	public String getRootPath() {
		return root_path;
	}

    public String findRootPath() {
        final String mf_dir = "/MoovieFish/";
        //HashSet<String> storages = getStorageSet();
        String[] storages = getStorageDirectories();

        //Log.d(TAG,"findRootPath(): Found " + storages.size() + " path");
        
        for (String s : storages) {
            
            String p = s + mf_dir;
            
            Log.d(TAG,"findRootPath(): checking: " + p);

            File f = new File(p);

            if(f.exists() && f.isDirectory()){
                Log.d(TAG, "findRootPath(): exists : "  + p);
                return p;
            }else{
                Log.d(TAG,"findRootPath(): NOT EXIST: " + p);
            }
        }
        return mf_dir;
    }

    @Override
    public void onCreate()
    {
    	super.onCreate();
        Log.d(TAG, "MFApplication.onCreate(): VERSION.SDK_INT: " + Build.VERSION.SDK_INT);

        if( Build.VERSION.SDK_INT >= 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy); 
        }
        root_path = findRootPath();
        amatch = Amatch.initInstance(this, MFApplication.this);
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
    String getMoviesUrl = String.format(GETMOVIES_REST, moovifishSite, dataLang);
    Log.d(TAG, "createMovieItems(): getMoviesUrl: " + getMoviesUrl);
    //JSONArray jsonarray = getMovieList(getMoviesUrl);
    JSONArray jsonarray =getJSONFromUrl("");
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
    try {
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

public JSONArray getMovieList(String serviceUrl) {
    InputStream in = requestWebService(serviceUrl);
    String response = getResponseText(in);
    Log.d(TAG, "getMovieList(): response: " + response);
    try {
        return new JSONArray(response);
    } catch (JSONException e){
        Log.d(TAG,"getMovieList(): Failed parse json: " + response);        
    }    
    return null;
}

public static InputStream requestWebService(String serviceUrl) {
    
    Log.d(TAG,"requestWebService() serviceUrl: " + serviceUrl);

    disableConnectionReuseIfNecessary();
 
    HttpURLConnection urlConnection = null;
    try {
        // create connection
        URL urlToRequest = new URL(serviceUrl);
        urlConnection = (HttpURLConnection) 
            urlToRequest.openConnection();
        urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
        urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);
         
        // handle issues
        int statusCode = urlConnection.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            // handle unauthorized (if service requires user login)
            Log.d(TAG,"requestWebService() serviceUrl: " + serviceUrl + " Error HTTP_UNAUTHORIZED");
        } else if (statusCode != HttpURLConnection.HTTP_OK) {
            // handle any other errors, like 404, 500,..
            Log.d(TAG,"requestWebService() serviceUrl: " + serviceUrl + " Error " + statusCode);
        }
         Log.d(TAG,"requestWebService() serviceUrl: " + serviceUrl + " Reading inputStream"); 
        // create JSON object from content
        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        //return new JSONObject(getResponseText(in));
        return in;
         
    } catch (MalformedURLException e) {
        // URL is invalid
        Log.d(TAG,"requestWebService() serviceUrl: " + serviceUrl + " Error URL is invalid");
    } catch (SocketTimeoutException e) {
        // data retrieval or connection timed out
        Log.d(TAG,"requestWebService() serviceUrl: " + serviceUrl + " Error connection timed out");
    } catch (IOException e) {
        // could not read response body 
        // (could not create input stream)
        Log.d(TAG,"requestWebService() serviceUrl: " + serviceUrl + " Error could not read response body");
    } finally {
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }           
    return null;
}
 
/**
 * required in order to prevent issues in earlier Android version.
 */
private static void disableConnectionReuseIfNecessary() {
    // see HttpURLConnection API doc
    if (Integer.parseInt(Build.VERSION.SDK) 
            < Build.VERSION_CODES.FROYO) {
        System.setProperty("http.keepAlive", "false");
    }
}
 
private static String getResponseText(InputStream inStream) {
    // very nice trick from 
    // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
    return new Scanner(inStream).useDelimiter("\\A").next();
}


public static HashSet<String> getStorageSet(){
    HashSet<String> storageSet = getStorageSet(new File("/system/etc/vold.fstab"), true);
    storageSet.addAll(getStorageSet(new File("/proc/mounts"), false));

    if (storageSet == null || storageSet.isEmpty()) {
        storageSet = new HashSet<String>();
        storageSet.add(Environment.getExternalStorageDirectory().getAbsolutePath());
    }
    return storageSet;
}

public static HashSet<String> getStorageSet(File file, boolean is_fstab_file) {
    HashSet<String> storageSet = new HashSet<String>();
    BufferedReader reader = null;
    try {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;
        while ((line = reader.readLine()) != null) {
            HashSet<String> _storage = null;
            if (is_fstab_file) {
                _storage = parseVoldFile(line);
            } else {
                _storage = parseMountsFile(line);
            }
            if (_storage == null)
                continue;
            storageSet.addAll(_storage);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }finally{
        try {
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }   
        reader = null;
    }
            /*
             * set default external storage
             */
            storageSet.add(Environment.getExternalStorageDirectory().getAbsolutePath());
            return storageSet;
        }

        private static HashSet<String> parseMountsFile(String str) {
            if (str == null)
                return null;
            if (str.length()==0)
                return null;
            if (str.startsWith("#"))
                return null;
            HashSet<String> storageSet = new HashSet<String>();
            /*
             * /dev/block/vold/179:19 /mnt/sdcard2 vfat rw,dirsync,nosuid,nodev,noexec,relatime,uid=1000,gid=1015,fmask=0002,dmask=0002,allow_utime=0020,codepage=cp437,iocharset=iso8859-1,shortname=mixed,utf8,errors=remount-ro 0 0
             * /dev/block/vold/179:33 /mnt/sdcard vfat rw,dirsync,nosuid,nodev,noexec,relatime,uid=1000,gid=1015,fmask=0002,dmask=0002,allow_utime=0020,codepage=cp437,iocharset=iso8859-1,shortname=mixed,utf8,errors=remount-ro 0 0
             */
            Pattern patter = Pattern.compile("/dev/block/vold.*?(/mnt/.+?) vfat .*");
            Matcher matcher = patter.matcher(str);
            boolean b = matcher.find();
            if (b) {
                String _group = matcher.group(1);
                storageSet.add(_group);
            }

            return storageSet;
        }

        private static HashSet<String> parseVoldFile(String str) {
            if (str == null)
                return null;
            if (str.length()==0)
                return null;
            if (str.startsWith("#"))
                return null;
            HashSet<String> storageSet = new HashSet<String>();
            /*
             * dev_mount sdcard /mnt/sdcard auto /devices/platform/msm_sdcc.1/mmc_host
             * dev_mount SdCard /mnt/sdcard/extStorages /mnt/sdcard/extStorages/SdCard auto sd /devices/platform/s3c-sdhci.2/mmc_host/mmc1
             */
            Pattern patter1 = Pattern.compile("(/mnt/[^ ]+?)((?=[ ]+auto[ ]+)|(?=[ ]+(\\d*[ ]+)))");
            /*
             * dev_mount ins /mnt/emmc emmc /devices/platform/msm_sdcc.3/mmc_host
             */
            Pattern patter2 = Pattern.compile("(/mnt/.+?)[ ]+");
            Matcher matcher1 = patter1.matcher(str);
            boolean b1 = matcher1.find();
            if (b1) {
                String _group = matcher1.group(1);
                storageSet.add(_group);
            }

            Matcher matcher2 = patter2.matcher(str);
            boolean b2 = matcher2.find();
            if (!b1 && b2) {
                String _group = matcher2.group(1);
                storageSet.add(_group);
            }
            return storageSet;
        }
/**
 * Raturns all available SD-Cards in the system (include emulated)
 *
 * Warning: Hack! Based on Android source code of version 4.3 (API 18)
 * Because there is no standart way to get it.
 * TODO: Test on future Android versions 4.4+
 *
 * @return paths to all available SD-Cards in the system (include emulated)
 */

public static String[] getStorageDirectories()
{
    // Final set of paths
    final Set<String> rv = new HashSet<String>();
    // Primary physical SD-CARD (not emulated)
    final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
    // All Secondary SD-CARDs (all exclude primary) separated by ":"
    final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
    // Primary emulated SD-CARD
    final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
    if(TextUtils.isEmpty(rawEmulatedStorageTarget))
    {
        // Device has physical external storage; use plain paths.
        if(TextUtils.isEmpty(rawExternalStorage))
        {
            // EXTERNAL_STORAGE undefined; falling back to default.
            rv.add("/storage/sdcard0");
        }
        else
        {
            rv.add(rawExternalStorage);
        }
    }
    else
    {
        // Device has emulated storage; external storage paths should have
        // userId burned into them.
        final String rawUserId;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            rawUserId = "";
        }
        else
        {
            final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            final String[] folders = DIR_SEPARATOR.split(path);
            final String lastFolder = folders[folders.length - 1];
            boolean isDigit = false;
            try
            {
                Integer.valueOf(lastFolder);
                isDigit = true;
            }
            catch(NumberFormatException ignored)
            {
            }
            rawUserId = isDigit ? lastFolder : "";
        }
        // /storage/emulated/0[1,2,...]
        if(TextUtils.isEmpty(rawUserId))
        {
            rv.add(rawEmulatedStorageTarget);
        }
        else
        {
            rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
        }
    }
    // Add all secondary storages
    if(!TextUtils.isEmpty(rawSecondaryStoragesStr))
    {
        // All Secondary SD-CARDs splited into array
        final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
        Collections.addAll(rv, rawSecondaryStorages);
    }
    return rv.toArray(new String[rv.size()]);
}

    }
