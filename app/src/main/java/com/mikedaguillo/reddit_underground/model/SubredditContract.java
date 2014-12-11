package com.mikedaguillo.reddit_underground.model;

import android.provider.BaseColumns;

/**
 * Created by Mike on 12/11/2014.
 *
 * This class represents a contract for a subreddits table containing subreddits for
 * which to store post information locally
 */
public final class SubredditContract {
    /**
     * Contains the name of the table to create that contans the row counters.
     */
    public static final String TABLE_NAME = "Subreddits";

    /**
     * Contains the SQL query to use to create the table containing the projects.
     */
    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + SubredditContract.TABLE_NAME +
            " ("+ SubredditContract.SubredditEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + SubredditContract.SubredditEntry.COLUMN_NAME_TITLE + " TEXT);";

    /**
     * This class represents the rows for an entry in the project table. The
     * primary key is the _id column from the BaseColumn class.
     */
    public static abstract class SubredditEntry implements BaseColumns {
        // Name of the project as shown in the application.
        public static final String COLUMN_NAME_TITLE = "title";
    }
}
