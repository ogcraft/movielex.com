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
import org.apache.http.Header;
//import org.json.*;
//import com.loopj.android.http.*;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

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
	public static final String appVersion = "1.2.2"; 
	private static final String TAG = "MoovieFishApp";
	private String root_path = Environment.getExternalStorageDirectory() + "/MoovieFish/";
	public static Amatch amatch = null;
    private static final Pattern DIR_SEPARATOR = Pattern.compile("/");
    static int CONNECTION_TIMEOUT = 0; //msec
    static int DATARETRIEVAL_TIMEOUT = 0; //msec
    static String moovifishSite = "http://mooviefish.com";
    //static String moovifishSite = "http://192.168.10.109:3000";
    static String dataLang = "ru";
    static final String BASE_URL = "http://mooviefish.com";
    static final String RESOURCE_PREFIX = BASE_URL + "/files/";

    static final String GETMOVIES_REST = "%s/api/movies/%s";
    static final String GETMOVIEDETAIL_REST = "%s/api/movie/%s/%d";

    public AQuery aq = new AQuery(this);

    public List<MovieItem> movieItems;

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
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MFApplication.onCreate(): VERSION.SDK_INT: " + Build.VERSION.SDK_INT);

        if( Build.VERSION.SDK_INT >= 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy); 
        }

        amatch = Amatch.initInstance(MFApplication.this);

        root_path = findRootPath();
        //getMovieItems();
    }


    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();        
        return ret;
    }

    public void createMovieItemsFromJson(JSONArray jsonarray) {
        Log.d(TAG, "createMovieItemsFromJson downloaded movies: " + jsonarray.length());

        movieItems = new ArrayList<MovieItem>();

        try {
            for(int i=0;i<jsonarray.length();i++)
            {
                JSONObject c=jsonarray.getJSONObject(i);// Used JSON Object from Android
                //Log.d(TAG, "JSON: " + c.toString());
                MovieItem mi = new MovieItem(
                    c.getString("id"), 
                    c.getString("shortname"), 
                    c.getString("title"), 
                    c.getString("desc"), 
                    c.getString("img"), 
                    c.getString("fpkeys-file"));
                String folder_name = root_path + mi.id;
                MFApplication.createFolderForMovie(folder_name);
                
                mi.img = resolveResourceForMovie(mi.img, folder_name);

                JSONArray trans_jsonarray = c.getJSONArray("translations");
                Log.d(TAG,"trans: " + trans_jsonarray.length());
                for(int j=0;j<trans_jsonarray.length();j++)
                {

                  JSONObject t=trans_jsonarray.getJSONObject(j);
                  //Log.d(TAG, "Trans: " + t.toString());
                  MovieTranslations mt = 
                  new MovieTranslations(
                    t.getString("file"),
                    t.getString("title"), 
                    t.getString("desc"), 
                    t.getString("img"),
                    t.getString("lang"));

                  mi.translations.add(mt);
                }
              
                Log.d(TAG,"createMovieItemsFromJson(): adding " + mi.title);
                movieItems.add(mi);
            }
            Log.d(TAG, "createMovieItemsFromJson() collected movies: " + movieItems.size());
        } catch (JSONException e) {
            Log.d(TAG, "createMovieItemsFromJson(): Json parsing failed");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getFileNameForUrl(String url, String id) {
        String fn = root_path + id + "/" + url.substring(url.lastIndexOf("/") + 1);
        return fn;
    }

    public String resolveResourceForMovie(String url, String folder_name) {
        Log.d(TAG, "resolveResourceForMovie(): url: " + url + " folder: " + folder_name);
        
        String fn = folder_name + "/" + url.substring(url.lastIndexOf("/") + 1);
        Log.d(TAG, "resolveResourceForMovie(): fn: " + fn);
        
        File target = new File(fn);
        if(!target.isFile()) {
            AjaxCallback<File> cb = new AjaxCallback<File>();
            
            cb.url(url).type(File.class).targetFile(target);
            
            aq.sync(cb);
        //File f = cb.getResult();

            AjaxStatus status = cb.getStatus();
            Log.d(TAG, "resolveResourceForMovie() After callback status: " + status.getCode());
        }
        return fn;  
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

public static boolean createFolderForMovie(String folder_name) {
    
    File folder = new File(folder_name);
    Log.d(TAG, "Creating folder: " + folder_name);
    boolean success = true;
    if (!folder.exists()) {
        success = folder.mkdirs();
    }
    return success;
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

}
