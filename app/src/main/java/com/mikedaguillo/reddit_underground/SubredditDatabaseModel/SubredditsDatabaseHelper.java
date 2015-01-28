package com.mikedaguillo.reddit_underground.SubredditDatabaseModel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Blob;
import java.util.ArrayList;

/**
 * Created by Mike on 12/14/2014.
 *
 * The DatabaseHelper for the stored subreddits database
 *
 */
public class SubredditsDatabaseHelper  extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "StoredSubredditsDatabase";
    private static final String SUBREDDIT_TABLE_NAME = "Subreddits";
    private static final String POSTS_TABLE_NAME = "Posts";
    private static final String COMMENTS_TABLE_NAME = "Comments";

    public SubredditsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // sql string command to create a new table called subreddits with an id column and a name column
        sqLiteDatabase.execSQL("CREATE TABLE " + SUBREDDIT_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT);");
        sqLiteDatabase.execSQL("CREATE TABLE " + POSTS_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, author TEXT, subreddit TEXT, numComments INTEGER, thumbnailBlob BLOB, imageBlob BLOB, subredditID INTEGER, FOREIGN KEY(subredditID) REFERENCES " + SUBREDDIT_TABLE_NAME + "(_id));");
        sqLiteDatabase.execSQL("CREATE TABLE " + COMMENTS_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, author TEXT, body TEXT, postTitle TEXT, ups INTEGER, downs INTEGER, postID INTEGER, FOREIGN KEY(postID) REFERENCES " + POSTS_TABLE_NAME + "(_id));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SUBREDDIT_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + POSTS_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + COMMENTS_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public long addSubreddit(String name) {
        // place the name of the subreddit you want to add into a ContentValues object
        ContentValues values = new ContentValues(1);
        values.put("name", name);

        // call getWritableDatabase and insert in the "Subreddits" table, under the "name" column the
        // values in the ContentValues object
        
        return getWritableDatabase().insert(SUBREDDIT_TABLE_NAME, "name", values);
    }

    public long addPost(String title, String author, String subreddit, int numComments, byte[] thumbnail, byte[] image, int subredditID) {
        ContentValues values = new ContentValues(7);
        values.put("title", title);
        values.put("author", author);
        values.put("subreddit", subreddit);
        values.put("numComments", numComments);
        values.put("thumbnailBlob", thumbnail);
        values.put("imageBlob", image);
        values.put("subredditID", subredditID);

        return getWritableDatabase().insert(POSTS_TABLE_NAME, "title", values);
    }

    public long addComment (String author, String body, String postTitle, int ups, int downs, int postID) {
        ContentValues values = new ContentValues(6);
        values.put("author", author);
        values.put("body", body);
        values.put("postTitle", postTitle);
        values.put("ups", ups);
        values.put("downs", downs);
        values.put("postID", postID);

        return getWritableDatabase().insert(COMMENTS_TABLE_NAME, "author", values);
    }

    public Cursor getSubreddits() {
        Cursor cursor = getReadableDatabase().rawQuery("select * from " + SUBREDDIT_TABLE_NAME, null);
        return cursor;
    }

    public Cursor getPosts(int subredditID) {
        Cursor cursor = getReadableDatabase().rawQuery("select * from " + POSTS_TABLE_NAME + " where subredditID = " + subredditID, null);
        return cursor;
    }

    public Cursor getPosts(String subredditName) {
        Cursor cursor = getReadableDatabase().rawQuery("select * from " + POSTS_TABLE_NAME + " where subreddit = \"" + subredditName + "\"", null);
        return cursor;
    }

    public Cursor getComments(int postID) {
        Cursor cursor = getReadableDatabase().rawQuery("select * from " + COMMENTS_TABLE_NAME + " where postID = " + postID, null);
        return cursor;
    }

    public Cursor getComments(String postTitle) {
        Cursor cursor = getReadableDatabase().rawQuery("select * from " + COMMENTS_TABLE_NAME + " where postTitle = \"" + postTitle + "\"", null);
        return cursor;
    }

    public void deleteAll() {
        getWritableDatabase().delete(SUBREDDIT_TABLE_NAME, null, null);
        getWritableDatabase().delete(POSTS_TABLE_NAME, null, null);
        getWritableDatabase().delete(COMMENTS_TABLE_NAME, null, null);
    }

    public void deleteSubreddit(String subreddit) {
        try{
            String[] sub = new String[] {subreddit};
            getWritableDatabase().delete(SUBREDDIT_TABLE_NAME, "name = ?", sub);

            // Necessary to delete the comments that are tied to each post
            // Collects all of the Posts id's in the Posts tables, which is the foreign key
            // In the comments table that ties a comment to a post.
            Cursor cursor = getReadableDatabase().rawQuery("select _id from " + POSTS_TABLE_NAME + " where subreddit = \"" + subreddit + "\"", null);
            ArrayList<String> postIDs = new ArrayList<String>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                postIDs.add(Integer.toString(cursor.getInt(0)));
            }
            String[] posts = new String[postIDs.size()];
            posts = postIDs.toArray(posts);

            // Now we can delete the posts from their table
            getWritableDatabase().delete(POSTS_TABLE_NAME, "subreddit = ?", sub);

            // Finally we delete the rogue comments that no longer
            // have a post associated with them
            getReadableDatabase().delete(COMMENTS_TABLE_NAME, "postID = ?", posts);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
