package com.movielex.movielexapp;
import java.io.File;
import android.os.Environment;
import android.net.Uri;
import com.movielex.movielexapp.MovieTranslations;
import java.util.ArrayList;
import java.util.List;

public class MovieItem {
    public String id;
    public String shortname;
    public String title;
    public String year_released;
    public String desc;
    public String desc_short;
    public String img;
    public String fpkeys_file;
    public String src_url;
    public String duration;
    public List<MovieTranslations> translations;

    public MovieItem() {
        this.translations = new ArrayList<MovieTranslations>();
    }
/*
    public MovieItem(String id, String shortname, String title, String year_released, String desc, String desc_short, String img, String fpkeys_file) {
        this.id = id;
        this.shortname = shortname;
        this.title = title;
        this.year_released = year_released;
        this.desc = desc;
        this.desc_short = desc_short;
        this.img = img;
        this.fpkeys_file = fpkeys_file;
        this.translations = new ArrayList<MovieTranslations>();
    }
*/    
    @Override
    public String toString() {
        return id + "|" + title;
    }

    public MovieTranslations getTranslationForLang(String lang) {
        for(int i=0; i<translations.size(); i++)
        {
            MovieTranslations t = translations.get(i);
            if(t.lang.equals(lang)) {
                return t;
            }
        }
        return null;
    }

    public String getTranslationFileName(String lang) {
        MovieTranslations t = getTranslationForLang(lang);
        if(t != null) {
            return t.file;
        }
        return "";
    }

    public Uri getImgUri() {
        //String fn = Environment.getExternalStorageDirectory() + "/MovieLex/" + desc;
        File file = new File(img); 
        return(Uri.fromFile(file));
    }
}
