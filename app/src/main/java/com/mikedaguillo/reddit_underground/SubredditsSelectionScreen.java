package com.mikedaguillo.reddit_underground;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.cd.reddit.Reddit;
import com.cd.reddit.RedditException;
import com.cd.reddit.json.mapping.RedditLink;
import com.mikedaguillo.reddit_underground.SubredditDatabaseModel.SubredditsDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mike on 12/10/2014.
 */
public class SubredditsSelectionScreen extends ActionBarActivity {

    private ListView subredditsListView;
    private Button cacheButton;
    private Button displayButton;
    private ArrayList<String> subreddits;
    private ArrayList<String> checkedSubreddits;
    private ArrayList<List<RedditLink>> listofSubreddits; // array list to store the data returned from reddit
    public static final String TAG = SubredditsSelectionScreen.class.getSimpleName(); //Tag for error messages

    // For accessing the application database
    private SubredditsDatabaseHelper database;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subreddit_selector_screen);

        Intent intent = getIntent();
        subreddits = intent.getStringArrayListExtra("Subreddits");
        checkedSubreddits = new ArrayList<String>();

        subredditsListView = (ListView) findViewById(R.id.subredditsListView);
        subredditsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        cacheButton = (Button) findViewById(R.id.subredditCacheButton);
        displayButton = (Button) findViewById(R.id.subredditDisplayButton);
        CacheButtonListener cacheListener = new CacheButtonListener();
        cacheButton.setOnClickListener(cacheListener);

        // disable the display button until subreddit data has been cached
        displayButton.setEnabled(false);

        subredditsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // change the checkbox state
                CheckedTextView checkedTextView = ((CheckedTextView) view);
                checkedTextView.setChecked(!checkedTextView.isChecked());

                if (checkedTextView.isChecked()) {
                    checkedSubreddits.add(checkedTextView.getText().toString());
                    Log.i(TAG, checkedTextView.getText().toString());
                    Log.i(TAG, checkedSubreddits.toString());
                } else {
                    checkedSubreddits.remove(checkedTextView.getText().toString());
                    Log.i(TAG, checkedSubreddits.toString());
                }

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, subreddits);
        subredditsListView.setAdapter(adapter);

        database = new SubredditsDatabaseHelper(this);
    }

    private void updateDatabase(ArrayList<List<RedditLink>> redditLinks) {
        Log.i(TAG, "Executing updateDatabase");
        database.deleteAll();
        Cursor Subreddit;
        Cursor Post;

        for (int i = 0; i < checkedSubreddits.size(); i++) {
            database.addSubreddit(checkedSubreddits.get(i));
            List<RedditLink> subredditInfo = redditLinks.get(i);
            for (RedditLink links : subredditInfo) {
                database.addPost(links.getTitle(), links.getAuthor(), links.getSubreddit(), links.getNum_comments(), i);
            }
            Subreddit = database.getSubreddits();
            Post = database.getPosts(i);
            Subreddit.moveToPosition(i);
            Post.moveToFirst();
            Log.i(TAG, "Added the subreddit: " + Subreddit.getString(1) + " to the sqlite database, at position " + Subreddit.getString(0));
            while (!Post.isAfterLast()) {
                Log.i(TAG, Post.getString(1));
                Post.moveToNext();
            }
        }



    }

    private class CacheButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            //enable the display button when the subreddit string contains a value
            if (checkedSubreddits.size() > 0) {
                displayButton.setEnabled(true);
            }

            ConnectToReddit connection = new ConnectToReddit();
            connection.execute();
        }
    }

    //custom AsyncTask to run in the background to grab data from Reddit
    private class ConnectToReddit extends AsyncTask<Object, Void, ArrayList<List<RedditLink>>> {

        @Override
        protected ArrayList<List<RedditLink>> doInBackground(Object... objects) {

           listofSubreddits = new ArrayList<List<RedditLink>>();

            // Create raw4j Reddit object
            Reddit redditInstance = new Reddit("RedditUnderground");

            for (String subreddit : checkedSubreddits) {
                try {
                    // Grab the raw4j data structure that stores the JSON data for a list of posts in a subreddit
                    List<RedditLink> subRedditListing = redditInstance.listingFor(subreddit, "hot");
                    try {
                        listofSubreddits.add(subRedditListing);
                    }
                    catch (Exception e) {
                        Log.e(TAG, "Exception: " + e);
                    }
                    Log.i(TAG, "Storing subreddits with their data in the array");

                } catch (RedditException e) {
                    Log.e(TAG, "Exception: " + e);
                }
            }
            return listofSubreddits;
        }

        @Override
        protected void onPostExecute(ArrayList<List<RedditLink>> redditPosts) {
            Log.i(TAG, "Executing onPostExecute");
            updateDatabase(redditPosts);
        }

    }
}


