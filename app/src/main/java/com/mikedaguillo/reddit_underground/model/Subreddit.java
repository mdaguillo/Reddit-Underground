package com.mikedaguillo.reddit_underground.model;

import java.util.ArrayList;

/**
 * Created by Mike on 12/11/2014.
 *
 * Represents a subreddit with a name and array of posts
 * to knit.
 */
public class Subreddit {
    private long mId;
    private String mSubredditName;
    private ArrayList<Post> mPosts = new ArrayList<Post>();

    public long getId() { return mId; }

    public void setId(long id) { id = mId; }

    public String getName() { return mSubredditName; }

    public void setName(String name) { name = mSubredditName; }

    public ArrayList<Post> getPosts() { return mPosts; }

    public void setPosts(ArrayList<Post> posts) { posts = mPosts; }
}
