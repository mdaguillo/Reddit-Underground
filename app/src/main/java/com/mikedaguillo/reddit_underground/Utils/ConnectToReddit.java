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
public class ConnectToReddit extends AsyncTask<Object, ProgressUpdates, RedditResults> {

    private Reddit redditInstance;
    private String subreddit;
    private Context screenContext;
    private SubredditsDatabaseHelper database;
    private ArrayList<String> checkedSubreddits; // for use when in the subreddits selection screen
    public static final String TAG = ConnectToReddit.class.getSimpleName(); //Tag for error messages
    public ProgressDialog downloadDialog;
    int totalProgressSteps;
    int progressStepsProcessed;

    public ConnectToReddit(SubredditsDatabaseHelper database, Context context, String subreddit) {
        redditInstance = new Reddit("RedditUnderground");
        screenContext = context;
        this.database = database;
        this.subreddit = subreddit;
        checkedSubreddits = null;
        downloadDialog = new ProgressDialog(screenContext);
    }

    public ConnectToReddit(SubredditsDatabaseHelper database, Context context, Reddit reddit, ArrayList<String> checked) {
        redditInstance = reddit;
        screenContext = context;
        checkedSubreddits = checked;
        this.database = database;
        downloadDialog = new ProgressDialog(screenContext);
    }

    @Override
    protected void onProgressUpdate(ProgressUpdates... updates) {
        //super.onProgressUpdate();
        switch (updates[0].dialog) {
            case Download:
                downloadDialog.setMessage(updates[0].progressMessage);
                downloadDialog.setProgress(updates[0].progressPercentage);
                break;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        initiateProgressDialog(downloadDialog, "Downloading", "Initiating data download.");
        downloadDialog.show();
    }

    @Override
    protected RedditResults doInBackground(Object... objects) {
        ArrayList<List<RedditLink>> listofSubredditContent = new ArrayList<List<RedditLink>>();
        ArrayList<Object[]> arrayofThumbnailByteArrays = new ArrayList<Object[]>();
        ArrayList<Object[]> arrayofImageByteArrays = new ArrayList<Object[]>();
        ArrayList<RedditComments> listofComments = new ArrayList<RedditComments>();
        ProgressUpdates updates = new ProgressUpdates();

        updates.dialog = ProgressUpdates.Dialog.Download;
        updates.progressMessage = "Retrieving subreddit listings from reddit.";
        publishProgress(updates);

        // First retrieve the top 25 posts listed for each subreddit and add the resulting List<RedditLink> (the content) to the listofSubredditContent array
        int numOfSubreddits;
        if (checkedSubreddits != null) {
            for (String subreddit : checkedSubreddits) {
                retrieveListingForSubreddit(subreddit, listofSubredditContent);
            }
            numOfSubreddits = listofSubredditContent.size();
        }
        else { // Manual download mode
            retrieveListingForSubreddit(subreddit, listofSubredditContent);
            numOfSubreddits = 1;
        }

        totalProgressSteps = numOfSubreddits * 25 * 2;
        progressStepsProcessed = 0;

        // Iterate through each of the checked subreddits and grab the thumbnail, the image, and the comments for each post
        for (int i = 0; i < numOfSubreddits; i++) {
            Object[] thumbnailByteArrays = new Object[25];
            Object[] imageByteArrrays = new Object[25];
            List<RedditLink> subredditPage = listofSubredditContent.get(i);
            String subredditName = subredditPage.get(0).getSubreddit();

            for (int j = 0; j < 25; j++) {
                progressStepsProcessed++;
                updates.progressPercentage = calculateProgressPercentage(progressStepsProcessed, totalProgressSteps);
                updates.progressMessage = "Processing post " + (j+1) + " in /r/" + subredditName + ".";
                publishProgress(updates);

                try {
                    RedditComments theComments = redditInstance.commentsFor(subredditPage.get(j).getSubreddit(), subredditPage.get(j).getId(), 5, 1);
                    listofComments.add(theComments);
                } catch (RedditException e) {
                    Log.e(TAG, "An error occurred trying to get the comments" + e);
                    // Add to the listofComments so we don't get an indexOutOfBounds Error later while processing
                    listofComments.add(null);
                }

                byte[] thumbnailBytes = getByteArray(subredditPage.get(j).getThumbnail());
                byte[] imageBytes = null;

                // Checks if image URL is a direct image link or imgur gallery
                // If it's an imgur gallery, need to parse out the direct link to the image
                // and then send to the getByteArray function. All imgur direct links start with
                // i.imgur.com etc so the regex checks for simply the imgur.com domain

                String url = subredditPage.get(j).getUrl();
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
                    imageBytes = getByteArray(subredditPage.get(j).getUrl());
                }

                thumbnailByteArrays[j] = (thumbnailBytes != null ? thumbnailBytes : -1 );
                imageByteArrrays[j] = (imageBytes != null ? imageBytes : -1);
            }

            arrayofThumbnailByteArrays.add(thumbnailByteArrays);
            arrayofImageByteArrays.add(imageByteArrrays);
        }

        RedditResults results = new RedditResults();
        results.numOfSubreddits = numOfSubreddits;
        results.redditLinks = listofSubredditContent;
        results.images = arrayofImageByteArrays;
        results.thumbnails = arrayofThumbnailByteArrays;
        results.comments = listofComments;

        updateDatabase(results);

        return results;
    }

    @Override
    protected void onPostExecute(RedditResults redditPosts) {
        super.onPostExecute(redditPosts);
        Log.i(TAG, "Executing onPostExecute");
        //updateDatabase(redditPosts);
        downloadDialog.dismiss();
    }

    private int calculateProgressPercentage(int currentStep, int totalSteps)
    {
        int result = (int)(((double)currentStep/totalSteps)*100);
        return result;
    }

    private void retrieveListingForSubreddit(String subreddit, ArrayList<List<RedditLink>> subredditContent) {
        Log.i(TAG, String.format("Storing subreddit: %s with its content in the listofSubredditContent array", subreddit));
        try {
            // Grab the List<RedditLink> data structure that stores the JSON data for a list of posts in a subreddit
            List<RedditLink> subRedditListing = redditInstance.listingFor(subreddit, "hot");
            subredditContent.add(subRedditListing);
        } catch (RedditException e) {
            Log.e(TAG, String.format("Exception occurred while retrieving the top 25 posts from the subreddit: %s. Exception: %s", subreddit, e.toString()));
        }
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

    private void initiateProgressDialog(ProgressDialog dialog, String title, String message) {
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle(title);
        dialog.setMessage(message);
    }

    // Called after the async task is completed to update the internal database with
    // the information retrieved from reddit.
    private void updateDatabase(RedditResults resultsFromReddit) {
        Log.i(TAG, "Executing updateDatabase");

        ProgressUpdates databaseUpdates = new ProgressUpdates();
        databaseUpdates.dialog = ProgressUpdates.Dialog.Download;
        databaseUpdates.progressPercentage = progressStepsProcessed;
        databaseUpdates.progressMessage = "Deleting any old data currently in the database.";
        publishProgress(databaseUpdates);

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
        for (int i = 0; i < resultsFromReddit.numOfSubreddits; i++) {
            long[] subredditIndexes = new long[resultsFromReddit.numOfSubreddits];

            // Add the current subreddit to the sqlite subreddit table
            if (checkedSubreddits != null) {
                subredditIndexes[i] = database.addSubreddit(checkedSubreddits.get(i));
            }
            else {
                subredditIndexes[i] = database.addSubreddit(subreddit);
            }

            // Grab the list of posts from the current subreddit
            List<RedditLink> subredditPage = resultsFromReddit.redditLinks.get(i);
            String subredditName = subredditPage.get(0).getSubreddit();

            // Grab the image and thumbnail byte arrays to store into the database.
            // The arrays are object arrays, because if there was no thumbnail or image, a -1 placeholder is used instead
            Object[] thumbnailByteArrays = resultsFromReddit.thumbnails.get(i);
            Object[] imageByteArrays = resultsFromReddit.images.get(i);

            // Iterate through the posts and add them into the posts database table
            // Four scenarios need to be checked, whether the bytearrays are both -1, if one is -1 and the other not, or if they both contain data.

            for (int j = 0; j < 25; j++) {
                progressStepsProcessed++;
                databaseUpdates.progressPercentage = calculateProgressPercentage(progressStepsProcessed, totalProgressSteps);
                databaseUpdates.progressMessage = "Storing post " + (j+1) + " data from /r/" + subredditName + " in the database.";
                publishProgress(databaseUpdates);

                if (thumbnailByteArrays[j] instanceof byte[] && imageByteArrays[j] instanceof  byte[]) {
                    byte[] thumbnailImage = (byte[]) thumbnailByteArrays[j];
                    byte[] imageByteArray = (byte[]) imageByteArrays[j];
                    long rowIndex = database.addPost(subredditPage.get(j).getTitle(), subredditPage.get(j).getAuthor(), subredditPage.get(j).getSubreddit(), subredditPage.get(j).getNum_comments(), thumbnailImage, imageByteArray, (int) subredditIndexes[i]);
                    int rowIndexInt = (int) rowIndex;

                    processComments(resultsFromReddit, subredditPage, i, j, rowIndexInt);
                }
                else if (thumbnailByteArrays[j] instanceof byte[] && !(imageByteArrays[j] instanceof  byte[])) {
                    byte[] thumbnailImage = (byte[]) thumbnailByteArrays[j];
                    long rowIndex = database.addPost(subredditPage.get(j).getTitle(), subredditPage.get(j).getAuthor(), subredditPage.get(j).getSubreddit(), subredditPage.get(j).getNum_comments(), thumbnailImage, null, (int) subredditIndexes[i]);
                    int rowIndexInt = (int) rowIndex;

                    // Retrieve the comments for each post that have been stored in the comments array
                    processComments(resultsFromReddit, subredditPage, i, j, rowIndexInt);
                }
                else if (!(thumbnailByteArrays[j] instanceof byte[]) && imageByteArrays[j] instanceof  byte[]) {
                    byte[] imageByteArray = (byte[]) imageByteArrays[j];
                    long rowIndex = database.addPost(subredditPage.get(j).getTitle(), subredditPage.get(j).getAuthor(), subredditPage.get(j).getSubreddit(), subredditPage.get(j).getNum_comments(), null, imageByteArray, (int) subredditIndexes[i]);
                    int rowIndexInt = (int) rowIndex;

                    // Retrieve the comments for each post that have been stored in the comments array
                    processComments(resultsFromReddit, subredditPage, i, j, rowIndexInt);
                }
                else {
                    long rowIndex = database.addPost(subredditPage.get(j).getTitle(), subredditPage.get(j).getAuthor(), subredditPage.get(j).getSubreddit(), subredditPage.get(j).getNum_comments(), null, null, (int) subredditIndexes[i]);
                    int rowIndexInt = (int) rowIndex;

                    // Retrieve the comments for each post that have been stored in the comments array
                    processComments(resultsFromReddit, subredditPage, i, j, rowIndexInt);
                }
            }
        }

        database.close();
    }

    private void processComments(RedditResults resultsFromReddit, List<RedditLink> subredditPage, int subredditBeingProcessed, int postBeingProcessed, int rowIndexInt) {
        // Retrieve the comments for each post that have been stored in the comments array
        int currentPostPosition = ((postBeingProcessed) + (25*subredditBeingProcessed));
        for (RedditComment comment : resultsFromReddit.comments.get(currentPostPosition).getComments()) {
            if (comment != null)
                database.addComment(comment.getAuthor(), comment.getBody(), subredditPage.get(postBeingProcessed).getTitle(), comment.getUps(), comment.getDowns(), rowIndexInt);
        }
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
}

// ProgressUpdate helper class to pass information to the UI thread
class ProgressUpdates {
    public enum Dialog {Download, Storing}

    public Dialog dialog;
    public int progressPercentage = 0;
    public String progressMessage;
}

// Encapsulates the data processed from Reddit
class RedditResults {
    public int numOfSubreddits;
    public ArrayList<List<RedditLink>> redditLinks;
    public ArrayList<Object[]> thumbnails;
    public ArrayList<Object[]> images;
    public ArrayList<RedditComments> comments;
}