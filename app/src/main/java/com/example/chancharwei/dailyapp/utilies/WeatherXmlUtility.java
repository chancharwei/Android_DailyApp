package com.example.chancharwei.dailyapp.utilies;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList; //not sure for using
import java.util.HashMap;
import java.util.List; //not sure for using
import java.util.Map;


public class WeatherXmlUtility {
    private static final String TAG = WeatherXmlUtility.class.getName();
    private static final String ns = null;
    private static final String xml_1st_layer = "dataroot";
    private static final String xml_2nd_layer = "_x0031_050429_行政區經緯度_x0028_toPost_x0029_";
    public HashMap<String, double[]> absoluteLocation = null;

    public HashMap<String, double[]> loadXmlFromNetwork (String urlString){
        InputStream stream = null;
        try{
            absoluteLocation = new HashMap<>();
            stream = downloadUrl(urlString);
            parse(stream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        return absoluteLocation;
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    private List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, xml_1st_layer);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(xml_2nd_layer)) {
                readGroup(parser);
            } else {
                skip(parser);
            }
        }
        return entries;
    }


    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private void readGroup(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, xml_2nd_layer);
        String locationName = null;
        double[] getAbsolueLocation ={0,0}; //{latitue,lontitue}
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("行政區名")){
                locationName = readText(parser);
            }else if(name.equals("中心點經度")){
                getAbsolueLocation[0] = Double.parseDouble(readText(parser));
            }else if(name.equals("中心點緯度")){
                getAbsolueLocation[1] = Double.parseDouble(readText(parser));
            }else{
                skip(parser);
            }
        }

        if(locationName != null && getAbsolueLocation[0]>0 && getAbsolueLocation[1] > 0){
            absoluteLocation.put(locationName,getAbsolueLocation);
            //Log.d(TAG,"locationName = "+locationName+" Location[0] = "+getAbsolueLocation[0]+" Location[1] = "+getAbsolueLocation[1]);
        }
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
