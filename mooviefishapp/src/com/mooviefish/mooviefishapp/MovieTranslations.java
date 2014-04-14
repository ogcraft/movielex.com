package com.mooviefish.mooviefishapp;
import java.io.File;
import android.os.Environment;
import android.net.Uri;


public class MovieTranslations {
    public String id;
    public String title;
    public String desc;
    public String img;
    //public String[] translations;

    public MovieTranslations(String id, String title, String desc, String img) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.img = img;
    }
    
    @Override
    public String toString() {
        return id + "|" + title;
    }

    public Uri getImgUri() {
        //String fn = Environment.getExternalStorageDirectory() + "/MoovieFish/" + desc;
        File file = new File(img); 
        return(Uri.fromFile(file));
    }
}