package com.mikedaguillo.reddit_underground;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Mike on 12/19/2014.
 */
public class ImageViewScreen extends ActionBarActivity{

    private WebView webView;
    public static final String TAG = ImageViewScreen.class.getSimpleName(); //Tag for error messages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view_layout);

        webView = (WebView) findViewById(R.id.webView);

        // Sets the zoom settings and default image size on load
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);

        Intent intent = getIntent();
        byte[] imageByteArray = intent.getByteArrayExtra("Image_Byte_Array");
        if (imageByteArray != null){
            String html="<html><body><img src='{IMAGE_URL}' /></body></html>";

            String imgageBase64 = Base64.encodeToString(imageByteArray, Base64.DEFAULT);
            String imageString = "data:image/png;base64," + imgageBase64;

            // Use image for the img src parameter in your html and load to webview
            html = html.replace("{IMAGE_URL}", imageString);
            webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", "");

        }
        else {
            String html="<html><body><img src='{IMAGE_URL}' /></body></html>";
            Bitmap defaultImage = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_reddit_icon);

            // Convert bitmap to Base64 encoded image for web
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            defaultImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String imgageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
            String image = "data:image/png;base64," + imgageBase64;

            // Use image for the img src parameter in your html and load to webview
            html = html.replace("{IMAGE_URL}", image);
            webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", "");

        }

    }
}
