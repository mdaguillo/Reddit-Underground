package com.mikedaguillo.reddit_underground;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Mike on 12/10/2014.
 */
public class SubredditsSelectionScreen extends ActionBarActivity {

    private ListView subredditsListView;
    private Button cacheButton;
    private Button displayButton;
    private ArrayList<String> subreddits;
    private ArrayList<String> checkedSubreddits;
    public static final String TAG = SubredditsSelectionScreen.class.getSimpleName(); //Tag for error messages

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
                }
                else {
                    checkedSubreddits.remove(checkedTextView.getText().toString());
                    Log.i(TAG, checkedSubreddits.toString());
                }

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, subreddits);
        subredditsListView.setAdapter(adapter);
    }

    private class CacheButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            //enable the display button when the subreddit string contains a value
            if (checkedSubreddits.size() > 0){
                displayButton.setEnabled(true);
            }

            Log.i(TAG, checkedSubreddits.toString());
        }
    }
}
