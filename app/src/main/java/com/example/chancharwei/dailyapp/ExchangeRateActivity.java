package com.example.chancharwei.dailyapp;

import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class ExchangeRateActivity extends AppCompatActivity {
    final static String TAG = ExchangeRateActivity.class.getName();

    TextView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.exchangerate,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        Intent featureIntent = new Intent();
        switch (itemThatWasClickedId) {
            case android.R.id.home:
                featureIntent.setClass(this, MainActivity.class);
                startActivity(featureIntent);
                finish();
                break;
            case R.id.weather:
                featureIntent.setClass(this,WeatherActivity.class);
                startActivity(featureIntent);
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return true;

    }
}
