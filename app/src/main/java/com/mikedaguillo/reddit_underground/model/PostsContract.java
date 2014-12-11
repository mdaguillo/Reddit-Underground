package com.mikedaguillo.reddit_underground.model;

import android.provider.BaseColumns;

/**
 * Created by Mike on 12/11/2014.
 *
 * This class represents a contract for a posts table containing posts
 * for subreddits. The subreddit must exist before creating posts
 * since the posts have a foreign key to the subreddit.
 */
public final class PostsContract {

    /**
     * Contains the name of the table to create that contains the row counters.
     */
    public static final String TABLE_NAME = "Posts";

    /**
     * Contains the SQL query to use to create the table containing the row counters.
     */
    public static final String SQL_CREATE_TABLE = "CREATE TABLE "
            + PostsContract.TABLE_NAME + " ("
            + PostsContract.PostsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + PostsContract.PostsEntry.COLUMN_NAME_SUBREDDIT_ID + " INTEGER,"
            + PostsContract.PostsEntry.COLUMN_NAME_JSON_DATA + " INTEGER,"
            + "FOREIGN KEY (" + PostsContract.PostsEntry.COLUMN_NAME_SUBREDDIT_ID + ") REFERENCES projects(" + SubredditContract.SubredditEntry._ID + "));";

    /**
     * This class represents the rows for an entry in the row_counter table. The
     * primary key is the _id column from the BaseColumn class.
     */
    public static abstract class PostsEntry implements BaseColumns {

        // Identifier of the project to which the row counter belongs
        public static final String COLUMN_NAME_SUBREDDIT_ID = "Subreddit_ID";

        // Final amount of rows to reach
        public static final String COLUMN_NAME_JSON_DATA = "JSON_Data";
    }
}