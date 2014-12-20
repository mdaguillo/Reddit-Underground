package com.mikedaguillo.reddit_underground;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mike on 12/10/2014.
 */
public class SubredditsSelectionScreen extends ActionBarActivity {

    private ListView subredditsListView;
    private Button cacheButton;
    private ArrayList<String> subreddits;
    private ArrayList<String> checkedSubreddits;
    private ArrayList<List<RedditLink>> listofSubreddits; // array list to store the data returned from reddit
    private ArrayList<Object[]> arrayofThumbnailByteArrays;
    private ArrayList<Object[]> arrayofImageByteArrays;
    public static final String TAG = SubredditsSelectionScreen.class.getSimpleName(); //Tag for error messages
    public ProgressDialog dialog;

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
        CacheButtonListener cacheListener = new CacheButtonListener();
        cacheButton.setOnClickListener(cacheListener);
        cacheButton.setEnabled(false);

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

            Object[] thumbnailByteArrays = arrayofThumbnailByteArrays.get(i);
            Object[] imageByteArrays = arrayofImageByteArrays.get(i);

            for (int j = 0; j < subredditInfo.size(); j++) {
                if (thumbnailByteArrays[j] instanceof byte[] && imageByteArrays[j] instanceof  byte[]) {
                    byte[] thumbnailImage = (byte[]) thumbnailByteArrays[j];
                    byte[] imageByteArray = (byte[]) imageByteArrays[j];
                    database.addPost(subredditInfo.get(j).getTitle(), subredditInfo.get(j).getAuthor(), subredditInfo.get(j).getSubreddit(), subredditInfo.get(j).getNum_comments(), thumbnailImage, imageByteArray, i);
                }
                else if (thumbnailByteArrays[j] instanceof byte[] && !(imageByteArrays[j] instanceof  byte[])) {
                    byte[] thumbnailImage = (byte[]) thumbnailByteArrays[j];
                    database.addPost(subredditInfo.get(j).getTitle(), subredditInfo.get(j).getAuthor(), subredditInfo.get(j).getSubreddit(), subredditInfo.get(j).getNum_comments(), thumbnailImage, null, i);
                }
                else if (!(thumbnailByteArrays[j] instanceof byte[]) && imageByteArrays[j] instanceof  byte[]) {
                    byte[] imageByteArray = (byte[]) imageByteArrays[j];
                    database.addPost(subredditInfo.get(j).getTitle(), subredditInfo.get(j).getAuthor(), subredditInfo.get(j).getSubreddit(), subredditInfo.get(j).getNum_comments(), null, imageByteArray, i);
                }
                else {
                    database.addPost(subredditInfo.get(j).getTitle(), subredditInfo.get(j).getAuthor(), subredditInfo.get(j).getSubreddit(), subredditInfo.get(j).getNum_comments(), null, null, i);
                }
            }

            Subreddit = database.getSubreddits();
            Post = database.getPosts(i);
            Subreddit.moveToPosition(i);
            Post.moveToFirst();
            Log.i(TAG, "Added the subreddit: " + Subreddit.getString(1) + " to the sqlite database, at position " + Subreddit.getString(0));
            while (!Post.isAfterLast()) {
                Log.i(TAG, Post.getString(3));
                Post.moveToNext();
            }
        }

        dialog.dismiss();
        database.close();
    }


    private class CacheButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            dialog = ProgressDialog.show(SubredditsSelectionScreen.this, "", "Please wait while the data downloads", true);
            ConnectToReddit connection = new ConnectToReddit();
            connection.execute();
        }
    }

    //custom AsyncTask to run in the background to grab data from Reddit
    private class ConnectToReddit extends AsyncTask<Object, Void, ArrayList<List<RedditLink>>> {

        @Override
        protected ArrayList<List<RedditLink>> doInBackground(Object... objects) {

           listofSubreddits = new ArrayList<List<RedditLink>>();
           arrayofThumbnailByteArrays = new ArrayList<Object[]>();
           arrayofImageByteArrays = new ArrayList<Object[]>();

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

            for (int i = 0; i < checkedSubreddits.size(); i++) {

                Object[] thumbnailByteArrays = new Object[26];
                Object[] imageByteArrrays = new Object[26];
                List<RedditLink> posts = listofSubreddits.get(i);

                for (int j = 0; j < listofSubreddits.get(i).size(); j++) {

                    byte[] thumbnailBytes = getByteArray(posts.get(j).getThumbnail());
                    byte[] imageBytes = getByteArray(posts.get(j).getUrl());

                    if (thumbnailBytes != null) {
                        thumbnailByteArrays[j] = thumbnailBytes;
                    }
                    else {
                        thumbnailByteArrays[j] = -1;
                    }

                    if (imageBytes != null) {
                        imageByteArrrays[j] = imageBytes;
                    }
                    else {
                        imageByteArrrays[j] = -1;
                    }
                }

                arrayofThumbnailByteArrays.add(thumbnailByteArrays);
                arrayofImageByteArrays.add(imageByteArrrays);
            }

            return listofSubreddits;
        }

        @Override
        protected void onPostExecute(ArrayList<List<RedditLink>> redditPosts) {
            Log.i(TAG, "Executing onPostExecute");
            updateDatabase(redditPosts);
        }

    }

    // Converts an image from the RedditLink object into a byte[] that can be stored in the
    // SQLite database
    private byte[] getByteArray(String url){
        try {
            URL imageUrl = new URL(url);
            URLConnection ucon = imageUrl.openConnection();

            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            Bitmap image = BitmapFactory.decodeStream(bis);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);

            /*ByteArrayBuffer baf = new ByteArrayBuffer(500);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }*/

            return outputStream.toByteArray();
        } catch (Exception e) {
            Log.i(TAG, "Error: " + e.toString());
        }
        return null;
    }
}


