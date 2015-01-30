package com.mikedaguillo.reddit_underground;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cd.reddit.Reddit;
import com.cd.reddit.RedditException;
import com.cd.reddit.json.mapping.RedditLink;
import com.mikedaguillo.reddit_underground.SubredditDatabaseModel.SubredditsDatabaseHelper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class RedditInstance extends ActionBarActivity {

    public static final String TAG = RedditInstance.class.getSimpleName(); //Tag for error messages

    private String subreddit;
    private ProgressBar mProgressBar; // create the progress bar to display while the list loads
    private SubredditsDatabaseHelper databaseHelper;
    private ArrayList<RedditListItem> redditPosts; // array list to store the data need from a post in a subreddit
    private ArrayList<Bitmap> thumbnailBitmaps; // array of the bitmaps to display the thumbnails


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reddit_underground);

        // Retrieve the subreddit passed in the intent if user manually selects a subreddit
        Intent intent = getIntent();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.VISIBLE);
        subreddit = intent.getStringExtra("Stored_Subreddit");
        databaseHelper = new SubredditsDatabaseHelper(this);
        setView(subreddit, databaseHelper);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private class PostClickListener implements AdapterView.OnItemClickListener {

        Cursor cursor = databaseHelper.getPosts(subreddit);

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            cursor.moveToPosition(position);

            Intent intent = new Intent(view.getContext(), CommentsListActivity.class);
            intent.putExtra("Stored Post", cursor.getInt(0));
            startActivity(intent);
        }
    }

    // Helper function to set the list view items to the appropriate text from the string and int arrays
    // This one is used if the activity is created from the stored subreddits screen
    private void setView(String subredditToRetrieve, SubredditsDatabaseHelper databaseHelper) {
        RedditViewAdapter redditView = new RedditViewAdapter(subredditToRetrieve, databaseHelper);
        ListView listView = (ListView) findViewById(R.id.subredditsListView);
        listView.setAdapter(redditView);
        PostClickListener listener = new PostClickListener();
        listView.setOnItemClickListener(listener);
    }


    // Custom view adapter to display subreddit posts on the RedditInstance class
     class RedditViewAdapter extends BaseAdapter {
        String storedSubreddit;
        SubredditsDatabaseHelper databaseHelper;
        Cursor cursor;

        public RedditViewAdapter(String storedSubreddit, SubredditsDatabaseHelper databaseHelper) {
            this.storedSubreddit = storedSubreddit;
            this.databaseHelper = databaseHelper;
            cursor = databaseHelper.getPosts(storedSubreddit);
        }

        @Override
        public int getCount() {
            int count = cursor.getCount();
            return count;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int listPosition, View view, ViewGroup viewGroup) {
            if(view == null)
            {
                LayoutInflater inflater = (LayoutInflater) RedditInstance.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.reddit_list_item, viewGroup, false);
            }

            TextView postTitle = (TextView)view.findViewById(R.id.postTitle);
            TextView postAuthor = (TextView)view.findViewById(R.id.postAuthor);
            TextView postSubReddit = (TextView)view.findViewById(R.id.postSubReddit);
            TextView postComments = (TextView)view.findViewById(R.id.postComments);
            ImageView postThumbnail = (ImageView)view.findViewById(R.id.thumbnail);

            cursor.moveToPosition(listPosition);

            postTitle.setText(cursor.getString(1));
            postAuthor.setText(cursor.getString(2));
            postSubReddit.setText(cursor.getString(3));
            postComments.setText("Comments: " + cursor.getInt(4));

            try {
                // retrieve the thumbnail from the database
                postThumbnail.setImageBitmap(BitmapFactory.decodeByteArray(cursor.getBlob(5), 0, cursor.getBlob(5).length));
            }
            catch(Exception e) {
                // use the default image
                Bitmap default_image = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_reddit_icon);
                postThumbnail.setImageBitmap(default_image);
            }

            return view;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseHelper.close();
    }

    @Override
    protected  void onResume() {
        super.onResume();
        databaseHelper = new SubredditsDatabaseHelper(this);
    }
}

