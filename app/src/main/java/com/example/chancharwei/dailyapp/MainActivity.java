package com.example.chancharwei.dailyapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    final String TAG = getClass().getName();
    private ImageView mImageView_weather,mImageView_exchangeRate;
    private ImageButton mImageButton_weather,mImageButton_exchangeRate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG,"onCreate");
        mImageView_weather = findViewById(R.id.weather_icon);
        mImageButton_weather = findViewById(R.id.weather_background);
        mImageButton_weather.setBackgroundResource(R.drawable.background_border);
        mImageButton_weather.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    mImageButton_weather.setBackgroundResource(R.drawable.background_border2);
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    mImageButton_weather.setBackgroundResource(R.drawable.background_border);
                    startFeature(WeatherActivity.class);
                }
                return false;
            }
        });
        mImageView_exchangeRate = findViewById(R.id.exchangeRate_icon);
        mImageButton_exchangeRate = findViewById(R.id.exchangeRate_background);
        mImageButton_exchangeRate.setBackgroundResource(R.drawable.background_border);
        mImageButton_exchangeRate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    mImageButton_exchangeRate.setBackgroundResource(R.drawable.background_border2);
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    mImageButton_exchangeRate.setBackgroundResource(R.drawable.background_border);
                    startFeature(ExchangeRateActivity.class);
                }
                return false;
            }
        });
    }


    @Override
    protected void onStart() {
        Log.i(TAG,"onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG,"onResume");
        super.onResume();
    }


    @Override
    protected void onPause() {
        Log.i(TAG,"onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(TAG,"onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.i(TAG,"onRestart");
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemThatWasClickedId = item.getItemId();
        Intent featureIntent = new Intent();
        Context context = MainActivity.this;
        switch (itemThatWasClickedId){
            case R.id.weather:
                Log.i(TAG,"Select WeatherActivity feature ");
                featureIntent.setClass(context,WeatherActivity.class);
                //String textToShow = "Search clicked";
                //Toast.makeText(context, textToShow, Toast.LENGTH_SHORT).show();
                startActivity(featureIntent);
                finish();
                break;
            case R.id.exchangeRate:
                Log.i(TAG,"Select ExchangeRateActivity feature ");
                featureIntent.setClass(context,ExchangeRateActivity.class);
                startActivity(featureIntent);
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void startFeature(Class featureClass){
        Intent featureIntent = new Intent();
        featureIntent.setClass(MainActivity.this,featureClass);
        startActivity(featureIntent);
        finish();
    }
}
