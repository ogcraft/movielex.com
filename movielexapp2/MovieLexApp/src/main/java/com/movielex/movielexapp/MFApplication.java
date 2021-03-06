package com.movielex.movielexapp;

import amatch_generated.amatch_interface;
import com.movielex.util.*;

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
import java.io.FilenameFilter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log; 
import android.widget.Toast;
import java.util.*;
import java.util.regex.*;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import android.text.TextUtils;
import android.os.Build;
import com.movielex.movielexapp.Amatch;
import java.net.URL;
import android.os.StrictMode;
import android.os.Build;
import android.os.Process;
import org.apache.http.Header;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.view.WindowManager;
import android.view.Display;
import android.graphics.Point;
import android.accounts.AccountManager;
import android.accounts.Account;
import android.telephony.TelephonyManager;
import android.provider.Settings;
import android.app.AlertDialog.Builder;
import android.app.AlertDialog;

/**
* This is a global state for MovieLexApp
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
    public static final String appName = "MovieLexApp";
	public static final String appVersion = "1.1.1";
	public static final String amatchVersion = amatch_interface.AMATCH_VER;
	private static final String TAG = "MovieLexApp";
	private String root_path = Environment.getExternalStorageDirectory() + "/MovieLex/";
	public static Amatch amatch = null;
	public static boolean isAppValid = true;
    private static final Pattern DIR_SEPARATOR = Pattern.compile("/");
    static int CONNECTION_TIMEOUT = 0; //msec
    static int DATARETRIEVAL_TIMEOUT = 0; //msec
    static final int MAX_CHAR_ALLOWED_MOVIE_DETAILS_DESC_VIEW = 600;
    static final int MAX_CHAR_ALLOWED_MOVIE_LIST_DESC_VIEW = 150;
    static String moovifishSite = "http://movielex.com";
    //static String moovifishSite = "http://192.168.10.109:3000";
    public int height = 0;
    public int width = 0;
    public String google_account = "";
    public String device_id = "";
    public String androidOS = "";
    public boolean expireMovieDataCache = false;
    static final String BASE_URL = "http://movielex.com";
    static final String RESOURCE_PREFIX = BASE_URL + "/files/";

    static final String GETMOVIES_REST = "%s/api/movies/%s";
    static final String GETMOVIEDETAIL_REST = "%s/api/movie/%s/%s";
    static final String PUTUSERID_REST = "%s/api/user/%s";
    static final String ACQUIRE_MOVIE_REST = "%s/api/acquire/%s/%s";
	//http://www.movielex.com/api/acquire/11/1001
    static final String GETACQUIRE_PERMISSION_REST = "%s/api/acquire/%s/%s";
	public String inappBase64EncodedPublicKey = "";
    public AQuery aq = new AQuery(this);
    public SharedPreferences sharedPrefs;

    public List<MovieItem> movieItems;

	// The InApp helper object
	public IabHelper iabHelper;
    IabHelper.QueryInventoryFinishedListener iabGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "IAB Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (iabHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                //complain("IAB Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "IAB Query inventory was successful.");

            // Update Ui if needed;
            //setWaitScreen(false); // Enables or disables the "please wait" screen.
            Log.d(TAG, "IAB Initial inventory query finished; enabling main UI.");
        }
    };

	// Enables or disables the "please wait" screen.
    //void setWaitScreen(boolean set) {
    //    findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
    //    findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
    //}

	void alert(Context ctx, String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(ctx);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MFApplication.onCreate(): VERSION.SDK_INT: " + Build.VERSION.SDK_INT
                + " appVer: " + appVersion + " amatchVer: " + amatchVersion);

        if( Build.VERSION.SDK_INT >= 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy); 
        }
		
		inappBase64EncodedPublicKey = getInappBase64EncodedPublicKey();

        amatch = Amatch.initInstance(MFApplication.this);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        root_path = findRootPath();
        google_account = getGoogleAccount();
        androidOS = Build.VERSION.RELEASE;
        device_id = getDeviceId(getApplicationContext());
        Log.d(TAG,"MFApplication.onCreate(): device_id: " + device_id + " androidOS: " + androidOS);

    }
	
    public String getTAG() {
      return TAG;
    }
    public boolean isLargScreen() {
        return (height > 800);
    }
    public String getRootPath() {
        return root_path;
    }
	/////////////////////////  InApp billing //////////////////////    
	public String getInappBase64EncodedPublicKey() {
		String s = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6GMDkWtD0BdVkZoICr8jEah5pKyXKEUI3X0U0yf5akCgousendLw8QnS/Bkr+cQwadH63bLp9uhjG7FuE4OrHdvzhvbWFokVloyrZ8ijkvBp9s0u8heCCedQH4cSx3jpVgYUtPlAJlJ/P5rHkMivjA8cZwTopD5V3NxaTUVz3qO+Yei9XWxcri2FPchEhaNpRtCqVstVGPjn7JsAXpYnt2KB4Fhx+jg2Rr1rH0mKmzIbvZ3uju3wGt1BPv2qAxAHVKTbABWerJRFf6icfFJMQz/Tqd2Vt2n/GsSUKTBZe+JbVLEI1v/wHHRpETn91VsU0p0+Xtri9VRwVkW+Jm/9swIDAQAB";
		return s;
	}
	
	public void createIabHelper(Context ctx) {
		
		// Create the InApp helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");

        iabHelper = new IabHelper(ctx, getInappBase64EncodedPublicKey());

        // enable debug logging (for a production application, you should set this to false).
        //iabHelper.enableDebugLogging(true);

		// Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting IAB setup.");
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "IAB Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    //complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (iabHelper == null) return;

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "IAB Setup successful. Querying inventory.");
                iabHelper.queryInventoryAsync(iabGotInventoryListener);
            }
        });
	}

	public void unbindIabHelper() {
		if (iabHelper != null) iabHelper.dispose();
		iabHelper = null;
	}

	///////////////////////////////////////////////////////////////
    public String getDataLang() {
        return sharedPrefs.getString("data_lang", "ru");
    }
    
    public String getTransLang() {
        return sharedPrefs.getString("trans_lang", "ru");
    }
    
	public int getDelayMs() {
        return sharedPrefs.getInt("delay", 0);
    }

    public String findRootPath() {
        Log.d(TAG, "android.os.Build.DEVICE: " + android.os.Build.DEVICE);
        final String mf_dir = "/MovieLex/";
        String s = Environment.getExternalStorageDirectory().getPath();
        if(android.os.Build.DEVICE.contains("Samsung") || android.os.Build.MANUFACTURER.contains("Samsung")){
            s = s + "/external_sd/";
        }
        
        String p = s + mf_dir;

        Log.d(TAG,"findRootPath(): checking: " + p);

        File f = new File(p);
        if(f.exists() && f.isDirectory()){
                Log.d(TAG, "findRootPath(): exists : "  + p);
                return p;
            }else{
                String msg = "Location \"" + p + "\" does not exist. Creating";
                Log.d(TAG,"findRootPath(): NOT EXIST: " + msg);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                if(!f.mkdir()) {
                    Log.d(TAG,"Directory is not created");
                    Toast.makeText(getApplicationContext(), "Directory " + p + " is not created", Toast.LENGTH_SHORT).show();
                }
				//try {
				//	Thread.sleep(1000);
				//} catch (InterruptedException e) {}
            }
        return p;
    }

    public String findRootPath1() {
        final String mf_dir = "/MovieLex/";
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
                String msg = "Location \"" + p + "\" does not exist. Creating";
                Log.d(TAG,"findRootPath(): NOT EXIST: " + msg);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                if(!f.mkdir()) {
                    Log.d(TAG,"Directory is not created");
                    Toast.makeText(getApplicationContext(), "Directory " + p + " is not created", Toast.LENGTH_LONG).show();
                }
            }
        }
        return mf_dir;
    }

    public void cleanDownloadedData() {
        String p = getRootPath();
        Log.d(TAG,"MFApplication.cleanDownloadedData() cleaning " + p);
        File dn = new File(p);
        String [] children = dn.list();
        for ( int i = 0 ; i < children.length ; i ++ )
        {
            File child = new File( dn, children[i] );
            cleanDirectory(child);
        }
        
    }

    public void refreshMovieData() {
        String p = getRootPath() + "movies.json";
        Log.d(TAG, "MFApplication.refreshMovieData(): removing " + p);
        File json_file = new File(p);
        if(json_file != null) {
          json_file.delete();
        }
        expireMovieDataCache = true;

    }

    
    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();        
        return ret;
    }

	public static String[] collectDirs( String dn) {
		File file = new File(dn);
		String[] directories = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		return directories;
	}

	public void cleanOldMovieData(ArrayList<String> movie_ids) {
		ArrayList<String> directories = 
			new ArrayList(
			Arrays.asList(collectDirs(getRootPath()))); 
		directories.removeAll(movie_ids);
		for(String dn : directories){
			File d = new File(getRootPath(),dn);	
			Log.d(TAG,"removing dir: " + d.getPath());
			deleteDirectory(d);
		}
	}

    public List<MovieItem> createMovieItemsFromJson(JSONArray jsonarray) {
        Log.d(TAG, "createMovieItemsFromJson downloaded movies: " + jsonarray.length());

        ArrayList<MovieItem> movies = new ArrayList<MovieItem>();
		ArrayList<String> movie_ids = new ArrayList<String>();
        try {
            for(int i=0;i<jsonarray.length();i++)
            {
                JSONObject c=jsonarray.getJSONObject(i);// Used JSON Object from Android
                //Log.d(TAG, "JSON: " + c.toString());
                String year_released = c.getString("year-released");
                String title_year = String.format("%s (%s)",
                    c.getString("title"), year_released);
                MovieItem mi = new MovieItem();
                mi.id = c.getString("id");
                mi.shortname = c.getString("shortname"); 
                mi.title = title_year;
                mi.year_released = year_released; 
                mi.desc = c.getString("desc");
                mi.desc_short = c.getString("desc-short"); 
                mi.img = c.getString("img"); 
                mi.fpkeys_file = c.getString("fpkeys-file");
                mi.src_url = c.getString("src-url");
                mi.duration = c.getString("duration");
                
				movie_ids.add(mi.id);
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
                movies.add(mi);
            }
            Log.d(TAG, "createMovieItemsFromJson() collected movies: " + movies.size());
			cleanOldMovieData(movie_ids);
        } catch (JSONException e) {
            Log.d(TAG, "createMovieItemsFromJson(): Json parsing failed");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        expireMovieDataCache = false;
        return movies;
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
            
            if(expireMovieDataCache) {
                cb.expire(-1);
            }

            aq.sync(cb);
        //File f = cb.getResult();

            AjaxStatus status = cb.getStatus();
            Log.d(TAG, "resolveResourceForMovie() After callback status: " + status.getCode());
        }
        return fn;  
    }

	public JSONArray getJSONFromFile( File fl) {
		JSONArray jsonarr=null;
		String json = "";
		try {
			Log.d(TAG, "getJSONFromFile(): " + fl.getPath());
			//json = getStringFromFile(json_fn);
        	FileInputStream fin = new FileInputStream(fl);
        	json = convertStreamToString(fin);
			//json = testMovieJson;
			//Make sure you close all streams.
			fin.close();        
			//Log.d(TAG, "json: " + json);
		} catch(Exception e) {
			Log.d(TAG,"getJSONFromFile() Failed read json: " + fl.getPath());
			return null;
		} 
		// try parse the string to a JSONArray
		try {
			jsonarr = new JSONArray(json);
		} catch (JSONException e) {
			Log.d(TAG,"Failed parse json: " + fl.getPath());
		}
		return jsonarr;
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
/*
public boolean isNetworkConnection() {
    ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
    return netInfo != null;
}
*/

public static void deleteFiles(String path, String regex) {
    File file = new File(path);
    if (file.exists()) {
        String deleteCmd = "rm -fr " + path + regex;
        Runtime runtime = Runtime.getRuntime();
        try {
            Log.d(TAG,"deleteFiles() exec cmd: " + deleteCmd);
            runtime.exec(deleteCmd);
        } catch (IOException e) { 
            Log.d(TAG,"deleteFiles() Failed with " + e.getMessage());
        }
    } else {
        Log.d(TAG,"deleteFiles() path: " + path + " not exists");
    }
}

public static void deleteDirectory( File dir )
{

    if ( dir.isDirectory() )
    {
        String [] children = dir.list();
        for ( int i = 0 ; i < children.length ; i ++ )
        {
         File child =    new File( dir , children[i] );
         if(child.isDirectory()){
             deleteDirectory( child );
             child.delete();
         }else{
             child.delete();

         }
        }
        dir.delete();
    }
}

public static void cleanDirectory( File dir )
{
	if ( dir.isDirectory() )
	{
		String [] children = dir.list();
		for ( int i = 0 ; i < children.length ; i ++ )
		{
			File child =    new File( dir , children[i] );
			if (child.isFile() && 
					(child.getName().endsWith(".png") || 
					 child.getName().endsWith(".jpg") )) {
				continue;
			}
			if(child.isDirectory()){
				deleteDirectory( child );
				Log.d(TAG,"cleanDirectory()1 deleting: " + child.getName());
				child.delete();
			}else{
				Log.d(TAG,"cleanDirectory()2 deleting: " + child.getName());
				child.delete();
			}
		}
	}
}
public static int getWidth(Context mContext){
    int width=0;
    WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    if(Build.VERSION.SDK_INT>12){
        Point size = new Point();
        display.getSize(size);
        width = size.x;
    }
    else{
        width = display.getWidth();  // Deprecated
    }
    return width;
}

public static int getHeight(Context mContext){
    int height=0;
    WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    if(Build.VERSION.SDK_INT>12){
        Point size = new Point();
        display.getSize(size);
        height = size.y;
    }
    else{
        height = display.getHeight();  // Deprecated
    }
    return height;
}

public String getGoogleAccount() {

    AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
    Account[] list = manager.getAccounts();
    for(Account account: list)
    {
        Log.d(TAG,"MFApplication.collectAccountInfo(): account: " + account.toString());
        if(account.type.equalsIgnoreCase("com.google"))
        {
            return account.name;
        }
    }
    return null;
}

// How to get unique device id from:
// http://stackoverflow.com/questions/17046436/android-settings-secure-android-id
//
public static String getDeviceId(Context context) {
    String id = getUniqueID(context);
    if (id == null)
        id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    return id;
}

private static String getUniqueID(Context context) {

    String telephonyDeviceId = "NoTelephonyId";
    String androidDeviceId = "NoAndroidId";

    // get telephony id
    try {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyDeviceId = tm.getDeviceId();
        if (telephonyDeviceId == null) {
            telephonyDeviceId = "NoTelephonyId";
        }
    } catch (Exception e) {
    }

    // get internal android device id
    try {
        androidDeviceId = android.provider.Settings.Secure.getString(context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        if (androidDeviceId == null) {
            androidDeviceId = "NoAndroidId";
        }
    } catch (Exception e) {

    }

    // build up the uuid
    try {
        String id = getStringIntegerHexBlocks(androidDeviceId.hashCode())
                + "-"
                + getStringIntegerHexBlocks(telephonyDeviceId.hashCode());

        return id;
    } catch (Exception e) {
        return "0000-0000-1111-1111";
    }
}

public static String getStringIntegerHexBlocks(int value) {
    String result = "";
    String string = Integer.toHexString(value);

    int remain = 8 - string.length();
    char[] chars = new char[remain];
    Arrays.fill(chars, '0');
    string = new String(chars) + string;

    int count = 0;
    for (int i = string.length() - 1; i >= 0; i--) {
        count++;
        result = string.substring(i, i + 1) + result;
        if (count == 4) {
            result = "-" + result;
            count = 0;
        }
    }

    if (result.startsWith("-")) {
        result = result.substring(1, result.length());
    }

    return result;
}

static String testMovieJson = "[{\"movie-state\":\"active\",\"desc-short\":\"MovieLex демонстрационный фрагмент\",\"fpkeys-file\":\"http://movielex.com/files/1/somelikemovielex-en.fpkeys\",\"desc\":\"MovieLex демонстрационный фрагмент\",\"duration\":\"3 мин 35 сек\",\"year-released\":\"1959\",\"title\":\"В джазе только девушки\",\"id\":\"1\",\"shortname\":\"movielexdemo\",\"src-url\":\"\",\"img\":\"http://movielex.com/files/1/somelikemovielex.jpg\",\"translations\":[{\"desc\":\"From original dvd\",\"file\":\"http://movielex.com/files/1/somelikemovielex-ru.trans\",\"title\":\"Русский\",\"lang\":\"ru\",\"img\":\"http://movielex.com/files/flags/flag-ru.png\"}, {\"desc\":\"From original dvd\",\"file\":\"http://movielex.com/files/1/somelikemovielex-en.trans\",\"title\":\"English\",\"lang\":\"en\",\"img\":\"http://movielex.com/files/flags/flag-en.png\"}, {\"desc\":\"From original dvd\",\"file\":\"http://movielex.com/1/flags/somelikemovielex-de.trans\",\"title\":\"Deutsch\",\"lang\":\"de\",\"img\":\"http://movielex.com/files/flags/flag-de.png\"}]}]";

} // end of MVApplication
