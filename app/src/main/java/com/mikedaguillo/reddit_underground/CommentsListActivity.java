package com.mikedaguillo.reddit_underground;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mikedaguillo.reddit_underground.SubredditDatabaseModel.RedditThing;
import com.mikedaguillo.reddit_underground.SubredditDatabaseModel.SubredditsDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mike on 1/28/2015.
 *
 * Follows a similar style to the Reddit is fun app available on the android market place.
 * The listview adapter is the same as the Reddit is fun app
 *
 */
public class CommentsListActivity extends ActionBarActivity {
    private static final String TAG = "CommentsListActivity";

    /** Custom list adapter that fits the database data into the list. */
    private CommentsListAdapter mCommentsAdapter = null;
    private SubredditsDatabaseHelper mDatabase = null;
    private int post = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_list_view);

        mDatabase = new SubredditsDatabaseHelper(this);

        // Retrieve the post from the selection
        // And the comments from the post cursor
        Intent intent = getIntent();
        post = intent.getIntExtra("Stored Post", 0);
        Cursor comments = mDatabase.getComments(post);
        Cursor postCursor = mDatabase.getPost(post);
        List<RedditThing> commentThings = getRedditThings(postCursor, comments); // Create the list items

        // Set up the view
        ListView commentsListView = (ListView) findViewById(R.id.commentsListView);
        mCommentsAdapter = new CommentsListAdapter(this, commentThings);
        commentsListView.setAdapter(mCommentsAdapter);
        RedditThingClickListener listener = new RedditThingClickListener(postCursor);
        commentsListView.setOnItemClickListener(listener);

    }

    private boolean isHiddenCommentHeadPosition(int position) {
        return mCommentsAdapter != null && mCommentsAdapter.getItemViewType(position) == CommentsListAdapter.HIDDEN_ITEM_HEAD_VIEW_TYPE;
    }

    private boolean isHiddenCommentDescendantPosition(int position) {
        return mCommentsAdapter != null && mCommentsAdapter.getItem(position).isHiddenCommentDescendant();
    }

    private boolean isLoadMoreCommentsPosition(int position) {
        return mCommentsAdapter != null && mCommentsAdapter.getItemViewType(position) == CommentsListAdapter.MORE_ITEM_VIEW_TYPE;
    }

     /* *
     *
     * This is a custom list adaptor derived from the same adapter used
     * in the Reddit is fun app. You can find the original source code for this
     * custom list adapter on github at the following location:
     *
     * https://github.com/talklittle/reddit-is-fun
     *
     *
     * */
    final class CommentsListAdapter extends ArrayAdapter<RedditThing> {
         public static final int OP_ITEM_VIEW_TYPE = 0;
         public static final int COMMENT_ITEM_VIEW_TYPE = 1;
         public static final int MORE_ITEM_VIEW_TYPE = 2;
         public static final int HIDDEN_ITEM_HEAD_VIEW_TYPE = 3;
         // The number of view types
         public static final int VIEW_TYPE_COUNT = 4;

         public boolean mIsLoading = true;

         private int mFrequentSeparatorPos = ListView.INVALID_POSITION;

         public CommentsListAdapter(Context context, List<RedditThing> objects) {
            super(context, 0, objects);
         }

         @Override
         public int getItemViewType(int position) {
             if (position == 0)
                 return OP_ITEM_VIEW_TYPE;
             if (position == mFrequentSeparatorPos) {
                 // We don't want the separator view to be recycled.
                 return IGNORE_ITEM_VIEW_TYPE;
             }
             // TODO implement ability to hide comments when comment depth is > 1
             /*
             Object object = getItem(position);
             if (object.isHiddenCommentDescendant())
                 return IGNORE_ITEM_VIEW_TYPE;
             if (object.isHiddenCommentHead())
                 return HIDDEN_ITEM_HEAD_VIEW_TYPE;
             if (object.isLoadMoreCommentsPlaceholder())
                 return MORE_ITEM_VIEW_TYPE;
              */

             return COMMENT_ITEM_VIEW_TYPE;
         }

         @Override
         public int getViewTypeCount() {
             return VIEW_TYPE_COUNT;
         }

         @Override
         public boolean isEmpty() {
             if (mIsLoading)
                 return false;
             return super.isEmpty();
         }

         @Override
         public long getItemId(int i) {
             return 0;
         }

         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View view = convertView;

             RedditThing item = this.getItem(position);

             try {
                 if (position == 0) {
                     // The OP
                     if (view == null) {
                         LayoutInflater mInflater = (LayoutInflater) CommentsListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                         view = mInflater.inflate(R.layout.reddit_list_item, parent, false);
                     }

                     fillPostListItemView(view, item);

                 }
                 else { // Regular comment
                     // Here view may be passed in for re-use, or we make a new one.
                     if (view == null) {
                         LayoutInflater mInflater = (LayoutInflater) CommentsListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                         view = mInflater.inflate(R.layout.comment_list_item, parent, false);
                     }
                     else {
                         view = convertView;
                     }


                     fillCommentsListItemView(view, item);
                 }
             }
             catch (Exception e) {
                 Log.e(TAG, "Failed to load comments: " + e);
             }

             return view;
         }
    }

    public static List<RedditThing> getRedditThings(Cursor postCursor, Cursor commentsCursor) {
        List<RedditThing> things = new ArrayList<RedditThing>();
        postCursor.moveToFirst();
        RedditThing postThing = new RedditThing();
        postThing.setTitle(postCursor.getString(1));
        postThing.setAuthor(postCursor.getString(2));
        postThing.setSubreddit(postCursor.getString(3));
        postThing.setNum_comments(postCursor.getInt(4));
        try {
            postThing.setThumbnailByteArray(postCursor.getBlob(5));
        }
        catch(Exception e) {
            Log.e(TAG, "Failed to store the Byte Array into the RedditThing: " + e);
        }

        things.add(postThing);

        commentsCursor.moveToFirst();
        while (!commentsCursor.isAfterLast()) {
            RedditThing thing = new RedditThing();
            thing.setAuthor(commentsCursor.getString(1));
            thing.setBody(commentsCursor.getString(2));
            thing.setUps(commentsCursor.getInt(4));
            thing.setDowns(commentsCursor.getInt(5));
            things.add(thing);

            commentsCursor.moveToNext();
        }

        return things;
    }

    private class RedditThingClickListener implements AdapterView.OnItemClickListener {
        Cursor postCursor = null;

        public RedditThingClickListener(Cursor cursor) {
            postCursor = cursor;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (position == 0) {
                Intent intent = new Intent(view.getContext(), ImageViewScreen.class);
                intent.putExtra("Image_Byte_Array", postCursor.getBlob(6));
                startActivity(intent);
            }
        }
    }

    public void fillPostListItemView(View view, RedditThing thing) {
        // Set the values of the Views for the Reddit Post List Item

        TextView postTitle = (TextView)view.findViewById(R.id.postTitle);
        TextView postAuthor = (TextView)view.findViewById(R.id.postAuthor);
        TextView postSubReddit = (TextView)view.findViewById(R.id.postSubReddit);
        TextView postComments = (TextView)view.findViewById(R.id.postComments);
        ImageView postThumbnail = (ImageView)view.findViewById(R.id.thumbnail);

        postTitle.setText(thing.getTitle());
        postAuthor.setText(thing.getAuthor());
        postSubReddit.setText(thing.getSubreddit());
        postComments.setText("Comments: " + thing.getNum_comments());

        try {
            // retrieve the thumbnail from the database
            postThumbnail.setImageBitmap(BitmapFactory.decodeByteArray(thing.getThumbnailByteArray(), 0, thing.getThumbnailByteArray().length));
        }
        catch(Exception e) {
            // use the default image
            Bitmap default_image = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_reddit_icon);
            postThumbnail.setImageBitmap(default_image);
        }

    }

    public static void fillCommentsListItemView(View view, RedditThing item) {
        // Set the values of the Views for the CommentsListItem

        TextView votesView = (TextView) view.findViewById(R.id.votes);
        TextView submitterView = (TextView) view.findViewById(R.id.submitter);
        TextView bodyView = (TextView) view.findViewById(R.id.body);

        try {
            votesView.setText(Integer.toString(item.getUps() - item.getDowns()));
        } catch (Exception e) {
            Log.e(TAG, "Error setting the votes text in the votes view: " + e);
        }
        if (item.getSSAuthor() != null)
            submitterView.setText(item.getSSAuthor());
        else
            submitterView.setText(item.getAuthor());

        if (item.getSpannedBody() != null)
            bodyView.setText(item.getSpannedBody());
        else
            bodyView.setText(item.getBody());

        setCommentIndent(view, item.getIndent());
    }

    public static void setCommentIndent(View commentListItemView, int indentLevel) {
        View[] indentViews = new View[] {
                commentListItemView.findViewById(R.id.left_indent1),
                commentListItemView.findViewById(R.id.left_indent2),
                commentListItemView.findViewById(R.id.left_indent3),
                commentListItemView.findViewById(R.id.left_indent4),
                commentListItemView.findViewById(R.id.left_indent5),
                commentListItemView.findViewById(R.id.left_indent6),
                commentListItemView.findViewById(R.id.left_indent7),
                commentListItemView.findViewById(R.id.left_indent8)
        };
        for (int i = 0; i < indentLevel && i < indentViews.length; i++) {
            indentViews[i].setVisibility(View.VISIBLE);
            indentViews[i].setBackgroundResource(R.color.background_grey);
        }
        for (int i = indentLevel; i < indentViews.length; i++) {
            indentViews[i].setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDatabase.close();
    }

    @Override
    protected  void onResume() {
        super.onResume();
        mDatabase = new SubredditsDatabaseHelper(this);
    }
}
