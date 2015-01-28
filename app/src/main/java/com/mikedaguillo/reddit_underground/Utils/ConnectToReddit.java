package com.mikedaguillo.reddit_underground.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.cd.reddit.Reddit;
import com.cd.reddit.RedditException;
import com.cd.reddit.json.mapping.RedditComment;
import com.cd.reddit.json.mapping.RedditLink;
import com.cd.reddit.json.util.RedditComments;
import com.mikedaguillo.reddit_underground.SubredditDatabaseModel.SubredditsDatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mike on 1/23/2015.
 *
 * Custom AsyncTask to run in the background to grab data from Reddit
 *
 */
public class ConnectToReddit extends AsyncTask<Object, Void, ArrayList<List<RedditLink>>> {

    private Reddit redditInstance;
    private String subreddit;
    private Context screenContext;
    private SubredditsDatabaseHelper database;
    private ArrayList<List<RedditLink>> listofSubreddits; // array list to store the data returned from reddit
    private ArrayList<RedditComments> listofComments; // array list to store the comments for each post
    private ArrayList<Object[]> arrayofThumbnailByteArrays;
    private ArrayList<Object[]> arrayofImageByteArrays;
    private ArrayList<String> checkedSubreddits; // for use when in the subreddits selection screen
    public ProgressDialog dialog;
    private int numofsubreddits;
    public static final String TAG = ConnectToReddit.class.getSimpleName(); //Tag for error messages

    public ConnectToReddit(SubredditsDatabaseHelper database, Context context) {
        redditInstance = new Reddit("RedditUnderground");
        screenContext = context;
        this.database = database;
    }


    public ConnectToReddit(SubredditsDatabaseHelper database, Context context, Reddit reddit) {
        redditInstance = reddit;
        screenContext = context;
        this.database = database;
    }

    public ConnectToReddit(SubredditsDatabaseHelper database, Context context, String subreddit) {
        redditInstance = new Reddit("RedditUnderground");
        screenContext = context;
        this.database = database;
        this.subreddit = subreddit;
        checkedSubreddits = null;
    }

    public ConnectToReddit(SubredditsDatabaseHelper database, Context context, Reddit reddit, ArrayList<String> checked) {
        redditInstance = reddit;
        screenContext = context;
        checkedSubreddits = checked;
        this.database = database;
    }

    @Override
    protected ArrayList<List<RedditLink>> doInBackground(Object... objects) {

        // Arrays to store the information from reddit to be accessed from the other
        // SubredditsSelectionScreen class
        listofSubreddits = new ArrayList<List<RedditLink>>();
        arrayofThumbnailByteArrays = new ArrayList<Object[]>();
        arrayofImageByteArrays = new ArrayList<Object[]>();
        listofComments = new ArrayList<RedditComments>();

        if (checkedSubreddits != null) {
            for (String subreddit : checkedSubreddits) {
                try {
                    // Grab the List<RedditLink> data structure that stores the JSON data for a list of posts in a subreddit
                    List<RedditLink> subRedditListing = redditInstance.listingFor(subreddit, "hot");
                    try {
                        listofSubreddits.add(subRedditListing);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception: " + e);
                    }
                    Log.i(TAG, "Storing subreddits with their data in the array");
                } catch (RedditException e) {
                    Log.e(TAG, "Exception: " + e);
                }
            }
            numofsubreddits = checkedSubreddits.size();
        }
        else {
            try {
                // Grab the List<RedditLink> data structure that stores the JSON data for a list of posts in a subreddit
                List<RedditLink> subRedditListing = redditInstance.listingFor(subreddit, "hot");
                try {
                    listofSubreddits.add(subRedditListing);
                } catch (Exception e) {
                    Log.e(TAG, "Exception: " + e);
                }
                Log.i(TAG, "Storing subreddits with their data in the array");
            } catch (RedditException e) {
                Log.e(TAG, "Exception: " + e);
            }
            numofsubreddits = 1;
        }

        // Iterate through each of the checked subreddits and grab the thumbnail,
        // the image, and the comments for each post
        for (int i = 0; i < numofsubreddits; i++) {

            Object[] thumbnailByteArrays = new Object[26];
            Object[] imageByteArrrays = new Object[26];
            List<RedditLink> posts = listofSubreddits.get(i);

            for (int j = 0; j < posts.size(); j++) {


                // TODO connect to Reddit and retrieve the comments for each post
                // Attempt to grab the comments from Reddit for each post
                // Store the retrieved data in the comments table in the database
                /* String commentString = "http://www.reddit.com/r/" + posts.get(j).getSubreddit() + "/comments/" + posts.get(j).getId() + ".json";
                try {
                    JSONArray commentsJSON = getJsonArray(commentString).getJSONObject(1).getJSONObject("data").getJSONArray("children");
                    Log.i(TAG, "Number of comments retrieved: " + commentsJSON.length());
                    Log.i(TAG, commentsJSON.getJSONObject(0).getJSONObject("data").getString("body"));
                } catch (IOException e) {
                    Log.e(TAG, "IOException getting comments: " + e);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException getting comments: " + e);
                }
                */

                RedditComments theComments;

                // Infinite loop to make sure the comments are returning the proper amount
                try {
                    theComments = redditInstance.commentsFor(posts.get(j).getSubreddit(), posts.get(j).getId(), 5, 1);
                    listofComments.add(theComments);
                } catch (RedditException e) {
                    Log.e(TAG, "An error occurred trying to get the comments" + e);
                }



                byte[] thumbnailBytes = getByteArray(posts.get(j).getThumbnail());
                byte[] imageBytes = null;

                // Checks if image URL is a direct image link or imgur gallery
                // If it's an imgur gallery, need to parse out the direct link to the image
                // and then send to the getByteArray function. All imgur direct links start with
                // i.imgur.com etc so the regex checks for simply the imgur.com domain

                String url = posts.get(j).getUrl();
                Pattern pattern = Pattern.compile("((http://www.imgur.com/)(.+)|(http://imgur.com/)(.+))");
                Matcher matcher = pattern.matcher(url);

                if (matcher.matches()) {

                    String jsonString = "";

                    // imgur links will not load a proper json object unless the word "gallery"
                    // is in the URL. If it is not already in the URL, add the word. If there's
                    // another parameter (such as /a/), it needs to be replaced with gallery

                    if (url.contains("gallery")) {
                        jsonString = url + ".json";
                    }
                    else if (url.contains("/a/")){
                        String[] splitString = url.split("/a/");
                        jsonString = splitString[0] + "/gallery/" + splitString[1] + ".json";
                    }
                    else {
                        jsonString = matcher.group(4) + "gallery/" + matcher.group(5) + ".json";
                    }

                    Log.i(TAG, jsonString);
                    try {
                        // Make a connection to the URL and download the JSON Object
                        JSONObject imgurGalleryJSON = getJsonObject(jsonString);

                        // Albums will have multiple images, in which case you need to check
                        // to see if the JSON Object has the "album_images" object.
                        // Temporarily store just the first image in the album if that's the case
                        JSONObject imageJSON = imgurGalleryJSON.getJSONObject("data").getJSONObject("image");
                        String imageURL = "http://i.imgur.com/" + imageJSON.getString("hash") + imageJSON.getString("ext");
                        if (imageJSON.has("album_images")) {
                            imageJSON = imgurGalleryJSON.getJSONObject("data").getJSONObject("image").getJSONObject("album_images").getJSONArray("images").getJSONObject(0);
                            imageURL = "http://i.imgur.com/" + imageJSON.getString("hash") + imageJSON.getString("ext");
                            Log.i(TAG, "Grabbed first image. URL is: " + imageURL);
                        }

                        imageBytes = getByteArray(imageURL);
                    }
                    catch(Exception e) {
                        Log.e(TAG, "An error occurred while trying to parse the imgur JSON Object: " + e);
                    }
                }
                else {
                    imageBytes = getByteArray(posts.get(j).getUrl());
                }

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

    private JSONObject getJsonObject(String jsonString) throws IOException, JSONException {
        URL address = new URL(jsonString);
        URLConnection connection = address.openConnection();

        InputStream inputStream = connection.getInputStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(bufferedInputStream, "UTF-8"));
        StringBuilder responseStringBuilder = new StringBuilder();
        String inputString;
        while ((inputString = streamReader.readLine()) != null) {
            responseStringBuilder.append(inputString);
        }

        return new JSONObject(responseStringBuilder.toString());
    }

    private JSONArray getJsonArray(String jsonString) throws IOException, JSONException {
        URL address = new URL(jsonString);
        URLConnection connection = address.openConnection();

        InputStream inputStream = connection.getInputStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(bufferedInputStream, "UTF-8"));
        StringBuilder responseStringBuilder = new StringBuilder();
        String inputString;
        while ((inputString = streamReader.readLine()) != null) {
            responseStringBuilder.append(inputString);
        }

        return new JSONArray(responseStringBuilder.toString());
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(screenContext, "", "Please wait while the data downloads", true);
    }

    @Override
    protected void onPostExecute(ArrayList<List<RedditLink>> redditPosts) {
        super.onPostExecute(redditPosts);
        Log.i(TAG, "Executing onPostExecute");
        updateDatabase(redditPosts);
        dialog.dismiss();
    }

    // Converts an image from the RedditLink object into a byte[] that can be stored in the
    // SQLite database. Calculates the size of the image first, then scales it down
    // if necessary. Scaling and compression necessary to reduce memory leaks and heap overloads
    private byte[] getByteArray(String url){
        try {
            URL imageUrl = new URL(url);
            URLConnection ucon = imageUrl.openConnection();

            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.decodeStream(bis, null, options);
            bis.reset();

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 800, 600);

            // Decode bitmap with inSampleSize set to compress image
            options.inJustDecodeBounds = false;
            Bitmap image = BitmapFactory.decodeStream(bis, null, options);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            Log.i(TAG, "Error: " + e.toString());
        }
        return null;
    }

    // Used to determine the sampleSize integar that the BitmapFactory.Options object uses to
    // to help scale the image in the getByteArray function
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    // Called after the async task is completed to update the internal database with
    // the information retrieved from reddit.
    private void updateDatabase(ArrayList<List<RedditLink>> redditLinks) {
        Log.i(TAG, "Executing updateDatabase");
        if (checkedSubreddits != null) {
            for (String subreddits : checkedSubreddits) {
                database.deleteSubreddit(subreddits);
            }
        }
        else {
            database.deleteSubreddit(subreddit);
        }
        Cursor Subreddit;
        Cursor Post;
        Cursor Comment;

        // Iterate through all of the subreddits selected
        for (int i = 0; i < numofsubreddits; i++) {

            long[] subredditIndexes = new long[numofsubreddits];
            // Add the current subreddit to the sqlite subreddit table
            if (checkedSubreddits != null) {
                subredditIndexes[i] = database.addSubreddit(checkedSubreddits.get(i));
            }
            else {
               subredditIndexes[i] = database.addSubreddit(subreddit);
            }

            // Grab the list of posts from the current subreddit
            List<RedditLink> subredditInfo = redditLinks.get(i);

            // Grab the image and thumbnail byte arrays to store into the database.
            // The arrays are object arrays, because if there was no thumbnail or image, a -1
            // placeholder is used instead
            Object[] thumbnailByteArrays = arrayofThumbnailByteArrays.get(i);
            Object[] imageByteArrays = arrayofImageByteArrays.get(i);

            // Iterate through the posts and add them into the posts database table
            // Four scenarios need to be checked, whether the bytearrays have -1 at the position
            // or if one is -1 and the other not, or if they both contain data.
            for (int j = 0; j < subredditInfo.size(); j++) {
                if (thumbnailByteArrays[j] instanceof byte[] && imageByteArrays[j] instanceof  byte[]) {
                    byte[] thumbnailImage = (byte[]) thumbnailByteArrays[j];
                    byte[] imageByteArray = (byte[]) imageByteArrays[j];
                    long rowIndex = database.addPost(subredditInfo.get(j).getTitle(), subredditInfo.get(j).getAuthor(), subredditInfo.get(j).getSubreddit(), subredditInfo.get(j).getNum_comments(), thumbnailImage, imageByteArray, (int) subredditIndexes[i]);
                    int rowIndexInt = (int) rowIndex;

                    // TODO add comments from comment array into the database
                    // Retrieve the comments for each post that have been stored in the comments array
                    int currentPostPosition = ((j) + (26*i));
                    for (RedditComment comment : listofComments.get(currentPostPosition).getComments()) {
                        database.addComment(comment.getAuthor(), comment.getBody(), subredditInfo.get(j).getTitle(), comment.getUps(), comment.getDowns(), rowIndexInt);
                    }
                }
                else if (thumbnailByteArrays[j] instanceof byte[] && !(imageByteArrays[j] instanceof  byte[])) {
                    byte[] thumbnailImage = (byte[]) thumbnailByteArrays[j];
                    long rowIndex = database.addPost(subredditInfo.get(j).getTitle(), subredditInfo.get(j).getAuthor(), subredditInfo.get(j).getSubreddit(), subredditInfo.get(j).getNum_comments(), thumbnailImage, null, (int) subredditIndexes[i]);
                    int rowIndexInt = (int) rowIndex;

                    // TODO add comments from comment array into the database
                    // Retrieve the comments for each post that have been stored in the comments array
                    int currentPostPosition = ((j) + (26*i));
                    for (RedditComment comment : listofComments.get(currentPostPosition).getComments()) {
                        database.addComment(comment.getAuthor(), comment.getBody(), subredditInfo.get(j).getTitle(), comment.getUps(), comment.getDowns(), rowIndexInt);
                    }
                }
                else if (!(thumbnailByteArrays[j] instanceof byte[]) && imageByteArrays[j] instanceof  byte[]) {
                    byte[] imageByteArray = (byte[]) imageByteArrays[j];
                    long rowIndex = database.addPost(subredditInfo.get(j).getTitle(), subredditInfo.get(j).getAuthor(), subredditInfo.get(j).getSubreddit(), subredditInfo.get(j).getNum_comments(), null, imageByteArray, (int) subredditIndexes[i]);
                    int rowIndexInt = (int) rowIndex;

                    // TODO add comments from comment array into the database
                    // Retrieve the comments for each post that have been stored in the comments array
                    int currentPostPosition = ((j) + (26*i));
                    for (RedditComment comment : listofComments.get(currentPostPosition).getComments()) {
                        database.addComment(comment.getAuthor(), comment.getBody(), subredditInfo.get(j).getTitle(), comment.getUps(), comment.getDowns(), rowIndexInt);
                    }
                }
                else {
                    long rowIndex = database.addPost(subredditInfo.get(j).getTitle(), subredditInfo.get(j).getAuthor(), subredditInfo.get(j).getSubreddit(), subredditInfo.get(j).getNum_comments(), null, null, (int) subredditIndexes[i]);
                    int rowIndexInt = (int) rowIndex;

                    // TODO add comments from comment array into the database
                    // Retrieve the comments for each post that have been stored in the comments array
                    int currentPostPosition = ((j) + (26*i));
                    for (RedditComment comment : listofComments.get(currentPostPosition).getComments()) {
                        database.addComment(comment.getAuthor(), comment.getBody(), subredditInfo.get(j).getTitle(), comment.getUps(), comment.getDowns(), rowIndexInt);
                    }
                }

            }

            // Chunk of code to print values from the database to make sure information is
            // Storing properly.
            Subreddit = database.getSubreddits();
            Post = database.getPosts((int) subredditIndexes[i]);
            Subreddit.moveToPosition(i);
            Post.moveToFirst();
            Log.i(TAG, "Added the subreddit: " + Subreddit.getString(1) + " to the sqlite database, at position " + Subreddit.getString(0));


            // TODO check to see if comments are storing properly by cycling through and printing them
            while (!Post.isAfterLast()) {
                Log.i(TAG, Post.getString(1));
                int postPosition = Post.getInt(0);
                Comment = database.getComments(Post.getInt(0));
                Comment.moveToFirst();
                while (!Comment.isAfterLast()) {
                    int commentPosition = Comment.getInt(0);
                    Log.i(TAG, Comment.getString(2));
                    Comment.moveToNext();
                }
                Post.moveToNext();
            }

        }

        database.close();
    }

}