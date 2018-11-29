package com.example.chancharwei.dailyapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity {
    final String TAG = getClass().getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG,"onCreate");
        //Document doc = Jsoup.connect();

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

        if(itemThatWasClickedId == R.id.weather){
            Log.i(TAG,"Select Weather feature ");
            Context context = MainActivity.this;
            featureIntent.setClass(context,Weather.class);
            //String textToShow = "Search clicked";
            //Toast.makeText(context, textToShow, Toast.LENGTH_SHORT).show();
            startActivity(featureIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
