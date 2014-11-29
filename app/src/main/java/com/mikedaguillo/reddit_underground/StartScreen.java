package com.mikedaguillo.reddit_underground;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cd.reddit.Reddit;
import com.cd.reddit.RedditException;
import com.cd.reddit.json.mapping.RedditAccount;
import com.cd.reddit.json.mapping.RedditJsonMessage;
import com.cd.reddit.json.mapping.RedditSubreddit;

import java.util.List;

public class StartScreen extends ActionBarActivity {

    private String subreddit; // holds the subreddit to retrieve JSON data from
    public static final String TAG = StartScreen.class.getSimpleName(); //Tag for error messages

    private Button cacheButton; // button to store local subreddit data
    private Button displayButton; // button to display the subreddit options
    private Button loginButton; // button login to a reddit account
    private EditText subRedditInput; // edittext area for the user to input their subreddit of choice
    private EditText usernameInput; // edittext area for the username
    private EditText passwordInput; // edittext area for the password
    private LoginTextWatcher textWatcher; // text watcher for the login inputs
    private Reddit redditInstance; // Reddit object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        // register page elements to IDs and listeners to buttons
        displayButton = (Button) findViewById(R.id.displaybutton);
        cacheButton = (Button) findViewById(R.id.cachebutton);
        loginButton = (Button) findViewById(R.id.loginButton);
        subRedditInput = (EditText) findViewById(R.id.enterSubredditText);
        usernameInput = (EditText) findViewById(R.id.usernameEditText);
        passwordInput = (EditText) findViewById(R.id.passwordEditText);
        textWatcher = new LoginTextWatcher();
        usernameInput.addTextChangedListener(textWatcher);
        passwordInput.addTextChangedListener(textWatcher);
        CacheButtonListener cacheListener = new CacheButtonListener();
        DisplayButtonListener displayListener = new DisplayButtonListener();
        LoginButtonListener loginListener = new LoginButtonListener();
        cacheButton.setOnClickListener(cacheListener);
        displayButton.setOnClickListener(displayListener);
        loginButton.setOnClickListener(loginListener);
        redditInstance = new Reddit("RedditUnderground");

        // disable the display button until subreddit data has been cached
        displayButton.setEnabled(false);
        //loginButton.setEnabled(false);
    }

    @Override
    public android.support.v4.app.FragmentManager getSupportFragmentManager() {
        return null;
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

    private class LoginButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            LoginToReddit login = new LoginToReddit();
            login.execute();
        }
    }

    //method that constructs the string for the subreddit variable to be passed to the display activity
    private void setSubRedditURL(String url) {
        subreddit = url;
        Log.i(TAG, "The subreddit variable is set to: " + subreddit);
    }

    // TextWatcher to watch if text is entered into the edittext fields
    private class LoginTextWatcher implements TextWatcher {
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

    // method to check if there's any text in the login and password fields
    private  void checkFieldsForEmptyValues(){
        Button b = (Button) findViewById(R.id.loginButton);

        String s1 = usernameInput.getText().toString();
        String s2 = passwordInput.getText().toString();

        if (s1.length() > 0 && s2.length() > 0) {
            b.setEnabled(true);
        } else {
            b.setEnabled(false);
        }

    }

    // AsyncTask to Login
    private class LoginToReddit extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... objects) {
            // login to a reddit account
            try {
                redditInstance.login( "BetaRhoOmega", "Coffeeiphone1" );
                List<RedditSubreddit> subreddits = redditInstance.subreddits("mine/subscriber", 100);
                for (RedditSubreddit subreddit : subreddits) {
                    Log.i(TAG, subreddit.getSubredditName());
                }

            } catch (RedditException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
