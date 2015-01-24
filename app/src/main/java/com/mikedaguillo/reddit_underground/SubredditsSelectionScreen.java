package com.mikedaguillo.reddit_underground;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import com.cd.reddit.Reddit;
import com.mikedaguillo.reddit_underground.SubredditDatabaseModel.SubredditsDatabaseHelper;
import com.mikedaguillo.reddit_underground.Utils.ConnectToReddit;
import java.util.ArrayList;

/**
 * Created by Mike on 12/10/2014.
 *
 * Selection Screen to download and store the data retreived from reddit in a local SQLite
 * database. There is an async task that does all of the web related tasks necessary to retreive
 * the data from Reddit. Then there is an update database task that takes the information stored
 * in local variables and permanently stores it into the database for quick and easy retrieval
 * in the other activities.
 *
 */
public class SubredditsSelectionScreen extends ActionBarActivity {

    private ListView subredditsListView;
    private Button cacheButton;
    private Button deleteButton;
    private ArrayList<String> subreddits;
    private ArrayList<String> checkedSubreddits;
    public static final String TAG = SubredditsSelectionScreen.class.getSimpleName(); //Tag for error messages

    // For accessing the application database
    private SubredditsDatabaseHelper database;
    public Reddit redditInstance;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subreddit_selector_screen);

        // Grab the list of subreddits from the logged in user
        Intent intent = getIntent();
        subreddits = intent.getStringArrayListExtra("Subreddits");
        checkedSubreddits = new ArrayList<String>();

        subredditsListView = (ListView) findViewById(R.id.subredditsListView);
        subredditsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        cacheButton = (Button) findViewById(R.id.subredditCacheButton);
        deleteButton = (Button) findViewById(R.id.deleteAllButton);
        CacheButtonListener cacheListener = new CacheButtonListener();
        DeleteAllListener deleteListener = new DeleteAllListener();
        cacheButton.setOnClickListener(cacheListener);
        deleteButton.setOnClickListener(deleteListener);
        cacheButton.setEnabled(false);


        subredditsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // change the checkbox state
                CheckedTextView checkedTextView = ((CheckedTextView) view);
                checkedTextView.setChecked(!checkedTextView.isChecked());

                if (checkedTextView.isChecked()) {
                    checkedSubreddits.add(checkedTextView.getText().toString());
                } else {
                    checkedSubreddits.remove(checkedTextView.getText().toString());
                }

                // Enable the cache button only if there has been a selection
                if (checkedSubreddits.size() > 0) {
                    cacheButton.setEnabled(true);
                }
                else {
                    cacheButton.setEnabled(false);
                }

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, subreddits);
        subredditsListView.setAdapter(adapter);

        // To access the internal database and the Reddit server
        database = new SubredditsDatabaseHelper(this);
        redditInstance = new Reddit("RedditUnderground");
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.close();
    }


    private class CacheButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            ConnectToReddit connection = new ConnectToReddit(database, SubredditsSelectionScreen.this, redditInstance, checkedSubreddits);
            connection.execute();
        }
    }

    private class DeleteAllListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            database.deleteAll();
        }
    }
}


