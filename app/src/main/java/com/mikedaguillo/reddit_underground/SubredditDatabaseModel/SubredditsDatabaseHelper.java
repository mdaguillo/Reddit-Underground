package com.mikedaguillo.reddit_underground.SubredditDatabaseModel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Blob;

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

    public SubredditsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // sql string command to create a new table called subreddits with an id column and a name column
        sqLiteDatabase.execSQL("CREATE TABLE " + SUBREDDIT_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT);");
        sqLiteDatabase.execSQL("CREATE TABLE " + POSTS_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, author TEXT, subreddit TEXT, numComments INTEGER, thumbnailBlob BLOB, subredditID INTEGER, FOREIGN KEY(subredditID) REFERENCES " + SUBREDDIT_TABLE_NAME + "(_id));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SUBREDDIT_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + POSTS_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void addSubreddit(String name) {
        // place the name of the subreddit you want to add into a ContentValues object
        ContentValues values = new ContentValues(1);
        values.put("name", name);

        // call getWritableDatabase and insert in the "Subreddits" table, under the "name" column the
        // values in the ContentValues object
        getWritableDatabase().insert(SUBREDDIT_TABLE_NAME, "name", values);
        close();
    }

    public void addPost(String title, String author, String subreddit, int numComments, byte[] thumbnail, int subredditID) {
        ContentValues values = new ContentValues(5);
        values.put("title", title);
        values.put("author", author);
        values.put("subreddit", subreddit);
        values.put("numComments", numComments);
        values.put("thumbnailBlob", thumbnail);
        values.put("subredditID", subredditID);

        getWritableDatabase().insert(POSTS_TABLE_NAME, "title", values);
        close();
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

    public void deleteAll() {
        getWritableDatabase().delete(SUBREDDIT_TABLE_NAME, null, null);
        getWritableDatabase().delete(POSTS_TABLE_NAME, null, null);
        getWritableDatabase().execSQL("delete from sqlite_sequence where name='"+ SUBREDDIT_TABLE_NAME + "';");
        getWritableDatabase().execSQL("delete from sqlite_sequence where name='"+ POSTS_TABLE_NAME + "';");
    }

}
