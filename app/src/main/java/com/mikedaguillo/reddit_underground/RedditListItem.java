package com.mikedaguillo.reddit_underground;

/**
 * Created by Mike on 11/26/2014.
 *
 * Object that stores the relevant information that needs to be displayed from a reddit post
 *
 */

public class RedditListItem {
    private String title; //title of the post
    private String author; //author of the post
    private String subreddit; //subreddit posted in
    private int numOfComments; //number of comments the post has

    public RedditListItem (String postTitle, String postAuthor, String postSubReddit, int postNumOfComments) {
        title = postTitle;
        author = postAuthor;
        subreddit = postSubReddit;
        numOfComments = postNumOfComments;
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

}
