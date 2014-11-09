package com.mikedaguillo.reddit_underground;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class StartScreen extends ActionBarActivity {

    private static String subreddit;
    public static final String TAG = StartScreen.class.getSimpleName(); //Tag for error messages

    protected Button cacheButton;
    protected Button displayButton;
    protected EditText subRedditInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        displayButton = (Button) findViewById(R.id.displaybutton);
        cacheButton = (Button) findViewById(R.id.cachebutton);
        subRedditInput = (EditText) findViewById(R.id.enterSubredditText);
        CacheButtonListener cacheListener = new CacheButtonListener();
        DisplayButtonListener displayListener = new DisplayButtonListener();


        cacheButton.setOnClickListener(cacheListener);
        displayButton.setOnClickListener(displayListener);

        displayButton.setEnabled(false);
    }

    private class CacheButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (subRedditInput.getText().toString().trim().length() == 0) {
                setSubRedditURL("all");
            }
            else {
                setSubRedditURL(subRedditInput.getText().toString());
            }

            if (subreddit != null){
                displayButton.setEnabled(true);
            }

        }
    }

    private class DisplayButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), RedditInstance.class);
            intent.setData(Uri.parse(subreddit));
            startActivity(intent);

        }
    }

    private void setSubRedditURL(String url) {
        subreddit = "/r/" + url + ".json";
        Log.i(TAG, "The subreddit variable is set to: " + subreddit);
    }

}
