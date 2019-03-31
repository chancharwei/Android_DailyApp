package com.example.chancharwei.dailyapp.data;

import android.content.Context;
import android.util.Log;

import com.example.chancharwei.dailyapp.ExchangeRateActivity;
import com.example.chancharwei.dailyapp.utilies.ExchangeRateHTMLUtility;

import java.util.ArrayList;

public class ExchangeRateTableData {
    private static final String TAG = ExchangeRateTableData.class.getName();
    private ExchangeRateActivity mActivity;
    ArrayList<String> currencyTypeList = new ArrayList<>();
    private long rowId = 0;
    private String dateTime = "yyyy/MM/dd";
    private String currency = "TWD";
    private double cashRateBuyMax = 0.0;
    private double cashRateBuyMin = 0.0;
    private double cashRateSellMax = 0.0;
    private double cashRateSellMin = 0.0;
    private double spotRateBuyMax = 0.0;
    private double spotRateBuyMin = 0.0;
    private double spotRateSellMax = 0.0;
    private double spotRateSellMin = 0.0;

    public long getId(){
        return rowId;
    }

    public String getDateTime(){
        return dateTime;
    }

    public String getCurrency(){
        return currency;
    }
    public double getCashRateBuyMax(){
        return cashRateBuyMax;
    }
    public double getCashRateBuyMin(){
        return cashRateBuyMin;
    }
    public double getCashRateSellMax(){
        return cashRateSellMax;
    }
    public double getCashRateSellMin(){
        return cashRateSellMin;
    }
    public double getSpotRateBuyMax(){
        return spotRateBuyMax;
    }
    public double getSpotRateBuyMin(){
        return spotRateBuyMin;
    }
    public double getSpotRateSellMax(){
        return spotRateSellMax;
    }
    public double getSpotRateSellMin(){
        return spotRateSellMin;
    }

    public void setId(long value){
        rowId = value;
    }
    public void setDateTime(String value){
        dateTime = value;
    }
    public void setCurrency(String value){
        currency = value;
    }
    public void setCashRateBuyMax(double value){
        cashRateBuyMax = value;
    }
    public void setCashRateBuyMin(double value){
        cashRateBuyMin = value;
    }
    public void setCashRateSellMax(double value){
        cashRateSellMax = value;
    }
    public void setCashRateSellMin(double value){
        cashRateSellMin = value;
    }
    public void setSpotRateBuyMax(double value){
        spotRateBuyMax = value;
    }
    public void setSpotRateBuyMin(double value){
        spotRateBuyMin = value;
    }
    public void setSpotRateSellMax(double value){
        spotRateSellMax = value;
    }
    public void setSpotRateSellMin(double value){
        spotRateSellMin = value;
    }


    public void assignMappingData(int dataFromList,int selectDataPart,ArrayList<Object> dataList){
        switch(selectDataPart){
            case 0: //currency type
                setCurrency((String)dataList.get(dataFromList));
                break;
            case 1: //CashRateBuy
                setCashRateBuyMax((Double)dataList.get(dataFromList));
                setCashRateBuyMin((Double)dataList.get(dataFromList));
                break;
            case 2: //CashRateSell
                setCashRateSellMax((Double)dataList.get(dataFromList));
                setCashRateSellMin((Double)dataList.get(dataFromList));
                break;
            case 3: //SpotRateBuy
                setSpotRateBuyMax((Double)dataList.get(dataFromList));
                setSpotRateBuyMin((Double)dataList.get(dataFromList));
                break;
            case 4: //SpotRateSell
                setSpotRateSellMax((Double)dataList.get(dataFromList));
                setSpotRateSellMin((Double)dataList.get(dataFromList));
                break;
        }
    }

    public boolean updateWithNewData(ExchangeRateTableData originalData){
        boolean needUpdate = false;

        if(originalData != null){

            if(originalData.getCashRateBuyMax() < this.getCashRateBuyMax()){
                Log.d(TAG,"Byron update getCashRateBuyMax ori "+originalData.getCashRateBuyMax()+" new "+this.getCashRateBuyMax());
                originalData.setCashRateBuyMax(this.getCashRateBuyMax());
                needUpdate = true;
            }

            if(originalData.getCashRateBuyMin() > this.getCashRateBuyMin()){
                Log.d(TAG,"Byron update getCashRateBuyMin ori "+originalData.getCashRateBuyMin()+" new "+this.getCashRateBuyMin());

                originalData.setCashRateBuyMin(this.getCashRateBuyMin());
                needUpdate = true;
            }

            if(originalData.getCashRateSellMax() < this.getCashRateSellMax()){
                Log.d(TAG,"Byron update getCashRateSellMax ori "+originalData.getCashRateSellMax()+" new "+this.getCashRateSellMax());

                originalData.setCashRateSellMax(this.getCashRateSellMax());
                needUpdate = true;
            }

            if(originalData.getCashRateSellMin() > this.getCashRateSellMin()){
                Log.d(TAG,"Byron update getCashRateSellMin ori "+originalData.getCashRateSellMin()+" new "+this.getCashRateSellMin());

                originalData.setCashRateSellMin(this.getCashRateSellMin());
                needUpdate = true;
            }

            if(originalData.getSpotRateBuyMax() < this.getSpotRateBuyMax()){
                Log.d(TAG,"Byron update getSpotRateBuyMax ori "+originalData.getSpotRateBuyMax()+" new "+this.getSpotRateBuyMax());

                originalData.setSpotRateBuyMax(this.getSpotRateBuyMax());
                needUpdate = true;
            }

            if(originalData.getSpotRateBuyMin() > this.getSpotRateBuyMin()){
                Log.d(TAG,"Byron update getSpotRateBuyMin ori "+originalData.getSpotRateBuyMin()+" new "+this.getSpotRateBuyMin());

                originalData.setSpotRateBuyMin(this.getSpotRateBuyMin());
                needUpdate = true;
            }

            if(originalData.getSpotRateSellMax() < this.getSpotRateSellMax()){
                Log.d(TAG,"Byron update getSpotRateSellMax ori "+originalData.getSpotRateSellMax()+" new "+this.getSpotRateSellMax());

                originalData.setSpotRateSellMax(this.getSpotRateSellMax());
                needUpdate = true;
            }

            if(originalData.getSpotRateSellMin() > this.getSpotRateSellMin()){
                Log.d(TAG,"Byron update getSpotRateSellMin ori "+originalData.getSpotRateSellMin()+" new "+this.getSpotRateSellMin());

                originalData.setSpotRateSellMin(this.getSpotRateSellMin());
                needUpdate = true;
            }

            return needUpdate;
        }
        return false;
    }

}
