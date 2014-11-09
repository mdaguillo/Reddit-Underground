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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.jreddit.utils.Utils;

import org.json.simple.JSONArray;

import java.util.ArrayList;

public class RedditInstance extends ActionBarActivity {

    public static final String TAG = RedditInstance.class.getSimpleName(); //Tag for error messages
    private static String SUBREDDIT;
    protected ProgressBar mProgressBar; // create the progress bar to display while the list loads
    String[] mTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reddit_underground);

        Intent intent = getIntent();
        Uri subredditURI = intent.getData();
        SUBREDDIT = subredditURI.toString();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            ConnectToReddit connection = new ConnectToReddit();
            connection.execute();
        }
        else {
            Toast.makeText(this, "Unable to establish a connection to reddit", Toast.LENGTH_LONG).show();
        }
    }

    private void setView(String[] titles) {
        ListView listView = (ListView) findViewById(R.id.listView);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, titles);

        listView.setAdapter(adapter);
    }

    private class ConnectToReddit extends AsyncTask<Object, Void, String[]>    {

        @Override
        protected String[] doInBackground(Object... objects) {
            //Grab the JSONObject from the correct subreddit
            Utils utils = new Utils();
            org.json.simple.JSONObject gamesJSON = (org.json.simple.JSONObject) utils.get(SUBREDDIT, null);
            Log.i(TAG, "JSON object retrieved from URL.");


            //Grab the subreddit data
            org.json.simple.JSONObject gamesData = (org.json.simple.JSONObject) gamesJSON.get("data");
            Log.i(TAG, "JSON Data retrieved from original JSONObject.");

            JSONArray children = (JSONArray) gamesData.get("children");
            Log.i(TAG, "JSON Array 'Children' retrieved from JSON Data.");

            //Grab Each object out of the children array and place in an array list of JSONObjects
            ArrayList<org.json.simple.JSONObject> gamesPosts = new ArrayList<org.json.simple.JSONObject>();
            Log.i(TAG, "The size of the ArrayList is set to " + gamesPosts.size());
            for (int i = 0; i < children.size(); i++) {
                gamesPosts.add(i, (org.json.simple.JSONObject) children.get(i));
                Log.i(TAG, "ArrayList Object " + i + " created.");
            }
            Log.i(TAG, "JSON ArrayList<JSONObject> created for the data in the array");

            //Each JSONObject within the arraylist contains the JSONObject "data"
            //which we need to grab the title, author, and other info
            //Store this data in a new arraylist
            ArrayList<org.json.simple.JSONObject> gamesPostsData = new ArrayList<org.json.simple.JSONObject>();
            for (int i = 0; i < children.size(); i++) {
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
