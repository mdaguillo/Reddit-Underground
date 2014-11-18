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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.jreddit.utils.Utils;
import com.github.jreddit.utils.restclient.HttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class RedditInstance extends ActionBarActivity {

    public static final String TAG = RedditInstance.class.getSimpleName(); //Tag for error messages
    private String subreddit;
    protected ProgressBar mProgressBar; // create the progress bar to display while the list loads
    String[] mTitles; //array to store the post titles
    String[] mAuthors; //array to store the authors of the posts
    String[] mSubreddit; //array of subreddits if viewing /r/all
    int[] mNumComments; //array of total comments per post

    // Initialize REST Client
    RestClient restClient = new HttpRestClient();
    restClient.setUserAgent("bot/1.0 by name");

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
        }
        else {
            Toast.makeText(this, "Unable to establish a connection to reddit", Toast.LENGTH_LONG).show();
        }
    }

    //helper function to set the list view items to the appropriate text from the string and int arrays
    private void setView(String[] titles) {
        ListView listView = (ListView) findViewById(R.id.listView);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, titles);

        listView.setAdapter(adapter);
    }

    //custom AsyncTask to run in the background to grab data from Reddit
    private class ConnectToReddit extends AsyncTask<Object, Void, String[]>    {

        @Override
        protected String[] doInBackground(Object... objects) {

            //Grab the JSONObject from the correct subreddit
            //the utils object is from the reddit wrapper library, which uses simple JSON Objects
            //the .get() method retrieves JSON data from a subreddit URL
            Utils utils = new Utils();
            org.json.simple.JSONObject subredditParentJSON = (org.json.simple.JSONObject) utils.get(subreddit, null);
            Log.i(TAG, "JSON object retrieved from URL.");


            //Grab the subreddit "data" object from the subreddit's most parent JSON object
            org.json.simple.JSONObject subredditParentJSONData = getJSONObject(subredditParentJSON, "data");

            //Grab the JSONArray from the "data" JSON Object. This array contains more JSON Objects
            //that include the titles, author names, comments etc
            JSONArray subredditChildrenPosts = getJSONArray(subredditParentJSONData, "children");

            //Grab Each object out of the children array and place in an array list of JSONObjects
            ArrayList<org.json.simple.JSONObject> gamesPosts = new ArrayList<org.json.simple.JSONObject>();
            Log.i(TAG, "The size of the ArrayList is set to " + gamesPosts.size());
            for (int i = 0; i < subredditChildrenPosts.size(); i++) {
                gamesPosts.add(i, (org.json.simple.JSONObject) subredditChildrenPosts.get(i));
                Log.i(TAG, "ArrayList Object " + i + " created.");
            }
            Log.i(TAG, "JSON ArrayList<JSONObject> created for the data in the array");

            //Each JSONObject within the arraylist contains the JSONObject "data"
            //which we need to grab the title, author, and other info
            //Store this data in a new arraylist
            ArrayList<org.json.simple.JSONObject> gamesPostsData = new ArrayList<org.json.simple.JSONObject>();
            for (int i = 0; i < subredditChildrenPosts.size(); i++) {
                gamesPostsData.add(i, (org.json.simple.JSONObject) gamesPosts.get(i).get("data"));
                Log.i(TAG, "ArrayList of titles, entry " + i + " created.");
            }
            Log.i(TAG, "JSON ArrayList<JSONObject> created for the individual posts information");

            //Now with the each posts data in the array list gamesPostsData
            //set the mTitles size, and add each title into the mTitles array
            mTitles = new String[gamesPostsData.size()];
            Log.i(TAG, "mTitles string array size determined. Size is: " + mTitles.length);

            for (int i = 0; i < mTitles.length; i++) {
                mTitles[i] = (String) gamesPostsData.get(i).get("title");
                Log.i(TAG, "Iterating through mTitles and adding title info. Currently at entry: " + i);
            }

            return mTitles;
        }

        //helper method to extract child JSON Array from a parent JSON Object
        private JSONArray getJSONArray(JSONObject json, String arrayname) {
            JSONArray newJSONArray = (JSONArray) json.get(arrayname);
            Log.i(TAG, "JSON Array, " + arrayname + ", retrieved from JSON Data.");
            return newJSONArray;
        }

        //helper method to extract child JSON Object from a parent JSON Object
        private JSONObject getJSONObject(JSONObject json, String objectname) {
            JSONObject newJSONObject = (JSONObject) json.get(objectname);
            Log.i(TAG, "JSON object, " + objectname + ", retrieved from original JSONObject.");
            return newJSONObject;
        }

        @Override
        protected void onPostExecute(String[] result) {
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

}
