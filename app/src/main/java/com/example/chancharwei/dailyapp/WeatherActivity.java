package com.example.chancharwei.dailyapp;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.chancharwei.dailyapp.gps.GPSLocationListener;
import com.example.chancharwei.dailyapp.utilies.NetworkUtility;
import com.example.chancharwei.dailyapp.utilies.WeatherJsonUtility;
import com.example.chancharwei.dailyapp.utilies.WeatherXmlUtility;
import com.example.chancharwei.dailyapp.gps.GPSLocationManager;
import com.example.chancharwei.dailyapp.gps.GPSLocation;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import static android.text.TextUtils.isEmpty;

public class WeatherActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks,WeatherAdapter.WeatherAdapterOnclickHandler{
    final static String TAG = WeatherActivity.class.getName();
    private RecyclerView mRecyclerView;
    private WeatherAdapter mWadapter;
    private static boolean parsingLocationInfoDone;     //parsing xml for location info latitude & longitude
    private static HashMap<String, double[]> locationInfo = null;
    private static final int WEATHER_QUERY_ID = 0;
    private static final int ABSOLUTE_LOCATION_QUERY_ID = 1;
    private static final String WEATHERURL_KEY = "urlKey";

    private GoogleApiClient googleApiClient;
    private static boolean gpsSearchStarted = false;
    final static int REQUEST_LOCATION = 199;
    final static int PERMISSION_LOCATION = 1;
    private GPSLocationManager gpsLocationManager;
    private Location nowLocation = null;


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

        if(!parsingLocationInfoDone){
            getSupportLoaderManager().initLoader(ABSOLUTE_LOCATION_QUERY_ID, null, this); //loader init
        }

        gpsLocationManager = new GPSLocationManager(WeatherActivity.this);
        final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //check GPS enable? if not, show a dialog to make user enable GPS
        if(manager != null && (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))){
            Log.d(TAG,"GPS check = "+manager.isProviderEnabled(LocationManager.GPS_PROVIDER)+" NETWORK check = "+manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
            if(!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                Toast.makeText(this,"Recommend open Network to get Location more fast",Toast.LENGTH_SHORT).show();
            }
            checkLocationPermission(true);
        }else if(!hasGPSDevice(this)){
            Toast.makeText(this,"Device not support GPS feature",Toast.LENGTH_SHORT).show();
        }else{
            enableLocation();
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG,"onResume");

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"onDestroy");
        parsingLocationInfoDone = false;
        checkLocationPermission(false);
        super.onDestroy();
    }

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER)||providers.contains(LocationManager.GPS_PROVIDER);
    }
    //TODO realize the code https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi
    private void enableLocation(){
        if(googleApiClient == null){
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks(){

                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            Log.i(TAG,"check onConnected");
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.i(TAG,"check onConnectionSuspended");
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener(){

                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Log.e(TAG,"connect fail for enable location -> "+connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30*1000);
            locationRequest.setFastestInterval(5*1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                    final Status status = locationSettingsResult.getStatus();
                    switch (status.getStatusCode()){
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED :
                            try{
                                status.startResolutionForResult(WeatherActivity.this,REQUEST_LOCATION);
                            }catch (IntentSender.SendIntentException e){
                                e.printStackTrace();
                            }
                            break;
                    }
                }


            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode){
            case REQUEST_LOCATION:
                switch (resultCode){
                    case Activity.RESULT_OK:
                        checkLocationPermission(true);
                        break;

                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkLocationPermission(boolean gpsActive){
        int Location_FINE, Location_COARSE;
        Location_FINE = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        Location_COARSE = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        Log.d(TAG,"location permission FINE ("+Location_FINE+") COARSE ("+Location_COARSE+")");

        if(!gpsActive){
            if(Location_FINE == PackageManager.PERMISSION_DENIED && Location_COARSE == PackageManager.PERMISSION_DENIED){
                return;
            }else{
                gpsSearchStarted = gpsLocationManager.stop();
            }
        }else{
            if(Location_FINE == PackageManager.PERMISSION_DENIED || Location_COARSE == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(
                        this,new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_LOCATION);
            }else{
                //permission is PERMISSION_GRANTED
                //getSupportLoaderManager().initLoader(START_DETECT_LOCATION_QUERY_ID, null, this);
                if(!gpsSearchStarted){
                    gpsSearchStarted = gpsLocationManager.startGPS(new GPSListener(WeatherActivity.this));
                }

            }
        }


        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_LOCATION:
                boolean allowDetectLocation = true;
                if(grantResults.length > 0) {
                    int i=0;
                    for(int result : grantResults){
                        Log.d(TAG,"permission["+i+"] = "+permissions[i]+" ,result["+i+"] = "+grantResults[i]);
                        if(result == PackageManager.PERMISSION_DENIED){
                            allowDetectLocation = false;
                            break;
                        }
                        i++;
                    }
                    if(allowDetectLocation && !gpsSearchStarted){
                        gpsSearchStarted = gpsLocationManager.startGPS(new GPSListener(WeatherActivity.this));
                    }

                }else{
                    break;
                }

                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void makeSearchQuery(String query){
        mappingLocation(query);
    }

    private void mappingLocation(String queryLocation){
        Log.v(TAG,"check length 1 = "+queryLocation.length());
        Log.v(TAG,"check length 2 = "+queryLocation.getBytes().length); // Chinese word *3
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

            if(queryLocation.length() <= 4){  //屏東縣三地門鄉
                ArrayList<String> getLocationList = new ArrayList();
                int i = 0;
                if(locationInfo != null){
                    for(String key : locationInfo.keySet()){
                        if(key.contains(queryLocation)){
                            //Log.d(TAG,"get contain key = "+key);
                            getLocationList.add(key);
                        }
                    }
                    if(getLocationList.size() == 1){
                        queryLocation = getLocationList.get(0);
                        Log.d(TAG,"get Location = "+queryLocation);
                    }else if(getLocationList.size() > 1){
                        //TODO 花蓮縣花蓮市
                    }
                }
            }

            //If user input city,area(新北市,永和區) we can use "," to split it
            if(queryLocation.contains(",")||queryLocation.contains("，")||queryLocation.contains(".")){
                if(queryLocation.contains(",")){
                    dataFromQuery = queryLocation.split(",",2); //most split time 2-1
                }else if(queryLocation.contains("，")){
                    dataFromQuery = queryLocation.split("，",2);
                }else if(queryLocation.contains(".")){
                    //can not use to split string method,have error
                    dataFromQuery[0] = queryLocation.substring(0,queryLocation.indexOf("."));
                    dataFromQuery[1] = queryLocation.substring(queryLocation.indexOf(".")+1);
                }else{

                }
                city = dataFromQuery[0];
                area = dataFromQuery[1];

                Log.d(TAG,"get city = "+city+" area = "+area);
                weatherSearch(city,area);
                return;
            }
            //If user input cityarea(新北市永和區) we use "市" or "縣" to split it
            if(queryLocation.contains("市") || queryLocation.contains("縣")){

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
                }else if(queryLocation.contains("縣")){
                    dataFromQuery[0] = queryLocation.substring(0,queryLocation.indexOf("縣")+1);
                    dataFromQuery[1] = queryLocation.substring(queryLocation.indexOf("縣")+1);
                }

                city = dataFromQuery[0];
                area = dataFromQuery[1];

                Log.d(TAG,"get city = "+city+" area = "+area);
                weatherSearch(city,area);
                return;

            }else{

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
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default
        //return super.onCreateOptionsMenu(menu);

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                // This is your adapter that will be filtered
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                // **Here you can get the value "query" which is entered in the search box.**
                //TODO realize why implement this function will make search not call onCreate again
                makeSearchQuery(query);
                searchView.clearFocus(); //hide keyboard
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        Intent featureIntent = new Intent();
        switch (itemThatWasClickedId){
            case android.R.id.home:
                featureIntent.setClass(this,MainActivity.class);
                startActivity(featureIntent);
                finish();
                break;

            case R.id.exchangeRate:
                featureIntent.setClass(this,ExchangeRateActivity.class);
                startActivity(featureIntent);
                finish();
                break;
            case R.id.search:
                //if click search bar, this callback will trigger
                mRecyclerView.setVisibility(View.INVISIBLE);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    public Loader onCreateLoader(int id, @Nullable final Bundle args) {
        Log.d(TAG,"get Loader id ("+id+")");
        if(id == WEATHER_QUERY_ID){
            return new AsyncTaskLoader(this) {

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
                        Log.i(TAG,"get Json weather data");
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
        }else if(id == ABSOLUTE_LOCATION_QUERY_ID){
            return new AsyncTaskLoader(this) {

                @Override
                protected void onStartLoading() {
                    if(isStarted()){
                        forceLoad();
                    }
                }


                @Nullable
                @Override
                public HashMap<String, double[]> loadInBackground() {
                    Log.i(TAG,"parsing location xml info");
                    String info;
                    try {
                        URL locationInfoURL = new URL(WeatherXmlUtility.getLocationInfoXML());
                        if (NetworkUtility.HttpCheckStatusWithURL(locationInfoURL)) {
                            WeatherXmlUtility absoluteLocationParsing = new WeatherXmlUtility();
                            HashMap<String, double[]> absoluteLocation = absoluteLocationParsing.loadXmlFromNetwork(locationInfoURL);
                            return absoluteLocation;
                        } else {
                            Log.e(TAG, "Network Status Wrong");
                        }

                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    return null;
                }


            };
        }

        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        Log.i(TAG,"onLoadFinished");
        if(loader.getId() == WEATHER_QUERY_ID){
            if(data != null){
                mWadapter.setWeatherData((String[])data);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }else if(loader.getId() == ABSOLUTE_LOCATION_QUERY_ID){
            if(data != null){
                locationInfo = (HashMap<String, double[]>)data;
                Log.d(TAG,"check get last key = "+locationInfo.containsKey("花蓮縣富里鄉"));
                if(locationInfo.containsKey("花蓮縣富里鄉") == true){     //Last data with our parsing xml document
                    parsingLocationInfoDone = true;
                    if(nowLocation != null){
                        Toast.makeText(this,"longitude("+nowLocation.getLongitude()+") latitude("+nowLocation.getLongitude()+")",Toast.LENGTH_SHORT).show();
                        searchLocationArea(nowLocation);
                    }
                }
            }

        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {

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

    public void assignLocation(Location location){
        if(nowLocation == null){
            nowLocation = location;
            searchLocationArea(nowLocation);
        }else{
            if(location.getTime() - nowLocation.getTime() > 60000){ //update location time need to more than 1 min
                if(location.distanceTo(nowLocation)>1000){
                    nowLocation = location;
                    searchLocationArea(nowLocation);
                }
            }
        }



    }

    private void searchLocationArea(Location location){

        if(parsingLocationInfoDone && locationInfo!=null){
            float nearDitance=50000*1000;
            float getDistance=0;
            String getLocationArea = "";
            Location refLocation = new Location("");
            for(String key : locationInfo.keySet()){
                refLocation.setLongitude(locationInfo.get(key)[0]);
                refLocation.setLatitude(locationInfo.get(key)[1]);
                getDistance = location.distanceTo(refLocation);
                if(getDistance<nearDitance){
                    nearDitance = getDistance;
                    getLocationArea = key;
                    Log.d(TAG,"distance = "+getDistance+" Area = "+key);
                }
            }
            mappingLocation(getLocationArea);
        }else{
            Log.e(TAG,"can not the location where you are");
        }
    }

}

class GPSListener implements GPSLocationListener {
    final static String TAG = GPSListener.class.getName();
    private WeatherActivity wActivity;
    public GPSListener(WeatherActivity weatherActivity) {
        wActivity = weatherActivity;
    }

    @Override
    public void updateLocation(Location location) {

        if (location != null) {
            Log.i(TAG, "get location latitude = " + location.getLatitude() + " longitude = " + location.getLongitude()+" accuracy = "+location.getAccuracy());
            wActivity.assignLocation(location);
        }
    }

    @Override
    public void updateStatus(String provider, int status, Bundle extras) {
        Log.i(TAG, "location type = " + provider);
    }

    @Override
    public void updateGPSProviderStatus(int gpsStatus) {

        switch (gpsStatus) {
            case GPSLocation.GPSProviderStatus.GPS_ENABLED:
                //Log.i(TAG, "gps enable");
                break;
            case GPSLocation.GPSProviderStatus.GPS_DISABLED:
                Log.i(TAG, "gps disable");
                break;
            case GPSLocation.GPSProviderStatus.GPS_OUT_OF_SERVICE:
                Log.i(TAG, "gps out of service");
                break;
            case GPSLocation.GPSProviderStatus.GPS_TEMPORARILY_UNAVAILABLE:
                Log.i(TAG, "gps temporarily unavailable");
                break;
            case GPSLocation.GPSProviderStatus.GPS_AVAILABLE:
                Log.i(TAG, "gps temporarily available");
                break;
        }
    }

}
