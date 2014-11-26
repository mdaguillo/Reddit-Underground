package com.mikedaguillo.reddit_underground;

/**
 * Created by Mike on 11/26/2014.
 *
 * Object that stores the relevant information that needs to be displayed from a reddit post
 *
 */

public class RedditListItem {
    private String title; // title of the post
    private String author; // author of the post
    private String subreddit; // subreddit posted in
    private int numOfComments; // number of comments the post has
    private String thumbnailSrc; // path to the image
    private boolean isNSFW;

    public RedditListItem (String postTitle, String postAuthor, String postSubReddit, int postNumOfComments, String thumbnail, boolean nsfw) {
        title = postTitle;
        author = postAuthor;
        subreddit = postSubReddit;
        numOfComments = postNumOfComments;
        thumbnailSrc = thumbnail;
        isNSFW = nsfw;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public int getNumOfComments() {
        return numOfComments;
    }

    public String getThumbnailSrc() { return thumbnailSrc; }

    public boolean getNSFW() { return isNSFW; }

}
