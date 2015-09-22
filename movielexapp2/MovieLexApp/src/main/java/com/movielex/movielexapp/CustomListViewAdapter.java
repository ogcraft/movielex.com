package com.movielex.movielexapp;
 
import java.util.List;
import com.movielex.movielexapp.R;
import com.movielex.movielexapp.MovieItem;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;

public class CustomListViewAdapter extends ArrayAdapter<MovieItem> {
 
    Context context;
 
    public CustomListViewAdapter(Context context, int resourceId,
            List<MovieItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }
 
    /*private view holder class*/
    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDesc;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        MovieItem movieItem = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
 
        holder.txtDesc.setText(
            StringUtils.abbreviate(
                movieItem.desc_short, 
                MFApplication.MAX_CHAR_ALLOWED_MOVIE_LIST_DESC_VIEW));
        holder.txtTitle.setText(movieItem.title);
        holder.imageView.setImageURI(movieItem.getImgUri());
 
        return convertView;
    }
}
