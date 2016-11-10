package com.imagecachelib;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;

public class DownloadImageTask {

    private LruCache<String, Bitmap> mBitmapMemoryCache;

    private static DownloadImageTask instance = null;

    public static DownloadImageTask getInstance() {

        if (instance == null) {
            instance = new DownloadImageTask();
        }

        return instance;
    }

    // There will be only one instance of cache
    private DownloadImageTask() {

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/6th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 6;

        mBitmapMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mBitmapMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mBitmapMemoryCache.get(key);
    }

    /**
     * This method tries to get an image from an URL if this images is not at the current cache
     */
    public void loadImageFromURL(String url, ImageView target, OnTaskCompleted listener) {

        final String imageKey = String.valueOf(url);

        final Bitmap bitmap = getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            // Image found on the cache
            target.setImageBitmap(bitmap);

            if (listener != null) {
                listener.onTaskCompleted();
            }

        } else {
            // No image was found on the cache
            target.setImageResource(0);
            new DownloadImageFromURL(target, listener).execute(url);
        }
    }

    public void cancelRequest(ImageView target, OnTaskCompleted listener) {

        DownloadImageFromURL task = (DownloadImageFromURL) target.getTag(R.string.unique_id);

        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {

            // My AsyncTask has not finished yet. Cancelling it
            task.cancel(true);
            target.setImageResource(0);

            if (listener != null) {
                listener.onTaskCompleted();
            }
        }
    }

    class DownloadImageFromURL extends AsyncTask<String, Void, Bitmap> {

        private ImageView imageView;

        private OnTaskCompleted listener;

        public DownloadImageFromURL(ImageView imageView, OnTaskCompleted listener) {
            this.imageView = imageView;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageView.setTag(R.string.unique_id, this);
        }

        protected Bitmap doInBackground(String... urls) {

            String urldisplay = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bitmap = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }

            // Add bitmap to cache (key, bitmap)
            addBitmapToMemoryCache(String.valueOf(urldisplay), bitmap);

            return bitmap;
        }

        /* After decoding we update the view on the main UI. */
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);

            // Execute callback if exists
            if (listener != null) {
                listener.onTaskCompleted();
            }
        }
    }

}
