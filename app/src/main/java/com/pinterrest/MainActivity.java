package com.pinterrest;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.imagecachelib.DownloadImageTask;
import com.imagecachelib.OnTaskCompleted;
import com.pinterrest.adapters.GridViewAdapter;
import com.pinterrest.api.ServerApi;
import com.pinterrest.models.Post;
import com.pinterrest.utils.ConnectionUtils;
import com.pinterrest.utils.Constants;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSwipeContainer;

    private GridView mGridview;

    private GridViewAdapter mGridAdapter;

    private ProgressBar mProgressBar;

    private TextView mNoPostsFound;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                retrieveData();
            }
        });

        mSwipeContainer.setColorSchemeResources(R.color.red, R.color.red, R.color.red, R.color.red);

        mGridview = (GridView) findViewById(R.id.carte_gridview);

        mProgressBar = (ProgressBar) findViewById(R.id.grid_progressbar);

        mNoPostsFound = (TextView) findViewById(R.id.no_posts_found);

        mContext = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        retrieveData();
    }

    private void retrieveData() {

        if (!ConnectionUtils.isOnline(this)) {
            mProgressBar.setVisibility(View.GONE);
            Toast.makeText(this, getString(R.string.no_connectivity), Toast.LENGTH_SHORT).show();
            return;
        }

        /* This way we are using a default JSon converter for Retrofit
            If we want to change to XML for example we need to set
            setConverter(new SimpleXMLConverter()) to the restAdapter */
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.SERVER_HOST).build();

        ServerApi api = restAdapter.create(ServerApi.class);

        api.getData("application/json", new Callback<ArrayList<Post>>() {

            @Override
            public void success(ArrayList<Post> posts, Response response) {

                mProgressBar.setVisibility(View.GONE);

                if (posts.size() > 0) {

                    mGridAdapter = new GridViewAdapter(mContext, R.layout.grid_item_layout, posts);
                    mGridview.setAdapter(mGridAdapter);
                    mSwipeContainer.setRefreshing(false);
                    mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, final View v,
                                                int position, long id) {

                            // Click to cancel image loading
                            final ImageView image = (ImageView) v.findViewById(R.id.image);

                            DownloadImageTask.getInstance().cancelRequest(image, new OnTaskCompleted() {
                                @Override
                                public void onTaskCompleted() {
                                    Toast.makeText(MainActivity.this, getString(R.string.image_loading_canceled), Toast.LENGTH_SHORT).show();
                                    ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.grid_item_progressbar);
                                    progressBar.setVisibility(View.GONE);

                                    image.setImageDrawable(getDrawable(R.drawable.logo_pinterest));
                                }
                            });
                        }
                    });

                } else {
                    mNoPostsFound.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                mProgressBar.setVisibility(View.GONE);
                mNoPostsFound.setVisibility(View.VISIBLE);
                Toast.makeText(mContext, getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
