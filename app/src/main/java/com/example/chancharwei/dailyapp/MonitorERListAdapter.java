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

import java.util.ArrayList;

public class MonitorERListAdapter extends RecyclerView.Adapter<MonitorERListAdapter.ExchangeRateViewHolder>{
    final String TAG = MonitorERListAdapter.class.getName();
    ArrayList<String[]> mDataList;

    private ExchangeRateAdapterOnclickHandler mClickHandler;

    public MonitorERListAdapter(ExchangeRateAdapterOnclickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public interface ExchangeRateAdapterOnclickHandler{
        void onClick(String[] exchangeRateItem);
    }


    @NonNull
    @Override
    public MonitorERListAdapter.ExchangeRateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder");
        //parent is RecycleView.this
        int layoutIdForListItem = R.layout.monitor_exchangerate_list_item;
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        //inflate list_item textView in RecyclerView
        View view = inflater.inflate(layoutIdForListItem, parent, false);
        ExchangeRateViewHolder viewHolder = new ExchangeRateViewHolder(view);
        view.setFocusable(true);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ExchangeRateViewHolder holder, int position) {
        Log.i(TAG, "onBindVIewHolder ("+ position+")");
        String[] exchangeRateInfo = mDataList.get(position);
        if(Integer.parseInt(exchangeRateInfo[3]) == 0){
            holder.mImageCoin.setVisibility(View.INVISIBLE);
        }else{
            holder.mImageCoin.setVisibility(View.VISIBLE);
        }
        holder.mNowCurrency.setText(exchangeRateInfo[0]);
        holder.mTargetCurrency.setText(exchangeRateInfo[1]);
        holder.mNowExchangeRate.setText(exchangeRateInfo[5]);
        holder.mTargetExchangeRate.setText(exchangeRateInfo[4]);
        mappingFlagView(exchangeRateInfo[0],holder.mImageNowCurrency);
        mappingFlagView(exchangeRateInfo[1],holder.mImageTargetCurrency);
        //holder.mExchangeRateTextView.setText(exchangeRateInfo);
    }

    @Override
    public int getItemCount() {
        if(mDataList == null){
            //Log.d(TAG,"Byron check getItemCount mWeatherData null");
            return 0;
        }else{
            //Log.d(TAG,"Byron check getItemCount mWeatherData not null");
            return mDataList.size();
        }
    }

    private void mappingFlagView(String value,ImageView flagView){
        if(value.contains("USD")){
            flagView.setImageResource(R.drawable.flag_usd2);
        }else if(value.contains("HKD")){
            flagView.setImageResource(R.drawable.flag_hkd2);
        }else if(value.contains("GBP")){
            flagView.setImageResource(R.drawable.flag_gbp2);
        }else if(value.contains("AUD")){
            flagView.setImageResource(R.drawable.flag_aud2);
        }else if(value.contains("CAD")){
            flagView.setImageResource(R.drawable.flag_cad2);
        }else if(value.contains("SGD")){
            flagView.setImageResource(R.drawable.flag_sgd2);
        }else if(value.contains("CHF")){
            flagView.setImageResource(R.drawable.flag_chf2);
        }else if(value.contains("JPY")){
            flagView.setImageResource(R.drawable.flag_jpy2);
        }else if(value.contains("ZAR")){
            flagView.setImageResource(R.drawable.flag_zar2);
        }else if(value.contains("SEK")){
            flagView.setImageResource(R.drawable.flag_sek2);
        }else if(value.contains("NZD")){
            flagView.setImageResource(R.drawable.flag_nzd2);
        }else if(value.contains("THB")){
            flagView.setImageResource(R.drawable.flag_thb2);
        }else if(value.contains("PHP")){
            flagView.setImageResource(R.drawable.flag_php2);
        }else if(value.contains("IDR")){
            flagView.setImageResource(R.drawable.flag_idr2);
        }else if(value.contains("EUR")){
            flagView.setImageResource(R.drawable.flag_eur2);
        }else if(value.contains("KRW")){
            flagView.setImageResource(R.drawable.flag_krw2);
        }else if(value.contains("VND")){
            flagView.setImageResource(R.drawable.flag_vnd2);
        }else if(value.contains("MYR")){
            flagView.setImageResource(R.drawable.flag_myr2);
        }else if(value.contains("CNY")){
            flagView.setImageResource(R.drawable.flag_cny2);
        }else if(value.contains("TWI")) {
            flagView.setImageResource(R.drawable.flag_twi2);
        }
    }

    public void setExchangeRateDataList(ArrayList<String[]> exchangeRateDataList){
        mDataList = exchangeRateDataList;
        notifyDataSetChanged();
    }

    class ExchangeRateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView mImageNowCurrency,mImageTargetCurrency,mImageCoin;
        final TextView mNowCurrency,mTargetCurrency;
        final TextView mNowExchangeRate,mTargetExchangeRate;
        public ExchangeRateViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageNowCurrency = itemView.findViewById(R.id.imageView_NowCurrency);
            mImageTargetCurrency = itemView.findViewById(R.id.imageView_TargetCurrency);
            mNowCurrency = itemView.findViewById(R.id.textView_NowCurrency);
            mTargetCurrency = itemView.findViewById(R.id.textView_TargetCurrency);
            mNowExchangeRate = itemView.findViewById(R.id.textView_NowExRate);
            mTargetExchangeRate = itemView.findViewById(R.id.textView_TargetExRate);
            mImageCoin = itemView.findViewById(R.id.imageView_coin);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View itemView) {
            int position = getAdapterPosition();
            Log.d(TAG,"select item number ("+position+")");
            mClickHandler.onClick(mDataList.get(position));
        }

    }
}
