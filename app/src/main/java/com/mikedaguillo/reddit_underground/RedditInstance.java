package com.mikedaguillo.reddit_underground;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cd.reddit.Reddit;
import com.cd.reddit.RedditException;
import com.cd.reddit.json.mapping.RedditLink;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class RedditInstance extends ActionBarActivity {

    public static final String TAG = RedditInstance.class.getSimpleName(); //Tag for error messages
    private String subreddit;
    protected ProgressBar mProgressBar; // create the progress bar to display while the list loads
    String[] mTitles; //array to store the post titles
    ArrayList<RedditListItem> redditPosts; // array list to store the data need from a post in a subreddit


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

            redditPosts = new ArrayList<RedditListItem>();

            Reddit redditInstance = new Reddit("Mike");
            try {
                List<RedditLink> subRedditListing = redditInstance.listingFor(subreddit, "hot");

                //Now with the each posts data in the array list gamesPostsData
                //set the mTitles size, and add each title into the mTitles array
                for (int i = 0; i < subRedditListing.size(); i++){
                    RedditListItem post = new RedditListItem(subRedditListing.get(i).getTitle(),
                            subRedditListing.get(i).getAuthor(),
                            subRedditListing.get(i).getSubreddit(),
                            subRedditListing.get(i).getNumComments());

                    redditPosts.add(post);
                }


                /*mTitles = new String[subRedditListing.size()];
                Log.i(TAG, "mTitles string array size determined. Size is: " + mTitles.length);

                for (int i = 0; i < mTitles.length; i++) {
                    //mTitles[i] = (String) gamesPostsData.get(i).get("title");
                    mTitles[i] = (String) subRedditListing.get(i).getTitle();
                    Log.i(TAG, "Iterating through mTitles and adding title info. Currently at entry: " + i);
                }*/
            } catch (RedditException e) {
                e.printStackTrace();

            }

            return redditPosts;
        }

        @Override
        protected void onPostExecute(ArrayList<RedditListItem> result) {
            mProgressBar.setVisibility(View.INVISIBLE);
            setView(result);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    /**
     * Created by Mike on 11/26/2014.
     *
     * Custom view adapter to display subreddit posts on the RedditInstance class
     */
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

            RedditListItem post = posts.get(i);

            postTitle.setText(post.getTitle());
            postAuthor.setText(post.getAuthor());
            postSubReddit.setText(post.getSubreddit());
            postComments.setText("Comments: " + post.getNumOfComments());

            return view;
        }
    }

}

