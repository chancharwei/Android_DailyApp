package com.example.chancharwei.dailyapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>{
    private static final String TAG = WeatherAdapter.class.getName();
    private WeatherAdapterOnclickHandler mClickHandler;

    public WeatherAdapter(WeatherAdapterOnclickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public interface WeatherAdapterOnclickHandler{
        void onClick(String[] weatherItem);
    }
    private ArrayList<String[]> mWeatherData = null;

    /*public void WeatherAdapter(WeatherAdapterOnclickHandler clickHandler){
        mClickHandler = clickHandler;
    }*/
    @NonNull
    @Override
    public WeatherAdapter.WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder");
        //parent is RecycleView.this
        int layoutIdForListItem = R.layout.weather_list_item;
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        //inflate weather_list_item textView in RecyclerView
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        WeatherViewHolder viewHolder = new WeatherViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        Log.i(TAG, "onBindVIewHolder ("+ position+")");
        String[] weatherInfo = mWeatherData.get(position);
        holder.mTextView_StartTime.setText(transformAndGetTimeInfo(weatherInfo[0]));
        holder.mTextView_EndTime.setText("- "+transformAndGetTimeInfo(weatherInfo[1]));
        holder.mTextView_PoP12.setText(weatherInfo[2]+"%");
        holder.mTextView_T.setText(weatherInfo[3]);
        holder.mTextView_Wx.setText(weatherInfo[4]);
        holder.mTextView_MinT.setText(weatherInfo[5]);
        holder.mTextView_MaxT.setText(weatherInfo[6]);
        mappingWeatherImage(weatherInfo[2],weatherInfo[4],isNight(weatherInfo[0]),holder);
        //holder.mTextView_StartTime.setText(weatherInfo);
        //findTimeZone(weatherInfo[0]);
    }


    @Override
    public int getItemCount() {
        if(mWeatherData == null){
            //Log.d(TAG,"Byron check getItemCount mWeatherData null");
            return 0;
        }else{
            //Log.d(TAG,"Byron check getItemCount mWeatherData not null");
            return mWeatherData.size();
        }
    }

    private String transformAndGetTimeInfo(String time){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            SimpleDateFormat sdfWeek = new SimpleDateFormat("E");//星期
            SimpleDateFormat sdfAMPM = new SimpleDateFormat("a");//時段
            Date dt =sdf.parse(time);
            String[] time12hr = sdf.format(dt).toString().split(" ");
            String[] time12hrExcludeSecond = time12hr[1].split(":",3);
            String week=sdfWeek.format(dt);
            String timeDevision=sdfAMPM.format(dt);
            String timeAfterTransform = week
                    .concat(" ")
                    .concat(time12hrExcludeSecond[0])
                    .concat(":")
                    .concat(time12hrExcludeSecond[1])
                    .concat(timeDevision);
            Log.d(TAG,"timeAfterTransform = "+timeAfterTransform);
            return timeAfterTransform;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isNight(String startTime){
        String[] timeSplit = startTime.split(" ");
        String startTime_Day = timeSplit[0];
        String startTime_Time = timeSplit[1];

        if(Integer.parseInt(startTime_Time.split(":")[0]) >= 18){
            return true;
        }else{
            return false;
        }


        /*try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Long utStartTime = sdf.parse(startTime).getTime();
            Long utEndTime = sdf.parse(endTime).getTime();
            Long[] timeZone = new Long[4];
            timeZone[0] = sdf.parse(startTime_Day+" 00:00:00").getTime();
            timeZone[1] = sdf.parse(startTime_Day+" 06:00:00").getTime();
            timeZone[2] = sdf.parse(startTime_Day+" 12:00:00").getTime();
            timeZone[3] = sdf.parse(startTime_Day+" 18:00:00").getTime();
            int i;
            for(i=0;i<timeZone.length;i++){
                if(utStartTime == timeZone[i]){

                    break;
                }
            }
            timeZone[i]


            Log.d(TAG,"check utTime = "+utStartTime+" ut1 = "+ut1+" ut2 = "+ut2+" ut3 = "+ut3+" ut4 = "+ut4);
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
    }

    private void mappingWeatherImage(String dataPoP12,String Wx,boolean isNight,WeatherViewHolder holder){
        int PoP12;
        int weatherType = 0;
        try{
            PoP12 = Integer.parseInt(dataPoP12);
        }catch (NumberFormatException exception){
            Log.d(TAG,"didn't provide rain probability");
            PoP12 = -1;
            if(Wx.contains("雪")){
                weatherType = 1; //snow
            }else if(Wx.contains("雨") && Wx.contains("雷")){
                weatherType = 5;
            }else if(Wx.contains("雲") && Wx.contains("晴")){
                weatherType = 2;
            }else if(Wx.contains("雨")){
                weatherType = 7;
            }else if(Wx.contains("雲")||Wx.contains("陰")){
                weatherType = 3;
            }else{
                weatherType = 4;
            }

        }

        if(Wx.contains("雪")){
            weatherType = 1; //snow
        }else if(PoP12 >= 0 && PoP12 <= 30){
            if(Wx.contains("雲") && Wx.contains("晴")){
                weatherType = 2; //sunny-cloudy
            }else if(Wx.contains("雲")||Wx.contains("陰")){
                weatherType = 3; //cloud
            }else{
                weatherType = 4; //sunny
            }
        }else if(PoP12 > 30 && PoP12 <=70){
            if(Wx.contains("雨") && Wx.contains("雷")){
                weatherType = 5; //thunder
            }else if(Wx.contains("雲")||Wx.contains("陰")){
                weatherType = 3; //cloud
            }else {
            weatherType = 6; //sun-rain,moon-rain
            }
        }else if(PoP12 > 70){
            weatherType = 7;
            //rain
        }


        switch (weatherType){
            case 1:
                holder.mImageView_Weather.setImageResource(R.drawable.weather_snow);
                break;
            case 2:
                if(isNight){
                    holder.mImageView_Weather.setImageResource(R.drawable.weather_moon_cloud);
                }else{
                    holder.mImageView_Weather.setImageResource(R.drawable.weather_sun_cloud);
                }
                break;
            case 3:
                holder.mImageView_Weather.setImageResource(R.drawable.weather_cloudy);
                break;
            case 4:
                if(isNight){
                    holder.mImageView_Weather.setImageResource(R.drawable.weather_moon);
                }else{
                    holder.mImageView_Weather.setImageResource(R.drawable.weather_sunny);
                }
                break;
            case 5:
                holder.mImageView_Weather.setImageResource(R.drawable.weather_thunder);
                break;
            case 6:
                if(isNight){
                    holder.mImageView_Weather.setImageResource(R.drawable.weather_moon_rain);
                }else{
                    holder.mImageView_Weather.setImageResource(R.drawable.weather_sun_rain);
                }
                break;
            case 7:
                holder.mImageView_Weather.setImageResource(R.drawable.weather_rainy);
                break;

        }

    }

    public void setWeatherData(ArrayList<String[]> weatherData){
        //Log.d(TAG,"setWeatherData");
        mWeatherData = weatherData;
        notifyDataSetChanged();
    }

    class WeatherViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView mImageView_Weather;
        final TextView mTextView_StartTime,mTextView_EndTime,mTextView_PoP12,mTextView_T,mTextView_MinT,mTextView_MaxT,mTextView_Wx;
        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView_Weather = itemView.findViewById(R.id.imageView_Weather);
            mTextView_StartTime = itemView.findViewById(R.id.textView_StartTime);
            mTextView_EndTime = itemView.findViewById(R.id.textView_EndTime);
            mTextView_PoP12 = itemView.findViewById(R.id.textView_PoP12);
            mTextView_T = itemView.findViewById(R.id.textView_T);
            mTextView_MinT = itemView.findViewById(R.id.textView_MinT);
            mTextView_MaxT = itemView.findViewById(R.id.textView_MaxT);
            mTextView_Wx = itemView.findViewById(R.id.textView_Wx);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View itemView) {
            int position = getAdapterPosition();
            Log.d(TAG,"select item number ("+position+")");
            mClickHandler.onClick(mWeatherData.get(position));
        }



    }
}
