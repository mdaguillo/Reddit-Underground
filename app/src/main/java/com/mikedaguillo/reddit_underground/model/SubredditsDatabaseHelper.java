package com.mikedaguillo.reddit_underground.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Mike on 12/11/2014.
 *
 * This class helps open, create, and upgrade the database file containing the
 * subreddits and their posts counters.
 */
public class SubredditsDatabaseHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    // The name of the database file on the file system
    public static final String DATABASE_NAME = "Subreddits.db";

    public SubredditsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create the database to contain the data for the projects
        sqLiteDatabase.execSQL(SubredditContract.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(PostsContract.SQL_CREATE_TABLE);
    }

    /**
     * Create a new subreddit in the database with its posts.
     *
     * @param subredditName the name of the subreddit to create.
     * @param postsAmount the amount of posts to store for the subreddit.
     * @param JSONData the JSON Data for each post for the subreddit.
     */
    public void createSubreddit(String subredditName, int postsAmount, String JSONData) {
        SQLiteDatabase db = getWritableDatabase();

        // Create the database row for the project and keep its unique identifier
        ContentValues projectValues = new ContentValues();
        projectValues.put(SubredditContract.SubredditEntry.COLUMN_NAME_TITLE, subredditName);
        long subredditId;
        subredditId = db.insert(SubredditContract.TABLE_NAME, null, projectValues);
        // Insert the database rows for the row counters of the project in the database
        for (int i=0; i < postsAmount; i++) {
            ContentValues postValues = new ContentValues();
            postValues.put(PostsContract.PostsEntry.COLUMN_NAME_SUBREDDIT_ID, subredditId);
            postValues.put(PostsContract.PostsEntry.COLUMN_NAME_JSON_DATA, JSONData);
            db.insert(SubredditContract.TABLE_NAME, null, postValues);
        }
    }

    /**
     * Deletes the specified post from the database.
     *
     * @param post is the post to remove.
     */
    public void deletePost(Post post) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(PostsContract.TABLE_NAME,
                PostsContract.PostsEntry._ID +"=?",
                new String[] { String.valueOf(post.getId()) });
    }

    public Subreddit getSubreddit(long subredditId) {
        // Gets the database in the current database helper in read-only mode
        SQLiteDatabase db = getReadableDatabase();

        // After the query, the cursor points to the first database row
        // returned by the request
        Cursor subCursor = db.query(SubredditContract.TABLE_NAME, null, SubredditContract.SubredditEntry._ID + "=?", new String[] { String.valueOf(subredditId) }, null, null, null);
        subCursor.moveToNext();

        // Get the value for each column for the database row pointed by
        // the cursor using the getColumnIndex method of the cursor and
        // use it to initialize a Subreddit object by database row
        Subreddit subreddit = new Subreddit();
        subreddit.setId(subCursor.getLong(subCursor.getColumnIndex(SubredditContract.SubredditEntry._ID)));
        subreddit.setName(subCursor.getString(subCursor.getColumnIndex(SubredditContract.SubredditEntry.COLUMN_NAME_TITLE)));

        // Get all the posts for the current subreddit from the
        // database and add them all to the Subreddit object
        subreddit.setPosts(getPosts(subredditId));

        subCursor.close();

        return subreddit;
    }

    /**
     * Gets the list of stored subreddits from the database.
     *
     * @return the current stored subreddits from the database.
     */
    public ArrayList<Subreddit> getSubreddits()
    {
        ArrayList<Subreddit> subreddits = new ArrayList<Subreddit>();

        // Gets the database in the current database helper in read-only mode
        SQLiteDatabase db = getReadableDatabase();

        // After the query, the cursor points to the first database row
        // returned by the request.
        Cursor subCursor = db.query(SubredditContract.TABLE_NAME, null, null, null, null, null, null);
        while (subCursor.moveToNext()) {
            // Get the value for each column for the database row pointed by
            // the cursor using the getColumnIndex method of the cursor and
            // use it to initialize a Subreddit object by database row
            Subreddit subreddit = new Subreddit();
            long subredditId = subCursor.getLong(subCursor.getColumnIndex(SubredditContract.SubredditEntry._ID));
            subreddit.setId(subCursor.getLong(subCursor.getColumnIndex(SubredditContract.SubredditEntry._ID)));
            subreddit.setName(subCursor.getString(subCursor.getColumnIndex(SubredditContract.SubredditEntry.COLUMN_NAME_TITLE)));

            // Get all the porst for the current subreddit from the
            // database and add them all to the Subreddit object
            subreddit.setPosts(getPosts(subredditId));

            subreddits.add(subreddit);
        }

        subCursor.close();

        return subreddits;
    }

    private ArrayList<Post> getPosts(long subredditId)
    {
        ArrayList<Post> posts = new ArrayList<Post>();

        // Gets the database in the current database helper in read-only mode
        SQLiteDatabase db = getReadableDatabase();

        Cursor countCursor = db.query(PostsContract.TABLE_NAME,
                null,
                PostsContract.PostsEntry.COLUMN_NAME_SUBREDDIT_ID + "=?",
                new String[]{ String.valueOf(subredditId) },
                null,
                null,
                PostsContract.PostsEntry._ID);

        // After the query, the cursor points to the first database row
        // returned by the request.

        while (countCursor.moveToNext()) {
            // Get the value for each column for the database row pointed by
            // the cursor using the getColumnIndex method of the cursor and
            // use it to initialize a Post object by database row

            Post post = new Post();
            post.setId(countCursor.getLong(countCursor.getColumnIndex(PostsContract.PostsEntry._ID)));

            String JSONData = countCursor.getString(countCursor.getColumnIndex(PostsContract.PostsEntry.COLUMN_NAME_JSON_DATA));
            post.setJSONData(JSONData);

            posts.add(post);
        }

        countCursor.close();

        return posts;
    }


    /**
     *
     * This method must be implemented if your application is upgraded and must
     * include the SQL query to upgrade the database from your old to your new
     * schema.
     *
     * @param sqLiteDatabase the database being upgraded.
     * @param oldVersion the current version of the database before the upgrade.
     * @param newVersion the version of the database after the upgrade.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Logs that the database is being upgraded
        Log.i(SubredditsDatabaseHelper.class.getSimpleName(),
                "Upgrading database from version " + oldVersion + " to " + newVersion);
    }
}
