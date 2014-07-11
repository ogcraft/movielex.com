package com.mooviefish.mooviefishapp;
import java.io.File;
import android.os.Environment;
import android.net.Uri;


public class MovieTranslations {
    public String file;
    public String title;
    public String desc;
    public String img;
    public String lang;
   
    public MovieTranslations(String file, String title, String desc, String img, String lang) {
        this.file = file;
        this.title = title;
        this.desc = desc;
        this.img = img;
        this.lang = lang;
    }
    
    @Override
    public String toString() {
        return "Trans: " + lang + "|" + title;
    }

    public Uri getImgUri() {
        //String fn = Environment.getExternalStorageDirectory() + "/MoovieFish/" + desc;
        File file = new File(img); 
        return(Uri.fromFile(file));
    }
}