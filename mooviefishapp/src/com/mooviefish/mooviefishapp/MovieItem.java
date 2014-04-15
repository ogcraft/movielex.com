package com.mooviefish.mooviefishapp;
import java.io.File;
import android.os.Environment;
import android.net.Uri;
import com.mooviefish.mooviefishapp.MovieTranslations;
import java.util.ArrayList;
import java.util.List;

public class MovieItem {
    public String id;
    public String title;
    public String desc;
    public String img;
    public List<MovieTranslations> translations;

    public MovieItem(String id, String title, String desc, String img) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.img = img;
        this.translations = new ArrayList<MovieTranslations>();
    }
    
    @Override
    public String toString() {
        return id + "|" + title;
    }

    public String getTranslationFileName(String lang) {
        return (id + "-" + lang + ".mp3");
    }

    public Uri getImgUri() {
        //String fn = Environment.getExternalStorageDirectory() + "/MoovieFish/" + desc;
        File file = new File(img); 
        return(Uri.fromFile(file));
    }
}