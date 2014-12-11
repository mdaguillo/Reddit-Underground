package com.mikedaguillo.reddit_underground;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class StartScreen extends ActionBarActivity {

    public static final String TAG = StartScreen.class.getSimpleName(); //Tag for error messages

    private static final int LOGIN_REQUEST = 1;

    private boolean loggedIn = false;

    private final String LOGIN_TEXT = "Login To Your Reddit Account";
    private final String MANUAL_TEXT = "Manually Enter a Subreddit";
    private final String SUBREDDITS_TEXT = "Your Subscribed Subreddits";

    private ListView listView;

    private String[] loggedOutOptions = new String[] {LOGIN_TEXT, MANUAL_TEXT};
    private String[] loggedInOptions = new String[] {MANUAL_TEXT, SUBREDDITS_TEXT};

    private ArrayAdapter<String> loggedOutAdapter;
    private ArrayAdapter<String> loggedInAdapter;

    private ArrayList<String> subscribedSubreddits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        loggedOutAdapter = new ArrayAdapter<String>(this, R.layout.menu_item, R.id.menuItem, loggedOutOptions);
        loggedInAdapter = new ArrayAdapter<String>(this, R.layout.menu_item, R.id.menuItem, loggedInOptions);

        listView = (ListView) findViewById(R.id.startScreenListView);
        listView.setAdapter(loggedOutAdapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position; // ListView Clicked item index

                if (!loggedIn) {
                    switch (itemPosition) {
                        case 0:
                            Intent intent = new Intent(view.getContext(), LoginScreen.class);
                            startActivityForResult(intent, LOGIN_REQUEST);
                            break;
                        case 1:
                            intent = new Intent(view.getContext(), ManualEntryScreen.class);
                            startActivity(intent);
                            break;
                    }
                }
                else {
                    switch (itemPosition) {
                        case 0:
                            Intent intent = new Intent(view.getContext(), ManualEntryScreen.class);
                            startActivity(intent);
                            break;
                        case 1:
                            intent = new Intent(view.getContext(), SubredditsSelectionScreen.class);
                            intent.putStringArrayListExtra("Subreddits", subscribedSubreddits);
                            startActivity(intent);
                            break;
                    }
                }
            }
        });

    }

    @Override
    public android.support.v4.app.FragmentManager getSupportFragmentManager() {
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (loggedIn) {
            listView.setAdapter(loggedInAdapter);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (LOGIN_REQUEST) : {
                if (resultCode == Activity.RESULT_OK) {
                    String newText = data.getStringExtra("Reddit Account");
                    loggedIn = true;
                    Log.i(TAG, newText);
                    subscribedSubreddits = data.getStringArrayListExtra("Subreddits");
                    Log.i(TAG, "You are subscribed to this many subreddits: " + subscribedSubreddits.size());
                    listView.setAdapter(loggedInAdapter);
                }
                break;
            }
        }
    }
}
