package com.mikedaguillo.reddit_underground;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import org.json.JSONObject;

import com.github.jreddit.utils.Utils;

import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.util.ArrayList;

public class RedditUnderground extends ActionBarActivity {

    public static final String TAG = RedditUnderground.class.getSimpleName(); //Tag for error messages
    private static final String GAMESURL = "/r/games.json";
    protected ProgressBar mProgressBar; // create the progress bar to display while the list loads
    private org.json.simple.JSONObject subredditData;
    String[] mTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reddit_underground);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            connectToReddit connection = new connectToReddit();
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

    private class connectToReddit extends AsyncTask<Object, Void, String[]>    {

        @Override
        protected String[] doInBackground(Object... objects) {
            //Grab the JSONObject from the correct subreddit
            Utils utils = new Utils();
            org.json.simple.JSONObject gamesJSON = (org.json.simple.JSONObject) utils.get(GAMESURL, null);
            Log.i(TAG, "JSON Data retrieved from URL. Printing the JSONObject displays: " + gamesJSON.toJSONString());


            //Grab the subreddit data
            org.json.simple.JSONObject gamesData = (org.json.simple.JSONObject) gamesJSON.get("data");
            Log.i(TAG, "JSON Data retrieved from original JSONObject. Printing the JSONObject displays: " + gamesData.toJSONString());

            JSONArray children = (JSONArray) gamesData.get("children");
            Log.i(TAG, "JSON Array 'Children' retrieved from JSONObject. Printing the JSONArray displays: " + children.toJSONString());

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reddit_underground, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
