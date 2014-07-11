package com.mooviefish.mooviefishapp;
import java.io.File;
import android.os.Environment;
import android.net.Uri;
import com.mooviefish.mooviefishapp.MovieTranslations;
import java.util.ArrayList;
import java.util.List;

public class MovieItem {
    public String id;
    public String shortname;
    public String title;
    public String desc;
    public String img;
    public String fpkeys_file;
    public List<MovieTranslations> translations;

    public MovieItem(String id, String shortname, String title, String desc, String img, String fpkeys_file) {
        this.id = id;
        this.shortname = shortname;
        this.title = title;
        this.desc = desc;
        this.img = img;
        this.fpkeys_file = fpkeys_file;
        this.translations = new ArrayList<MovieTranslations>();
    }
    
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

    //public String getFpkeysFileName() {
    //    return (id + "/" + id + "-en.fpkeys");
    //}

    public Uri getImgUri() {
        //String fn = Environment.getExternalStorageDirectory() + "/MoovieFish/" + desc;
        File file = new File(img); 
        return(Uri.fromFile(file));
    }
}
