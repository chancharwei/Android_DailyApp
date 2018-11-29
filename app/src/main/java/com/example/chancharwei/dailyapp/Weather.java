package com.example.chancharwei.dailyapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class Weather extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{
    final String TAG = getClass().getName();
    private static final int WEATHER_QUERY_ID = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
        getSupportActionBar().setTitle("Weather");
        setContentView(R.layout.activity_weather);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //TODO check use bundle is parameters
        getSupportLoaderManager().initLoader(WEATHER_QUERY_ID, null, this); //loader init
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
    //TODO check how to link this function
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

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
            public String loadInBackground() {
                return null;
            }
        };
        //return null;
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String s) {

    }
}
