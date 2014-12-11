package com.mikedaguillo.reddit_underground.model;

/**
 * Created by Mike on 12/11/2014.
 *
 * Represents a single post in a subreddit with its JSON Data.
 */
public class Post {
    private long mId;
    private String mJSONData;

    public String getJSONData() { return mJSONData; }

    public void setJSONData(String data) { data = mJSONData; }

    /**
     * Gets the identifier of the post.
     *
     * @return the identifier of the post.
     */
    public long getId() {
        return (mId);
    }

    /**
     * Sets the identifier of the post.
     *
     * @param id the identifier to set.
     */
    public void setId(long id)  { mId = id; }
}
