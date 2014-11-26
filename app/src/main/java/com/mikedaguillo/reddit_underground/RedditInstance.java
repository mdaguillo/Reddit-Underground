package com.mikedaguillo.reddit_underground;

import android.content.Context;
import android.content.Intent;
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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cd.reddit.Reddit;
import com.cd.reddit.RedditException;
import com.cd.reddit.json.mapping.RedditLink;

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
    protected ProgressBar mProgressBar; // create the progress bar to display while the list loads
    ArrayList<RedditListItem> redditPosts; // array list to store the data need from a post in a subreddit
    ArrayList<Bitmap> thumbnailBitmaps; // array of the bitmaps to display the thumbnails


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reddit_underground);

        //retrieve the subreddit passed in the intent if user manually selects a subreddit
        Intent intent = getIntent();
        Uri subredditURI = intent.getData();
        subreddit = subredditURI.toString();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        //check if the network is available, if so establish a connection to Reddit
        if (isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            ConnectToReddit connection = new ConnectToReddit();

            connection.execute();
        } else {
            Toast.makeText(this, "Unable to establish a connection to reddit", Toast.LENGTH_LONG).show();
        }
    }

    //helper function to set the list view items to the appropriate text from the string and int arrays
    private void setView(ArrayList<RedditListItem> viewListItems) {

        RedditViewAdapter redditView = new RedditViewAdapter(viewListItems);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(redditView);
    }

    //custom AsyncTask to run in the background to grab data from Reddit
    private class ConnectToReddit extends AsyncTask<Object, Void, ArrayList<RedditListItem>> {

        @Override
        protected ArrayList<RedditListItem> doInBackground(Object... objects) {

            // Create the array that will hold the reddit posts in the custom list item data structure
            redditPosts = new ArrayList<RedditListItem>();
            thumbnailBitmaps = new ArrayList<Bitmap>();

            // Create raw4j Reddit object
            Reddit redditInstance = new Reddit("Mike");
            try {
                // Grab the raw4j data structure that stores the JSON data for a list of posts in a subreddit
                List<RedditLink> subRedditListing = redditInstance.listingFor(subreddit, "hot");

                // Create a RedditListItem object for each post in the List<RedditLink> object
                // and set the data in the RedditListItem to the relevant info in the post
                // finally add each of the RedditListItem's into the array that will store these objects
                for (int i = 0; i < subRedditListing.size(); i++){
                    RedditListItem post = new RedditListItem(subRedditListing.get(i).getTitle(),
                            subRedditListing.get(i).getAuthor(),
                            subRedditListing.get(i).getSubreddit(),
                            subRedditListing.get(i).getNum_comments(),
                            subRedditListing.get(i).getThumbnail(),
                            subRedditListing.get(i).isOver18());
                    redditPosts.add(post);
                }

                for (int i = 0; i < redditPosts.size(); i++) {
                    // iterate through the posts create a bitmap object from the URLs
                    // add to local scope ArrayList<Bitmap>
                    Log.i(TAG, redditPosts.get(i).getThumbnailSrc());
                    Log.i(TAG, "" + redditPosts.get(i).getNSFW());

                    if (redditPosts.get(i).getThumbnailSrc().equals("")){
                        // if there's no image, display placeholder reddit icon
                        thumbnailBitmaps.add(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_reddit_icon));
                    }
                    else if (redditPosts.get(i).getThumbnailSrc().equals("nsfw")){
                        thumbnailBitmaps.add(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_reddit_icon_nsfw));
                    }
                    else {
                        thumbnailBitmaps.add(getImageBitmap(redditPosts.get(i).getThumbnailSrc()));
                    }
                }

            } catch (RedditException e) {
                e.printStackTrace();
            }

            return redditPosts;
        }

        @Override
        protected void onPostExecute(ArrayList<RedditListItem> result) {
            // Remove the progress bar and set thew view
            mProgressBar.setVisibility(View.INVISIBLE);
            setView(result);
        }
    }

    // Method to check if there is a network connection available
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }


    // Custom view adapter to display subreddit posts on the RedditInstance class
     class RedditViewAdapter extends BaseAdapter {

        ArrayList<RedditListItem> posts;

        public RedditViewAdapter(ArrayList<RedditListItem> redditPosts) {
            posts = redditPosts;
        }

        @Override
        public int getCount() {
            return posts.size();
        }

        @Override
        public RedditListItem getItem(int i) {
            return posts.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view==null)
            {
                LayoutInflater inflater = (LayoutInflater) RedditInstance.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.reddit_list_item, viewGroup, false);
            }

            TextView postTitle = (TextView)view.findViewById(R.id.postTitle);
            TextView postAuthor = (TextView)view.findViewById(R.id.postAuthor);
            TextView postSubReddit = (TextView)view.findViewById(R.id.postSubReddit);
            TextView postComments = (TextView)view.findViewById(R.id.postComments);
            ImageView postThumbnail = (ImageView)view.findViewById(R.id.thumbnail);

            RedditListItem post = posts.get(i);

            postTitle.setText(post.getTitle());
            postAuthor.setText(post.getAuthor());
            postSubReddit.setText(post.getSubreddit());
            postComments.setText("Comments: " + post.getNumOfComments());
            postThumbnail.setImageBitmap(thumbnailBitmaps.get(i));

            return view;
        }
    }

    private Bitmap getImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Error getting bitmap", e);
        }
        return bm;
    }
}

