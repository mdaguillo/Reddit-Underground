package com.mikedaguillo.reddit_underground;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Mike on 12/10/2014.
 * Activity to enter a manual subreddit and display the results
 */

public class ManualEntryScreen extends ActionBarActivity {

    public static final String TAG = ManualEntryScreen.class.getSimpleName(); //Tag for error messages
    private String subreddit; // holds the subreddit to retrieve JSON data from
    private Button cacheButton; // button to store local subreddit data
    private Button displayButton; // button to display the subreddit options
    private EditText subRedditInput; // edittext area for the user to input their subreddit of choice

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_entry_layout);

        displayButton = (Button) findViewById(R.id.displaybutton);
        cacheButton = (Button) findViewById(R.id.cachebutton);
        subRedditInput = (EditText) findViewById(R.id.enterSubredditText);
        CacheButtonListener cacheListener = new CacheButtonListener();
        DisplayButtonListener displayListener = new DisplayButtonListener();
        cacheButton.setOnClickListener(cacheListener);
        displayButton.setOnClickListener(displayListener);

        // disable the display button until subreddit data has been cached
        displayButton.setEnabled(false);
        //loginButton.setEnabled(false);*/
    }

    private class CacheButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            // condition checks if there is any text in the edittext box
            if (subRedditInput.getText().toString().trim().length() == 0) {
                setSubRedditURL("all");
            }
            else {
                setSubRedditURL(subRedditInput.getText().toString());
            }

            //enable the display button when the subreddit string contains a value
            if (subreddit != null){
                displayButton.setEnabled(true);
            }

        }
    }

    private class DisplayButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            //create an intent with the subreddit data to pass to the reddit display activity
            Intent intent = new Intent(view.getContext(), RedditInstance.class);
            intent.setData(Uri.parse(subreddit));
            startActivity(intent);
        }
    }

    //method that constructs the string for the subreddit variable to be passed to the display activity
    private void setSubRedditURL(String url) {
        subreddit = url;
        Log.i(TAG, "The subreddit variable is set to: " + subreddit);
    }
}