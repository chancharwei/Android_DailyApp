package com.example.chancharwei.dailyapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>{
    private static final String TAG = WeatherAdapter.class.getName();
    private WeatherAdapterOnclickHandler mClickHandler;

    public WeatherAdapter(WeatherAdapterOnclickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public interface WeatherAdapterOnclickHandler{
        void onClick(String weatherItem);
    }
    private String[] mWeatherData = null;

    /*public void WeatherAdapter(WeatherAdapterOnclickHandler clickHandler){
        mClickHandler = clickHandler;
    }*/
    @NonNull
    @Override
    public WeatherAdapter.WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder");
        //parent is RecycleView.this
        int layoutIdForListItem = R.layout.list_item;
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        //inflate list_item textView in RecyclerView
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        WeatherViewHolder viewHolder = new WeatherViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        Log.i(TAG, "onBindVIewHolder ("+ position+")");
        String weatherInfo = mWeatherData[position];
        holder.mWeatherTextView.setText(weatherInfo);
    }


    @Override
    public int getItemCount() {
        if(mWeatherData == null){
            //Log.d(TAG,"Byron check getItemCount mWeatherData null");
            return 0;
        }else{
            //Log.d(TAG,"Byron check getItemCount mWeatherData not null");
            return mWeatherData.length;
        }
    }

    public void setWeatherData(String[] weatherData){
        //Log.d(TAG,"setWeatherData");
        mWeatherData = weatherData;
        notifyDataSetChanged();
    }

    class WeatherViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mWeatherTextView;
        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            mWeatherTextView = (TextView)itemView.findViewById(R.id.weather_list_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View itemView) {
            int position = getAdapterPosition();
            Log.d(TAG,"select item number ("+position+")");
            mClickHandler.onClick(mWeatherData[position]);
        }

    }
}
