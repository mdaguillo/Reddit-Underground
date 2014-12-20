package com.mikedaguillo.reddit_underground;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cd.reddit.Reddit;
import com.cd.reddit.RedditException;
import com.cd.reddit.json.mapping.RedditSubreddit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mike on 12/10/2014.
 * Activity to login to a reddit account
 */

public class LoginScreen extends ActionBarActivity {
    public static final String TAG = LoginScreen.class.getSimpleName(); //Tag for error messages
    private EditText usernameInput; // edittext area for the username
    private EditText passwordInput; // edittext area for the password
    private Button loginButton; // button login to a reddit account
    private LoginTextWatcher textWatcher; // text watcher for the login inputs
    private Reddit redditInstance; // Reddit object
    private ProgressBar mProgressBar; // create the progress bar to display while the login attempt occurs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        // register activity elements to IDs and listeners
        loginButton = (Button) findViewById(R.id.loginButton);
        usernameInput = (EditText) findViewById(R.id.usernameEditText);
        passwordInput = (EditText) findViewById(R.id.passwordEditText);
        textWatcher = new LoginTextWatcher();
        usernameInput.addTextChangedListener(textWatcher);
        passwordInput.addTextChangedListener(textWatcher);
        LoginButtonListener loginListener = new LoginButtonListener();
        loginButton.setOnClickListener(loginListener);
        mProgressBar = (ProgressBar) findViewById(R.id.loginProgressBar);
        redditInstance = new Reddit("RedditUnderground");

        loginButton.setEnabled(false);
        mProgressBar.setVisibility(View.INVISIBLE);
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

        String s1 = usernameInput.getText().toString();
        String s2 = passwordInput.getText().toString();

        if (s1.length() > 0 && s2.length() > 0) {
            loginButton.setEnabled(true);
        } else {
            loginButton.setEnabled(false);
        }

    }

    private class LoginButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            mProgressBar.setVisibility(view.VISIBLE);
            LoginToReddit login = new LoginToReddit(usernameInput.getText().toString(), passwordInput.getText().toString());
            login.execute();
        }
    }

    // AsyncTask to Login
    private class LoginToReddit extends AsyncTask<Object, Void, Void> {

        private String username;
        private String password;

        public LoginToReddit(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Void doInBackground(Object... objects) {
            // login to a reddit account
            try {
                redditInstance.login( username, password );
                List<RedditSubreddit> subreddits = redditInstance.subreddits("mine/subscriber", 100);
                ArrayList<String> subredditTitles = new ArrayList<String>();
                for (RedditSubreddit subreddit : subreddits) {
                    subredditTitles.add(subreddit.getSubredditName());
                    Log.i(TAG, subreddit.getSubredditName());
                }

                Intent resultIntent = new Intent();
                resultIntent.putExtra("Reddit Account", "BetaRhoOmega");
                resultIntent.putStringArrayListExtra("Subreddits", subredditTitles);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

            } catch (RedditException e) {
                Toast toast = Toast.makeText(LoginScreen.this, "Login Failed: Please Try Again", Toast.LENGTH_LONG);
                toast.show();
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
