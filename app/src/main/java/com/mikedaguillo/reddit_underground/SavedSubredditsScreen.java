package com.mikedaguillo.reddit_underground;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mikedaguillo.reddit_underground.SubredditDatabaseModel.SubredditsDatabaseHelper;

import java.util.ArrayList;

/**
 * Created by Mike on 12/15/2014.
 *
 * Activity to select the subreddits that are pulled from the app database
 */
public class SavedSubredditsScreen extends ActionBarActivity{

    private ListView subredditsListView;
    private ArrayList<String> savedSubreddits;
    private SubredditsDatabaseHelper databaseHelper;
    public static final String TAG = SavedSubredditsScreen.class.getSimpleName(); //Tag for error messages

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_subreddits_screen_layout);

        subredditsListView = (ListView) findViewById(R.id.savedSubredditsListView);
        databaseHelper = new SubredditsDatabaseHelper(this);
        savedSubreddits = new ArrayList<String>();
        getSavedSubreddits();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.menu_item, R.id.menuItem, savedSubreddits);
        subredditsListView.setAdapter(adapter);
        SubredditClickListener listener = new SubredditClickListener();
        subredditsListView.setOnItemClickListener(listener);
    }

    // Grab the titles of the subreddits stored in the database and add them to the ArrayList
    private ArrayList<String> getSavedSubreddits() {
        Cursor cursor = databaseHelper.getSubreddits();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                savedSubreddits.add(cursor.getString(1));
                cursor.moveToNext();
            }
        }
        else {
            Log.i(TAG, "Cursor returned 0 items");
        }
        return savedSubreddits;
    }

    private class SubredditClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent intent = new Intent(view.getContext(), RedditInstance.class);
            intent.putExtra("Stored_Subreddit", savedSubreddits.get(position));
            intent.putExtra("Intent_Int", 2);
            startActivity(intent);
        }
    }

}
