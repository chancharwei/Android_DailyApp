package com.example.chancharwei.dailyapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.chancharwei.dailyapp.utilies.NetworkUtility;
import com.example.chancharwei.dailyapp.utilies.WeatherJsonUtility;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Weather extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String[]>,WeatherAdapter.WeatherAdapterOnclickHandler{
    final static String TAG = Weather.class.getName();
    private RecyclerView mRecyclerView;
    private WeatherAdapter mWadapter;
    private static final int WEATHER_QUERY_ID = 0;
    private static final String CITY_KEY = "cityKey";
    private static final String AREA_KEY = "areaKey";
    private static final String WEATHERURL_KEY = "urlKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        getSupportActionBar().setTitle("Weather");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.i(TAG,"onCreate");
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_weather);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);  //can use 3 layoutManager LinearLayoutManager,StaggeredGridLayoutManager,GridLayoutManager
        //TODO
        mRecyclerView.setHasFixedSize(true);

        mWadapter = new WeatherAdapter(this);
        mRecyclerView.setAdapter(mWadapter);


        getSupportLoaderManager().initLoader(WEATHER_QUERY_ID, null, this); //loader init
        //TODO will cancel in the future
        weatherSearch("新北市","永和區");

    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        Intent featureIntent = new Intent();
        if(itemThatWasClickedId == android.R.id.home){
            featureIntent.setClass(this,MainActivity.class);
            startActivity(featureIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(String weatherItem) {
        Toast.makeText(this, weatherItem, Toast.LENGTH_SHORT)
                .show();
    }

    //TODO check how to link this function
    @NonNull
    @Override
    public Loader<String[]> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<String[]>(this) {

            @Override
            protected void onStartLoading() {
                if(args == null){
                    return;
                }

                if(isStarted()){
                    forceLoad();
                }
            }


            @Nullable
            @Override
            public String[] loadInBackground() {
                String url_weather = args.getString(WEATHERURL_KEY);
                String info;
                if(url_weather == null){
                    return null;
                }
                try {
                    URL weatherURL = new URL(url_weather);
                    if (NetworkUtility.HttpCheckStatusWithURL(weatherURL)) {
                        info = NetworkUtility.getResponseFromHttpUrl(weatherURL);
                        String[] weatherData = WeatherJsonUtility.getWeatherStringsFromJson(Weather.this, info);
                        return weatherData;
                    } else {
                        Log.e(TAG, "Network Status Wrong");
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }


        };
        //return null;
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String[]> loader) {
        Log.i(TAG,"onLoaderReset");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String[]> loader, String[] weatherData) {
        Log.i(TAG,"onLoadFinished");
        if(weatherData != null){
            mWadapter.setWeatherData(weatherData);
        }


    }



    private void weatherSearch(String city,String area){
        String weatherUrl;
        Bundle weatherBundle = new Bundle();
        WeatherJsonUtility weatherJson = new WeatherJsonUtility();
        weatherJson.selectWeatherDataFromLocation(city);
        weatherUrl = weatherJson.buildWeatherUrl(area);
        weatherBundle.putString(WEATHERURL_KEY,weatherUrl);
        if(getSupportLoaderManager().getLoader(WEATHER_QUERY_ID) == null){
            getSupportLoaderManager().initLoader(WEATHER_QUERY_ID, weatherBundle, this);
        }else{
            getSupportLoaderManager().restartLoader(WEATHER_QUERY_ID,weatherBundle,this);
        }

    }
}
