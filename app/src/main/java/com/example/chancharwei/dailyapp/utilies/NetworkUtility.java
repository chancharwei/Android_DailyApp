package com.example.chancharwei.dailyapp.utilies;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class NetworkUtility {
    private static final String TAG = NetworkUtility.class.getName();

    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_NOT_IMPLEMENTED = 501;
    public static final int HTTP_NOT_MODIFIED = 304;
    public static final int HTTP_NO_CONTENT = 204;
    public static final int HTTP_OK = 200;


    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static boolean HttpCheckStatusWithURL(URL url){
        try{
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.connect();
            int status = conn.getResponseCode();
            conn.disconnect();
            if(status == HTTP_OK){
                return true;
            }else{
                Log.e(TAG,"Network Status = "+status);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
