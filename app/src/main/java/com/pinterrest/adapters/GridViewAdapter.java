package com.pinterrest.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.imagecachelib.DownloadImageTask;
import com.imagecachelib.OnTaskCompleted;
import com.pinterrest.R;
import com.pinterrest.models.Post;

import java.util.ArrayList;

public class GridViewAdapter extends ArrayAdapter<Post> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Post> data = new ArrayList<Post>();

    public GridViewAdapter(Context context, int layoutResourceId, ArrayList<Post> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) row.findViewById(R.id.image);
            holder.imageTitle = (TextView) row.findViewById(R.id.image_title);
            holder.imageLikes = (TextView) row.findViewById(R.id.image_likes);
            holder.progressBar = (ProgressBar) row.findViewById(R.id.grid_item_progressbar);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.imageTitle.setText(data.get(position).getUser().getUsername());
        holder.imageLikes.setText(data.get(position).getLikes().toString());
        holder.image.setTag(data.get(position).getUrls().getRegular());
        holder.progressBar.setVisibility(View.VISIBLE);

        DownloadImageTask.getInstance().loadImageFromURL(data.get(position).getUrls().getRegular(),
                holder.image, new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted() {
                        holder.progressBar.setVisibility(View.GONE);
                    }
                });

        return row;
    }

    static class ViewHolder {
        ImageView image;
        TextView imageTitle;
        TextView imageLikes;
        ProgressBar progressBar;
    }
}