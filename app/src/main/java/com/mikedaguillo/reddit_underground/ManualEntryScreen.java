package com.mikedaguillo.reddit_underground;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.mikedaguillo.reddit_underground.SubredditDatabaseModel.SubredditsDatabaseHelper;
import com.mikedaguillo.reddit_underground.Utils.ConnectToReddit;

/**
 * Created by Mike on 12/10/2014.
 *
 * Activity to enter a manual subreddit, store the data, and display the results\
 *
 */

public class ManualEntryScreen extends ActionBarActivity {

    public static final String TAG = ManualEntryScreen.class.getSimpleName(); //Tag for error messages
    private Button cacheButton; // button to store local subreddit data
    private Button displayButton; // button to display the subreddit options
    private EditText subRedditInput; // edittext area for the user to input their subreddit of choice
    private String subreddit;
    private TextWatcher textWatcher;

    // For accessing the application database
    private SubredditsDatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_entry_layout);

        displayButton = (Button) findViewById(R.id.displaybutton);
        cacheButton = (Button) findViewById(R.id.cachebutton);
        subRedditInput = (EditText) findViewById(R.id.enterSubredditText);
        textWatcher = new ManualEntryTextWatcher();
        subRedditInput.addTextChangedListener(textWatcher);
        CacheButtonListener cacheListener = new CacheButtonListener();
        DisplayButtonListener displayListener = new DisplayButtonListener();
        cacheButton.setOnClickListener(cacheListener);
        displayButton.setOnClickListener(displayListener);
        database = new SubredditsDatabaseHelper(this);

        // Disable the buttons until subreddit data has been cached
        cacheButton.setEnabled(false);
        displayButton.setEnabled(false);
    }

    private class CacheButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            subreddit = subRedditInput.getText().toString();
            ConnectToReddit connection = new ConnectToReddit(database, ManualEntryScreen.this, subreddit);
            connection.execute();
            displayButton.setEnabled(true);
        }
    }

    // TextWatcher to watch if text is entered into the manual entry text field
    private class ManualEntryTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
        {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            checkFieldsForEmptyValues();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    // Method to check if there's any text in the manual entry text field
    private  void checkFieldsForEmptyValues(){
        String s1 = subRedditInput.getText().toString();

        if (s1.length() > 0) {
            cacheButton.setEnabled(true);
        } else {
            cacheButton.setEnabled(false);
        }

    }

    private class DisplayButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), RedditInstance.class);
            intent.putExtra("Stored_Subreddit", subreddit);
            startActivity(intent);
        }
    }
}

