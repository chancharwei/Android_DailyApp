package com.example.chancharwei.dailyapp;

import android.app.SearchManager;
import android.content.Context;
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
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.chancharwei.dailyapp.utilies.NetworkUtility;
import com.example.chancharwei.dailyapp.utilies.WeatherJsonUtility;

import java.net.URL;

import static android.text.TextUtils.isEmpty;

public class WeatherActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String[]>,WeatherAdapter.WeatherAdapterOnclickHandler{
    final static String TAG = WeatherActivity.class.getName();
    private RecyclerView mRecyclerView;
    private WeatherAdapter mWadapter;
    private static String queryInfo;
    private static final int WEATHER_QUERY_ID = 0;
    private static final String CITY_KEY = "cityKey";
    private static final String AREA_KEY = "areaKey";
    private static final String WEATHERURL_KEY = "urlKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //getSupportActionBar().setTitle("WeatherActivity");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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

        makeSearchQuery();
        //TODO will cancel in the future
        //weatherSearch("新北市","永和區");

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

    private void makeSearchQuery(){
        Log.d(TAG,"Byron makeSearchQuery E");
        Intent intent = getIntent();
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG,"Byron get query info "+query);
            //mappingLocation("新北市,永和區");
            mappingLocation(query);
        }
    }

    private void mappingLocation(String queryLocation){
        Log.d(TAG,"Byron check length 1 = "+queryLocation.length());
        Log.d(TAG,"Byron check length 2 = "+queryLocation.getBytes().length); // Chinese word *3
        String city,area;
        String[] dataFromQuery = new String[2];
        boolean containChinese = false;
        if(isEmpty(queryLocation) == true){
            return;
        }else if(queryLocation.length() == queryLocation.getBytes().length){
            containChinese = false;
        }else{
            containChinese = true;
        }

        if(containChinese == true){
            //If user input city,area(新北市,永和區) we can use "," to split it
            if(queryLocation.contains(",")||queryLocation.contains("，")||queryLocation.contains(".")){
                Log.d(TAG,"Byron get "+queryLocation.indexOf(","));
                if(queryLocation.contains(",")){
                    dataFromQuery = queryLocation.split(",",2); //most split time 2-1
                }else if(queryLocation.contains("，")){
                    dataFromQuery = queryLocation.split("，",2);
                }else if(queryLocation.contains(".")){
                    //can not use to split string method,have error
                    Log.d(TAG,"Byron get "+queryLocation.indexOf("."));
                    dataFromQuery[0] = queryLocation.substring(0,queryLocation.indexOf("."));
                    dataFromQuery[1] = queryLocation.substring(queryLocation.indexOf(".")+1);
                }else{

                }
                city = dataFromQuery[0];
                area = dataFromQuery[1];

                Log.d(TAG,"Byron get city = "+city+" area = "+area);
                weatherSearch(city,area);
                return;
            }
            //If user input cityarea(新北市永和區) we use "市" or "縣" to split it
            if(queryLocation.contains("市") || queryLocation.contains("縣")){

                Log.d(TAG,"Byron get 市");
                //TODO 
                if(queryLocation.contains("市") && queryLocation.contains("縣")){
                    if(queryLocation.indexOf("市")>queryLocation.indexOf("縣")){
                        dataFromQuery[0] = queryLocation.substring(0,queryLocation.indexOf("縣")+1);
                        dataFromQuery[1] = queryLocation.substring(queryLocation.indexOf("縣")+1);
                    }else{
                        dataFromQuery[0] = queryLocation.substring(0,queryLocation.indexOf("市")+1);
                        dataFromQuery[1] = queryLocation.substring(queryLocation.indexOf("市")+1);
                    }
                }else if(queryLocation.contains("市")){
                    dataFromQuery[0] = queryLocation.substring(0,queryLocation.indexOf("市")+1);
                    dataFromQuery[1] = queryLocation.substring(queryLocation.indexOf("市")+1);
                    Log.d(TAG,"Byron get dataFromQuery = "+dataFromQuery[0]+" area = "+dataFromQuery[1]);
                }else if(queryLocation.contains("縣")){
                    dataFromQuery[0] = queryLocation.substring(0,queryLocation.indexOf("縣")+1);
                    dataFromQuery[1] = queryLocation.substring(queryLocation.indexOf("縣")+1);
                }

                city = dataFromQuery[0];
                area = dataFromQuery[1];

                Log.d(TAG,"Byron get city = "+city+" area = "+area);
                weatherSearch(city,area);
                return;

            }
        }else{

        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.weather,menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //TODO layout for search bar
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default
        //return super.onCreateOptionsMenu(menu);
        return true;
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
                        String[] weatherData = WeatherJsonUtility.getWeatherStringsFromJson(WeatherActivity.this, info);
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
